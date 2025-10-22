package com.lanqiDoctor.demo.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.ui.model.PermissionItem;

import java.util.List;

/**
 * 权限列表适配器
 */
public class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.ViewHolder> {
    
    private List<PermissionItem> permissionList;
    private OnPermissionClickListener listener;
    
    public interface OnPermissionClickListener {
        void onPermissionClick(PermissionItem item);
    }
    
    public PermissionAdapter(List<PermissionItem> permissionList, OnPermissionClickListener listener) {
        this.permissionList = permissionList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_permission, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PermissionItem item = permissionList.get(position);
        
        holder.tvPermissionName.setText(item.getName());
        holder.tvPermissionDescription.setText(item.getDescription());
        holder.tvPermissionType.setText(item.getTypeText());
        holder.tvPermissionType.setTextColor(holder.itemView.getContext()
                .getResources().getColor(item.getTypeColor()));
        
        // 设置权限状态图标
        if (item.isGranted()) {
            holder.ivPermissionStatus.setImageResource(R.drawable.ic_check_circle);
            holder.ivPermissionStatus.setColorFilter(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_green_dark));
            holder.tvPermissionStatus.setText("已授予");
            holder.tvPermissionStatus.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.ivPermissionStatus.setImageResource(R.drawable.ic_error_circle);
            holder.ivPermissionStatus.setColorFilter(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_red_dark));
            holder.tvPermissionStatus.setText("未授予");
            holder.tvPermissionStatus.setTextColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.holo_red_dark));
        }
        
        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPermissionClick(item);
            }
        });
        
        // 高亮显示未授予的必需权限
        if (!item.isGranted() && item.getType() == PermissionItem.Type.ESSENTIAL) {
            holder.itemView.setBackgroundColor(holder.itemView.getContext()
                    .getResources().getColor(R.color.permission_highlight_bg));
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.getContext()
                    .getResources().getColor(android.R.color.transparent));
        }
    }
    
    @Override
    public int getItemCount() {
        return permissionList.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPermissionName;
        TextView tvPermissionDescription;
        TextView tvPermissionType;
        TextView tvPermissionStatus;
        ImageView ivPermissionStatus;
        
        ViewHolder(View itemView) {
            super(itemView);
            tvPermissionName = itemView.findViewById(R.id.tv_permission_name);
            tvPermissionDescription = itemView.findViewById(R.id.tv_permission_description);
            tvPermissionType = itemView.findViewById(R.id.tv_permission_type);
            tvPermissionStatus = itemView.findViewById(R.id.tv_permission_status);
            ivPermissionStatus = itemView.findViewById(R.id.iv_permission_status);
        }
    }
}
