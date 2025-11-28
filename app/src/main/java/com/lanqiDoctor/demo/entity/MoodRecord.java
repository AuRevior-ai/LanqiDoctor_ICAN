package com.lanqiDoctor.demo.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 心情记录实体
 */
@Entity(
        tableName = "mood_record",
        indices = {
                @Index(value = "recordDate"),
                @Index(value = {"favorite", "recordDate"})
        }
)
public class MoodRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** 记录日期（当天零点时间戳，毫秒） */
    @ColumnInfo(name = "recordDate")
    private long recordDate;

    /** 创建时间 */
    @ColumnInfo(name = "createdAt")
    private long createdAt;

    /** 更新时间 */
    @ColumnInfo(name = "updatedAt")
    private long updatedAt;

    /** 心情级别，使用 {@link com.lanqiDoctor.demo.model.MoodLevel#name()} 存储 */
    @NonNull
    @ColumnInfo(name = "moodLevel")
    private String moodLevel = "NEUTRAL";

    /** 能量值（1-5） */
    @ColumnInfo(name = "energyLevel")
    private int energyLevel = 3;

    /** 压力值（1-5） */
    @ColumnInfo(name = "stressLevel")
    private int stressLevel = 3;

    /** 活动标签（以逗号分隔的字符串） */
    @ColumnInfo(name = "activities")
    private String activities;

    /** 备注 */
    @ColumnInfo(name = "note")
    private String note;

    /** 是否收藏到忆迹 */
    @ColumnInfo(name = "favorite")
    private boolean favorite;

    /** 第一张图片兼容字段（保留旧版本数据） */
    @ColumnInfo(name = "imageUri")
    private String imageUri;

    /** 多图 Uri 列表，使用 JSON 字符串存储 */
    @ColumnInfo(name = "imageUris")
    private String imageUris;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(long recordDate) {
        this.recordDate = recordDate;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @NonNull
    public String getMoodLevel() {
        return moodLevel;
    }

    public void setMoodLevel(@NonNull String moodLevel) {
        this.moodLevel = moodLevel;
    }

    public int getEnergyLevel() {
        return energyLevel;
    }

    public void setEnergyLevel(int energyLevel) {
        this.energyLevel = energyLevel;
    }

    public int getStressLevel() {
        return stressLevel;
    }

    public void setStressLevel(int stressLevel) {
        this.stressLevel = stressLevel;
    }

    public String getActivities() {
        return activities;
    }

    public void setActivities(String activities) {
        this.activities = activities;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public String getImageUri() {
        List<String> list = getImageUriList();
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        if (imageUri == null) {
            setImageUriList(null);
        } else {
            setImageUriList(Collections.singletonList(imageUri));
        }
    }

    public List<String> getImageUriList() {
        if (imageUris == null || imageUris.trim().isEmpty()) {
            if (imageUri == null || imageUri.trim().isEmpty()) {
                return Collections.emptyList();
            }
            return new ArrayList<>(Collections.singletonList(imageUri));
        }
        try {
            JSONArray jsonArray = new JSONArray(imageUris);
            List<String> result = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                String value = jsonArray.optString(i);
                if (value != null && !value.isEmpty()) {
                    result.add(value);
                }
            }
            if (result.isEmpty() && imageUri != null && !imageUri.isEmpty()) {
                result.add(imageUri);
            }
            return result;
        } catch (JSONException e) {
            if (imageUri == null || imageUri.trim().isEmpty()) {
                return Collections.emptyList();
            }
            return new ArrayList<>(Collections.singletonList(imageUri));
        }
    }

    public void setImageUriList(List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            this.imageUris = null;
            this.imageUri = null;
            return;
        }
        List<String> filtered = new ArrayList<>();
        for (String uri : uris) {
            if (uri != null && !uri.trim().isEmpty()) {
                filtered.add(uri);
            }
        }
        if (filtered.isEmpty()) {
            this.imageUris = null;
            this.imageUri = null;
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray();
            for (String uri : filtered) {
                jsonArray.put(uri);
            }
            this.imageUris = jsonArray.toString();
        } catch (Exception e) {
            this.imageUris = null;
        }
        this.imageUri = filtered.get(0);
    }

    public String getImageUrisRaw() {
        return imageUris;
    }

    public void setImageUrisRaw(String imageUris) {
        this.imageUris = imageUris;
    }

    /**
     * 提供给 Room 默认命名规则的访问器，避免因自定义方法导致的警告。
     */
    public String getImageUris() {
        return imageUris;
    }

    /**
     * 提供给 Room 默认命名规则的访问器，保持与 getImageUris 对应。
     */
    public void setImageUris(String imageUris) {
        this.imageUris = imageUris;
    }

    /**
     * 初始化记录的时间戳。
     */
    public void markCreated() {
        long now = System.currentTimeMillis();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * 更新最后修改时间。
     */
    public void touch() {
        updatedAt = System.currentTimeMillis();
    }
}
