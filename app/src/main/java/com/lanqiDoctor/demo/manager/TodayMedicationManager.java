package com.lanqiDoctor.demo.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.lanqiDoctor.demo.database.dao.MedicationIntakeRecordDao;
import com.lanqiDoctor.demo.database.dao.MedicationRecordDao;
import com.lanqiDoctor.demo.database.entity.MedicationIntakeRecord;
import com.lanqiDoctor.demo.database.entity.MedicationRecord;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 今日服药管理器
 * 负责在应用启动时初始化今日服药数据
 *
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class TodayMedicationManager {

    private static final String TAG = "TodayMedicationManager";
    private static final String PREFS_NAME = "medication_times";
    private static final String PREFS_LAST_INIT = "today_medication_init";
    private static final String KEY_LAST_INIT_DATE = "last_init_date";

    // 时间设置的键值
    private static final String KEY_ONCE_TIME = "once_time";
    private static final String KEY_TWICE_MORNING = "twice_morning";
    private static final String KEY_TWICE_EVENING = "twice_evening";
    private static final String KEY_THREE_MORNING = "three_morning";
    private static final String KEY_THREE_NOON = "three_noon";
    private static final String KEY_THREE_EVENING = "three_evening";

    private static TodayMedicationManager instance;
    private Context context;
    private MedicationRecordDao medicationDao;
    private MedicationIntakeRecordDao intakeDao;
    private SharedPreferences timePrefs;
    private SharedPreferences initPrefs;

    private String userId; // 新增

    private TodayMedicationManager(Context context) {
        this.context = context.getApplicationContext();
        this.medicationDao = new MedicationRecordDao(this.context);
        this.intakeDao = new MedicationIntakeRecordDao(this.context);
        this.timePrefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.initPrefs = this.context.getSharedPreferences(PREFS_LAST_INIT, Context.MODE_PRIVATE);

        // 修复：每次都重新获取userId，不要在构造函数中固定
        // UserStateManager userStateManager = UserStateManager.getInstance(this.context);
        // this.userId = userStateManager.getUserId();
    }

    public static synchronized TodayMedicationManager getInstance(Context context) {
        if (instance == null) {
            instance = new TodayMedicationManager(context);
        }
        return instance;
    }

    /**
     * 初始化今日服药数据
     */
    public void initTodayMedicationData() {
        // 修复：在方法调用时获取当前userId
        String currentUserId = UserStateManager.getInstance(context).getUserId();
        Log.d(TAG, "开始初始化今日服药数据，currentUserId: " + currentUserId);

        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.w(TAG, "用户ID为空，跳过初始化");
            return;
        }

        // 检查今天是否已经初始化过（针对当前用户）
        String today_init = getTodayString();
        String userKeyPrefix = "user_" + currentUserId + "_";
        String lastInitKey = userKeyPrefix + KEY_LAST_INIT_DATE;
        String lastInitDate = initPrefs.getString(lastInitKey, "");

        if (today_init.equals(lastInitDate)) {
            Log.d(TAG, "用户 " + currentUserId + " 今日服药数据已初始化，跳过");
            return;
        }

        try {
            // 获取当前用户的所有活跃药物
            List<MedicationRecord> activeMedications = medicationDao.findActiveMedications(currentUserId);
            Log.d(TAG, "找到用户 " + currentUserId + " 的活跃药物数量: " + activeMedications.size());

            // 获取今日的开始时间
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            long todayStart = today.getTimeInMillis();

            int totalCreated = 0;
            int totalSkipped = 0;

            // 为每个药物创建今日的服药记录
            for (MedicationRecord medication : activeMedications) {
                String frequency = medication.getFrequency();
                Log.d(TAG, "处理药物: " + medication.getMedicationName() + ", 频率: " + frequency);

                if (frequency == null) {
                    Log.w(TAG, "药物频率为空: " + medication.getMedicationName());
                    continue;
                }

                List<String> timeKeys = getTimeKeysForFrequency(frequency);

                for (String timeKey : timeKeys) {
                    try {
                        String timeString = timePrefs.getString(timeKey, null);

                        if (timeString == null || "未设置".equals(timeString)) {
                            Log.w(TAG, "时间未设置，跳过: " + timeKey);
                            continue;
                        }

                        // 计算计划服药时间
                        long plannedTime = calculatePlannedTime(todayStart, timeString);

                        // 检查是否已有记录
                        if (!intakeDao.existsTodayRecordByName(currentUserId, medication.getMedicationName(), plannedTime)) {
                            // 创建默认的"未服用"记录
                            MedicationIntakeRecord intakeRecord = new MedicationIntakeRecord();
                            intakeRecord.setMedicationId(medication.getId());
                            intakeRecord.setMedicationName(medication.getMedicationName());
                            intakeRecord.setPlannedTime(plannedTime);
                            intakeRecord.setStatus(0); // 默认未服用
                            intakeRecord.setActualDosage(medication.getDosage());
                            intakeRecord.setUserId(currentUserId); // 重要：设置正确的userId

                            long id = intakeDao.insertOrUpdateByNameAndTime(intakeRecord);

                            if (id > 0) {
                                totalCreated++;
                                Log.d(TAG, "为用户 " + currentUserId + " 创建默认未服用记录: " + medication.getMedicationName() +
                                        " 时间: " + timeString + " ID: " + id);
                            } else {
                                Log.e(TAG, "创建记录失败: " + medication.getMedicationName());
                            }
                        } else {
                            totalSkipped++;
                            Log.d(TAG, "记录已存在，跳过: " + medication.getMedicationName() + " 时间: " + timeString);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "处理时间键时发生异常: " + timeKey, e);
                    }
                }
            }

            // 记录今日已初始化（针对当前用户）
            initPrefs.edit().putString(lastInitKey, getTodayString()).apply();

            Log.d(TAG, "用户 " + currentUserId + " 今日服药数据初始化完成 - 创建: " + totalCreated + ", 跳过: " + totalSkipped);

        } catch (Exception e) {
            Log.e(TAG, "初始化今日服药数据时发生异常", e);
        }
    }

    /**
     * 强制重新初始化今日服药数据（用于测试或特殊情况）
     */
    public void forceReinitTodayMedicationData() {
        Log.d(TAG, "强制重新初始化今日服药数据");
        
        // 获取当前用户ID
        String currentUserId = UserStateManager.getInstance(context).getUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.w(TAG, "用户ID为空，无法强制重新初始化");
            return;
        }

        // 清除当前用户的初始化标记
        String userKeyPrefix = "user_" + currentUserId + "_";
        String lastInitKey = userKeyPrefix + KEY_LAST_INIT_DATE;
        initPrefs.edit().remove(lastInitKey).apply();

        // 重新初始化
        initTodayMedicationData();
    }

    /**
     * 获取今日字符串（格式：yyyy-MM-dd）
     */
    private String getTodayString() {
        Calendar calendar = Calendar.getInstance();
        return String.format("%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * 根据频率获取时间键
     */
    private List<String> getTimeKeysForFrequency(String frequency) {
        List<String> timeKeys = new ArrayList<>();

        switch (frequency) {
            case "每日一次":
            case "每日1次":
                timeKeys.add(KEY_ONCE_TIME);
                break;
            case "每日两次":
            case "每日2次":
                timeKeys.add(KEY_TWICE_MORNING);
                timeKeys.add(KEY_TWICE_EVENING);
                break;
            case "每日三次":
            case "每日3次":
                timeKeys.add(KEY_THREE_MORNING);
                timeKeys.add(KEY_THREE_NOON);
                timeKeys.add(KEY_THREE_EVENING);
                break;
        }

        return timeKeys;
    }

    /**
     * 计算计划服药时间
     */
    private long calculatePlannedTime(long todayStart, String timeString) {
        try {
            String[] parts = timeString.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(todayStart);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            return calendar.getTimeInMillis();
        } catch (Exception e) {
            Log.e(TAG, "解析时间字符串失败: " + timeString, e);
            return todayStart;
        }
    }

    /**
     * 获取今日服药统计信息
     */
    public TodayMedicationStats getTodayStats() {
        try {
            // 获取当前用户ID
            String currentUserId = UserStateManager.getInstance(context).getUserId();
            if (currentUserId == null || currentUserId.isEmpty()) {
                return new TodayMedicationStats(0, 0);
            }

            List<MedicationIntakeRecord> todayRecords = intakeDao.findTodayRecords(currentUserId);

            int totalCount = todayRecords.size();
            int takenCount = 0;

            for (MedicationIntakeRecord record : todayRecords) {
                if (record.getStatus() != null && record.getStatus() == 1) {
                    takenCount++;
                }
            }

            return new TodayMedicationStats(totalCount, takenCount);

        } catch (Exception e) {
            Log.e(TAG, "获取今日服药统计失败", e);
            return new TodayMedicationStats(0, 0);
        }
    }

    /**
     * 今日服药统计信息
     */
    public static class TodayMedicationStats {
        public final int totalCount;
        public final int takenCount;
        public final float completionRate;
        public final boolean allTaken;

        public TodayMedicationStats(int totalCount, int takenCount) {
            this.totalCount = totalCount;
            this.takenCount = takenCount;
            this.completionRate = totalCount > 0 ? (float) takenCount / totalCount : 0f;
            this.allTaken = totalCount > 0 && takenCount >= totalCount;
        }
    }
}