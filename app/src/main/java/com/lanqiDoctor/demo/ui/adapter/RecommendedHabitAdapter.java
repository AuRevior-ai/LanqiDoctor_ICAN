package com.lanqiDoctor.demo.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.entity.Habit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AI推荐习惯适配器
 * 
 * @author 智途心伴开发团队
 * @version 1.0
 */
public class RecommendedHabitAdapter extends RecyclerView.Adapter<RecommendedHabitAdapter.ViewHolder> {

    private Context context;
    private List<Habit> habitList = new ArrayList<>();
    private Set<Integer> selectedPositions = new HashSet<>();
    private OnItemActionListener onItemActionListener;

    public interface OnItemActionListener {
        void onItemClick(Habit habit, int position);
        void onItemSelected(Habit habit, int position, boolean isSelected);
    }

    public RecommendedHabitAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<Habit> habitList) {
        if (habitList != null) {
            this.habitList = habitList;
            android.util.Log.d("RecommendedHabitAdapter", "设置数据，习惯数量: " + habitList.size());
            for (int i = 0; i < habitList.size(); i++) {
                android.util.Log.d("RecommendedHabitAdapter", "习惯 " + i + ": " + habitList.get(i).getHabitName());
            }
        } else {
            android.util.Log.w("RecommendedHabitAdapter", "设置的习惯列表为null");
        }
        notifyDataSetChanged();
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.onItemActionListener = listener;
    }

    /**
     * 获取选中的习惯
     */
    public List<Habit> getSelectedHabits() {
        List<Habit> selectedHabits = new ArrayList<>();
        for (int position : selectedPositions) {
            if (position < habitList.size()) {
                selectedHabits.add(habitList.get(position));
            }
        }
        return selectedHabits;
    }

    /**
     * 全选/取消全选
     */
    public void selectAll(boolean isSelectAll) {
        selectedPositions.clear();
        if (isSelectAll) {
            for (int i = 0; i < habitList.size(); i++) {
                selectedPositions.add(i);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 获取选中的数量
     */
    public int getSelectedCount() {
        return selectedPositions.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.util.Log.d("RecommendedHabitAdapter", "创建ViewHolder, parent高度: " + parent.getHeight() + ", parent宽度: " + parent.getWidth());
        View view = LayoutInflater.from(context).inflate(R.layout.item_recommended_habit, parent, false);
        ViewHolder holder = new ViewHolder(view);
        android.util.Log.d("RecommendedHabitAdapter", "ViewHolder创建完成，itemView高度: " + view.getLayoutParams().height);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        android.util.Log.d("RecommendedHabitAdapter", "绑定位置 " + position + " 的ViewHolder");
        Habit habit = habitList.get(position);
        android.util.Log.d("RecommendedHabitAdapter", "习惯名称: " + habit.getHabitName());
        
        // 检查ViewHolder的布局参数
        View itemView = holder.itemView;
        android.util.Log.d("RecommendedHabitAdapter", "绑定前 - itemView可见性: " + itemView.getVisibility() + 
                ", 宽度: " + itemView.getWidth() + ", 高度: " + itemView.getHeight());
        
        holder.bind(habit, position);
        
        // 绑定后再次检查
        android.util.Log.d("RecommendedHabitAdapter", "绑定后 - itemView可见性: " + itemView.getVisibility() + 
                ", 宽度: " + itemView.getWidth() + ", 高度: " + itemView.getHeight());
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private CheckBox cbSelected;
        private TextView tvHabitName;
        private TextView tvDescription;
        private TextView tvReason;
        private View layoutRating;
        private ImageView[] stars = new ImageView[5];

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            android.util.Log.d("RecommendedHabitAdapter", "初始化ViewHolder");
            
            cardView = (CardView) itemView;
            cbSelected = itemView.findViewById(R.id.checkbox_select);
            tvHabitName = itemView.findViewById(R.id.tv_habit_name);
            tvDescription = itemView.findViewById(R.id.tv_habit_description);
            tvReason = itemView.findViewById(R.id.tv_reason);
            layoutRating = itemView.findViewById(R.id.layout_rating);
            
            // 检查基本控件是否找到
            android.util.Log.d("RecommendedHabitAdapter", "控件检查 - cardView: " + (cardView != null) +
                    ", cbSelected: " + (cbSelected != null) +
                    ", tvHabitName: " + (tvHabitName != null) +
                    ", tvDescription: " + (tvDescription != null) +
                    ", tvReason: " + (tvReason != null) +
                    ", layoutRating: " + (layoutRating != null));
            
            // 初始化星级评分 - 添加null检查
            try {
                stars[0] = itemView.findViewById(R.id.star1);
                stars[1] = itemView.findViewById(R.id.star2);
                stars[2] = itemView.findViewById(R.id.star3);
                stars[3] = itemView.findViewById(R.id.star4);
                stars[4] = itemView.findViewById(R.id.star5);
                
                // 检查所有星星控件是否找到
                for (int i = 0; i < stars.length; i++) {
                    if (stars[i] == null) {
                        android.util.Log.w("RecommendedHabitAdapter", "星星控件 " + (i+1) + " 未找到");
                    }
                }
                android.util.Log.d("RecommendedHabitAdapter", "星级控件初始化完成");
            } catch (Exception e) {
                android.util.Log.e("RecommendedHabitAdapter", "初始化星级评分控件失败", e);
            }
        }

        public void bind(Habit habit, int position) {
            android.util.Log.d("RecommendedHabitAdapter", "开始绑定习惯: " + habit.getHabitName() + ", 位置: " + position);
            
            // 设置习惯信息
            tvHabitName.setText(habit.getHabitName());
            tvDescription.setText(habit.getDescription());

            // 设置推荐理由
            String reasonText = buildReasonText(habit);
            if (reasonText != null && !reasonText.isEmpty()) {
                tvReason.setText("💡 推荐理由：" + reasonText);
                tvReason.setVisibility(View.VISIBLE);
            } else {
                tvReason.setVisibility(View.GONE);
            }

            // 设置星级评分（基于优先级）
            android.util.Log.d("RecommendedHabitAdapter", "设置星级评分，优先级: " + habit.getPriority());
            setStarRating(habit.getPriority());

            // 设置选中状态
            boolean isSelected = selectedPositions.contains(position);
            cbSelected.setChecked(isSelected);

            // 设置点击监听
            cbSelected.setOnClickListener(v -> {
                boolean newSelected = cbSelected.isChecked();
                if (newSelected) {
                    selectedPositions.add(position);
                } else {
                    selectedPositions.remove(position);
                }
                
                if (onItemActionListener != null) {
                    onItemActionListener.onItemSelected(habit, position, newSelected);
                }
            });

            cardView.setOnClickListener(v -> {
                if (onItemActionListener != null) {
                    onItemActionListener.onItemClick(habit, position);
                }
            });

            // 设置卡片的选中状态样式
            if (isSelected) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light, context.getTheme()));
                } else {
                    cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
                }
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white, context.getTheme()));
                } else {
                    cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
                }
            }
            
            android.util.Log.d("RecommendedHabitAdapter", "完成绑定习惯: " + habit.getHabitName());
        }

        private String buildReasonText(Habit habit) {
            // 根据习惯分类生成推荐理由
            String category = habit.getCategory();
            if (category == null) return null;
            
            switch (category) {
                case "HEALTH":
                    return "有助于改善您的整体健康状况";
                case "EXERCISE":
                    return "增强体质，提高心肺功能";
                case "DIET":
                    return "优化营养摄入，保持健康体重";
                case "STUDY":
                    return "提升认知能力，保持大脑活跃";
                default:
                    return "有助于形成良好的生活习惯";
            }
        }

        private void setStarRating(Integer priority) {
            // 确保优先级在合理范围内 (1-5)
            int rating = priority != null ? priority : 3;
            rating = Math.max(1, Math.min(5, rating)); // 限制在1-5之间
            
            try {
                for (int i = 0; i < stars.length; i++) {
                    if (stars[i] != null) {
                        if (i < rating) {
                            stars[i].setImageResource(R.drawable.ic_star_filled);
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                stars[i].setColorFilter(context.getResources().getColor(android.R.color.holo_orange_light, context.getTheme()));
                            } else {
                                stars[i].setColorFilter(context.getResources().getColor(android.R.color.holo_orange_light));
                            }
                        } else {
                            stars[i].setImageResource(R.drawable.ic_star_empty);
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                stars[i].setColorFilter(context.getResources().getColor(android.R.color.darker_gray, context.getTheme()));
                            } else {
                                stars[i].setColorFilter(context.getResources().getColor(android.R.color.darker_gray));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("RecommendedHabitAdapter", "设置星级评分失败", e);
            }
        }
    }
}
