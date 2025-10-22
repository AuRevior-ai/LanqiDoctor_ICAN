package com.lanqiDoctor.demo.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hjq.http.listener.OnHttpListener;
import com.lanqiDoctor.demo.http.api.AiChatApi;
import com.lanqiDoctor.demo.http.api.AiService;
import com.lanqiDoctor.demo.http.api.ChatMessage;
import com.lanqiDoctor.demo.model.Recipe;
import com.lanqiDoctor.demo.model.WeeklyRecipePlan;
import com.lanqiDoctor.demo.util.ChatLlmUtil;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;

/**
 * 食谱管理器
 * 负责食谱推荐、周计划生成、AI交互等功能
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class RecipeManager {
    
    private static final String TAG = "RecipeManager";
    private static final String PREFS_NAME = "recipe_prefs";
    private static final String KEY_WEEKLY_PLAN = "weekly_plan";
    private static final String KEY_FAVORITE_RECIPES = "favorite_recipes";
    private static final String KEY_LAST_PLAN_DATE = "last_plan_date";
    
    private static RecipeManager instance;
    private Context context;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private UserStateManager userStateManager;
    private Handler mainHandler;
    
    private RecipeManager(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        try {
            this.userStateManager = UserStateManager.getInstance(context);
            Log.d(TAG, "UserStateManager初始化" + (userStateManager != null ? "成功" : "失败"));
        } catch (Exception e) {
            Log.e(TAG, "UserStateManager初始化失败", e);
            this.userStateManager = null;
        }
        
        Log.d(TAG, "RecipeManager初始化完成");
    }
    
    public static synchronized RecipeManager getInstance(Context context) {
        if (instance == null) {
            instance = new RecipeManager(context);
        }
        return instance;
    }
    
    /**
     * 关闭ChatLlmUtil资源
     */
    public void shutdown() {
        // RecipeManager不再持有ChatLlmUtil，由调用者管理其生命周期
        Log.d(TAG, "RecipeManager shutdown");
    }
    
    /**
     * 食谱推荐回调接口
     */
    public interface RecipeRecommendationCallback {
        void onSuccess(List<Recipe> recipes);
        void onError(String error);
    }
    
    /**
     * 周计划生成回调接口
     */
    public interface WeeklyPlanCallback {
        void onSuccess(WeeklyRecipePlan plan);
        void onError(String error);
    }
    
    /**
     * 单个食谱生成回调接口
     */
    public interface SingleRecipeCallback {
        void onSuccess(Recipe recipe);
        void onError(String error);
    }
    
    /**
     * 获取当前周计划
     */
    public WeeklyRecipePlan getCurrentWeeklyPlan() {
        try {
            String planJson = sharedPreferences.getString(KEY_WEEKLY_PLAN, "");
            if (!TextUtils.isEmpty(planJson)) {
                WeeklyRecipePlan plan = gson.fromJson(planJson, WeeklyRecipePlan.class);
                
                // 检查计划是否过期（超过一周）
                if (plan != null && isWeeklyPlanValid(plan)) {
                    return plan;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取周计划失败", e);
        }
        return null;
    }
    
    /**
     * 检查周计划是否仍然有效
     */
    private boolean isWeeklyPlanValid(WeeklyRecipePlan plan) {
        if (plan == null || plan.getEndDate() == null) {
            return false;
        }
        
        Date now = new Date();
        return now.before(plan.getEndDate()) || isSameDay(now, plan.getEndDate());
    }
    
    /**
     * 判断两个日期是否是同一天
     */
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    
    /**
     * 生成或获取周食谱计划
     */
    public void getOrGenerateWeeklyPlan(WeeklyPlanCallback callback) {
        getOrGenerateWeeklyPlan(null, callback);
    }
    
    /**
     * 生成或获取周食谱计划（带 ChatLlmUtil）
     */
    public void getOrGenerateWeeklyPlan(ChatLlmUtil chatLlmUtil, WeeklyPlanCallback callback) {
        // 首先检查是否有有效的周计划
        WeeklyRecipePlan existingPlan = getCurrentWeeklyPlan();
        if (existingPlan != null) {
            Log.d(TAG, "使用现有周计划");
            if (callback != null) {
                callback.onSuccess(existingPlan);
            }
            return;
        }
        
        // 如果没有有效计划，生成新的
        Log.d(TAG, "生成新的周计划");
        generateWeeklyPlan(chatLlmUtil, callback);
    }
    
    /**
     * 生成新的周食谱计划
     */
    public void generateWeeklyPlan(WeeklyPlanCallback callback) {
        generateWeeklyPlan(null, callback);
    }
    
    /**
     * 生成新的周食谱计划（带 ChatLlmUtil）
     */
    public void generateWeeklyPlan(ChatLlmUtil chatLlmUtil, WeeklyPlanCallback callback) {
        if (chatLlmUtil == null) {
            if (callback != null) {
                callback.onError("ChatLlmUtil未初始化，请传入有效的ChatLlmUtil实例");
            }
            return;
        }
        
        try {
            String userRegion = getUserRegionInfo();
            String currentSeason = getCurrentSeason();
            
            Log.d(TAG, "用户地区: " + userRegion + ", 当前季节: " + currentSeason);
            
            String prompt = buildWeeklyPlanPrompt(userRegion, currentSeason);
            
            Log.d(TAG, "生成的提示词长度: " + (prompt != null ? prompt.length() : "null"));
            
            if (TextUtils.isEmpty(prompt)) {
                Log.e(TAG, "提示词为空，无法生成周计划");
                if (callback != null) {
                    callback.onError("提示词生成失败");
                }
                return;
            }
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("user", prompt));
            
            // 使用传入的ChatLlmUtil
            chatLlmUtil.sendSyncRequest(messages, new ChatLlmUtil.LlmCallback() {
                @Override
                public void onAssistantMessage(String content) {
                    try {
                        if (!TextUtils.isEmpty(content)) {
                            WeeklyRecipePlan plan = parseWeeklyPlanFromResponse(content, userRegion);
                            if (plan != null) {
                                saveWeeklyPlan(plan);
                                if (callback != null) {
                                    callback.onSuccess(plan);
                                }
                                Log.d(TAG, "周计划生成成功");
                            } else {
                                if (callback != null) {
                                    callback.onError("解析周计划失败");
                                }
                            }
                        } else {
                            if (callback != null) {
                                callback.onError("AI响应为空");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "处理周计划响应失败", e);
                        if (callback != null) {
                            callback.onError("处理响应失败: " + e.getMessage());
                        }
                    }
                }
                
                @Override
                public void onError(String errorMsg) {
                    Log.e(TAG, "生成周计划请求失败: " + errorMsg);
                    if (callback != null) {
                        callback.onError("请求失败: " + errorMsg);
                    }
                }
                
                @Override
                public void onComplete() {
                    Log.d(TAG, "周计划生成请求结束");
                }
                
                @Override
                public void onStreamUpdate(String content) {
                    // 不使用流式更新
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "生成周计划异常", e);
            if (callback != null) {
                callback.onError("生成计划异常: " + e.getMessage());
            }
        }
    }
    
    /**
     * 重新生成指定餐次的食谱
     */
    public void regenerateRecipe(Date date, Recipe.MealType mealType, SingleRecipeCallback callback) {
        regenerateRecipe(null, date, mealType, callback);
    }
    
    /**
     * 重新生成指定餐次的食谱（带 ChatLlmUtil）
     */
    public void regenerateRecipe(ChatLlmUtil chatLlmUtil, Date date, Recipe.MealType mealType, SingleRecipeCallback callback) {
        if (chatLlmUtil == null) {
            if (callback != null) {
                callback.onError("ChatLlmUtil未初始化，请传入有效的ChatLlmUtil实例");
            }
            return;
        }
        
        try {
            String userRegion = getUserRegionInfo();
            String currentSeason = getCurrentSeason();
            
            String prompt = buildSingleRecipePrompt(mealType, userRegion, currentSeason);
            
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("user", prompt));
            
            // 使用传入的ChatLlmUtil
            chatLlmUtil.sendSyncRequest(messages, new ChatLlmUtil.LlmCallback() {
                @Override
                public void onAssistantMessage(String content) {
                    try {
                        if (!TextUtils.isEmpty(content)) {
                            Recipe recipe = parseRecipeFromResponse(content, mealType);
                            if (recipe != null) {
                                // 更新周计划中的对应食谱
                                updateRecipeInWeeklyPlan(date, mealType, recipe);
                                
                                if (callback != null) {
                                    callback.onSuccess(recipe);
                                }
                                Log.d(TAG, "食谱重新生成成功: " + recipe.getName());
                            } else {
                                if (callback != null) {
                                    callback.onError("解析食谱失败");
                                }
                            }
                        } else {
                            if (callback != null) {
                                callback.onError("AI响应为空");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "处理食谱响应失败", e);
                        if (callback != null) {
                            callback.onError("处理响应失败: " + e.getMessage());
                        }
                    }
                }
                
                @Override
                public void onError(String errorMsg) {
                    Log.e(TAG, "重新生成食谱请求失败: " + errorMsg);
                    if (callback != null) {
                        callback.onError("请求失败: " + errorMsg);
                    }
                }
                
                @Override
                public void onComplete() {
                    Log.d(TAG, "重新生成食谱请求结束");
                }
                
                @Override
                public void onStreamUpdate(String content) {
                    // 不使用流式更新
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "重新生成食谱异常", e);
            if (callback != null) {
                callback.onError("生成食谱异常: " + e.getMessage());
            }
        }
    }
    
    /**
     * 构建周计划生成的提示词
     */
    private String buildWeeklyPlanPrompt(String userRegion, String currentSeason) {
        // 确保参数不为null
        String safeUserRegion = (userRegion != null) ? userRegion : "陕西省西安市";
        String safeCurrentSeason = (currentSeason != null) ? currentSeason : "春季";
        
        return String.format(
            "请为用户生成一周（7天）的健康食谱计划。用户地区：%s，当前季节：%s。\n\n" +
            "要求：\n" +
            "1. 结合地区饮食特色和时令食材\n" +
            "2. 营养均衡，包含早餐、午餐、晚餐\n" +
            "3. 一周内食谱不重复\n" +
            "4. 标注每道菜的食材用量和热量\n" +
            "5. 考虑制作难度适中\n\n" +
            "请按以下格式输出：\n" +
            "【第X天 星期X】\n" +
            "早餐：[菜名]（[热量]千卡）\n" +
            "食材：[食材1] [用量][单位]，[食材2] [用量][单位]...\n" +
            "制作时间：[X]分钟\n\n" +
            "午餐：[菜名]（[热量]千卡）\n" +
            "食材：[食材1] [用量][单位]，[食材2] [用量][单位]...\n" +
            "制作时间：[X]分钟\n\n" +
            "晚餐：[菜名]（[热量]千卡）\n" +
            "食材：[食材1] [用量][单位]，[食材2] [用量][单位]...\n" +
            "制作时间：[X]分钟\n\n" +
            "请严格按照此格式输出，方便程序解析。",
            safeUserRegion, safeCurrentSeason
        );
    }
    
    /**
     * 构建单个食谱生成的提示词
     */
    private String buildSingleRecipePrompt(Recipe.MealType mealType, String userRegion, String currentSeason) {
        // 确保参数不为null
        String safeMealTypeName = (mealType != null && mealType.getDisplayName() != null) ? 
            mealType.getDisplayName() : "餐点";
        String safeUserRegion = (userRegion != null) ? userRegion : "陕西省西安市";
        String safeCurrentSeason = (currentSeason != null) ? currentSeason : "春季";
        
        return String.format(
            "请重新推荐一道%s食谱。用户地区：%s，当前季节：%s。\n\n" +
            "要求：\n" +
            "1. 结合地区饮食特色和时令食材\n" +
            "2. 营养均衡，适合%s食用\n" +
            "3. 标注食材用量和热量\n" +
            "4. 制作难度适中\n\n" +
            "请按以下格式输出：\n" +
            "菜名：[菜名]\n" +
            "热量：[X]千卡\n" +
            "食材：[食材1] [用量][单位]，[食材2] [用量][单位]...\n" +
            "制作时间：[X]分钟\n" +
            "制作步骤：\n" +
            "1. [步骤1]\n" +
            "2. [步骤2]\n" +
            "...\n\n" +
            "请严格按照此格式输出。",
            safeMealTypeName, safeUserRegion, safeCurrentSeason, safeMealTypeName
        );
    }
    
    /**
     * 从AI响应解析周计划
     */
    private WeeklyRecipePlan parseWeeklyPlanFromResponse(String response, String userRegion) {
        try {
            WeeklyRecipePlan plan = new WeeklyRecipePlan();
            plan.setId("weekly_plan_" + System.currentTimeMillis());
            plan.setName("AI推荐周食谱");
            plan.setUserRegion(userRegion);
            
            // 设置开始和结束日期（本周一到周日）
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            Date startDate = calendar.getTime();
            plan.setStartDate(startDate);
            
            calendar.add(Calendar.DAY_OF_WEEK, 6);
            Date endDate = calendar.getTime();
            plan.setEndDate(endDate);
            
            // 解析每日计划
            List<WeeklyRecipePlan.DailyRecipePlan> dailyPlans = parseDailyPlansFromResponse(response);
            plan.setDailyPlans(dailyPlans);
            
            return plan;
        } catch (Exception e) {
            Log.e(TAG, "解析周计划失败", e);
            return null;
        }
    }
    
    /**
     * 解析每日计划
     */
    private List<WeeklyRecipePlan.DailyRecipePlan> parseDailyPlansFromResponse(String response) {
        List<WeeklyRecipePlan.DailyRecipePlan> dailyPlans = new ArrayList<>();
        
        try {
            // 使用正则表达式解析每日计划
            Pattern dayPattern = Pattern.compile("【第(\\d+)天\\s+(\\S+)】([\\s\\S]*?)(?=【第\\d+天|$)");
            Matcher dayMatcher = dayPattern.matcher(response);
            
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            
            while (dayMatcher.find()) {
                String dayNumber = dayMatcher.group(1);
                String dayOfWeek = dayMatcher.group(2);
                String dayContent = dayMatcher.group(3);
                
                WeeklyRecipePlan.DailyRecipePlan dailyPlan = new WeeklyRecipePlan.DailyRecipePlan();
                dailyPlan.setDate(calendar.getTime());
                dailyPlan.setDayOfWeek(dayOfWeek);
                
                // 解析早餐、午餐、晚餐
                parseRecipesForDay(dayContent, dailyPlan);
                
                dailyPlans.add(dailyPlan);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "解析每日计划失败", e);
        }
        
        return dailyPlans;
    }
    
    /**
     * 解析一天中的食谱
     */
    private void parseRecipesForDay(String dayContent, WeeklyRecipePlan.DailyRecipePlan dailyPlan) {
        try {
            // 解析早餐
            Recipe breakfast = parseRecipeFromDayContent(dayContent, "早餐", Recipe.MealType.BREAKFAST);
            if (breakfast != null) {
                dailyPlan.setBreakfast(breakfast);
            }
            
            // 解析午餐
            Recipe lunch = parseRecipeFromDayContent(dayContent, "午餐", Recipe.MealType.LUNCH);
            if (lunch != null) {
                dailyPlan.setLunch(lunch);
            }
            
            // 解析晚餐
            Recipe dinner = parseRecipeFromDayContent(dayContent, "晚餐", Recipe.MealType.DINNER);
            if (dinner != null) {
                dailyPlan.setDinner(dinner);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "解析一天食谱失败", e);
        }
    }
    
    /**
     * 从一天的内容中解析指定餐次的食谱
     */
    private Recipe parseRecipeFromDayContent(String content, String mealName, Recipe.MealType mealType) {
        try {
            // 使用正则表达式匹配食谱信息
            Pattern recipePattern = Pattern.compile(
                mealName + "：([^（\\n]+)（([\\d.]+)千卡）[\\s\\S]*?" +
                "食材：([^\\n]+)[\\s\\S]*?" +
                "制作时间：(\\d+)分钟"
            );
            
            Matcher matcher = recipePattern.matcher(content);
            if (matcher.find()) {
                Recipe recipe = new Recipe();
                recipe.setId(mealType.name().toLowerCase() + "_" + System.currentTimeMillis());
                
                // 安全地获取菜名
                String recipeName = matcher.group(1);
                recipe.setName(recipeName != null ? recipeName.trim() : mealType.getDisplayName() + "食谱");
                recipe.setMealType(mealType);
                
                // 设置热量
                try {
                    String caloriesStr = matcher.group(2);
                    if (caloriesStr != null) {
                        double calories = Double.parseDouble(caloriesStr);
                        recipe.setCalories(calories);
                    } else {
                        recipe.setCalories(300); // 默认热量
                    }
                } catch (NumberFormatException e) {
                    recipe.setCalories(300); // 默认热量
                }
                
                // 解析食材
                String ingredientsStr = matcher.group(3);
                List<Recipe.Ingredient> ingredients = parseIngredients(ingredientsStr);
                recipe.setIngredients(ingredients);
                
                // 设置制作时间
                try {
                    String timeStr = matcher.group(4);
                    if (timeStr != null) {
                        int cookingTime = Integer.parseInt(timeStr);
                        recipe.setCookingTime(cookingTime);
                    } else {
                        recipe.setCookingTime(30); // 默认时间
                    }
                } catch (NumberFormatException e) {
                    recipe.setCookingTime(30); // 默认时间
                }
                
                recipe.setDifficulty(2); // 默认难度
                recipe.setCreateTime(new Date());
                
                return recipe;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "解析食谱失败: " + mealName, e);
        }
        
        return null;
    }
    
    /**
     * 从单个食谱响应解析食谱
     */
    private Recipe parseRecipeFromResponse(String response, Recipe.MealType mealType) {
        try {
            Recipe recipe = new Recipe();
            recipe.setId(mealType.name().toLowerCase() + "_" + System.currentTimeMillis());
            recipe.setMealType(mealType);
            recipe.setCreateTime(new Date());
            
            // 解析菜名
            Pattern namePattern = Pattern.compile("菜名：([^\\n]+)");
            Matcher nameMatcher = namePattern.matcher(response);
            if (nameMatcher.find() && nameMatcher.group(1) != null) {
                recipe.setName(nameMatcher.group(1).trim());
            } else {
                recipe.setName(mealType.getDisplayName() + "食谱");
            }
            
            // 解析热量
            Pattern caloriesPattern = Pattern.compile("热量：([\\d.]+)千卡");
            Matcher caloriesMatcher = caloriesPattern.matcher(response);
            if (caloriesMatcher.find() && caloriesMatcher.group(1) != null) {
                try {
                    double calories = Double.parseDouble(caloriesMatcher.group(1));
                    recipe.setCalories(calories);
                } catch (NumberFormatException e) {
                    recipe.setCalories(300);
                }
            } else {
                recipe.setCalories(300);
            }
            
            // 解析食材
            Pattern ingredientsPattern = Pattern.compile("食材：([^\\n]+)");
            Matcher ingredientsMatcher = ingredientsPattern.matcher(response);
            if (ingredientsMatcher.find() && ingredientsMatcher.group(1) != null) {
                String ingredientsStr = ingredientsMatcher.group(1);
                List<Recipe.Ingredient> ingredients = parseIngredients(ingredientsStr);
                recipe.setIngredients(ingredients);
            }
            
            // 解析制作时间
            Pattern timePattern = Pattern.compile("制作时间：(\\d+)分钟");
            Matcher timeMatcher = timePattern.matcher(response);
            if (timeMatcher.find() && timeMatcher.group(1) != null) {
                try {
                    int cookingTime = Integer.parseInt(timeMatcher.group(1));
                    recipe.setCookingTime(cookingTime);
                } catch (NumberFormatException e) {
                    recipe.setCookingTime(30);
                }
            } else {
                recipe.setCookingTime(30);
            }
            
            // 解析制作步骤
            Pattern stepsPattern = Pattern.compile("制作步骤：[\\s\\S]*?\\n((?:\\d+\\.[^\\n]+\\n?)+)");
            Matcher stepsMatcher = stepsPattern.matcher(response);
            if (stepsMatcher.find() && stepsMatcher.group(1) != null) {
                String stepsStr = stepsMatcher.group(1);
                List<String> steps = parseSteps(stepsStr);
                recipe.setSteps(steps);
            } else {
                // 提供默认步骤
                List<String> defaultSteps = new ArrayList<>();
                defaultSteps.add("准备所需食材");
                defaultSteps.add("按常规方法制作");
                defaultSteps.add("调味后即可享用");
                recipe.setSteps(defaultSteps);
            }
            
            recipe.setDifficulty(2);
            
            return recipe;
            
        } catch (Exception e) {
            Log.e(TAG, "解析单个食谱失败", e);
            return null;
        }
    }
    
    /**
     * 解析食材列表
     */
    private List<Recipe.Ingredient> parseIngredients(String ingredientsStr) {
        List<Recipe.Ingredient> ingredients = new ArrayList<>();
        
        try {
            if (TextUtils.isEmpty(ingredientsStr)) {
                return ingredients;
            }
            
            String[] parts = ingredientsStr.split("，|,");
            for (String part : parts) {
                part = part.trim();
                if (!TextUtils.isEmpty(part)) {
                    // 使用正则表达式解析食材、用量和单位
                    Pattern pattern = Pattern.compile("([^\\d]+)\\s*([\\d.]+)\\s*([^\\d]*)");
                    Matcher matcher = pattern.matcher(part);
                    
                    if (matcher.find()) {
                        String name = matcher.group(1);
                        String amount = matcher.group(2);
                        String unit = matcher.group(3);
                        
                        // 安全地处理可能为null的group
                        name = (name != null) ? name.trim() : part;
                        amount = (amount != null) ? amount.trim() : "适量";
                        unit = (unit != null) ? unit.trim() : "";
                        
                        if (TextUtils.isEmpty(unit)) {
                            unit = "个"; // 默认单位
                        }
                        
                        ingredients.add(new Recipe.Ingredient(name, amount, unit));
                    } else {
                        // 如果正则匹配失败，至少保存食材名称
                        ingredients.add(new Recipe.Ingredient(part, "适量", ""));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "解析食材失败", e);
        }
        
        return ingredients;
    }
    
    /**
     * 解析制作步骤
     */
    private List<String> parseSteps(String stepsStr) {
        List<String> steps = new ArrayList<>();
        
        try {
            String[] lines = stepsStr.split("\\n");
            for (String line : lines) {
                line = line.trim();
                if (!TextUtils.isEmpty(line) && line.matches("^\\d+\\..*")) {
                    // 移除步骤编号
                    String step = line.replaceFirst("^\\d+\\.", "").trim();
                    if (!TextUtils.isEmpty(step)) {
                        steps.add(step);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "解析制作步骤失败", e);
        }
        
        return steps;
    }
    
    /**
     * 获取用户地区信息
     */
    private String getUserRegionInfo() {
        try {
            if (userStateManager == null) {
                Log.w(TAG, "userStateManager为null，使用默认地区");
                return "陕西省西安市";
            }
            
            String province = userStateManager.getUserProvince();
            String city = userStateManager.getUserCity();
            
            Log.d(TAG, "原始地区信息 - 省份: " + province + ", 城市: " + city);
            
            // 安全地处理可能为null的地区信息
            String safeProvince = (province != null && !TextUtils.isEmpty(province)) ? province : "陕西省";
            String safeCity = (city != null && !TextUtils.isEmpty(city)) ? city : "西安市";
            
            String result = safeProvince + safeCity;
            Log.d(TAG, "最终地区信息: " + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "获取用户地区失败", e);
            return "陕西省西安市"; // 默认地区
        }
    }
    
    /**
     * 获取当前季节
     */
    private String getCurrentSeason() {
        try {
            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH从0开始
            
            String season;
            if (month >= 3 && month <= 5) {
                season = "春季";
            } else if (month >= 6 && month <= 8) {
                season = "夏季";
            } else if (month >= 9 && month <= 11) {
                season = "秋季";
            } else {
                season = "冬季";
            }
            
            Log.d(TAG, "当前月份: " + month + ", 季节: " + season);
            return season;
        } catch (Exception e) {
            Log.e(TAG, "获取当前季节失败", e);
            return "春季"; // 默认季节
        }
    }
    
    /**
     * 保存周计划到本地
     */
    private void saveWeeklyPlan(WeeklyRecipePlan plan) {
        try {
            String planJson = gson.toJson(plan);
            sharedPreferences.edit()
                .putString(KEY_WEEKLY_PLAN, planJson)
                .putLong(KEY_LAST_PLAN_DATE, System.currentTimeMillis())
                .apply();
            Log.d(TAG, "周计划已保存");
        } catch (Exception e) {
            Log.e(TAG, "保存周计划失败", e);
        }
    }
    
    /**
     * 更新周计划中的指定食谱
     */
    private void updateRecipeInWeeklyPlan(Date date, Recipe.MealType mealType, Recipe newRecipe) {
        try {
            WeeklyRecipePlan plan = getCurrentWeeklyPlan();
            if (plan != null) {
                WeeklyRecipePlan.DailyRecipePlan dailyPlan = plan.getDailyPlanByDate(date);
                if (dailyPlan != null) {
                    dailyPlan.setRecipeByMealType(mealType, newRecipe);
                    saveWeeklyPlan(plan);
                    Log.d(TAG, "周计划中的食谱已更新");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "更新周计划中的食谱失败", e);
        }
    }
    
    /**
     * 获取收藏的食谱
     */
    public List<Recipe> getFavoriteRecipes() {
        try {
            String favoritesJson = sharedPreferences.getString(KEY_FAVORITE_RECIPES, "");
            if (!TextUtils.isEmpty(favoritesJson)) {
                Type type = new TypeToken<List<Recipe>>(){}.getType();
                return gson.fromJson(favoritesJson, type);
            }
        } catch (Exception e) {
            Log.e(TAG, "获取收藏食谱失败", e);
        }
        return new ArrayList<>();
    }
    
    /**
     * 添加食谱到收藏
     */
    public void addToFavorites(Recipe recipe) {
        try {
            List<Recipe> favorites = getFavoriteRecipes();
            recipe.setFavorite(true);
            
            // 检查是否已收藏
            boolean alreadyExists = false;
            for (int i = 0; i < favorites.size(); i++) {
                if (favorites.get(i).getId().equals(recipe.getId())) {
                    favorites.set(i, recipe);
                    alreadyExists = true;
                    break;
                }
            }
            
            if (!alreadyExists) {
                favorites.add(recipe);
            }
            
            String favoritesJson = gson.toJson(favorites);
            sharedPreferences.edit().putString(KEY_FAVORITE_RECIPES, favoritesJson).apply();
            Log.d(TAG, "食谱已添加到收藏");
        } catch (Exception e) {
            Log.e(TAG, "添加收藏失败", e);
        }
    }
    
    /**
     * 从收藏中移除食谱
     */
    public void removeFromFavorites(String recipeId) {
        try {
            List<Recipe> favorites = getFavoriteRecipes();
            favorites.removeIf(recipe -> recipe.getId().equals(recipeId));
            
            String favoritesJson = gson.toJson(favorites);
            sharedPreferences.edit().putString(KEY_FAVORITE_RECIPES, favoritesJson).apply();
            Log.d(TAG, "食谱已从收藏中移除");
        } catch (Exception e) {
            Log.e(TAG, "移除收藏失败", e);
        }
    }
    
    /**
     * 检查食谱是否已收藏
     */
    public boolean isRecipeFavorited(String recipeId) {
        List<Recipe> favorites = getFavoriteRecipes();
        for (Recipe recipe : favorites) {
            if (recipe.getId().equals(recipeId)) {
                return true;
            }
        }
        return false;
    }
}
