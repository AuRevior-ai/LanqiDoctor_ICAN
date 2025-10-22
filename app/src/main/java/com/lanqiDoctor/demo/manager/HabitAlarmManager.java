package com.lanqiDoctor.demo.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.lanqiDoctor.demo.database.entity.Habit;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;

/**
 * 习惯提醒闹钟管理器
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class HabitAlarmManager {
    private Context context;
    private AlarmManager alarmManager;

    public HabitAlarmManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * 设置习惯提醒
     */
    public void setHabitReminders(Habit habit) {
        if (habit == null || habit.getReminderTimes() == null) {
            return;
        }

        try {
            JSONArray reminderTimes = new JSONArray(habit.getReminderTimes());
            for (int i = 0; i < reminderTimes.length(); i++) {
                String timeStr = reminderTimes.getString(i);
                setReminderAlarm(habit, timeStr, i);
            }
        } catch (JSONException e) {
            android.util.Log.e("HabitAlarmManager", "解析提醒时间失败", e);
        }
    }

    /**
     * 设置单个提醒闹钟
     */
    private void setReminderAlarm(Habit habit, String timeStr, int index) {
        try {
            String[] timeParts = timeStr.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            // 如果时间已过，设置为明天
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(context, HabitReminderReceiver.class);
            intent.putExtra("habit_id", habit.getId());
            intent.putExtra("habit_name", habit.getHabitName());
            intent.putExtra("message", "该" + habit.getHabitName() + "啦！");

            int requestCode = (int) (habit.getId() * 1000 + index); // 确保唯一性
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 
                requestCode, 
                intent, 
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            // 设置重复闹钟
            long intervalMillis = getIntervalMillis(habit);
            if (intervalMillis > 0) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    intervalMillis,
                    pendingIntent
                );
            } else {
                // 每日重复
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                );
            }

            android.util.Log.d("HabitAlarmManager", "设置习惯提醒: " + habit.getHabitName() + " at " + timeStr);

        } catch (Exception e) {
            android.util.Log.e("HabitAlarmManager", "设置习惯提醒失败", e);
        }
    }

    /**
     * 获取重复间隔（毫秒）
     */
    private long getIntervalMillis(Habit habit) {
        if (habit.getFrequency() == null) return 0;

        switch (habit.getFrequency()) {
            case "HOURLY":
                int hours = habit.getFrequencyValue() != null ? habit.getFrequencyValue() : 1;
                return hours * 60 * 60 * 1000L; // 小时转毫秒
            case "DAILY":
                return AlarmManager.INTERVAL_DAY;
            case "WEEKLY":
                return AlarmManager.INTERVAL_DAY * 7;
            default:
                return 0;
        }
    }

    /**
     * 取消习惯提醒
     */
    public void cancelHabitReminders(Habit habit) {
        if (habit == null || habit.getReminderTimes() == null) {
            return;
        }

        try {
            JSONArray reminderTimes = new JSONArray(habit.getReminderTimes());
            for (int i = 0; i < reminderTimes.length(); i++) {
                cancelReminderAlarm(habit, i);
            }
        } catch (JSONException e) {
            android.util.Log.e("HabitAlarmManager", "取消习惯提醒失败", e);
        }
    }

    /**
     * 取消单个提醒闹钟
     */
    private void cancelReminderAlarm(Habit habit, int index) {
        Intent intent = new Intent(context, HabitReminderReceiver.class);
        int requestCode = (int) (habit.getId() * 1000 + index);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 
            requestCode, 
            intent, 
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        alarmManager.cancel(pendingIntent);
        android.util.Log.d("HabitAlarmManager", "取消习惯提醒: " + habit.getHabitName());
    }

    /**
     * 更新习惯提醒
     */
    public void updateHabitReminders(Habit habit) {
        // 先取消旧的提醒
        cancelHabitReminders(habit);
        
        // 如果习惯处于激活状态且启用了提醒，则设置新的提醒
        if (habit.getIsActive() && habit.getEnableNotification()) {
            setHabitReminders(habit);
        }
    }

    /**
     * 习惯提醒广播接收器
     */
    public static class HabitReminderReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Long habitId = intent.getLongExtra("habit_id", -1);
            String habitName = intent.getStringExtra("habit_name");
            String message = intent.getStringExtra("message");

            android.util.Log.d("HabitReminderReceiver", "收到习惯提醒: " + habitName);

            // 创建通知
            createHabitNotification(context, habitName, message, habitId);
        }

        private void createHabitNotification(Context context, String habitName, String message, Long habitId) {
            // 这里可以参考ClockActivity中的通知创建逻辑
            // 创建习惯提醒通知
            android.util.Log.i("HabitReminderReceiver", "创建习惯提醒通知: " + habitName);
            
            // TODO: 实现通知创建逻辑，类似于ClockActivity.AlarmReceiver中的实现
        }
    }
}
