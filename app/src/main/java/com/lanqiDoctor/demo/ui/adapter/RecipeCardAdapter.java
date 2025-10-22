package com.lanqiDoctor.demo.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.model.Recipe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 食谱卡片适配器
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class RecipeCardAdapter extends RecyclerView.Adapter<RecipeCardAdapter.RecipeViewHolder> {
    
    private Context context;
    private List<Recipe> recipes;
    private OnRecipeClickListener onRecipeClickListener;
    private OnRecipeLongClickListener onRecipeLongClickListener;
    
    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }
    
    public interface OnRecipeLongClickListener {
        void onRecipeLongClick(Recipe recipe, View view);
    }
    
    public RecipeCardAdapter(Context context) {
        this.context = context;
    }
    
    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }
    
    public void setOnRecipeClickListener(OnRecipeClickListener listener) {
        this.onRecipeClickListener = listener;
    }
    
    public void setOnRecipeLongClickListener(OnRecipeLongClickListener listener) {
        this.onRecipeLongClickListener = listener;
    }
    
    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe_card, parent, false);
        return new RecipeViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);
    }
    
    @Override
    public int getItemCount() {
        return recipes != null ? recipes.size() : 0;
    }
    
    class RecipeViewHolder extends RecyclerView.ViewHolder {
        
        private CardView cardView;
        private TextView tvRecipeName;
        private TextView tvRecipeDescription;
        private TextView tvCalories;
        private TextView tvCookingTime;
        private TextView tvMealType;
        private ImageView ivDifficulty;
        private ImageView ivFavorite;
        private LinearLayout llIngredients;
        private TextView tvIngredientsTitle;
        
        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView.findViewById(R.id.card_recipe);
            tvRecipeName = itemView.findViewById(R.id.tv_recipe_name);
            tvRecipeDescription = itemView.findViewById(R.id.tv_recipe_description);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            tvCookingTime = itemView.findViewById(R.id.tv_cooking_time);
            tvMealType = itemView.findViewById(R.id.tv_meal_type);
            ivDifficulty = itemView.findViewById(R.id.iv_difficulty);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
            llIngredients = itemView.findViewById(R.id.ll_ingredients);
            tvIngredientsTitle = itemView.findViewById(R.id.tv_ingredients_title);
        }
        
        public void bind(Recipe recipe) {
            // 设置基本信息
            if (tvRecipeName != null) {
                tvRecipeName.setText(recipe.getName() != null ? recipe.getName() : "未知菜名");
            }
            
            if (tvRecipeDescription != null) {
                tvRecipeDescription.setText(recipe.getDescription() != null ? recipe.getDescription() : "");
                tvRecipeDescription.setVisibility(
                    recipe.getDescription() != null && !recipe.getDescription().isEmpty() 
                        ? View.VISIBLE : View.GONE
                );
            }
            
            // 设置热量
            if (tvCalories != null) {
                tvCalories.setText(String.format(Locale.getDefault(), "%.0f千卡", recipe.getCalories()));
            }
            
            // 设置制作时间
            if (tvCookingTime != null) {
                tvCookingTime.setText(String.format(Locale.getDefault(), "%d分钟", recipe.getCookingTime()));
            }
            
            // 设置餐次类型
            if (tvMealType != null && recipe.getMealType() != null) {
                tvMealType.setText(recipe.getMealType().getDisplayName());
                
                // 根据餐次类型设置不同的背景颜色
                int backgroundRes = getMealTypeBackground(recipe.getMealType());
                tvMealType.setBackgroundResource(backgroundRes);
            }
            
            // 设置难度图标
            if (ivDifficulty != null) {
                setDifficultyIcon(ivDifficulty, recipe.getDifficulty());
            }
            
            // 设置收藏状态
            if (ivFavorite != null) {
                ivFavorite.setImageResource(recipe.isFavorite() 
                    ? R.drawable.ic_favorite_filled 
                    : R.drawable.ic_favorite_outline);
                ivFavorite.setOnClickListener(v -> {
                    // 切换收藏状态
                    recipe.setFavorite(!recipe.isFavorite());
                    ivFavorite.setImageResource(recipe.isFavorite() 
                        ? R.drawable.ic_favorite_filled 
                        : R.drawable.ic_favorite_outline);
                    
                    // 通知收藏状态变化
                    // 这里可以添加收藏逻辑的回调
                });
            }
            
            // 设置食材列表
            setupIngredients(recipe);
            
            // 设置点击事件
            if (cardView != null) {
                cardView.setOnClickListener(v -> {
                    if (onRecipeClickListener != null) {
                        onRecipeClickListener.onRecipeClick(recipe);
                    }
                });
                
                cardView.setOnLongClickListener(v -> {
                    if (onRecipeLongClickListener != null) {
                        onRecipeLongClickListener.onRecipeLongClick(recipe, v);
                        return true;
                    }
                    return false;
                });
            }
        }
        
        private void setupIngredients(Recipe recipe) {
            if (llIngredients == null || tvIngredientsTitle == null) {
                return;
            }
            
            // 清除之前的食材视图
            llIngredients.removeAllViews();
            
            if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
                tvIngredientsTitle.setVisibility(View.VISIBLE);
                
                for (Recipe.Ingredient ingredient : recipe.getIngredients()) {
                    TextView ingredientView = new TextView(context);
                    ingredientView.setText("• " + ingredient.toString());
                    ingredientView.setTextSize(12);
                    ingredientView.setTextColor(context.getResources().getColor(R.color.text_secondary));
                    
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 4, 0, 0);
                    ingredientView.setLayoutParams(params);
                    
                    llIngredients.addView(ingredientView);
                }
            } else {
                tvIngredientsTitle.setVisibility(View.GONE);
            }
        }
        
        private int getMealTypeBackground(Recipe.MealType mealType) {
            switch (mealType) {
                case BREAKFAST:
                    return R.drawable.bg_meal_type_breakfast;
                case LUNCH:
                    return R.drawable.bg_meal_type_lunch;
                case DINNER:
                    return R.drawable.bg_meal_type_dinner;
                case SNACK:
                    return R.drawable.bg_meal_type_snack;
                default:
                    return R.drawable.bg_meal_type_default;
            }
        }
        
        private void setDifficultyIcon(ImageView imageView, int difficulty) {
            int iconRes;
            switch (difficulty) {
                case 1:
                    iconRes = R.drawable.ic_difficulty_easy;
                    break;
                case 2:
                    iconRes = R.drawable.ic_difficulty_medium;
                    break;
                case 3:
                case 4:
                case 5:
                    iconRes = R.drawable.ic_difficulty_hard;
                    break;
                default:
                    iconRes = R.drawable.ic_difficulty_medium;
                    break;
            }
            imageView.setImageResource(iconRes);
        }
    }
}
