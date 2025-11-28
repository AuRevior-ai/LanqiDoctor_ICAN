package com.lanqiDoctor.demo.ui.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lanqiDoctor.demo.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 编辑页图片预览适配器。
 */
public class MoodPhotoPreviewAdapter extends RecyclerView.Adapter<MoodPhotoPreviewAdapter.PhotoViewHolder> {

    private final List<String> imageUris = new ArrayList<>();
    private OnRemoveListener removeListener;

    public void setOnRemoveListener(OnRemoveListener removeListener) {
        this.removeListener = removeListener;
    }

    public void submitList(List<String> uris) {
        imageUris.clear();
        if (uris != null) {
            imageUris.addAll(uris);
        }
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        if (position < 0 || position >= imageUris.size()) {
            return;
        }
        imageUris.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, imageUris.size() - position);
    }

    public List<String> getCurrentList() {
        return Collections.unmodifiableList(imageUris);
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_photo_thumbnail, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        holder.bind(imageUris.get(position));
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivPhoto;
        private final ImageButton btnRemove;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }

        void bind(String uriString) {
            Glide.with(ivPhoto.getContext())
                    .load(Uri.parse(uriString))
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.bg_image_placeholder)
                    .centerCrop()
                    .into(ivPhoto);
            btnRemove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                if (removeListener != null) {
                    removeListener.onRemove(position);
                }
            });
        }
    }

    public interface OnRemoveListener {
        void onRemove(int position);
    }
}
