package com.lanqiDoctor.demo.ui.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.base.BaseActivity;
import com.hjq.toast.ToastUtils;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.manager.RecipeManager;
import com.lanqiDoctor.demo.model.Recipe;
import com.lanqiDoctor.demo.model.WeeklyRecipePlan;
import com.lanqiDoctor.demo.ui.adapter.RecipeCardAdapter;
import com.lanqiDoctor.demo.util.ChatLlmUtil;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 我的食谱Activity
 * 
 * 功能包括：
 * - AI智能食谱推荐
 * - 按周生成食谱安排
 * - 个人食谱收藏
 * - 营养成分分析
 * - 地区特色和时令考虑
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class MyRecipesActivity extends BaseActivity implements 
    RecipeCardAdapter.OnRecipeClickListener, 
    RecipeCardAdapter.OnRecipeLongClickListener,
    OnRefreshListener {

    private RecyclerView rvRecipes;
    private SmartRefreshLayout smartRefreshLayout;
    private TextView tvWeeklyPlanTitle;
    private TextView tvEmptyHint;
    private View llEmptyView;
    
    private RecipeCardAdapter adapter;
    private RecipeManager recipeManager;
    private WeeklyRecipePlan currentWeeklyPlan;
    private List<Recipe> currentRecipes;
    private ChatLlmUtil chatLlmUtil;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_my_recipes;
    }

    @Override
    protected void initView() {
        TextView titleText = findViewById(R.id.tv_title);
        if (titleText != null) {
            titleText.setText("我的食谱");
        }
        
        // 返回按钮
        ImageView backButton = findViewById(R.id.iv_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
        
        // 初始化视图
        rvRecipes = findViewById(R.id.rv_recipes);
        smartRefreshLayout = findViewById(R.id.smart_refresh_layout);
        tvWeeklyPlanTitle = findViewById(R.id.tv_weekly_plan_title);
        tvEmptyHint = findViewById(R.id.tv_empty_hint);
        llEmptyView = findViewById(R.id.ll_empty_view);
        
        // 设置RecyclerView
        setupRecyclerView();
        
        // 设置下拉刷新
        setupSmartRefresh();
    }

    @Override
    protected void initData() {
        // 初始化食谱管理器
        recipeManager = RecipeManager.getInstance(this);
        currentRecipes = new ArrayList<>();
        
        // 初始化 ChatLlmUtil
        chatLlmUtil = new ChatLlmUtil(new Handler(Looper.getMainLooper()), this);
        
        // 加载食谱数据
        loadRecipesData();
    }
    
    /**
     * 设置RecyclerView
     */
    private void setupRecyclerView() {
        if (rvRecipes != null) {
            rvRecipes.setLayoutManager(new LinearLayoutManager(this));
            adapter = new RecipeCardAdapter(this);
            adapter.setOnRecipeClickListener(this);
            adapter.setOnRecipeLongClickListener(this);
            rvRecipes.setAdapter(adapter);
        }
    }
    
    /**
     * 设置下拉刷新
     */
    private void setupSmartRefresh() {
        if (smartRefreshLayout != null) {
            smartRefreshLayout.setOnRefreshListener(this);
            smartRefreshLayout.setEnableLoadMore(false); // 禁用上拉加载
        }
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        refreshRecipes();
    }

    /**
     * 加载食谱数据
     */
    private void loadRecipesData() {
        showLoading();
        
        // 获取或生成周食谱计划
        recipeManager.getOrGenerateWeeklyPlan(chatLlmUtil, new RecipeManager.WeeklyPlanCallback() {
            @Override
            public void onSuccess(WeeklyRecipePlan plan) {
                runOnUiThread(() -> {
                    hideLoading();
                    currentWeeklyPlan = plan;
                    updateWeeklyPlanTitle(plan);
                    displayWeeklyRecipes(plan);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideLoading();
                    ToastUtils.show("加载食谱失败: " + error);
                    showEmptyView();
                });
            }
        });
    }
    
    /**
     * 刷新食谱
     */
    private void refreshRecipes() {
        recipeManager.generateWeeklyPlan(chatLlmUtil, new RecipeManager.WeeklyPlanCallback() {
            @Override
            public void onSuccess(WeeklyRecipePlan plan) {
                runOnUiThread(() -> {
                    if (smartRefreshLayout != null) {
                        smartRefreshLayout.finishRefresh();
                    }
                    currentWeeklyPlan = plan;
                    updateWeeklyPlanTitle(plan);
                    displayWeeklyRecipes(plan);
                    ToastUtils.show("食谱已更新");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (smartRefreshLayout != null) {
                        smartRefreshLayout.finishRefresh();
                    }
                    ToastUtils.show("刷新失败: " + error);
                });
            }
        });
    }
    
    /**
     * 更新周计划标题
     */
    private void updateWeeklyPlanTitle(WeeklyRecipePlan plan) {
        if (tvWeeklyPlanTitle != null && plan != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日", Locale.getDefault());
            String startDate = sdf.format(plan.getStartDate());
            String endDate = sdf.format(plan.getEndDate());
            String title = String.format("本周食谱计划（%s - %s）", startDate, endDate);
            tvWeeklyPlanTitle.setText(title);
        }
    }
    
    /**
     * 显示周食谱
     */
    private void displayWeeklyRecipes(WeeklyRecipePlan plan) {
        currentRecipes.clear();
        
        if (plan != null && plan.getDailyPlans() != null) {
            for (WeeklyRecipePlan.DailyRecipePlan dailyPlan : plan.getDailyPlans()) {
                // 添加每日的食谱
                if (dailyPlan.getBreakfast() != null) {
                    currentRecipes.add(dailyPlan.getBreakfast());
                }
                if (dailyPlan.getLunch() != null) {
                    currentRecipes.add(dailyPlan.getLunch());
                }
                if (dailyPlan.getDinner() != null) {
                    currentRecipes.add(dailyPlan.getDinner());
                }
            }
        }
        
        if (currentRecipes.isEmpty()) {
            showEmptyView();
        } else {
            hideEmptyView();
            adapter.setRecipes(currentRecipes);
        }
    }
    
    /**
     * 显示空视图
     */
    private void showEmptyView() {
        if (llEmptyView != null) {
            llEmptyView.setVisibility(View.VISIBLE);
        }
        if (rvRecipes != null) {
            rvRecipes.setVisibility(View.GONE);
        }
    }
    
    /**
     * 隐藏空视图
     */
    private void hideEmptyView() {
        if (llEmptyView != null) {
            llEmptyView.setVisibility(View.GONE);
        }
        if (rvRecipes != null) {
            rvRecipes.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 显示加载状态
     */
    private void showLoading() {
        if (smartRefreshLayout != null) {
            smartRefreshLayout.autoRefresh();
        }
    }
    
    /**
     * 隐藏加载状态
     */
    private void hideLoading() {
        if (smartRefreshLayout != null) {
            smartRefreshLayout.finishRefresh();
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        // 显示食谱详情
        showRecipeDetails(recipe);
    }

    @Override
    public void onRecipeLongClick(Recipe recipe, View view) {
        // 显示重新生成选项
        showRegenerateDialog(recipe);
    }
    
    /**
     * 显示食谱详情
     */
    private void showRecipeDetails(Recipe recipe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(recipe.getName());
        
        StringBuilder details = new StringBuilder();
        details.append("餐次：").append(recipe.getMealType().getDisplayName()).append("\n");
        details.append("热量：").append(String.format("%.0f千卡", recipe.getCalories())).append("\n");
        details.append("制作时间：").append(recipe.getCookingTime()).append("分钟\n\n");
        
        // 添加食材列表
        if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            details.append("食材用量：\n");
            for (Recipe.Ingredient ingredient : recipe.getIngredients()) {
                details.append("• ").append(ingredient.toString()).append("\n");
            }
            details.append("\n");
        }
        
        // 添加制作步骤
        if (recipe.getSteps() != null && !recipe.getSteps().isEmpty()) {
            details.append("制作步骤：\n");
            for (int i = 0; i < recipe.getSteps().size(); i++) {
                details.append((i + 1)).append(". ").append(recipe.getSteps().get(i)).append("\n");
            }
        }
        
        builder.setMessage(details.toString());
        builder.setPositiveButton("收藏", (dialog, which) -> {
            toggleFavorite(recipe);
        });
        builder.setNegativeButton("关闭", null);
        builder.show();
    }
    
    /**
     * 显示重新生成对话框
     */
    private void showRegenerateDialog(Recipe recipe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("重新生成食谱");
        builder.setMessage("您是不喜欢这道菜吗？点击确定为您重新生成" + recipe.getMealType().getDisplayName() + "推荐。");
        builder.setPositiveButton("重新生成", (dialog, which) -> {
            regenerateRecipe(recipe);
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    
    /**
     * 重新生成食谱
     */
    private void regenerateRecipe(Recipe oldRecipe) {
        // 找到该食谱对应的日期
        Date recipeDate = findRecipeDateInPlan(oldRecipe);
        if (recipeDate == null) {
            ToastUtils.show("无法确定食谱日期");
            return;
        }
        
        showLoading();
        ToastUtils.show("正在为您重新生成" + oldRecipe.getMealType().getDisplayName() + "...");
        
        recipeManager.regenerateRecipe(chatLlmUtil, recipeDate, oldRecipe.getMealType(), 
            new RecipeManager.SingleRecipeCallback() {
                @Override
                public void onSuccess(Recipe newRecipe) {
                    runOnUiThread(() -> {
                        hideLoading();
                        // 更新列表中的食谱
                        updateRecipeInList(oldRecipe, newRecipe);
                        ToastUtils.show("食谱已重新生成");
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        hideLoading();
                        ToastUtils.show("重新生成失败: " + error);
                    });
                }
            });
    }
    
    /**
     * 在周计划中找到食谱对应的日期
     */
    private Date findRecipeDateInPlan(Recipe recipe) {
        if (currentWeeklyPlan == null || currentWeeklyPlan.getDailyPlans() == null) {
            return null;
        }
        
        for (WeeklyRecipePlan.DailyRecipePlan dailyPlan : currentWeeklyPlan.getDailyPlans()) {
            if (recipe.equals(dailyPlan.getBreakfast()) ||
                recipe.equals(dailyPlan.getLunch()) ||
                recipe.equals(dailyPlan.getDinner())) {
                return dailyPlan.getDate();
            }
        }
        
        return null;
    }
    
    /**
     * 更新列表中的食谱
     */
    private void updateRecipeInList(Recipe oldRecipe, Recipe newRecipe) {
        for (int i = 0; i < currentRecipes.size(); i++) {
            if (currentRecipes.get(i).getId().equals(oldRecipe.getId())) {
                currentRecipes.set(i, newRecipe);
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }
    
    /**
     * 切换收藏状态
     */
    private void toggleFavorite(Recipe recipe) {
        if (recipe.isFavorite()) {
            recipeManager.removeFromFavorites(recipe.getId());
            recipe.setFavorite(false);
            ToastUtils.show("已取消收藏");
        } else {
            recipeManager.addToFavorites(recipe);
            recipe.setFavorite(true);
            ToastUtils.show("已添加到收藏");
        }
        
        // 刷新适配器
        adapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭ChatLlmUtil的资源
        if (chatLlmUtil != null) {
            chatLlmUtil.shutdown();
        }
        // 关闭RecipeManager的资源
        if (recipeManager != null) {
            recipeManager.shutdown();
        }
    }
}
