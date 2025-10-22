package com.lanqiDoctor.demo.ui.activity;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.PendingIntent;
import android.os.Build;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import com.hjq.base.BaseActivity;
import com.hjq.base.BaseDialog;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.aop.Log;
import com.lanqiDoctor.demo.aop.Permissions;
import com.lanqiDoctor.demo.aop.SingleClick;
import com.lanqiDoctor.demo.app.AppActivity;
import com.lanqiDoctor.demo.database.dao.MedicationRecordDao;
import com.lanqiDoctor.demo.manager.MedicationAlarmManager;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.lanqiDoctor.demo.ui.dialog.TimeDialog;

/**
 * 用药时间设置Activity
 * 
 * @author rrrrrzy
 * @github https://github.com/rrrrrzy
 * @time 2025/6/16
 * @desc 为用户设置用药提醒时间
 */
public final class ClockActivity extends AppActivity {

    // 时间显示TextView
    private TextView tvOnceTime;
    private TextView tvTwiceMorningTime;
    private TextView tvTwiceEveningTime;
    private TextView tvThreeMorningTime;
    private TextView tvThreeNoonTime;
    private TextView tvThreeEveningTime;
    
    // 按钮
    private Button btnSaveSettings;
    private Button btnResetAllAlarms;
    
    // SharedPreferences用于保存时间设置
    private SharedPreferences timePrefs;
    private static final String PREFS_NAME = "medication_times";
    
    // 时间设置的键值
    private static final String KEY_ONCE_TIME = "once_time";
    private static final String KEY_TWICE_MORNING = "twice_morning";
    private static final String KEY_TWICE_EVENING = "twice_evening";
    private static final String KEY_THREE_MORNING = "three_morning";
    private static final String KEY_THREE_NOON = "three_noon";
    private static final String KEY_THREE_EVENING = "three_evening";
    
    // 权限请求码
    private static final int REQUEST_POST_NOTIFICATIONS = 1001;
    private static final int REQUEST_SYSTEM_ALERT_WINDOW = 1002;
    
    // 通知权限常量（兼容低版本SDK）
    private static final String POST_NOTIFICATIONS_PERMISSION = "android.permission.POST_NOTIFICATIONS";
    
    // 数据库操作和闹钟管理
    private MedicationRecordDao medicationDao;
    private MedicationAlarmManager alarmManager;

    @Log
    @Permissions({Permission.SCHEDULE_EXACT_ALARM})
    public static void start(BaseActivity activity) {
        Intent intent = new Intent(activity, ClockActivity.class);
        activity.startActivity(intent);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, ClockActivity.class);
        if (!(context instanceof BaseActivity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.clock_activity;
    }

    @Override
    protected void initView() {
        // 初始化时间显示TextView
        tvOnceTime = findViewById(R.id.tv_once_time);
        tvTwiceMorningTime = findViewById(R.id.tv_twice_morning_time);
        tvTwiceEveningTime = findViewById(R.id.tv_twice_evening_time);
        tvThreeMorningTime = findViewById(R.id.tv_three_morning_time);
        tvThreeNoonTime = findViewById(R.id.tv_three_noon_time);
        tvThreeEveningTime = findViewById(R.id.tv_three_evening_time);
        
        // 初始化按钮
        btnSaveSettings = findViewById(R.id.btn_save_settings);
        btnResetAllAlarms = findViewById(R.id.btn_reset_all_alarms);
        
        // 设置点击监听器
        setOnClickListener(tvOnceTime, tvTwiceMorningTime, tvTwiceEveningTime,
                tvThreeMorningTime, tvThreeNoonTime, tvThreeEveningTime,
                btnSaveSettings, btnResetAllAlarms);
    }

    @Override
    protected void initData() {
        timePrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        medicationDao = new MedicationRecordDao(this);
        alarmManager = new MedicationAlarmManager(this);
        
        // 加载已保存的时间设置
        loadSavedTimes();
    }

    @SingleClick
    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        
        if (viewId == R.id.tv_once_time) {
            showTimePickerDialog(KEY_ONCE_TIME, tvOnceTime, "每日一次服药时间", 8, 0);
        } else if (viewId == R.id.tv_twice_morning_time) {
            showTimePickerDialog(KEY_TWICE_MORNING, tvTwiceMorningTime, "每日两次 - 上午", 8, 0);
        } else if (viewId == R.id.tv_twice_evening_time) {
            showTimePickerDialog(KEY_TWICE_EVENING, tvTwiceEveningTime, "每日两次 - 下午", 18, 0);
        } else if (viewId == R.id.tv_three_morning_time) {
            showTimePickerDialog(KEY_THREE_MORNING, tvThreeMorningTime, "每日三次 - 早上", 8, 0);
        } else if (viewId == R.id.tv_three_noon_time) {
            showTimePickerDialog(KEY_THREE_NOON, tvThreeNoonTime, "每日三次 - 中午", 12, 0);
        } else if (viewId == R.id.tv_three_evening_time) {
            showTimePickerDialog(KEY_THREE_EVENING, tvThreeEveningTime, "每日三次 - 晚上", 18, 0);
        } else if (viewId == R.id.btn_save_settings) {
            saveSettingsAndUpdateAlarms();
        } else if (viewId == R.id.btn_reset_all_alarms) {
            resetAllAlarms();
        }
    }

