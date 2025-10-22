package com.lanqiDoctor.demo.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.entity.MedicationRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 用药记录适配器
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class MedicationRecordAdapter extends RecyclerView.Adapter<MedicationRecordAdapter.ViewHolder> {
    
    private List<MedicationRecord> medicationList;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    public interface OnItemClickListener {
        void onItemClick(MedicationRecord medication, int position);
    }
    
    public interface OnItemLongClickListener {
        boolean onItemLongClick(MedicationRecord medication, int position);
    }
    
    public MedicationRecordAdapter(List<MedicationRecord> medicationList) {
        this.medicationList = medicationList;
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
                .inflate(R.layout.item_medication_record, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicationRecord medication = medicationList.get(position);
        
        holder.tvMedicationName.setText(medication.getMedicationName());
        holder.tvDosageInfo.setText(medication.getDosage() + " " + medication.getUnit() + " · " + medication.getFrequency());
        
        // 显示状态
        String statusText = getStatusText(medication.getStatus());
        holder.tvStatus.setText(statusText);
        holder.tvStatus.setTextColor(getStatusColor(holder.itemView.getContext(), medication.getStatus()));
        
        // 显示开始时间
        if (medication.getStartDate() != null) {
            String startDate = dateFormat.format(new Date(medication.getStartDate()));
            holder.tvStartDate.setText("开始时间：" + startDate);
        } else {
            holder.tvStartDate.setText("开始时间：未设置");
        }
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(medication, position);
            }
        });
        
        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(medication, position);
            }
            return false;
        });
    }
    
    @Override
    public int getItemCount() {
        return medicationList.size();
    }
    
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "已停用";
            case 1: return "正在服用";
            case 2: return "已完成";
            default: return "未知";
        }
    }
    
    private int getStatusColor(android.content.Context context, Integer status) {
        if (status == null) return context.getColor(android.R.color.darker_gray);
        switch (status) {
            case 0: return context.getColor(android.R.color.darker_gray);
            case 1: return context.getColor(android.R.color.holo_green_dark);
            case 2: return context.getColor(android.R.color.holo_blue_dark);
            default: return context.getColor(android.R.color.darker_gray);
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicationName;
        TextView tvDosageInfo;
        TextView tvStatus;
        TextView tvStartDate;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicationName = itemView.findViewById(R.id.tv_medication_name);
            tvDosageInfo = itemView.findViewById(R.id.tv_dosage_info);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvStartDate = itemView.findViewById(R.id.tv_start_date);
        }
    }
}