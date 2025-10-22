package com.lanqiDoctor.demo.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.AlarmClock;

import com.lanqiDoctor.demo.database.dao.MedicationRecordDao;
import com.lanqiDoctor.demo.database.entity.MedicationRecord;
import com.lanqiDoctor.demo.ui.activity.ClockActivity;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 药物闹钟管理器
 * 负责管理所有药物提醒闹钟的设置和清除
 *
 * @author rrrrrzy
 * @version 1.0
 */
public class MedicationAlarmManager {

    private Context context;
    private MedicationRecordDao medicationDao;
    private SharedPreferences timePrefs;

    // SharedPreferences相关常量
    private static final String PREFS_NAME = "medication_times";
    private static final String KEY_ONCE_TIME = "once_time";
    private static final String KEY_TWICE_MORNING = "twice_morning";
    private static final String KEY_TWICE_EVENING = "twice_evening";
    private static final String KEY_THREE_MORNING = "three_morning";
    private static final String KEY_THREE_NOON = "three_noon";
    private static final String KEY_THREE_EVENING = "three_evening";

    // 用户设置相关
    private static final String KEY_ENABLE_SYSTEM_ALARM = "enable_system_alarm";

    public MedicationAlarmManager(Context context) {
        this.context = context.getApplicationContext(); // 使用Application Context避免内存泄漏
        this.medicationDao = new MedicationRecordDao(this.context);
        this.timePrefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 更新所有用药闹钟（包括应用内闹钟和系统闹钟）
     */
    public void updateAllMedicationAlarms() {
        updateAllMedicationAlarms(false); // 默认不自动设置系统闹钟
    }

    /**
     * 更新所有用药闹钟
     * @param includeSystemAlarm 是否同时设置系统闹钟
     */
    public void updateAllMedicationAlarms(boolean includeSystemAlarm) {
        // 先清除所有现有的应用内闹钟
        clearAllCustomAlarms();

        // 获取所有活跃的药物
        List<MedicationRecord> activeMedications = getActiveMedications();

        android.util.Log.i("MedicationAlarmManager", "找到活跃药物数量: " + activeMedications.size());

        if (activeMedications.isEmpty()) {
            android.util.Log.i("MedicationAlarmManager", "当前没有需要提醒的药物");
            return;
        }

        // 创建时间点映射 - 避免重复时间
        Map<String, List<String>> timeToMedicationsMap = new HashMap<>();

        for (MedicationRecord medication : activeMedications) {
            String medName = medication.getMedicationName();
            String frequency = medication.getFrequency();

            android.util.Log.i("MedicationAlarmManager", "处理药物: " + medName + ", 频率: " + frequency + ", 状态: " + medication.getStatus());

            if (frequency == null) {
                android.util.Log.w("MedicationAlarmManager", "药物 " + medName + " 的频率为空，跳过");
                continue;
            }

            // 根据频率确定需要的时间点
            List<String> timeKeys = getTimeKeysForFrequency(frequency);
            for (String timeKey : timeKeys) {
                String timeString = timePrefs.getString(timeKey, null);
                if (timeString != null && !"未设置".equals(timeString)) {
                    // 将药物添加到对应时间点
                    timeToMedicationsMap.computeIfAbsent(timeString, k -> new ArrayList<>()).add(medName);
                }
            }
        }

        // 为每个唯一的时间点设置闹钟
        int requestCode = 1;
        for (Map.Entry<String, List<String>> entry : timeToMedicationsMap.entrySet()) {
            String timeString = entry.getKey();
            List<String> medications = entry.getValue();

            String message = "蓝岐医童提醒您，应该服用：" + String.join("+", medications);
            android.util.Log.i("MedicationAlarmManager", "设置闹钟 " + timeString + ": " + message);

            setMedicationAlarmByTime(timeString, requestCode++, message);

            if (includeSystemAlarm) {
                setSystemAlarmByTime(timeString, "用药提醒 - " + medications.size() + "种药物");
            }
        }

        android.util.Log.i("MedicationAlarmManager", "闹钟更新完成 - 应用内闹钟: 是, 系统闹钟: " + (includeSystemAlarm ? "是" : "否"));
        android.util.Log.i("MedicationAlarmManager", "设置了 " + timeToMedicationsMap.size() + " 个不同时间点的闹钟");
    }

    /**
     * 根据频率获取对应的时间键
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
            default:
                android.util.Log.w("MedicationAlarmManager", "未识别的频率: " + frequency);
                break;
        }

        return timeKeys;
    }

    /**
     * 根据时间字符串设置应用内闹钟
     */
    private void setMedicationAlarmByTime(String timeString, int requestCode, String message) {
        try {
            String[] parts = timeString.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            // 设置闹钟时间
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // 如果设置的时间已过，则设置为明天
            Calendar now = Calendar.getInstance();
            if (calendar.before(now)) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                android.util.Log.e("MedicationAlarmManager", "获取AlarmManager失败");
                return;
            }

            // 创建闹钟意图，携带消息和时间信息
            Intent alarmIntent = new Intent("com.lanqiDoctor.demo.ALARM_ACTION");
            alarmIntent.setClass(context, ClockActivity.AlarmReceiver.class);
            alarmIntent.putExtra("message", message);
            alarmIntent.putExtra("time", timeString);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    alarmIntent,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                            PendingIntent.FLAG_UPDATE_CURRENT
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }

            android.util.Log.i("MedicationAlarmManager", "设置应用内闹钟成功: " + timeString + " (请求码: " + requestCode + ") - " + message);

        } catch (Exception e) {
            android.util.Log.e("MedicationAlarmManager", "设置应用内闹钟失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据时间字符串设置系统闹钟
     */
    private void setSystemAlarmByTime(String timeString, String alarmLabel) {
        try {
            String[] parts = timeString.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            // 设置重复的日期（每天）
            ArrayList<Integer> dates = new ArrayList<>();
            dates.add(Calendar.MONDAY);
            dates.add(Calendar.TUESDAY);
            dates.add(Calendar.WEDNESDAY);
            dates.add(Calendar.THURSDAY);
            dates.add(Calendar.FRIDAY);
            dates.add(Calendar.SATURDAY);
            dates.add(Calendar.SUNDAY);

            // 创建设置闹钟的Intent
            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
            intent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, "蓝岐医童用药提醒 - " + alarmLabel + " (" + timeString + ")");
            intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
            intent.putIntegerArrayListExtra(AlarmClock.EXTRA_DAYS, dates);
            intent.putExtra(AlarmClock.EXTRA_VIBRATE, true);

            // 添加必要的标志
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // 检查是否有应用可以处理此Intent
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                android.util.Log.i("MedicationAlarmManager", "系统闹钟设置成功: " + timeString + " - " + alarmLabel);
            } else {
                android.util.Log.w("MedicationAlarmManager", "没有找到可以设置闹钟的应用");
            }

        } catch (Exception e) {
            android.util.Log.e("MedicationAlarmManager", "设置系统闹钟失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有活跃的药物 - 改进的查询方法
     */
    private List<MedicationRecord> getActiveMedications() {
        List<MedicationRecord> activeMedications = new ArrayList<>();
        
        try {
            // 获取当前用户ID
            String userId = com.lanqiDoctor.demo.manager.UserStateManager.getInstance(context).getUserId();
            
            if (userId != null && !userId.isEmpty()) {
                // 如果有用户ID，按用户筛选
                activeMedications = medicationDao.findActiveMedications(userId);
                android.util.Log.i("MedicationAlarmManager", "通过用户ID查询找到 " + activeMedications.size() + " 个活跃药物");
            } else {
                // 如果没有用户ID，查询所有状态为1的药物
                List<MedicationRecord> statusBasedResults = medicationDao.findByStatus(1);
                if (statusBasedResults != null && !statusBasedResults.isEmpty()) {
                    activeMedications.addAll(statusBasedResults);
                    android.util.Log.i("MedicationAlarmManager", "通过状态查询找到 " + activeMedications.size() + " 个活跃药物");
                } else {
                    // 如果 findByStatus 没有结果，尝试获取所有药物并过滤
                    List<MedicationRecord> allMedications = medicationDao.findAll();
                    android.util.Log.i("MedicationAlarmManager", "总共有 " + (allMedications != null ? allMedications.size() : 0) + " 个药物记录");
                    
                    if (allMedications != null) {
                        for (MedicationRecord medication : allMedications) {
                            Integer status = medication.getStatus();
                            android.util.Log.d("MedicationAlarmManager", "药物: " + medication.getMedicationName() + ", 状态: " + status);
                            
                            // 如果状态为null或者为1，都认为是活跃的
                            if (status == null || status == 1) {
                                activeMedications.add(medication);
                            }
                        }
                        android.util.Log.i("MedicationAlarmManager", "通过全量过滤找到 " + activeMedications.size() + " 个活跃药物");
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MedicationAlarmManager", "获取活跃药物时出错: " + e.getMessage(), e);
        }
        
        return activeMedications;
    }
    /**
     * 检查是否需要自动设置系统闹钟的用户偏好
     */
    public boolean shouldAutoSetSystemAlarm() {
        return timePrefs.getBoolean(KEY_ENABLE_SYSTEM_ALARM, false);
    }

    /**
     * 设置自动系统闹钟的用户偏好
     */
    public void setAutoSystemAlarm(boolean enable) {
        timePrefs.edit().putBoolean(KEY_ENABLE_SYSTEM_ALARM, enable).apply();
    }

    /**
     * 设置系统闹钟（用户手动触发）
     */
    public void setupSystemAlarmsManually() {
        // 获取所有活跃的药物
        List<MedicationRecord> activeMedications = getActiveMedications();

        if (activeMedications.isEmpty()) {
            android.util.Log.i("MedicationAlarmManager", "当前没有需要提醒的药物");
            return;
        }

        // 创建时间点映射
        Map<String, List<String>> timeToMedicationsMap = new HashMap<>();

        for (MedicationRecord medication : activeMedications) {
            String medName = medication.getMedicationName();
            String frequency = medication.getFrequency();

            if (frequency == null) continue;

            List<String> timeKeys = getTimeKeysForFrequency(frequency);
            for (String timeKey : timeKeys) {
                String timeString = timePrefs.getString(timeKey, null);
                if (timeString != null && !"未设置".equals(timeString)) {
                    timeToMedicationsMap.computeIfAbsent(timeString, k -> new ArrayList<>()).add(medName);
                }
            }
        }

        // 为每个时间点设置系统闹钟
        for (Map.Entry<String, List<String>> entry : timeToMedicationsMap.entrySet()) {
            String timeString = entry.getKey();
            List<String> medications = entry.getValue();
            setSystemAlarmByTime(timeString, medications.size() + "种药物");
        }
    }

    /**
     * 删除系统闹钟 - 显示具体的闹钟信息
     */
    public void deleteSystemAlarmsWithDetails(Context activityContext) {
        // 获取当前设置的时间点
        List<String> setTimes = new ArrayList<>();
        Map<String, String> timeToLabelMap = new HashMap<>();

        // 检查各个时间点是否设置
        checkAndAddTime(KEY_ONCE_TIME, "每日一次", setTimes, timeToLabelMap);
        checkAndAddTime(KEY_TWICE_MORNING, "每日两次-上午", setTimes, timeToLabelMap);
        checkAndAddTime(KEY_TWICE_EVENING, "每日两次-下午", setTimes, timeToLabelMap);
        checkAndAddTime(KEY_THREE_MORNING, "每日三次-早上", setTimes, timeToLabelMap);
        checkAndAddTime(KEY_THREE_NOON, "每日三次-中午", setTimes, timeToLabelMap);
        checkAndAddTime(KEY_THREE_EVENING, "每日三次-晚上", setTimes, timeToLabelMap);

        if (setTimes.isEmpty()) {
            android.util.Log.i("MedicationAlarmManager", "没有设置的闹钟时间");
            return;
        }

        // 创建选择对话框
        String[] options = new String[setTimes.size() + 2];
        for (int i = 0; i < setTimes.size(); i++) {
            String time = setTimes.get(i);
            String label = timeToLabelMap.get(time);
            options[i] = time + " (" + label + ")";
        }
        options[setTimes.size()] = "删除所有蓝岐医童闹钟";
        options[setTimes.size() + 1] = "打开系统闹钟应用";

        new androidx.appcompat.app.AlertDialog.Builder(activityContext)
                .setTitle("选择要删除的闹钟")
                .setItems(options, (dialog, which) -> {
                    if (which < setTimes.size()) {
                        // 删除指定时间的闹钟
                        String selectedTime = setTimes.get(which);
                        deleteSystemAlarmByTime(selectedTime);
                    } else if (which == setTimes.size()) {
                        // 删除所有蓝岐医童闹钟
                        deleteAllSystemAlarms();
                    } else {
                        // 打开系统闹钟应用
                        openSystemAlarmApp();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 检查并添加时间到列表
     */
    private void checkAndAddTime(String timeKey, String label, List<String> setTimes, Map<String, String> timeToLabelMap) {
        String time = timePrefs.getString(timeKey, null);
        if (time != null && !"未设置".equals(time)) {
            setTimes.add(time);
            timeToLabelMap.put(time, label);
        }
    }

    /**
     * 根据时间删除系统闹钟
     */
    private void deleteSystemAlarmByTime(String timeString) {
        try {
            String[] parts = timeString.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            Intent intent = new Intent(AlarmClock.ACTION_DISMISS_ALARM)
                    .putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_TIME)
                    .putExtra(AlarmClock.EXTRA_HOUR, hour)
                    .putExtra(AlarmClock.EXTRA_MINUTES, minute);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                android.util.Log.i("MedicationAlarmManager", "正在删除 " + timeString + " 的系统闹钟");
            } else {
                android.util.Log.w("MedicationAlarmManager", "无法删除指定时间的系统闹钟");
                openSystemAlarmApp();
            }

        } catch (Exception e) {
            android.util.Log.e("MedicationAlarmManager", "删除指定时间的系统闹钟失败: " + e.getMessage());
            openSystemAlarmApp();
        }
    }

    /**
     * 删除所有系统闹钟
     */
    private void deleteAllSystemAlarms() {
        try {
            // 方法1：通过标签删除闹钟
            Intent intent = new Intent(AlarmClock.ACTION_DISMISS_ALARM)
                    .putExtra(AlarmClock.EXTRA_ALARM_SEARCH_MODE, AlarmClock.ALARM_SEARCH_MODE_LABEL)
                    .putExtra(AlarmClock.EXTRA_MESSAGE, "蓝岐医童用药提醒");

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                android.util.Log.i("MedicationAlarmManager", "正在删除所有蓝岐医童相关的系统闹钟");
            } else {
                android.util.Log.w("MedicationAlarmManager", "无法通过标签删除系统闹钟");
                openSystemAlarmApp();
            }

        } catch (Exception e) {
            android.util.Log.e("MedicationAlarmManager", "删除所有系统闹钟失败: " + e.getMessage());
            openSystemAlarmApp();
        }
    }

    /**
     * 打开系统闹钟应用
     */
    private void openSystemAlarmApp() {
        try {
            Intent intent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                android.util.Log.i("MedicationAlarmManager", "已打开系统闹钟应用");
            }
        } catch (Exception e) {
            android.util.Log.e("MedicationAlarmManager", "打开系统闹钟应用失败: " + e.getMessage());
        }
    }

    /**
     * 设置应用内闹钟（旧方法，保持兼容性）
     */
    private void setMedicationAlarm(String timeKey, int requestCode, String message) {
        String timeString = timePrefs.getString(timeKey, null);
        if (timeString == null || "未设置".equals(timeString)) {
            android.util.Log.w("MedicationAlarmManager", "时间未设置，跳过闹钟: " + timeKey);
            return;
        }

        setMedicationAlarmByTime(timeString, requestCode, message);
    }

    /**
     * 设置系统闹钟（旧方法，保持兼容性）
     */
    private void setSystemAlarm(String timeKey, String alarmLabel) {
        String timeString = timePrefs.getString(timeKey, null);
        if (timeString == null || "未设置".equals(timeString)) {
            android.util.Log.w("MedicationAlarmManager", "时间未设置，跳过系统闹钟: " + timeKey);
            return;
        }

        setSystemAlarmByTime(timeString, alarmLabel);
    }

    /**
     * 清除所有应用内闹钟
     */
    public void clearAllCustomAlarms() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            android.util.Log.e("MedicationAlarmManager", "获取AlarmManager失败，无法清除闹钟");
            return;
        }

        // 清除所有可能的闹钟请求码（1-20，预留更多空间）
        for (int i = 1; i <= 20; i++) {
            try {
                Intent alarmIntent = new Intent("com.lanqiDoctor.demo.ALARM_ACTION");
                alarmIntent.setClass(context, ClockActivity.AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        i,
                        alarmIntent,
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                                PendingIntent.FLAG_UPDATE_CURRENT
                );

                alarmManager.cancel(pendingIntent);
                android.util.Log.i("MedicationAlarmManager", "取消闹钟: " + i);

            } catch (Exception e) {
                android.util.Log.e("MedicationAlarmManager", "取消闹钟失败: " + e.getMessage());
            }
        }
    }

    /**
     * 检查时间设置是否完整
     */
    public static boolean isTimeSettingsComplete(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 检查是否至少设置了一种频率的时间
        String onceTime = prefs.getString(KEY_ONCE_TIME, null);
        String twiceMorning = prefs.getString(KEY_TWICE_MORNING, null);
        String twiceEvening = prefs.getString(KEY_TWICE_EVENING, null);
        String threeMorning = prefs.getString(KEY_THREE_MORNING, null);
        String threeNoon = prefs.getString(KEY_THREE_NOON, null);
        String threeEvening = prefs.getString(KEY_THREE_EVENING, null);

        boolean onceComplete = onceTime != null && !onceTime.equals("未设置");
        boolean twiceComplete = twiceMorning != null && !twiceMorning.equals("未设置") &&
                twiceEvening != null && !twiceEvening.equals("未设置");
        boolean threeComplete = threeMorning != null && !threeMorning.equals("未设置") &&
                threeNoon != null && !threeNoon.equals("未设置") &&
                threeEvening != null && !threeEvening.equals("未设置");

        return onceComplete || twiceComplete || threeComplete;
    }
}