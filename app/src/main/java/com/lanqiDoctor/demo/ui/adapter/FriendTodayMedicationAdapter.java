package com.lanqiDoctor.demo.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.model.TodayMedicationItem;


import java.util.List;

/**
 * 亲友今日服药适配器（只读模式）
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class FriendTodayMedicationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private List<TodayMedicationItem> items;
    private Context context;
    
    public FriendTodayMedicationAdapter(List<TodayMedicationItem> items) {
        this.items = items;
    }
    
    @Override
    public int getItemViewType(int position) {
        return items.get(position).getItemType();
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        
        if (viewType == TodayMedicationItem.TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_time_group_header, parent, false);
            return new TimeGroupHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_friend_today_medication, parent, false);
            return new FriendMedicationViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TodayMedicationItem item = items.get(position);
        
        if (holder instanceof TimeGroupHeaderViewHolder) {
            ((TimeGroupHeaderViewHolder) holder).bind(item);
        } else if (holder instanceof FriendMedicationViewHolder) {
            ((FriendMedicationViewHolder) holder).bind(item);
        }
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    /**
     * 时间段标题ViewHolder
     */
    static class TimeGroupHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTimeGroup;
        
        public TimeGroupHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimeGroup = itemView.findViewById(R.id.tv_time_group);
        }
        
        public void bind(TodayMedicationItem item) {
            tvTimeGroup.setText(item.getTimeGroup());
        }
    }
    
    /**
     * 亲友服药项目ViewHolder（只读模式）
     */
    static class FriendMedicationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMedicationName;
        private TextView tvDosage;
        private TextView tvTime;
        private ImageView ivStatusIcon;
        private TextView tvStatusText;
        private View layoutContainer;
        
        public FriendMedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicationName = itemView.findViewById(R.id.tv_medication_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivStatusIcon = itemView.findViewById(R.id.iv_status_icon);
            tvStatusText = itemView.findViewById(R.id.tv_status_text);
            layoutContainer = itemView.findViewById(R.id.layout_container);
        }
        
        public void bind(TodayMedicationItem item) {
            // 设置药物信息
            tvMedicationName.setText(item.getMedicationName());
            tvDosage.setText(item.getFullDosageInfo());
            tvTime.setText(item.getTimeString());
            
            // 设置服用状态（只读显示）
            if (item.isFriendTaken()) {
                // 已服用状态
                ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
                ivStatusIcon.setColorFilter(itemView.getContext().getColor(R.color.green_500));
                tvStatusText.setText("已服用");
                tvStatusText.setTextColor(itemView.getContext().getColor(R.color.green_500));
                layoutContainer.setBackgroundResource(R.drawable.medication_item_taken_background);
            } else {
                // 未服用状态
                ivStatusIcon.setImageResource(R.drawable.ic_clock);
                ivStatusIcon.setColorFilter(itemView.getContext().getColor(R.color.orange_500));
                tvStatusText.setText("待服用");
                tvStatusText.setTextColor(itemView.getContext().getColor(R.color.orange_500));
                layoutContainer.setBackgroundResource(R.drawable.medication_item_pending_background);
            }
            
            // 禁用点击（只读模式）
            itemView.setClickable(false);
            itemView.setFocusable(false);
            layoutContainer.setAlpha(0.8f); // 稍微降低透明度表示只读
        }
    }
}