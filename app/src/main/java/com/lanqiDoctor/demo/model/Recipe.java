package com.lanqiDoctor.demo.model;

import java.util.Date;
import java.util.List;

/**
 * 食谱数据模型
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class Recipe {
    
    /** 食谱ID */
    private String id;
    
    /** 食谱名称 */
    private String name;
    
    /** 食谱描述 */
    private String description;
    
    /** 食材列表 */
    private List<Ingredient> ingredients;
    
    /** 制作步骤 */
    private List<String> steps;
    
    /** 营养信息 */
    private NutritionInfo nutritionInfo;
    
    /** 热量（千卡） */
    private double calories;
    
    /** 制作时间（分钟） */
    private int cookingTime;
    
    /** 难度等级（1-5） */
    private int difficulty;
    
    /** 适合餐次（早餐、午餐、晚餐） */
    private MealType mealType;
    
    /** 地区特色标签 */
    private List<String> regionTags;
    
    /** 创建时间 */
    private Date createTime;
    
    /** 是否收藏 */
    private boolean isFavorite;
    
    /** 用户评分 */
    private float rating;
    
    public enum MealType {
        BREAKFAST("早餐"),
        LUNCH("午餐"),
        DINNER("晚餐"),
        SNACK("加餐");
        
        private final String displayName;
        
        MealType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public static class Ingredient {
        private String name;
        private String amount;
        private String unit;
        
        public Ingredient() {}
        
        public Ingredient(String name, String amount, String unit) {
            this.name = name;
            this.amount = amount;
            this.unit = unit;
        }
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
        
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        
        @Override
        public String toString() {
            return name + " " + amount + unit;
        }
    }
    
    public static class NutritionInfo {
        private double protein;     // 蛋白质（克）
        private double fat;         // 脂肪（克）
        private double carbs;       // 碳水化合物（克）
        private double fiber;       // 纤维（克）
        private double sodium;      // 钠（毫克）
        private double vitamin_c;   // 维生素C（毫克）
        private double calcium;     // 钙（毫克）
        private double iron;        // 铁（毫克）
        
        public NutritionInfo() {}
        
        // Getters and Setters
        public double getProtein() { return protein; }
        public void setProtein(double protein) { this.protein = protein; }
        
        public double getFat() { return fat; }
        public void setFat(double fat) { this.fat = fat; }
        
        public double getCarbs() { return carbs; }
        public void setCarbs(double carbs) { this.carbs = carbs; }
        
        public double getFiber() { return fiber; }
        public void setFiber(double fiber) { this.fiber = fiber; }
        
        public double getSodium() { return sodium; }
        public void setSodium(double sodium) { this.sodium = sodium; }
        
        public double getVitamin_c() { return vitamin_c; }
        public void setVitamin_c(double vitamin_c) { this.vitamin_c = vitamin_c; }
        
        public double getCalcium() { return calcium; }
        public void setCalcium(double calcium) { this.calcium = calcium; }
        
        public double getIron() { return iron; }
        public void setIron(double iron) { this.iron = iron; }
    }
    
    // Constructors
    public Recipe() {}
    
    public Recipe(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createTime = new Date();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }
    
    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }
    
    public NutritionInfo getNutritionInfo() { return nutritionInfo; }
    public void setNutritionInfo(NutritionInfo nutritionInfo) { this.nutritionInfo = nutritionInfo; }
    
    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }
    
    public int getCookingTime() { return cookingTime; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }
    
    public int getDifficulty() { return difficulty; }
    public void setDifficulty(int difficulty) { this.difficulty = difficulty; }
    
    public MealType getMealType() { return mealType; }
    public void setMealType(MealType mealType) { this.mealType = mealType; }
    
    public List<String> getRegionTags() { return regionTags; }
    public void setRegionTags(List<String> regionTags) { this.regionTags = regionTags; }
    
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
}