    /**
     * 显示时间选择对话框
     */
//    private void showTimePickerDialog(String key, TextView textView, String title, int defaultHour, int defaultMinute) {
//        // 获取当前保存的时间或使用默认时间
//        String savedTime = timePrefs.getString(key, null);
//        int hour = defaultHour;
//        int minute = defaultMinute;
//
//        if (savedTime != null && savedTime.contains(":")) {
//            String[] parts = savedTime.split(":");
//            hour = Integer.parseInt(parts[0]);
//            minute = Integer.parseInt(parts[1]);
//        }
//
//        TimePickerDialog dialog = new TimePickerDialog(this,
//            (view, selectedHour, selectedMinute) -> {
//                String timeString = String.format("%02d:%02d", selectedHour, selectedMinute);
//                textView.setText(timeString);
//
//                // 保存到SharedPreferences
//                timePrefs.edit().putString(key, timeString).apply();
//            }, hour, minute, true);
//
//        dialog.setTitle(title);
//        dialog.show();
//    }

    /**
     * 好看一点的时间选择框
     */
    private void showTimePickerDialog(String key, TextView textView, String title, int defaultHour, int defaultMinute) {
        String savedTime = timePrefs.getString(key, null);


        new TimeDialog.Builder(this)
                .setTitle(title)
                .setConfirm("确定")
                .setCancel("取消")
                .setHour(defaultHour)
                .setMinute(defaultMinute)
                .setIgnoreSecond()
                .setListener(new TimeDialog.OnListener() {
                    @Override
                    public void onSelected(BaseDialog dialog, int hour, int minute, int second) {
                        String timeString = String.format("%02d:%02d", hour, minute);
                        textView.setText(timeString);

                        timePrefs.edit().putString(key, timeString).apply();
                    }
                })
                .show();

    }

    /**
     * 加载已保存的时间设置
     */
    private void loadSavedTimes() {
        tvOnceTime.setText(timePrefs.getString(KEY_ONCE_TIME, "未设置"));
        tvTwiceMorningTime.setText(timePrefs.getString(KEY_TWICE_MORNING, "未设置"));
        tvTwiceEveningTime.setText(timePrefs.getString(KEY_TWICE_EVENING, "未设置"));
        tvThreeMorningTime.setText(timePrefs.getString(KEY_THREE_MORNING, "未设置"));
        tvThreeNoonTime.setText(timePrefs.getString(KEY_THREE_NOON, "未设置"));
        tvThreeEveningTime.setText(timePrefs.getString(KEY_THREE_EVENING, "未设置"));
    }

