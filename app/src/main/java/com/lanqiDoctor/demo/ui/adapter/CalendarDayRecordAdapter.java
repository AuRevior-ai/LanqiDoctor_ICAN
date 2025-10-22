package main.java.com.lanqiDoctor.demo.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.entity.MedicationIntakeRecord;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * 日历弹窗中的服药记录适配器（复用亲友服药卡片样式）
 */
public class CalendarDayRecordAdapter extends RecyclerView.Adapter<CalendarDayRecordAdapter.ViewHolder> {

    private List<MedicationIntakeRecord> records;

    public CalendarDayRecordAdapter(List<MedicationIntakeRecord> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_today_medication, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicationIntakeRecord record = records.get(position);
        
        // 设置药物名称
        holder.tvMedicationName.setText(record.getMedicationName());
        
        // 设置剂量信息
        String dosageInfo = record.getActualDosage() != null ? record.getActualDosage() : "未记录剂量";
        holder.tvDosage.setText(dosageInfo);
        
        // 设置时间
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeText = record.getPlannedTime() != null ? 
                timeFormat.format(record.getPlannedTime()) : "未知时间";
        holder.tvTime.setText(timeText);
        
        // 设置服用状态
        if (record.getStatus() != null && record.getStatus() == 1) {
            // 已服用状态
            holder.ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
            holder.ivStatusIcon.setColorFilter(holder.itemView.getContext().getColor(R.color.green_500));
            holder.tvStatusText.setText("已服用");
            holder.tvStatusText.setTextColor(holder.itemView.getContext().getColor(R.color.green_500));
            holder.layoutContainer.setBackgroundResource(R.drawable.medication_item_taken_background);
        } else {
            // 未服用状态
            holder.ivStatusIcon.setImageResource(R.drawable.ic_clock);
            holder.ivStatusIcon.setColorFilter(holder.itemView.getContext().getColor(R.color.orange_500));
            holder.tvStatusText.setText("待服用");
            holder.tvStatusText.setTextColor(holder.itemView.getContext().getColor(R.color.orange_500));
            holder.layoutContainer.setBackgroundResource(R.drawable.medication_item_pending_background);
        }
        
        // 设置为只读模式
        holder.itemView.setClickable(false);
        holder.itemView.setFocusable(false);
    }

    @Override
    public int getItemCount() {
        return records != null ? records.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicationName;
        TextView tvDosage;
        TextView tvTime;
        ImageView ivStatusIcon;
        TextView tvStatusText;
        View layoutContainer;

        ViewHolder(View itemView) {
            super(itemView);
            tvMedicationName = itemView.findViewById(R.id.tv_medication_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivStatusIcon = itemView.findViewById(R.id.iv_status_icon);
            tvStatusText = itemView.findViewById(R.id.tv_status_text);
            layoutContainer = itemView.findViewById(R.id.layout_container);
        }
    }
}