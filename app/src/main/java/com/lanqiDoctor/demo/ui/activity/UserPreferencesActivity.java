package com.lanqiDoctor.demo.ui.activity;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Button;
import android.app.AlertDialog;
import android.util.Log;

import com.hjq.base.BaseActivity;
import com.hjq.toast.ToastUtils;
import com.hjq.bar.TitleBar;
import com.hjq.bar.OnTitleBarListener;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.manager.UserStateManager;
import com.lanqiDoctor.demo.manager.CloudSyncManager;

/**
 * 用户偏好设置页面
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class UserPreferencesActivity extends BaseActivity implements View.OnClickListener {
    
    private TitleBar tbTitle;
    
    // 通知设置
    private LinearLayout llPushNotification;
    private Switch switchPushNotification;
    private LinearLayout llHealthReminder;
    private Switch switchHealthReminder;
    private LinearLayout llMedicationReminder;
    private Switch switchMedicationReminder;
    
    // 界面设置
    private LinearLayout llLanguage;
    private TextView tvLanguageValue;
    private LinearLayout llTheme;
    private TextView tvThemeValue;
    
    // 数据设置
    private LinearLayout llAutoSync;
    private Switch switchAutoSync;
    private LinearLayout llPrivacyLevel;
    private TextView tvPrivacyValue;
    
    private UserStateManager userStateManager;
    private Button btnManualSync;
    private CloudSyncManager cloudSyncManager;
    // 添加新的同步UI组件
    private TextView tvSyncStatus;
    private Button btnResetSync;

    @Override
    protected int getLayoutId() {
        return R.layout.user_preferences_activity;
    }
    
    @Override
    protected void initView() {
        tbTitle = findViewById(R.id.tb_title);
        
        // 通知设置组件
        llPushNotification = findViewById(R.id.ll_push_notification);
        switchPushNotification = findViewById(R.id.switch_push_notification);
        llHealthReminder = findViewById(R.id.ll_health_reminder);
        switchHealthReminder = findViewById(R.id.switch_health_reminder);
        llMedicationReminder = findViewById(R.id.ll_medication_reminder);
        switchMedicationReminder = findViewById(R.id.switch_medication_reminder);
        
        // 界面设置组件
        llLanguage = findViewById(R.id.ll_language);
        tvLanguageValue = findViewById(R.id.tv_language_value);
        llTheme = findViewById(R.id.ll_theme);
        tvThemeValue = findViewById(R.id.tv_theme_value);
        
        // 数据设置组件
        llAutoSync = findViewById(R.id.ll_auto_sync);
        switchAutoSync = findViewById(R.id.switch_auto_sync);
        llPrivacyLevel = findViewById(R.id.ll_privacy_level);
        tvPrivacyValue = findViewById(R.id.tv_privacy_value);

        // 数据同步组件
        btnManualSync = findViewById(R.id.btn_manual_sync);
        setOnClickListener(btnManualSync);

        // 添加同步状态显示
        tvSyncStatus = findViewById(R.id.tv_sync_status);
        btnResetSync = findViewById(R.id.btn_reset_sync);
        
        setOnClickListener(btnResetSync);
        
        // 设置点击监听
        setOnClickListener(llPushNotification, llHealthReminder, llMedicationReminder,
                llLanguage, llTheme, llAutoSync, llPrivacyLevel);
        
        // 修改：设置TitleBar监听器（正确的方法）
        tbTitle.setOnTitleBarListener(new OnTitleBarListener() {
            @Override
            public void onLeftClick(View view) {
                finish(); // 左侧按钮点击时关闭页面
            }

            @Override
            public void onTitleClick(View view) {
                // 标题点击事件（可选）
            }

            @Override
            public void onRightClick(View view) {
                // 右侧按钮点击事件（可选）
            }
        });
    }
    
    @Override
    protected void initData() {
        userStateManager = UserStateManager.getInstance(this);
        cloudSyncManager = CloudSyncManager.getInstance(this);
        
        // 加载当前设置
        loadCurrentSettings();
        // 显示同步状态
        updateSyncStatusDisplay();
    }
    
    private void loadCurrentSettings() {
        // 设置开关状态
        switchPushNotification.setChecked(userStateManager.isEnablePushNotification());
        switchHealthReminder.setChecked(userStateManager.isEnableHealthReminder());
        switchMedicationReminder.setChecked(userStateManager.isEnableMedicationReminder());
        switchAutoSync.setChecked(userStateManager.isAutoSyncHealthData());
        
        // 设置选择项文本
        tvLanguageValue.setText(getLanguageDisplayName(userStateManager.getPreferredLanguage()));
        tvThemeValue.setText(getThemeDisplayName(userStateManager.getThemeMode()));
        tvPrivacyValue.setText(getPrivacyLevelDisplayName(userStateManager.getPrivacyLevel()));
        
        // 设置Switch的监听器
        switchPushNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userStateManager.setEnablePushNotification(isChecked);
            ToastUtils.show(isChecked ? "推送通知已开启" : "推送通知已关闭");
        });
        
        switchHealthReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userStateManager.setEnableHealthReminder(isChecked);
            ToastUtils.show(isChecked ? "健康提醒已开启" : "健康提醒已关闭");
        });
        
        switchMedicationReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userStateManager.setEnableMedicationReminder(isChecked);
            ToastUtils.show(isChecked ? "用药提醒已开启" : "用药提醒已关闭");
        });
        
        switchAutoSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userStateManager.setAutoSyncHealthData(isChecked);
            ToastUtils.show(isChecked ? "自动同步已开启" : "自动同步已关闭");

            // 更新手动同步按钮的可用状态
            updateManualSyncButtonState();
        });

        // 更新手动同步按钮状态
        updateManualSyncButtonState();
    }


    /**
     * 更新手动同步按钮的状态
     */
    private void updateManualSyncButtonState() {
        boolean canSync = cloudSyncManager.canSyncToCloud();
        btnManualSync.setEnabled(canSync);
        
        if (canSync) {
            btnManualSync.setText("立即同步数据");
        } else {
            if (!userStateManager.isUserLoggedIn()) {
                btnManualSync.setText("请先登录");
            } else if (!userStateManager.isAutoSyncHealthData()) {
                btnManualSync.setText("请开启自动同步");
            } else {
                btnManualSync.setText("无法同步");
            }
        }
    }

    /**
     * 更新同步状态显示 - 优化版本
     */
    private void updateSyncStatusDisplay() {
        try {
            CloudSyncManager.SyncStatus status = cloudSyncManager.getSyncStatus();
            
            StringBuilder statusText = new StringBuilder();
            statusText.append("同步状态：");
            
            if (status.isFirstSync) {
                statusText.append("尚未同步");
                
                // 提示首次同步的特殊处理
                if (userStateManager.isUserLoggedIn() && userStateManager.isAutoSyncHealthData()) {
                    statusText.append("\n（首次同步将下载服务器数据）");
                }
            } else {
                statusText.append("已同步\n");
                
                if (status.lastMedicationSyncTime != null) {
                    statusText.append("用药信息：")
                            .append(formatSyncTime(status.lastMedicationSyncTime))
                            .append("\n");
                }
                
                if (status.lastIntakeSyncTime != null) {
                    statusText.append("服药记录：")
                            .append(formatSyncTime(status.lastIntakeSyncTime));
                }
            }
            
            tvSyncStatus.setText(statusText.toString());
            
            // 更新重置按钮状态
            btnResetSync.setEnabled(!status.isFirstSync);
            btnResetSync.setText(status.isFirstSync ? "暂无同步记录" : "重置同步状态");
            
        } catch (Exception e) {
            Log.e("UserPreferences", "更新同步状态显示失败", e);
            tvSyncStatus.setText("同步状态：获取失败");
        }
    }

    /**
     * 格式化同步时间显示
     */
    private String formatSyncTime(long timestamp) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(timestamp));
        } catch (Exception e) {
            return "时间格式错误";
        }
    }

    @Override
    public void onClick(View view) {
        if (view == llPushNotification) {
            // 点击布局时切换Switch状态
            switchPushNotification.setChecked(!switchPushNotification.isChecked());
            
        } else if (view == llHealthReminder) {
            switchHealthReminder.setChecked(!switchHealthReminder.isChecked());
            
        } else if (view == llMedicationReminder) {
            switchMedicationReminder.setChecked(!switchMedicationReminder.isChecked());
            
        } else if (view == llAutoSync) {
            switchAutoSync.setChecked(!switchAutoSync.isChecked());
            
        } else if (view == llLanguage) {
            showLanguageSelectionDialog();
            
        } else if (view == llTheme) {
            showThemeSelectionDialog();
            
        } else if (view == llPrivacyLevel) {
            showPrivacyLevelSelectionDialog();
        } else if (view == btnManualSync) {
            performManualSyncWithStatusUpdate();
        } else if (view == btnResetSync) {
            showResetSyncConfirmDialog();
        }
    }
    
    private void showLanguageSelectionDialog() {
        String[] languages = {"简体中文", "English"};
        String[] languageCodes = {"zh_CN", "en_US"};
        
        // 找到当前选中的语言
        String currentLanguage = userStateManager.getPreferredLanguage();
        int checkedItem = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLanguage)) {
                checkedItem = i;
                break;
            }
        }
        
        new AlertDialog.Builder(this)
                .setTitle("选择语言")
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    userStateManager.setPreferredLanguage(languageCodes[which]);
                    tvLanguageValue.setText(languages[which]);
                    ToastUtils.show("语言设置已更改");
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    private void showThemeSelectionDialog() {
        String[] themes = {"自动", "浅色", "深色"};
        String[] themeCodes = {"auto", "light", "dark"};
        
        // 找到当前选中的主题
        String currentTheme = userStateManager.getThemeMode();
        int checkedItem = 0;
        for (int i = 0; i < themeCodes.length; i++) {
            if (themeCodes[i].equals(currentTheme)) {
                checkedItem = i;
                break;
            }
        }
        
        new AlertDialog.Builder(this)
                .setTitle("选择主题")
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    userStateManager.setThemeMode(themeCodes[which]);
                    tvThemeValue.setText(themes[which]);
                    ToastUtils.show("主题设置已更改");
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 显示重置同步确认对话框
     */
    private void showResetSyncConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("重置同步状态")
                .setMessage("重置后将清除所有同步记录，下次同步将作为首次同步执行。\n\n确定要重置吗？")
                .setPositiveButton("重置", (dialog, which) -> {
                    cloudSyncManager.resetSyncState();
                    updateSyncStatusDisplay();
                    ToastUtils.show("同步状态已重置");
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 执行手动同步并更新状态显示
     */
    private void performManualSyncWithStatusUpdate() {
        if (!cloudSyncManager.canSyncToCloud()) {
            ToastUtils.show("无法同步：请先登录并开启自动同步健康数据");
            return;
        }
        
        ToastUtils.show("开始同步数据到云端...");
        
        cloudSyncManager.performFullSync(new CloudSyncManager.SyncCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d("UserPreferences", "手动同步成功: " + message);
                
                // 在主线程中更新UI
                runOnUiThread(() -> {
                    ToastUtils.show("数据同步成功");
                    updateSyncStatusDisplay(); // 更新同步状态显示
                    updateManualSyncButtonState(); // 更新按钮状态
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e("UserPreferences", "手动同步失败: " + error);
                
                // 在主线程中显示错误
                runOnUiThread(() -> {
                    ToastUtils.show("数据同步失败: " + error);
                    updateSyncStatusDisplay(); // 即使失败也要更新显示
                });
            }
        });
    }

    private void showPrivacyLevelSelectionDialog() {
        String[] levels = {"公开", "好友可见", "仅自己"};
        
        // 获取当前隐私级别
        int currentLevel = userStateManager.getPrivacyLevel();
        
        new AlertDialog.Builder(this)
                .setTitle("选择隐私级别")
                .setSingleChoiceItems(levels, currentLevel, (dialog, which) -> {
                    userStateManager.setPrivacyLevel(which);
                    tvPrivacyValue.setText(levels[which]);
                    ToastUtils.show("隐私设置已更改");
                    dialog.dismiss();
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    private String getLanguageDisplayName(String languageCode) {
        switch (languageCode) {
            case "zh_CN": return "简体中文";
            case "en_US": return "English";
            default: return "简体中文";
        }
    }
    
    private String getThemeDisplayName(String themeMode) {
        switch (themeMode) {
            case "auto": return "自动";
            case "light": return "浅色";
            case "dark": return "深色";
            default: return "自动";
        }
    }
    
    private String getPrivacyLevelDisplayName(int level) {
        switch (level) {
            case 0: return "公开";
            case 1: return "好友可见";
            case 2: return "仅自己";
            default: return "好友可见";
        }
    }
}