    /**
     * 保存设置并更新闹钟
     */
    private void saveSettingsAndUpdateAlarms() {
        if (!XXPermissions.isGranted(this, Permission.SCHEDULE_EXACT_ALARM)) {
            toast("需要精确闹钟权限才能设置提醒");
            return;
        }
        
        // Android 13+ 通知权限检查
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED) {
                // 手动申请通知权限
                ActivityCompat.requestPermissions(this, 
                    new String[]{POST_NOTIFICATIONS_PERMISSION},
                    REQUEST_POST_NOTIFICATIONS);
            }
        }
        
        // 检查悬浮窗权限（用于自启动）
        checkAndRequestOverlayPermission();
    }
    
    /**
     * 检查并申请悬浮窗权限
     */
//    @Permissions({Permission.SYSTEM_ALERT_WINDOW})
    private void checkAndRequestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!android.provider.Settings.canDrawOverlays(this)) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("需要悬浮窗权限")
                        .setMessage("为了在提醒时自动启动应用，建议授予悬浮窗权限。\n\n这将帮助您及时看到用药提醒。")
                        .setPositiveButton("去设置", (dialog, which) -> {
                            try {
                                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                                startActivityForResult(intent, REQUEST_SYSTEM_ALERT_WINDOW);
                            } catch (Exception e) {
                                android.util.Log.e("ClockActivity", "打开悬浮窗设置失败: " + e.getMessage());
                                continueSaveAndUpdate();
                            }
                        })
                        .setNegativeButton("跳过", (dialog, which) -> {
                            toast("已跳过悬浮窗权限，提醒时可能无法自动启动应用");
                            continueSaveAndUpdate();
                        })
                        .setCancelable(false)
                        .show();
                return;
            }
        }
        
        continueSaveAndUpdate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SYSTEM_ALERT_WINDOW) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (android.provider.Settings.canDrawOverlays(this)) {
                    toast("悬浮窗权限已授予，现在可以自动启动应用了");
                } else {
                    toast("未授予悬浮窗权限，提醒时可能无法自动启动应用");
                }
            }
            continueSaveAndUpdate();
        }
    }

    @Override
    @Permissions({Permission.NOTIFICATION_SERVICE})
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予，继续保存
                continueSaveAndUpdate();
            } else {
                // 权限被拒绝，但仍可以继续（通知可能不会显示）
                toast("通知权限被拒绝，可能无法收到提醒通知");
                continueSaveAndUpdate();
            }
        }
    }

    private void continueSaveAndUpdate() {
        // 更新应用内闹钟
        if (alarmManager != null) {
            alarmManager.updateAllMedicationAlarms();
        }
        // 询问用户是否要设置系统闹钟
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("设置系统闹钟")
                .setMessage("是否要同时在系统闹钟中设置提醒？\n\n选择\"是\"将自动设置系统闹钟，选择\"否\"仅使用应用内提醒。")
                .setPositiveButton("是，设置系统闹钟", (dialog, which) -> {
                    if (alarmManager != null) {
                        alarmManager.setAutoSystemAlarm(true);
                        alarmManager.updateAllMedicationAlarms(true);
                    }
                    toast("时间设置已保存，应用内闹钟和系统闹钟都已更新");
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton("否，仅应用内闹钟", (dialog, which) -> {
                    if (alarmManager != null) {
                        alarmManager.setAutoSystemAlarm(false);
                    }
                    toast("时间设置已保存，仅应用内闹钟已更新");
                    setResult(RESULT_OK);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 重置所有闹钟
     */
    private void resetAllAlarms() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("重置闹钟")
                .setMessage("确定要重置所有闹钟设置吗？这将清除所有时间设置和闹钟。")
                .setPositiveButton("重置", (dialog, which) -> {
                    // 清除所有时间设置
                    timePrefs.edit().clear().apply();
                    
                    // 清除所有闹钟
                    if (alarmManager != null) {
                        alarmManager.clearAllCustomAlarms();
                    }
                    
                    // 重新加载界面
                    loadSavedTimes();
                    
                    toast("所有闹钟设置已重置");
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 检查时间设置是否完整 - 保留此方法以保持向后兼容
     * @deprecated 请使用 MedicationAlarmManager.isTimeSettingsComplete()
     */
    @Deprecated
    public static boolean isTimeSettingsComplete(Context context) {
        return MedicationAlarmManager.isTimeSettingsComplete(context);
    }

    /**
     * 闹钟广播接收器
     */
    public static class AlarmReceiver extends android.content.BroadcastReceiver {
        private static final String CHANNEL_ID = "medication_reminder_channel";
        private static final int NOTIFY_ID = 10086;

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            if (message != null) {
                android.util.Log.i("AlarmReceiver", "收到用药提醒: " + message);

                // 创建通知渠道（Android 8.0+）
                createNotificationChannel(context);

                // 创建通知
                createMedicationNotification(context, message);

                // 尝试自启动应用到前台
                try {
                    startAppToForeground(context, message);
                } catch (Exception e) {
                    android.util.Log.w("AlarmReceiver", "自启动失败，仅显示通知: " + e.getMessage());
                    // 如果自启动失败，确保通知已显示
                    TodayMedicationActivity.startFromAlarm(context);
                }
            }
        }

        /**
         * 创建通知渠道
         */
        private void createNotificationChannel(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm != null && nm.getNotificationChannel(CHANNEL_ID) == null) {
                    NotificationChannel channel = new NotificationChannel(
                            CHANNEL_ID,
                            "用药提醒",
                            NotificationManager.IMPORTANCE_HIGH
                    );
                    channel.enableVibration(true);
                    channel.enableLights(true);
                    channel.setDescription("蓝岐医童用药提醒通知");
                    nm.createNotificationChannel(channel);
                }
            }
        }

        /**
         * 创建用药提醒通知
         */
        private void createMedicationNotification(Context context, String message) {
            // 点击通知跳转到今日服药界面
            Intent launchIntent = new Intent(context, TodayMedicationActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra("from_alarm", true);
            PendingIntent pi = PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                            PendingIntent.FLAG_UPDATE_CURRENT
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.launcher_ic)
                    .setContentTitle("蓝岐医童用药提醒")
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setVibrate(new long[]{0, 300, 150, 300})
                    .setFullScreenIntent(pi, true); // 全屏显示Intent

            NotificationManagerCompat.from(context).notify(NOTIFY_ID, builder.build());
        }

        /**
         * 尝试启动应用到前台
         */
        private void startAppToForeground(Context context, String message) {
            android.util.Log.i("AlarmReceiver", "尝试自启动应用到前台");

            // 方法1: 直接启动Activity (适用于Android 10以下或有特殊权限的应用)
            try {
                Intent startIntent = new Intent(context, TodayMedicationActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP 
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startIntent.putExtra("from_alarm", true);
                startIntent.putExtra("alarm_message", message);
                
                context.startActivity(startIntent);
                android.util.Log.i("AlarmReceiver", "成功启动Activity到前台");
                return;
            } catch (Exception e) {
                android.util.Log.w("AlarmReceiver", "直接启动Activity失败: " + e.getMessage());
            }

            // 方法2: 通过系统悬浮窗权限启动 (Android 10+的备选方案)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    if (canDrawOverlays(context)) {
                        startWithOverlayPermission(context, message);
                        return;
                    }
                } catch (Exception e) {
                    android.util.Log.w("AlarmReceiver", "悬浮窗启动失败: " + e.getMessage());
                }
            }

            // 方法3: 退回到普通启动方式
            android.util.Log.i("AlarmReceiver", "使用普通方式启动");
            TodayMedicationActivity.startFromAlarm(context);
        }

        /**
         * 检查是否有悬浮窗权限
         */
        private boolean canDrawOverlays(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return android.provider.Settings.canDrawOverlays(context);
            }
            return true;
        }

        /**
         * 使用悬浮窗权限启动应用
         */
        private void startWithOverlayPermission(Context context, String message) {
            Intent intent = new Intent(context, TodayMedicationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("from_alarm", true);
            intent.putExtra("alarm_message", message);
            
            context.startActivity(intent);
            android.util.Log.i("AlarmReceiver", "通过悬浮窗权限成功启动");
        }
    }
}