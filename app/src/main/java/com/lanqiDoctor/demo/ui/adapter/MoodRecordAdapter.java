package com.lanqiDoctor.demo.ui.adapter;

import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.entity.MoodRecord;
import com.lanqiDoctor.demo.model.MoodLevel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 心情记录列表适配器
 */
public class MoodRecordAdapter extends RecyclerView.Adapter<MoodRecordAdapter.MoodRecordViewHolder> {

    private final List<MoodRecord> records = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private MoodRecordActionListener actionListener;

    public void setActionListener(MoodRecordActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void submitList(List<MoodRecord> newRecords) {
        records.clear();
        if (newRecords != null) {
            records.addAll(newRecords);
        }
        notifyDataSetChanged();
    }

    public MoodRecord getItem(int position) {
        return records.get(position);
    }

    @NonNull
    @Override
    public MoodRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_record, parent, false);
        return new MoodRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodRecordViewHolder holder, int position) {
        MoodRecord record = records.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class MoodRecordViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMoodLabel;
        private final TextView tvTime;
        private final ImageView ivMoodPhoto;
        private final TextView tvNote;
        private final TextView tvActivities;
        private final ImageButton btnFavorite;

        MoodRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMoodLabel = itemView.findViewById(R.id.tv_mood_label);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivMoodPhoto = itemView.findViewById(R.id.iv_mood_photo);
            tvNote = itemView.findViewById(R.id.tv_note);
            tvActivities = itemView.findViewById(R.id.tv_activities);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }

        void bind(MoodRecord record) {
            MoodLevel moodLevel = MoodLevel.fromStorage(record.getMoodLevel());
            tvMoodLabel.setText(itemView.getContext().getString(moodLevel.getLabelRes()));
            tvMoodLabel.setTextColor(ContextCompat.getColor(itemView.getContext(), moodLevel.getColorRes()));
            tvMoodLabel.setBackground(createBadgeBackground(moodLevel));

            tvTime.setText(formatTime(record.getCreatedAt()));

            List<String> images = record.getImageUriList();
            if (images != null && !images.isEmpty()) {
                ivMoodPhoto.setVisibility(View.VISIBLE);
                Glide.with(ivMoodPhoto.getContext())
                        .load(Uri.parse(images.get(0)))
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.bg_image_placeholder)
                        .centerCrop()
                        .into(ivMoodPhoto);
            } else {
                ivMoodPhoto.setVisibility(View.GONE);
                ivMoodPhoto.setImageDrawable(null);
            }

            if (record.getNote() != null && !record.getNote().trim().isEmpty()) {
                tvNote.setText(record.getNote().trim());
                tvNote.setVisibility(View.VISIBLE);
            } else {
                tvNote.setVisibility(View.GONE);
            }

            if (record.getActivities() != null && !record.getActivities().trim().isEmpty()) {
                tvActivities.setText("#" + record.getActivities().replace(",", "  #"));
                tvActivities.setVisibility(View.VISIBLE);
            } else {
                tvActivities.setVisibility(View.GONE);
            }

            btnFavorite.setImageResource(record.isFavorite() ? R.drawable.ic_mood_star_on : R.drawable.ic_mood_star_off);
            btnFavorite.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onToggleFavorite(record);
                }
            });

            itemView.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onRecordClick(record);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onRecordLongPress(record);
                    return true;
                }
                return false;
            });
        }

        private GradientDrawable createBadgeBackground(MoodLevel moodLevel) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(dpToPx(16));
            drawable.setColor(ContextCompat.getColor(itemView.getContext(), moodLevel.getBackgroundColorRes()));
            return drawable;
        }

        private String formatTime(long timestamp) {
            if (timestamp <= 0) {
                return "";
            }
            return timeFormat.format(new Date(timestamp));
        }

        private float dpToPx(float dp) {
            return dp * itemView.getResources().getDisplayMetrics().density;
        }
    }

    public interface MoodRecordActionListener {
        void onRecordClick(MoodRecord record);

        void onRecordLongPress(MoodRecord record);

        void onToggleFavorite(MoodRecord record);
    }
}
