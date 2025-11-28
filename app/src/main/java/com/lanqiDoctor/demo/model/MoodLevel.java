package com.lanqiDoctor.demo.model;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

import com.lanqiDoctor.demo.R;

/**
 * 心情级别定义
 */
public enum MoodLevel {

    VERY_HAPPY(R.string.mood_level_very_happy, R.color.mood_very_happy, R.color.mood_very_happy_bg, "\uD83D\uDE04"),
    HAPPY(R.string.mood_level_happy, R.color.mood_happy, R.color.mood_happy_bg, "\uD83D\uDE42"),
    NEUTRAL(R.string.mood_level_neutral, R.color.mood_neutral, R.color.mood_neutral_bg, "\uD83D\uDE10"),
    SAD(R.string.mood_level_sad, R.color.mood_sad, R.color.mood_sad_bg, "\uD83D\uDE23"),
    VERY_SAD(R.string.mood_level_very_sad, R.color.mood_very_sad, R.color.mood_very_sad_bg, "\uD83D\uDE14");

    private final int labelRes;
    private final int colorRes;
    private final int backgroundColorRes;
    private final String emoji;

    MoodLevel(@StringRes int labelRes, @ColorRes int colorRes, @ColorRes int backgroundColorRes, String emoji) {
        this.labelRes = labelRes;
        this.colorRes = colorRes;
        this.backgroundColorRes = backgroundColorRes;
        this.emoji = emoji;
    }

    @StringRes
    public int getLabelRes() {
        return labelRes;
    }

    @ColorRes
    public int getColorRes() {
        return colorRes;
    }

    @ColorRes
    public int getBackgroundColorRes() {
        return backgroundColorRes;
    }

    public String getEmoji() {
        return emoji;
    }

    public static MoodLevel fromStorage(String value) {
        if (value == null) {
            return NEUTRAL;
        }
        for (MoodLevel moodLevel : values()) {
            if (moodLevel.name().equalsIgnoreCase(value)) {
                return moodLevel;
            }
        }
        return NEUTRAL;
    }
}
