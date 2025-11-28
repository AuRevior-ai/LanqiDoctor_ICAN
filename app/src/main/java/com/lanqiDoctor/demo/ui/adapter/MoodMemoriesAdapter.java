package com.lanqiDoctor.demo.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.entity.MoodRecord;
import com.lanqiDoctor.demo.model.MoodLevel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 忆迹瀑布流适配器。
 */
public class MoodMemoriesAdapter extends RecyclerView.Adapter<MoodMemoriesAdapter.MemoryViewHolder> {

    private final List<MoodRecord> records = new ArrayList<>();
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault());
    private MemoryActionListener actionListener;

    public void setActionListener(MemoryActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void submitList(List<MoodRecord> data) {
        records.clear();
        if (data != null) {
            records.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_memory, parent, false);
        return new MemoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoryViewHolder holder, int position) {
        holder.bind(records.get(position));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class MemoryViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivCover;
        private final TextView tvDateTime;
        private final TextView tvMood;
        private final ImageButton btnFavorite;

        MemoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_memory_cover);
            tvDateTime = itemView.findViewById(R.id.tv_memory_datetime);
            tvMood = itemView.findViewById(R.id.tv_memory_mood);
            btnFavorite = itemView.findViewById(R.id.btn_memory_favorite);
        }

        void bind(MoodRecord record) {
            List<String> images = record.getImageUriList();
            if (images != null && !images.isEmpty()) {
                Glide.with(ivCover.getContext())
                        .load(images.get(0))
                        .placeholder(R.drawable.bg_image_placeholder)
                        .centerCrop()
                        .into(ivCover);
            } else {
                Glide.with(ivCover.getContext())
                        .load(R.drawable.bg_image_placeholder)
                        .centerCrop()
                        .into(ivCover);
            }

            tvDateTime.setText(dateTimeFormat.format(new Date(record.getCreatedAt())));
            MoodLevel moodLevel = MoodLevel.fromStorage(record.getMoodLevel());
            tvMood.setText(moodLevel.getEmoji());
            btnFavorite.setImageResource(record.isFavorite() ? R.drawable.ic_mood_star_on : R.drawable.ic_mood_star_off);

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

            btnFavorite.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onToggleFavorite(record);
                }
            });
        }
    }

    public interface MemoryActionListener {
        void onRecordClick(MoodRecord record);

        void onRecordLongPress(MoodRecord record);

        void onToggleFavorite(MoodRecord record);
    }
}
