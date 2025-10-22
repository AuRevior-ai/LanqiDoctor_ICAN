package com.lanqiDoctor.demo.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.entity.Habit;

import java.util.List;

/**
 * 习惯记录适配器
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.ViewHolder> {
    
    private List<Habit> habitList;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    
    public interface OnItemClickListener {
        void onItemClick(Habit habit, int position);
    }
    
    public interface OnItemLongClickListener {
        boolean onItemLongClick(Habit habit, int position);
    }
    
    public HabitAdapter(List<Habit> habitList) {
        this.habitList = habitList;
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habitList.get(position);
        
        // 设置习惯名称
        holder.tvHabitName.setText(habit.getHabitName());
        
        // 设置习惯描述
        if (habit.getDescription() != null && !habit.getDescription().isEmpty()) {
            holder.tvDescription.setText(habit.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }
        
        // 设置频次和周期信息
        String frequencyInfo = habit.getFrequencyDescription();
        if (habit.getDuration() != null && habit.getDuration() > 0) {
            frequencyInfo += " · " + habit.getDurationDescription();
        }
        holder.tvFrequencyInfo.setText(frequencyInfo);
        
        // 设置周期信息
        holder.tvCycleInfo.setText("周期: " + habit.getCycleDescription());
        
        // 设置进度信息
        int completedDays = habit.getCompletedDays() != null ? habit.getCompletedDays() : 0;
        int cycleDays = habit.getCycleDays() != null ? habit.getCycleDays() : 0;
        String progressText = "已坚持 " + completedDays + " 天";
        if (cycleDays > 0) {
            progressText += " / " + cycleDays + " 天";
        }
        holder.tvProgress.setText(progressText);
        
        // 设置状态标签
        setStatusInfo(holder, habit);
        
        // 设置分类图标
        setCategoryIcon(holder, habit.getCategory());
        
        // 设置激活状态样式
        setActiveState(holder, habit.getIsActive());
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(habit, position);
            }
        });
        
        // 设置长按事件
        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(habit, position);
            }
            return false;
        });
    }
    
    @Override
    public int getItemCount() {
        return habitList.size();
    }
    
    /**
     * 设置状态信息
     */
    private void setStatusInfo(ViewHolder holder, Habit habit) {
        String statusText;
        int statusColor;
        
        if (!habit.getIsActive()) {
            statusText = "已暂停";
            statusColor = holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray);
        } else if (habit.getStatus() == 2) {
            statusText = "已完成";
            statusColor = holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark);
        } else {
            // 根据优先级设置状态
            Integer priority = habit.getPriority();
            if (priority != null && priority >= 4) {
                statusText = "高优先级";
                statusColor = holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark);
            } else if (priority != null && priority <= 2) {
                statusText = "低优先级";
                statusColor = holder.itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark);
            } else {
                statusText = "进行中";
                statusColor = holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark);
            }
        }
        
        holder.tvStatus.setText(statusText);
        holder.tvStatus.setTextColor(statusColor);
    }
    
    /**
     * 设置分类图标
     */
    private void setCategoryIcon(ViewHolder holder, String category) {
        int iconRes;
        int iconColor;
        
        if (category == null) category = "OTHER";
        
        switch (category) {
            case "HEALTH":
                iconRes = android.R.drawable.ic_menu_my_calendar; // 健康图标
                iconColor = 0xFF4CAF50; // 绿色
                break;
            case "EXERCISE":
                iconRes = android.R.drawable.ic_menu_directions; // 运动图标
                iconColor = 0xFF2196F3; // 蓝色
                break;
            case "DIET":
                iconRes = android.R.drawable.ic_menu_gallery; // 使用现有的图标代替
                iconColor = 0xFFFF9800; // 橙色
                break;
            case "STUDY":
                iconRes = android.R.drawable.ic_menu_edit; // 学习图标
                iconColor = 0xFF9C27B0; // 紫色
                break;
            case "OTHER":
            default:
                iconRes = android.R.drawable.ic_menu_agenda; // 其他图标
                iconColor = 0xFF607D8B; // 灰蓝色
                break;
        }
        
        holder.ivCategoryIcon.setImageResource(iconRes);
        holder.ivCategoryIcon.setColorFilter(iconColor);
    }
    
    /**
     * 设置激活状态样式
     */
    private void setActiveState(ViewHolder holder, Boolean isActive) {
        float alpha = (isActive != null && isActive) ? 1.0f : 0.6f;
        holder.itemView.setAlpha(alpha);
        
        // 如果是暂停状态，可以添加一些视觉效果
        if (isActive == null || !isActive) {
            holder.itemView.setBackgroundColor(
                holder.itemView.getContext().getResources().getColor(android.R.color.background_light)
            );
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvHabitName;
        TextView tvDescription;
        TextView tvFrequencyInfo;
        TextView tvCycleInfo;
        TextView tvProgress;
        TextView tvStatus;
        
        ViewHolder(View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvHabitName = itemView.findViewById(R.id.tv_habit_name);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvFrequencyInfo = itemView.findViewById(R.id.tv_frequency_info);
            tvCycleInfo = itemView.findViewById(R.id.tv_cycle_info);
            tvProgress = itemView.findViewById(R.id.tv_progress);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}
