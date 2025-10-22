package com.lanqiDoctor.demo.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.model.TodayMedicationItem;

import java.util.List;

/**
 * 今日服药情况适配器
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class TodayMedicationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private List<TodayMedicationItem> items;
    private OnMedicationStatusChangeListener listener;
    
    public interface OnMedicationStatusChangeListener {
        void onMedicationStatusChanged(TodayMedicationItem item, boolean isChecked);
    }
    
    public TodayMedicationAdapter(List<TodayMedicationItem> items, OnMedicationStatusChangeListener listener) {
        this.items = items;
        this.listener = listener;
    }
    
    @Override
    public int getItemViewType(int position) {
        return items.get(position).getItemType();
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        if (viewType == TodayMedicationItem.TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_medication_time_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_today_medication, parent, false);
            return new MedicationViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TodayMedicationItem item = items.get(position);
        
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(item);
        } else if (holder instanceof MedicationViewHolder) {
            ((MedicationViewHolder) holder).bind(item);
        }
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    // 时间段标题ViewHolder
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTimeGroup;
        
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimeGroup = itemView.findViewById(R.id.tv_time_group);
        }
        
        public void bind(TodayMedicationItem item) {
            tvTimeGroup.setText(item.getTimeGroup());
        }
    }
    
    // 药物项目ViewHolder
    class MedicationViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbMedicationStatus;
        private TextView tvMedicationName;
        private TextView tvDosageInfo;
        private TextView tvPlannedTime;
        private TextView tvStatusHint;
        
        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            cbMedicationStatus = itemView.findViewById(R.id.cb_medication_status);
            tvMedicationName = itemView.findViewById(R.id.tv_medication_name);
            tvDosageInfo = itemView.findViewById(R.id.tv_dosage_info);
            tvPlannedTime = itemView.findViewById(R.id.tv_planned_time);
            tvStatusHint = itemView.findViewById(R.id.tv_status_hint);
        }
        
        public void bind(TodayMedicationItem item) {
            // 药物名称
            tvMedicationName.setText(item.getMedicationName());
            
            // 剂量信息
            tvDosageInfo.setText(item.getFullDosageInfo());
            
            // 计划时间
            tvPlannedTime.setText(item.getTimeString());
            
            // 服药状态
            boolean isTaken = item.isTaken();
            cbMedicationStatus.setChecked(isTaken);
            
            // 状态提示
            if (isTaken) {
                tvStatusHint.setText("已服用");
                tvStatusHint.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else {
                tvStatusHint.setText("未服用");
                tvStatusHint.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
            }
            
            // 设置复选框点击监听器
            cbMedicationStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onMedicationStatusChanged(item, isChecked);
                }
                
                // 更新状态提示
                if (isChecked) {
                    tvStatusHint.setText("已服用");
                    tvStatusHint.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                } else {
                    tvStatusHint.setText("未服用");
                    tvStatusHint.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
                }
            });
        }
    }
}