package com.lanqiDoctor.demo.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hjq.base.BaseActivity;
import com.hjq.toast.ToastUtils;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.dao.MedicationIntakeRecordDao;
import com.lanqiDoctor.demo.database.dao.MedicationRecordDao;
import com.lanqiDoctor.demo.database.entity.MedicationIntakeRecord;
import com.lanqiDoctor.demo.database.entity.MedicationRecord;
import com.lanqiDoctor.demo.manager.UserStateManager;
import com.lanqiDoctor.demo.ui.adapter.TodayMedicationAdapter;
import com.lanqiDoctor.demo.model.TodayMedicationItem;
import com.lanqiDoctor.demo.manager.CloudSyncManager;
import com.lanqiDoctor.demo.manager.TodayMedicationManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 今日服药情况Activity
 *
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class TodayMedicationActivity extends BaseActivity implements TodayMedicationAdapter.OnMedicationStatusChangeListener {

    private TextView tvCurrentDate;
    private RecyclerView rvMedicationList;
    private TextView tvEmptyView;
    private FloatingActionButton refreshIcon;
    private TodayMedicationAdapter adapter;
    private MedicationRecordDao medicationDao;
    private MedicationIntakeRecordDao intakeDao;
    private CloudSyncManager cloudSyncManager;

    private List<TodayMedicationItem> medicationItems;
    private SharedPreferences timePrefs;

    // 时间设置的键值
    private static final String PREFS_NAME = "medication_times";
    private static final String KEY_ONCE_TIME = "once_time";
    private static final String KEY_TWICE_MORNING = "twice_morning";
    private static final String KEY_TWICE_EVENING = "twice_evening";
    private static final String KEY_THREE_MORNING = "three_morning";
    private static final String KEY_THREE_NOON = "three_noon";
    private static final String KEY_THREE_EVENING = "three_evening";

    private String userId; // 新增

    /**
     * 启动今日服药情况Activity
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, TodayMedicationActivity.class);
        if (!(context instanceof BaseActivity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * 启动今日服药情况Activity（带标记）
     */
    public static void startFromAlarm(Context context) {
        Intent intent = new Intent(context, TodayMedicationActivity.class);
        intent.putExtra("from_alarm", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
            | Intent.FLAG_ACTIVITY_CLEAR_TOP 
            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        
        try {
            context.startActivity(intent);
            Log.d("TodayMedicationActivity", "通过闹钟成功启动Activity");
        } catch (Exception e) {
            Log.e("TodayMedicationActivity", "启动Activity失败: " + e.getMessage());
            // 如果启动失败，尝试更简单的启动方式
            try {
                Intent simpleIntent = new Intent(context, TodayMedicationActivity.class);
                simpleIntent.putExtra("from_alarm", true);
                simpleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(simpleIntent);
            } catch (Exception ex) {
                Log.e("TodayMedicationActivity", "简单启动方式也失败: " + ex.getMessage());
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.today_medication_activity;
    }

    @Override
    protected void initView() {
        tvCurrentDate = findViewById(R.id.tv_current_date);
        rvMedicationList = findViewById(R.id.rv_medication_list);
        tvEmptyView = findViewById(R.id.tv_empty_view);
        refreshIcon = findViewById(R.id.refresh_today_medication);

        // 设置RecyclerView
        rvMedicationList.setLayoutManager(new LinearLayoutManager(this));
        medicationItems = new ArrayList<>();
        adapter = new TodayMedicationAdapter(medicationItems, this);
        rvMedicationList.setAdapter(adapter);
        // 修复：设置刷新按钮监听器，添加页面刷新逻辑
        refreshIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TodayMedication", "用户点击刷新按钮");
                ToastUtils.show("正在刷新服药记录...");
                
                // 强制重新初始化服药记录
                TodayMedicationManager.getInstance(TodayMedicationActivity.this).forceReinitTodayMedicationData();
                
                // 重新加载页面数据
                loadTodayMedicationData();
                
                ToastUtils.show("刷新完成");
            }
        });
    }

    @Override
    protected void initData() {
        medicationDao = new MedicationRecordDao(this);
        intakeDao = new MedicationIntakeRecordDao(this);
        timePrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        cloudSyncManager = CloudSyncManager.getInstance(this);

        // 新增：初始化userId
        userId = UserStateManager.getInstance(this).getUserId();

        // 显示当前日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.CHINA);
        tvCurrentDate.setText(dateFormat.format(new Date()));

        // 检查是否从闹钟跳转
        boolean fromAlarm = getIntent().getBooleanExtra("from_alarm", false);
        if (fromAlarm) {
            ToastUtils.show("服药提醒时间到了，请记录您的服药情况");
        }

        loadTodayMedicationData();
    }

    /**
     * 加载今日服药数据
     */
    private void loadTodayMedicationData() {
        // // 强制重新初始化（测试用）
        // TodayMedicationManager.getInstance(this).forceReinitTodayMedicationData();
        // 添加这些调试日志
        Log.d("TodayMedication", "=== 调试信息开始 ===");
        Log.d("TodayMedication", "当前用户ID: " + userId);

        // 测试查询所有药物
        List<MedicationRecord> allMedications = medicationDao.findAll();
        Log.d("TodayMedication", "数据库中所有药物数量: " + allMedications.size());
        for (MedicationRecord med : allMedications) {
            Log.d("TodayMedication", "药物: " + med.getMedicationName() + ", userId: " + med.getUserId() + ", status: " + med.getStatus());
        }
        
        // 测试按用户ID查询
        if (userId != null) {
            List<MedicationRecord> userMedications = medicationDao.findActiveMedications(userId);
            Log.d("TodayMedication", "当前用户的活跃药物数量: " + userMedications.size());
        }
        Log.d("TodayMedication", "=== 调试信息结束 ===");

        Log.d("TodayMedication", "开始加载今日服药数据，userId: " + userId);
        try {
            // 确保今日数据已初始化（双重保险）
            TodayMedicationManager medicationManager = TodayMedicationManager.getInstance(this);
            intakeDao.validateUserData(userId);
            // loadTodayMedicationData();

            medicationManager.initTodayMedicationData();

            // 获取当前用户的所有活跃药物
            List<MedicationRecord> activeMedications = medicationDao.findActiveMedications(userId); // 传入userId
            Log.d("TodayMedication", "找到活跃药物数量: " + activeMedications.size());
            
            if (activeMedications.isEmpty()) {
                Log.d("TodayMedication", "没有活跃药物，显示空视图");
                showEmptyView("暂无需要服用的药物");
                return;
            }

            // 清空现有数据
            medicationItems.clear();
            Log.d("TodayMedication", "清空现有数据");

            // 获取今日的开始时间
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            long todayStart = today.getTimeInMillis();
            Log.d("TodayMedication", "今日开始时间: " + todayStart);

            // 按时间段分组：早上、中午、晚上
            Map<String, List<TodayMedicationItem>> timeGroupMap = new HashMap<>();
            timeGroupMap.put("早上", new ArrayList<>());
            timeGroupMap.put("中午", new ArrayList<>());
            timeGroupMap.put("晚上", new ArrayList<>());

            // 为每个药物创建今日的服药项目
            for (MedicationRecord medication : activeMedications) {
                String frequency = medication.getFrequency();
                Log.d("TodayMedication", "处理药物: " + medication.getMedicationName() + ", 频率: " + frequency);

                if (frequency == null) {
                    Log.w("TodayMedication", "药物频率为空: " + medication.getMedicationName());
                    continue;
                }

                List<String> timeKeys = getTimeKeysForFrequency(frequency);
                Log.d("TodayMedication", "获得时间键数量: " + timeKeys.size());

                for (String timeKey : timeKeys) {
                    try {
                        String timeString = timePrefs.getString(timeKey, null);
                        Log.d("TodayMedication", "时间键 " + timeKey + " 对应时间: " + timeString);

                        if (timeString == null || "未设置".equals(timeString)) {
                            Log.w("TodayMedication", "时间未设置，跳过: " + timeKey);
                            continue;
                        }

                        // 计算计划服药时间
                        long plannedTime = calculatePlannedTime(todayStart, timeString);
                        Log.d("TodayMedication", "计算的计划时间: " + plannedTime);

                        // 查询服药记录（使用药物名称）
                        MedicationIntakeRecord intakeRecord = intakeDao.findTodayRecordByNameAndTime(userId,
                                medication.getMedicationName(), plannedTime);

                        // 如果仍然没有记录，记录错误但继续处理
                        if (intakeRecord == null) {
                            Log.e("TodayMedication", "未找到服药记录: " + medication.getMedicationName() +
                                    " 时间: " + timeString + "，可能初始化失败");
                            continue;
                        }

                        Log.d("TodayMedication", "找到服药记录: " + medication.getMedicationName() +
                                " 状态: " + intakeRecord.getStatus());

                        // 创建今日服药项目
                        TodayMedicationItem item = new TodayMedicationItem();
                        item.setMedicationName(medication.getMedicationName()); // 使用名称而不是ID
                        item.setDosage(medication.getDosage());
                        item.setUnit(medication.getUnit());
                        item.setPlannedTime(plannedTime);
                        item.setTimeString(timeString);
                        item.setIntakeRecord(intakeRecord);

                        // 确定时间组
                        String timeGroup = getTimeGroup(timeKey);
                        item.setTimeGroup(timeGroup);

                        // 添加到对应的时间组
                        List<TodayMedicationItem> groupItems = timeGroupMap.get(timeGroup);
                        if (groupItems != null) {
                            groupItems.add(item);
                            Log.d("TodayMedication", "添加到时间组 " + timeGroup + ": " + medication.getMedicationName());
                        } else {
                            Log.w("TodayMedication", "未找到时间组: " + timeGroup);
                        }
                    } catch (Exception e) {
                        Log.e("TodayMedication", "处理时间键时发生异常: " + timeKey, e);
                    }
                }
            }

            // 按时间段顺序添加到列表
            addItemsWithHeader(medicationItems, "早上", timeGroupMap.get("早上"));
            addItemsWithHeader(medicationItems, "中午", timeGroupMap.get("中午"));
            addItemsWithHeader(medicationItems, "晚上", timeGroupMap.get("晚上"));

            Log.d("TodayMedication", "最终项目数量: " + medicationItems.size());

            // 更新UI
            if (medicationItems.isEmpty()) {
                Log.d("TodayMedication", "没有服药项目，显示空视图");
                showEmptyView("今日暂无服药安排");
            } else {
                Log.d("TodayMedication", "显示服药列表");
                showMedicationList();
            }

        } catch (Exception e) {
            Log.e("TodayMedication", "加载今日服药数据时发生异常", e);
            showEmptyView("数据加载失败，请重试");
        }
    }

    /**
     * 添加带标题的项目
     */
    private void addItemsWithHeader(List<TodayMedicationItem> allItems, String header, List<TodayMedicationItem> items) {
        if (!items.isEmpty()) {
            // 添加时间段标题
            TodayMedicationItem headerItem = new TodayMedicationItem();
            headerItem.setItemType(TodayMedicationItem.TYPE_HEADER);
            headerItem.setTimeGroup(header);
            allItems.add(headerItem);

            // 添加具体的药物项目
            for (TodayMedicationItem item : items) {
                item.setItemType(TodayMedicationItem.TYPE_MEDICATION);
                allItems.add(item);
            }
        }
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
     * 根据时间键获取时间组
     */
    private String getTimeGroup(String timeKey) {
        switch (timeKey) {
            case KEY_ONCE_TIME:
            case KEY_TWICE_MORNING:
            case KEY_THREE_MORNING:
                return "早上";
            case KEY_THREE_NOON:
                return "中午";
            case KEY_TWICE_EVENING:
            case KEY_THREE_EVENING:
                return "晚上";
            default:
                return "早上";
        }
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
            e.printStackTrace();
            return todayStart;
        }
    }

    /**
     * 显示空视图
     */
    private void showEmptyView(String message) {
        rvMedicationList.setVisibility(View.GONE);
        tvEmptyView.setVisibility(View.VISIBLE);
        tvEmptyView.setText(message);
    }

    /**
     * 显示药物列表
     */
    private void showMedicationList() {
        rvMedicationList.setVisibility(View.VISIBLE);
        tvEmptyView.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMedicationStatusChanged(TodayMedicationItem item, boolean isChecked) {
        if (isChecked) {
            // 标记为已服用
            markMedicationAsTaken(item);
        } else {
            // 标记为未服用
            markMedicationAsNotTaken(item);
        }
    }
    /**
     * 标记药物为已服用
     */
    private void markMedicationAsTaken(TodayMedicationItem item) {
        MedicationIntakeRecord intakeRecord = item.getIntakeRecord();

        if (intakeRecord != null) {
            // 更新现有记录
            intakeRecord.setActualTime(System.currentTimeMillis());
            intakeRecord.setStatus(1); // 已服用
            
            // 确保userId设置正确
            if (intakeRecord.getUserId() == null || intakeRecord.getUserId().isEmpty()) {
                intakeRecord.setUserId(userId);
            }
            
            int affectedRows = intakeDao.updateByNameAndTime(userId, intakeRecord);
            
            if (affectedRows > 0) {
                Log.d("TodayMedication", "成功更新服药记录为已服用: " + item.getMedicationName() + ", 影响行数: " + affectedRows);
                
                // 立即验证更新结果
                MedicationIntakeRecord verifyRecord = intakeDao.findTodayRecordByNameAndTime(userId,
                        item.getMedicationName(), item.getPlannedTime());
                if (verifyRecord != null) {
                    Log.d("TodayMedication", "验证更新结果: " + item.getMedicationName() + 
                            " 状态=" + verifyRecord.getStatus());
                }
            } else {
                Log.e("TodayMedication", "更新服药记录失败: " + item.getMedicationName() + ", 影响行数: " + affectedRows);
            }
        } else {
            Log.w("TodayMedication", "未找到服药记录，创建新记录: " + item.getMedicationName());

            intakeRecord = new MedicationIntakeRecord();
            intakeRecord.setMedicationName(item.getMedicationName());
            intakeRecord.setPlannedTime(item.getPlannedTime());
            intakeRecord.setActualTime(System.currentTimeMillis());
            intakeRecord.setActualDosage(item.getDosage());
            intakeRecord.setStatus(1); // 已服用
            intakeRecord.setUserId(userId); // 确保record里也有userId

            long result = intakeDao.insertOrUpdateByNameAndTime(intakeRecord);
            
            if (result > 0) {
                item.setIntakeRecord(intakeRecord);
                Log.d("TodayMedication", "成功创建新的服药记录: " + item.getMedicationName() + ", ID: " + result);
            } else {
                Log.e("TodayMedication", "创建服药记录失败: " + item.getMedicationName());
                ToastUtils.show("记录服药状态失败，请重试");
                return;
            }
        }

        ToastUtils.show("已记录 " + item.getMedicationName() + " 服用情况");

        // 触发云端同步（如果启用）
        triggerCloudSyncIfEnabled();
    }
    
    /**
     * 标记药物为未服用
     */
    private void markMedicationAsNotTaken(TodayMedicationItem item) {
        MedicationIntakeRecord intakeRecord = item.getIntakeRecord();

        if (intakeRecord != null) {
            // 更新记录为未服用状态
            intakeRecord.setActualTime(null);
            intakeRecord.setStatus(0); // 未服用
            
            // 确保userId设置正确
            if (intakeRecord.getUserId() == null || intakeRecord.getUserId().isEmpty()) {
                intakeRecord.setUserId(userId);
            }
            
            int affectedRows = intakeDao.updateByNameAndTime(userId, intakeRecord);
            
            if (affectedRows > 0) {
                Log.d("TodayMedication", "成功更新服药记录为未服用: " + item.getMedicationName() + ", 影响行数: " + affectedRows);
            } else {
                Log.e("TodayMedication", "更新服药记录失败: " + item.getMedicationName() + ", 影响行数: " + affectedRows);
            }
        } else {
            Log.w("TodayMedication", "未找到服药记录，无法标记为未服用: " + item.getMedicationName());
        }

        ToastUtils.show("已取消 " + item.getMedicationName() + " 的服用记录");
        // 触发云端同步（如果启用）
        triggerCloudSyncIfEnabled();
    }

    /**
     * 如果启用了自动同步，则触发云端同步
     */
    private void triggerCloudSyncIfEnabled() {
        if (cloudSyncManager.canSyncToCloud()) {
            // 在后台执行同步，不打扰用户
            new Thread(() -> {
                cloudSyncManager.autoSyncInBackground();
            }).start();
        }
    }

    /**
     * 获取今日服药状态 - 供其他Activity调用
     */
    public static Map<String, Object> getTodayMedicationStatus(Context context) {
        Map<String, Object> statusMap = new HashMap<>();

        try {
            // 1. 获取userId（用context，不要用this）
            String userId = com.lanqiDoctor.demo.manager.UserStateManager.getInstance(context).getUserId();

            MedicationIntakeRecordDao intakeDao = new MedicationIntakeRecordDao(context);
            List<MedicationIntakeRecord> todayRecords = intakeDao.findTodayRecords(userId);

            int totalCount = 0;
            int takenCount = 0;

            // 还需要获取应该服用的总数
            MedicationRecordDao medicationDao = new MedicationRecordDao(context);
            List<MedicationRecord> activeMedications = medicationDao.findActiveMedications(userId); // 传入userId

            SharedPreferences timePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            // 计算今日应该服用的总次数
            for (MedicationRecord medication : activeMedications) {
                String frequency = medication.getFrequency();
                if (frequency != null) {
                    switch (frequency) {
                        case "每日一次":
                        case "每日1次":
                            totalCount += 1;
                            break;
                        case "每日两次":
                        case "每日2次":
                            totalCount += 2;
                            break;
                        case "每日三次":
                        case "每日3次":
                            totalCount += 3;
                            break;
                    }
                }
            }

            // 计算已服用次数
            for (MedicationIntakeRecord record : todayRecords) {
                if (record.getStatus() != null && record.getStatus() == 1) {
                    takenCount++;
                }
            }

            statusMap.put("totalCount", totalCount);
            statusMap.put("takenCount", takenCount);
            statusMap.put("completionRate", totalCount > 0 ? (float) takenCount / totalCount : 0f);
            statusMap.put("allTaken", totalCount > 0 && takenCount >= totalCount);

        } catch (Exception e) {
            e.printStackTrace();
            statusMap.put("error", e.getMessage());
        }

        return statusMap;
    }
}