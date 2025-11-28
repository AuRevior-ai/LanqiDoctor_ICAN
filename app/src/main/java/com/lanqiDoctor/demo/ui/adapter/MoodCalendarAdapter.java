package com.lanqiDoctor.demo.ui.adapter;

import android.graphics.Color;
import android.net.Uri;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lanqiDoctor.demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义日历网格适配器，支持双击新增心情。
 */
public class MoodCalendarAdapter extends RecyclerView.Adapter<MoodCalendarAdapter.DayViewHolder> {

    private final List<DayCell> days = new ArrayList<>();
    private long selectedDayStart;
    private OnDayInteractionListener interactionListener;
    private long lastClickDay = -1L;
    private long lastClickTimestamp = 0L;

    public void setOnDayInteractionListener(OnDayInteractionListener listener) {
        this.interactionListener = listener;
    }

    public void submitList(List<DayCell> items) {
        days.clear();
        if (items != null) {
            days.addAll(items);
        }
        notifyDataSetChanged();
    }

    public void setSelectedDay(long dayStart) {
        this.selectedDayStart = dayStart;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(days.get(position));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    private void handleClick(DayCell cell) {
        if (!cell.inCurrentMonth) {
            return;
        }
        long now = SystemClock.elapsedRealtime();
        if (cell.dayStart == lastClickDay && now - lastClickTimestamp < 320) {
            if (interactionListener != null) {
                interactionListener.onDayDoubleTap(cell.dayStart);
            }
        } else {
            if (interactionListener != null) {
                interactionListener.onDaySelected(cell.dayStart);
            }
        }
        lastClickDay = cell.dayStart;
        lastClickTimestamp = now;
    }

    class DayViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvDayNumber;
        private final ImageView ivDayImage;
        private final FrameLayout containerDay;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
            ivDayImage = itemView.findViewById(R.id.iv_day_image);
            containerDay = itemView.findViewById(R.id.container_day);
        }

        void bind(DayCell cell) {
            tvDayNumber.setText(String.valueOf(cell.dayOfMonth));
            containerDay.setAlpha(cell.inCurrentMonth ? 1f : 0.35f);
            containerDay.setBackground(cell.dayStart == selectedDayStart ? ContextCompat.getDrawable(containerDay.getContext(), R.drawable.bg_calendar_day_selected) : null);

            if (cell.hasImage()) {
                ivDayImage.setVisibility(View.VISIBLE);
                Glide.with(ivDayImage.getContext())
                        .load(Uri.parse(cell.coverImageUri))
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.bg_image_placeholder)
                        .centerCrop()
                        .into(ivDayImage);
                tvDayNumber.setTextColor(Color.WHITE);
            } else {
                ivDayImage.setVisibility(View.GONE);
                ivDayImage.setImageDrawable(null);
                int colorRes = cell.inCurrentMonth ? R.color.text_primary : R.color.text_secondary;
                tvDayNumber.setTextColor(ContextCompat.getColor(tvDayNumber.getContext(), colorRes));
            }

            if (cell.isToday && !cell.hasImage()) {
                tvDayNumber.setTextColor(ContextCompat.getColor(tvDayNumber.getContext(), R.color.icon_tint));
            }

            itemView.setOnClickListener(v -> handleClick(cell));
        }
    }

    public interface OnDayInteractionListener {
        void onDaySelected(long dayStart);

        void onDayDoubleTap(long dayStart);
    }

    public static class DayCell {
        public long dayStart;
        public int dayOfMonth;
        public boolean inCurrentMonth;
        public boolean isToday;
        public String coverImageUri;

        public boolean hasImage() {
            return coverImageUri != null && !coverImageUri.trim().isEmpty();
        }
    }
}
