package com.lanqiDoctor.demo.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.lanqiDoctor.demo.dao.MoodRecordDao;
import com.lanqiDoctor.demo.database.AppDatabase;
import com.lanqiDoctor.demo.entity.MoodRecord;
import com.lanqiDoctor.demo.model.MoodLevel;
import com.lanqiDoctor.demo.model.MoodStatistics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 心情记录管理器
 */
public class MoodRecordManager {

    private static final long DAY_MILLIS = TimeUnit.DAYS.toMillis(1);

    private static volatile MoodRecordManager instance;
    private final MoodRecordDao moodRecordDao;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final CopyOnWriteArrayList<DataChangedListener> listeners = new CopyOnWriteArrayList<>();

    private MoodRecordManager(Context context) {
        moodRecordDao = AppDatabase.getInstance(context).moodRecordDao();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static MoodRecordManager getInstance(Context context) {
        if (instance == null) {
            synchronized (MoodRecordManager.class) {
                if (instance == null) {
                    instance = new MoodRecordManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void registerListener(DataChangedListener listener) {
        if (listener != null) {
            listeners.addIfAbsent(listener);
        }
    }

    public void unregisterListener(DataChangedListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void loadRecordsForDay(long dayStart, LoadCallback<List<MoodRecord>> callback) {
        long normalizedStart = normalizeDay(dayStart);
        long end = normalizedStart + DAY_MILLIS;
        executor.execute(() -> {
            try {
                List<MoodRecord> result = moodRecordDao.getRecordsForDay(normalizedStart, end);
                postSuccess(callback, result);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void loadRecordsBetween(long start, long end, LoadCallback<List<MoodRecord>> callback) {
        long normalizedStart = normalizeDay(start);
        long normalizedEnd = normalizeDay(end) - 1;
        executor.execute(() -> {
            try {
                List<MoodRecord> result = moodRecordDao.getRecordsBetween(normalizedStart, normalizedEnd);
                postSuccess(callback, result);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void loadMemoryRecords(LoadCallback<List<MoodRecord>> callback) {
        executor.execute(() -> {
            try {
                List<MoodRecord> result = moodRecordDao.getMemoryRecords();
                postSuccess(callback, result);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void loadAllRecords(LoadCallback<List<MoodRecord>> callback) {
        executor.execute(() -> {
            try {
                List<MoodRecord> result = moodRecordDao.getAllRecords();
                postSuccess(callback, result);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void saveRecord(MoodRecord record, OperationCallback callback) {
        executor.execute(() -> {
            try {
                if (record.getId() == 0) {
                    record.markCreated();
                    if (record.getRecordDate() == 0) {
                        record.setRecordDate(normalizeDay(System.currentTimeMillis()));
                    } else {
                        record.setRecordDate(normalizeDay(record.getRecordDate()));
                    }
                    long id = moodRecordDao.insertRecord(record);
                    record.setId(id);
                } else {
                    record.touch();
                    record.setRecordDate(normalizeDay(record.getRecordDate()));
                    moodRecordDao.updateRecord(record);
                }
                notifyDataChanged();
                postSuccess(callback);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void deleteRecord(MoodRecord record, OperationCallback callback) {
        executor.execute(() -> {
            try {
                moodRecordDao.deleteRecord(record);
                notifyDataChanged();
                postSuccess(callback);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void toggleFavorite(MoodRecord record, boolean favorite, OperationCallback callback) {
        record.setFavorite(favorite);
        saveRecord(record, callback);
    }

    public void loadStatistics(StatisticsCallback callback) {
        executor.execute(() -> {
            try {
                List<MoodRecord> allRecords = moodRecordDao.getAllRecords();
                MoodStatistics statistics = buildStatistics(allRecords);
                postSuccess(callback, statistics);
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    private MoodStatistics buildStatistics(List<MoodRecord> allRecords) {
        MoodStatistics statistics = new MoodStatistics();
        statistics.setTotalRecords(allRecords != null ? allRecords.size() : 0);
        if (allRecords == null || allRecords.isEmpty()) {
            statistics.setFavoriteRecords(0);
            statistics.setMonthRecords(0);
            statistics.setAverageEnergy(0f);
            statistics.setAverageStress(0f);
            statistics.setCurrentStreak(0);
            statistics.setFrequentMood(null);
            return statistics;
        }

        EnumMap<MoodLevel, Integer> moodCounts = new EnumMap<>(MoodLevel.class);
        for (MoodLevel level : MoodLevel.values()) {
            moodCounts.put(level, 0);
        }

        long monthStart = getMonthStart(System.currentTimeMillis());
        int monthRecords = 0;
        for (MoodRecord record : allRecords) {
            MoodLevel moodLevel = MoodLevel.fromStorage(record.getMoodLevel());
            moodCounts.put(moodLevel, moodCounts.get(moodLevel) + 1);
            if (record.getRecordDate() >= monthStart) {
                monthRecords++;
            }
        }
        statistics.getMoodCounts().putAll(moodCounts);
        statistics.setMonthRecords(monthRecords);

        statistics.setFavoriteRecords(moodRecordDao.getFavoriteCount());

        Float avgEnergy = moodRecordDao.getAverageEnergy(0, Long.MAX_VALUE);
        Float avgStress = moodRecordDao.getAverageStress(0, Long.MAX_VALUE);
        statistics.setAverageEnergy(avgEnergy != null ? avgEnergy : 0f);
        statistics.setAverageStress(avgStress != null ? avgStress : 0f);

        statistics.setCurrentStreak(calculateCurrentStreak(moodRecordDao.getAllRecordDatesDesc()));
        statistics.setFrequentMood(findFrequentMood(moodCounts));

        return statistics;
    }

    private MoodLevel findFrequentMood(Map<MoodLevel, Integer> moodCounts) {
        MoodLevel frequentMood = null;
        int maxCount = 0;
        for (Map.Entry<MoodLevel, Integer> entry : moodCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                frequentMood = entry.getKey();
            }
        }
        return frequentMood;
    }

    private int calculateCurrentStreak(List<Long> recordDatesDesc) {
        if (recordDatesDesc == null || recordDatesDesc.isEmpty()) {
            return 0;
        }
        List<Long> distinct = new ArrayList<>();
        Long previous = null;
        for (Long date : recordDatesDesc) {
            if (date == null) {
                continue;
            }
            if (previous == null || !previous.equals(date)) {
                distinct.add(date);
                previous = date;
            }
        }
        if (distinct.isEmpty()) {
            return 0;
        }
        int streak = 1;
        long lastDate = distinct.get(0);
        for (int i = 1; i < distinct.size(); i++) {
            long expectedNext = lastDate - DAY_MILLIS;
            long current = distinct.get(i);
            if (current == expectedNext) {
                streak++;
                lastDate = current;
            } else {
                break;
            }
        }
        return streak;
    }

    private long normalizeDay(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getMonthStart(long referenceTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(referenceTime);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private <T> void postSuccess(@Nullable LoadCallback<T> callback, T data) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onSuccess(data));
    }

    private void postSuccess(@Nullable OperationCallback callback) {
        if (callback == null) {
            return;
        }
        mainHandler.post(callback::onSuccess);
    }

    private void postSuccess(@Nullable StatisticsCallback callback, MoodStatistics statistics) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onSuccess(statistics));
    }

    private void postError(@Nullable LoadCallback<?> callback, Exception e) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onError(e.getMessage()));
    }

    private void postError(@Nullable OperationCallback callback, Exception e) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onError(e.getMessage()));
    }

    private void postError(@Nullable StatisticsCallback callback, Exception e) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onError(e.getMessage()));
    }

    private void notifyDataChanged() {
        if (listeners.isEmpty()) {
            return;
        }
        mainHandler.post(() -> {
            for (DataChangedListener listener : listeners) {
                listener.onMoodDataChanged();
            }
        });
    }

    public interface LoadCallback<T> {
        void onSuccess(T data);

        void onError(String errorMessage);
    }

    public interface OperationCallback {
        void onSuccess();

        void onError(String errorMessage);
    }

    public interface StatisticsCallback {
        void onSuccess(MoodStatistics statistics);

        void onError(String errorMessage);
    }

    public interface DataChangedListener {
        void onMoodDataChanged();
    }
}
