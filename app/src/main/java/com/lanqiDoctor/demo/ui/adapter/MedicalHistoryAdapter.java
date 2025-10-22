package com.lanqiDoctor.demo.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.entity.MedicalHistory;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * 既往病史记录适配器
 * 
 * @author 蓝旗医生开发团队
 * @version 1.0
 */
public class MedicalHistoryAdapter extends RecyclerView.Adapter<MedicalHistoryAdapter.ViewHolder> {
    
    private List<MedicalHistory> historyList;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    public interface OnItemClickListener {
        void onItemClick(MedicalHistory history, int position);
    }
    
    public interface OnItemLongClickListener {
        boolean onItemLongClick(MedicalHistory history, int position);
    }
    
    public MedicalHistoryAdapter(List<MedicalHistory> historyList) {
        this.historyList = historyList;
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
                .inflate(R.layout.item_medical_history, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicalHistory history = historyList.get(position);
        
        holder.tvDiseaseName.setText(history.getDiseaseName());
        
        // 显示诊断时间和严重程度
        String diagnosisInfo = "";
        if (history.getDiagnosisDate() != null && !history.getDiagnosisDate().isEmpty()) {
            diagnosisInfo += "诊断时间：" + history.getDiagnosisDate();
        }
        if (history.getSeverity() != null && !history.getSeverity().isEmpty()) {
            if (!diagnosisInfo.isEmpty()) {
                diagnosisInfo += " · ";
            }
            diagnosisInfo += "严重程度：" + history.getSeverity();
        }
        holder.tvDiagnosisInfo.setText(diagnosisInfo.isEmpty() ? "暂无诊断信息" : diagnosisInfo);
        
        // 显示治疗状况
        String treatmentStatus = history.getTreatmentStatus();
        if (treatmentStatus != null && !treatmentStatus.isEmpty()) {
            holder.tvTreatmentStatus.setText(treatmentStatus);
            holder.tvTreatmentStatus.setTextColor(getTreatmentStatusColor(holder.itemView.getContext(), treatmentStatus));
        } else {
            holder.tvTreatmentStatus.setText("治疗状况未知");
            holder.tvTreatmentStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
        }
        
        // 显示就诊医院
        if (history.getHospital() != null && !history.getHospital().isEmpty()) {
            holder.tvHospital.setText("就诊医院：" + history.getHospital());
            holder.tvHospital.setVisibility(View.VISIBLE);
        } else {
            holder.tvHospital.setVisibility(View.GONE);
        }
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(history, position);
            }
        });
        
        // 设置长按事件
        holder.itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                return onItemLongClickListener.onItemLongClick(history, position);
            }
            return false;
        });
    }
    
    @Override
    public int getItemCount() {
        return historyList.size();
    }
    
    private int getTreatmentStatusColor(android.content.Context context, String treatmentStatus) {
        switch (treatmentStatus) {
            case "已治愈":
                return context.getResources().getColor(android.R.color.holo_green_dark);
            case "治疗中":
                return context.getResources().getColor(android.R.color.holo_orange_dark);
            case "慢性病":
                return context.getResources().getColor(android.R.color.holo_red_dark);
            default:
                return context.getResources().getColor(android.R.color.darker_gray);
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDiseaseName;
        TextView tvDiagnosisInfo;
        TextView tvTreatmentStatus;
        TextView tvHospital;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvDiseaseName = itemView.findViewById(R.id.tv_disease_name);
            tvDiagnosisInfo = itemView.findViewById(R.id.tv_diagnosis_info);
            tvTreatmentStatus = itemView.findViewById(R.id.tv_treatment_status);
            tvHospital = itemView.findViewById(R.id.tv_hospital);
        }
    }
}
