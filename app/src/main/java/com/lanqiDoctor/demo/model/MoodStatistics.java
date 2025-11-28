package com.lanqiDoctor.demo.model;

import java.util.EnumMap;
import java.util.Map;

/**
 * 心情统计数据
 */
public class MoodStatistics {

    private final EnumMap<MoodLevel, Integer> moodCounts = new EnumMap<>(MoodLevel.class);
    private int totalRecords;
    private int monthRecords;
    private int favoriteRecords;
    private int currentStreak;
    private float averageEnergy;
    private float averageStress;
    private MoodLevel frequentMood;

    public MoodStatistics() {
        for (MoodLevel level : MoodLevel.values()) {
            moodCounts.put(level, 0);
        }
    }

    public Map<MoodLevel, Integer> getMoodCounts() {
        return moodCounts;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getMonthRecords() {
        return monthRecords;
    }

    public void setMonthRecords(int monthRecords) {
        this.monthRecords = monthRecords;
    }

    public int getFavoriteRecords() {
        return favoriteRecords;
    }

    public void setFavoriteRecords(int favoriteRecords) {
        this.favoriteRecords = favoriteRecords;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public float getAverageEnergy() {
        return averageEnergy;
    }

    public void setAverageEnergy(float averageEnergy) {
        this.averageEnergy = averageEnergy;
    }

    public float getAverageStress() {
        return averageStress;
    }

    public void setAverageStress(float averageStress) {
        this.averageStress = averageStress;
    }

    public MoodLevel getFrequentMood() {
        return frequentMood;
    }

    public void setFrequentMood(MoodLevel frequentMood) {
        this.frequentMood = frequentMood;
    }
}
