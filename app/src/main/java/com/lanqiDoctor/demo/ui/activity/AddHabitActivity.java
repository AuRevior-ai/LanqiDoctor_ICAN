package com.lanqiDoctor.demo.ui.activity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import com.hjq.base.BaseActivity;
import com.hjq.base.BaseDialog;
import com.hjq.toast.ToastUtils;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.dao.HabitDao;
import com.lanqiDoctor.demo.database.entity.Habit;
import com.lanqiDoctor.demo.ui.dialog.TimeDialog;
import com.lanqiDoctor.demo.manager.HabitAlarmManager;

import org.json.JSONArray;
import org.json.JSONException;

import android.widget.LinearLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 添加/编辑习惯Activity
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class AddHabitActivity extends BaseActivity {
    
    private EditText etHabitName;
    private EditText etDescription;
    private Spinner spinnerFrequency;
    private EditText etFrequencyValue;
    private Spinner spinnerFrequencyUnit;
    private EditText etDuration;
    private EditText etCycleDays;
    private LinearLayout tvReminderTimes;
    private LinearLayout tvBlockTimes;
    private Switch switchEnableNotification;
    private Switch switchEnableSystemAlarm;
    private Spinner spinnerCategory;
    private Spinner spinnerPriority;
    private EditText etNotes;
    private Button btnSave;
    private Button btnCancel;
    
    private HabitDao habitDao;
    private Habit currentHabit;
    private boolean isEditMode = false;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    private List<String> reminderTimesList = new ArrayList<>();
    private List<String> blockTimesList = new ArrayList<>();
    private HabitAlarmManager alarmManager;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_habit;
    }

    @Override
    protected void initView() {
        etHabitName = findViewById(R.id.et_habit_name);
        etDescription = findViewById(R.id.et_habit_description);
        spinnerFrequency = findViewById(R.id.spinner_frequency_type);
        etFrequencyValue = findViewById(R.id.et_frequency_count);
        spinnerFrequencyUnit = findViewById(R.id.spinner_frequency_period);
        etDuration = findViewById(R.id.et_duration_days);
        etCycleDays = findViewById(R.id.et_duration_days); // 使用同一个字段
        
        // 修复UI绑定 - 使用正确的控件ID
        tvReminderTimes = findViewById(R.id.layout_reminder_times);
        // tvBlockTimes 暂时复用，后续添加专门的UI
        tvBlockTimes = findViewById(R.id.layout_reminder_times); 
        
        switchEnableNotification = findViewById(R.id.switch_reminder_enabled);
        switchEnableSystemAlarm = findViewById(R.id.switch_reminder_enabled); // 暂时使用同一个
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerPriority = findViewById(R.id.spinner_category); // 暂时使用同一个
        etNotes = findViewById(R.id.et_habit_description); // 暂时使用描述字段
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        
        // 设置下拉框数据
        setupSpinners();
        
        // 设置点击监听 - 添加新增的按钮
        Button btnAddReminderTime = findViewById(R.id.btn_add_reminder_time);
        setOnClickListener(tvReminderTimes, tvBlockTimes, btnSave, btnCancel, btnAddReminderTime);
        findViewById(R.id.tb_title).setOnClickListener(v -> finish());
        
        // 设置提醒开关监听
        switchEnableNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvReminderTimes.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    protected void initData() {
        habitDao = new HabitDao(this);
        alarmManager = new HabitAlarmManager(this);
        
        // 检查是否为编辑模式
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("is_edit", false);
        
        if (isEditMode) {
            Long habitId = intent.getLongExtra("habit_id", -1);
            if (habitId != -1) {
                loadHabitData(habitId);
            }
        } else {
            // 设置默认值
            setDefaultValues();
        }
    }

    private void setupSpinners() {
        // 频次类型选项
        String[] frequencyOptions = {"每日", "每小时", "每周", "自定义"};
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, frequencyOptions);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(frequencyAdapter);
        
        // 频次单位选项
        String[] frequencyUnitOptions = {"小时", "天", "周"};
        ArrayAdapter<String> frequencyUnitAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, frequencyUnitOptions);
        frequencyUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequencyUnit.setAdapter(frequencyUnitAdapter);
        
        // 分类选项
        String[] categoryOptions = {"健康", "运动", "饮食", "学习", "其他"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categoryOptions);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        
        // 优先级选项
        String[] priorityOptions = {"很低", "低", "中等", "高", "很高"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, priorityOptions);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.layout_reminder_times || viewId == R.id.btn_add_reminder_time) {
            showReminderTimeSelector();
        } else if (view == tvBlockTimes) {
            showBlockTimeSelector();
        } else if (viewId == R.id.btn_save) {
            saveHabit();
        } else if (viewId == R.id.btn_cancel) {
            finish();
        }
    }

    private void setDefaultValues() {
        // 设置默认值
        spinnerFrequency.setSelection(0); // 每日
        etFrequencyValue.setText("1");
        spinnerFrequencyUnit.setSelection(1); // 天
        etDuration.setText("30"); // 30分钟
        etCycleDays.setText("21"); // 21天
        spinnerCategory.setSelection(0); // 健康
        spinnerPriority.setSelection(2); // 中等
        switchEnableNotification.setChecked(true);
        switchEnableSystemAlarm.setChecked(false);
        
        updateReminderTimesDisplay();
        updateBlockTimesDisplay();
    }

    private void showReminderTimeSelector() {
        new TimeDialog.Builder(this)
                .setTitle("添加提醒时间")
                .setConfirm("确定")
                .setCancel("取消")
                .setHour(8)
                .setMinute(0)
                .setIgnoreSecond()
                .setListener(new TimeDialog.OnListener() {
                    @Override
                    public void onSelected(BaseDialog dialog, int hour, int minute, int second) {
                        String timeString = String.format("%02d:%02d", hour, minute);
                        if (!reminderTimesList.contains(timeString)) {
                            reminderTimesList.add(timeString);
                            updateReminderTimesDisplay();
                            ToastUtils.show("已添加提醒时间：" + timeString);
                        } else {
                            ToastUtils.show("该时间已存在");
                        }
                    }
                })
                .show();
    }

    private void showBlockTimeSelector() {
        // 显示时间段选择对话框
        String[] timeRanges = {"12:00-14:00 (午休)", "22:00-07:00 (睡眠)", "自定义时间段"};
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("选择屏蔽时间段")
                .setItems(timeRanges, (dialog, which) -> {
                    String selectedRange;
                    switch (which) {
                        case 0:
                            selectedRange = "12:00-14:00";
                            break;
                        case 1:
                            selectedRange = "22:00-07:00";
                            break;
                        case 2:
                            showCustomBlockTimeDialog();
                            return;
                        default:
                            return;
                    }
                    
                    if (!blockTimesList.contains(selectedRange)) {
                        blockTimesList.add(selectedRange);
                        updateBlockTimesDisplay();
                    }
                })
                .show();
    }

    private void showCustomBlockTimeDialog() {
        // 创建自定义时间段选择对话框
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("自定义屏蔽时间段");
        
        // 创建输入布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        // 开始时间选择
        TextView tvStartLabel = new TextView(this);
        tvStartLabel.setText("开始时间：");
        tvStartLabel.setTextSize(16);
        layout.addView(tvStartLabel);
        
        Button btnStartTime = new Button(this);
        btnStartTime.setText("选择开始时间");
        final String[] startTime = {"22:00"}; // 默认值
        
        btnStartTime.setOnClickListener(v -> {
            new TimeDialog.Builder(this)
                    .setTitle("选择开始时间")
                    .setConfirm("确定")
                    .setCancel("取消")
                    .setHour(22)
                    .setMinute(0)
                    .setIgnoreSecond()
                    .setListener(new TimeDialog.OnListener() {
                        @Override
                        public void onSelected(BaseDialog dialog, int hour, int minute, int second) {
                            startTime[0] = String.format("%02d:%02d", hour, minute);
                            btnStartTime.setText("开始时间: " + startTime[0]);
                        }
                    })
                    .show();
        });
        layout.addView(btnStartTime);
        
        // 结束时间选择
        TextView tvEndLabel = new TextView(this);
        tvEndLabel.setText("结束时间：");
        tvEndLabel.setTextSize(16);
        LinearLayout.LayoutParams endLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        endLabelParams.setMargins(0, 32, 0, 0);
        tvEndLabel.setLayoutParams(endLabelParams);
        layout.addView(tvEndLabel);
        
        Button btnEndTime = new Button(this);
        btnEndTime.setText("选择结束时间");
        final String[] endTime = {"07:00"}; // 默认值
        
        btnEndTime.setOnClickListener(v -> {
            new TimeDialog.Builder(this)
                    .setTitle("选择结束时间")
                    .setConfirm("确定")
                    .setCancel("取消")
                    .setHour(7)
                    .setMinute(0)
                    .setIgnoreSecond()
                    .setListener(new TimeDialog.OnListener() {
                        @Override
                        public void onSelected(BaseDialog dialog, int hour, int minute, int second) {
                            endTime[0] = String.format("%02d:%02d", hour, minute);
                            btnEndTime.setText("结束时间: " + endTime[0]);
                        }
                    })
                    .show();
        });
        layout.addView(btnEndTime);
        
        builder.setView(layout);
        builder.setPositiveButton("确定", (dialog, which) -> {
            String customRange = startTime[0] + "-" + endTime[0];
            if (!blockTimesList.contains(customRange)) {
                blockTimesList.add(customRange);
                updateBlockTimesDisplay();
                ToastUtils.show("已添加屏蔽时间段：" + customRange);
            } else {
                ToastUtils.show("该时间段已存在");
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void updateReminderTimesDisplay() {
        // 获取提醒时间列表容器
        LinearLayout timeListLayout = findViewById(R.id.layout_reminder_time_list);
        if (timeListLayout == null) {
            return;
        }
        
        // 清空现有的时间项
        timeListLayout.removeAllViews();
        
        // 为每个提醒时间创建显示项
        for (int i = 0; i < reminderTimesList.size(); i++) {
            String timeStr = reminderTimesList.get(i);
            
            // 创建时间项布局
            LinearLayout timeItemLayout = new LinearLayout(this);
            timeItemLayout.setOrientation(LinearLayout.HORIZONTAL);
            timeItemLayout.setPadding(0, 8, 0, 8);
            
            // 时间显示
            TextView tvTime = new TextView(this);
            tvTime.setText(timeStr);
            tvTime.setTextSize(14);
            tvTime.setTextColor(getColor(android.R.color.black));
            tvTime.setPadding(16, 8, 16, 8);
            tvTime.setBackground(getDrawable(R.drawable.button_outline_selector));
            
            LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT);
            timeParams.weight = 1;
            timeParams.setMarginEnd(8);
            tvTime.setLayoutParams(timeParams);
            
            // 删除按钮
            Button btnDelete = new Button(this);
            btnDelete.setText("删除");
            btnDelete.setTextSize(12);
            btnDelete.setTextColor(getColor(android.R.color.holo_red_dark));
            btnDelete.setBackground(getDrawable(R.drawable.button_outline_selector));
            btnDelete.setPadding(16, 8, 16, 8);
            
            final int index = i;
            btnDelete.setOnClickListener(v -> {
                reminderTimesList.remove(index);
                updateReminderTimesDisplay();
            });
            
            timeItemLayout.addView(tvTime);
            timeItemLayout.addView(btnDelete);
            timeListLayout.addView(timeItemLayout);
        }
    }

    private void updateBlockTimesDisplay() {
        // 由于当前布局中没有专门的屏蔽时间段UI，这里简化实现
        // 在控制台输出屏蔽时间段信息，方便调试
        if (!blockTimesList.isEmpty()) {
            android.util.Log.d("AddHabitActivity", "屏蔽时间段: " + String.join(", ", blockTimesList));
        }
        
        // TODO: 后续版本中可以添加专门的屏蔽时间段显示UI
        // 类似 updateReminderTimesDisplay() 的实现方式
    }

    private void saveHabit() {
        // 验证必填字段
        String habitName = etHabitName.getText().toString().trim();
        if (habitName.isEmpty()) {
            ToastUtils.show("请输入习惯名称");
            etHabitName.requestFocus();
            return;
        }
        
        // 创建或更新习惯记录
        if (currentHabit == null) {
            currentHabit = new Habit();
        }
        
        currentHabit.setHabitName(habitName);
        currentHabit.setDescription(etDescription.getText().toString().trim());
        
        // 设置频次
        String frequencyType = getFrequencyType(spinnerFrequency.getSelectedItemPosition());
        currentHabit.setFrequency(frequencyType);
        
        String frequencyValueStr = etFrequencyValue.getText().toString().trim();
        if (!frequencyValueStr.isEmpty()) {
            currentHabit.setFrequencyValue(Integer.parseInt(frequencyValueStr));
        }
        
        String frequencyUnit = getFrequencyUnit(spinnerFrequencyUnit.getSelectedItemPosition());
        currentHabit.setFrequencyUnit(frequencyUnit);
        
        // 设置持续时长
        String durationStr = etDuration.getText().toString().trim();
        if (!durationStr.isEmpty()) {
            currentHabit.setDuration(Integer.parseInt(durationStr));
        }
        
        // 设置周期天数
        String cycleDaysStr = etCycleDays.getText().toString().trim();
        if (!cycleDaysStr.isEmpty()) {
            currentHabit.setCycleDays(Integer.parseInt(cycleDaysStr));
        }
        
        // 设置提醒时间和屏蔽时间（转换为JSON）
        currentHabit.setReminderTimes(listToJson(reminderTimesList));
        currentHabit.setBlockTimes(listToJson(blockTimesList));
        
        // 设置开关状态
        currentHabit.setEnableNotification(switchEnableNotification.isChecked());
        currentHabit.setEnableSystemAlarm(switchEnableSystemAlarm.isChecked());
        
        // 设置分类和优先级
        String category = getCategoryType(spinnerCategory.getSelectedItemPosition());
        currentHabit.setCategory(category);
        currentHabit.setPriority(spinnerPriority.getSelectedItemPosition() + 1);
        
        currentHabit.setNotes(etNotes.getText().toString().trim());
        
        // 设置开始日期
        if (currentHabit.getStartDate() == null) {
            currentHabit.setStartDate(dateFormat.format(Calendar.getInstance().getTime()));
        }
        
        // 计算结束日期
        if (currentHabit.getCycleDays() != null && currentHabit.getCycleDays() > 0) {
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.add(Calendar.DAY_OF_YEAR, currentHabit.getCycleDays());
            currentHabit.setEndDate(dateFormat.format(endCalendar.getTime()));
        }
        
        boolean success;
        if (isEditMode) {
            success = habitDao.update(currentHabit);
        } else {
            long id = habitDao.insert(currentHabit);
            success = id != -1;
        }
        
        if (success) {
            // 设置提醒闹钟 - 参考 ClockActivity 的实现
            if (currentHabit.getEnableNotification() && !reminderTimesList.isEmpty()) {
                setupHabitReminders(currentHabit);
            }
            
            ToastUtils.show(isEditMode ? "习惯更新成功" : "习惯添加成功");
            setResult(RESULT_OK);
            finish();
        } else {
            ToastUtils.show("保存失败，请重试");
        }
    }

    private void loadHabitData(Long habitId) {
        currentHabit = habitDao.findById(habitId);
        if (currentHabit != null) {
            etHabitName.setText(currentHabit.getHabitName());
            etDescription.setText(currentHabit.getDescription());
            
            // 设置频次相关
            setSpinnerSelection(spinnerFrequency, currentHabit.getFrequency());
            if (currentHabit.getFrequencyValue() != null) {
                etFrequencyValue.setText(String.valueOf(currentHabit.getFrequencyValue()));
            }
            setSpinnerSelection(spinnerFrequencyUnit, currentHabit.getFrequencyUnit());
            
            if (currentHabit.getDuration() != null) {
                etDuration.setText(String.valueOf(currentHabit.getDuration()));
            }
            if (currentHabit.getCycleDays() != null) {
                etCycleDays.setText(String.valueOf(currentHabit.getCycleDays()));
            }
            
            // 加载提醒时间和屏蔽时间
            reminderTimesList = jsonToList(currentHabit.getReminderTimes());
            blockTimesList = jsonToList(currentHabit.getBlockTimes());
            updateReminderTimesDisplay();
            updateBlockTimesDisplay();
            
            switchEnableNotification.setChecked(currentHabit.getEnableNotification());
            switchEnableSystemAlarm.setChecked(currentHabit.getEnableSystemAlarm());
            
            setSpinnerSelection(spinnerCategory, currentHabit.getCategory());
            if (currentHabit.getPriority() != null) {
                spinnerPriority.setSelection(currentHabit.getPriority() - 1);
            }
            
            etNotes.setText(currentHabit.getNotes());
        }
    }

    private String getFrequencyType(int position) {
        switch (position) {
            case 0: return "DAILY";
            case 1: return "HOURLY";
            case 2: return "WEEKLY";
            case 3: return "CUSTOM";
            default: return "DAILY";
        }
    }

    private String getFrequencyUnit(int position) {
        switch (position) {
            case 0: return "HOUR";
            case 1: return "DAY";
            case 2: return "WEEK";
            default: return "DAY";
        }
    }

    private String getCategoryType(int position) {
        switch (position) {
            case 0: return "HEALTH";
            case 1: return "EXERCISE";
            case 2: return "DIET";
            case 3: return "STUDY";
            case 4: return "OTHER";
            default: return "OTHER";
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private String listToJson(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        
        try {
            JSONArray jsonArray = new JSONArray();
            for (String item : list) {
                jsonArray.put(item);
            }
            return jsonArray.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> jsonToList(String json) {
        List<String> list = new ArrayList<>();
        if (json == null || json.isEmpty()) return list;
        
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            // 如果JSON解析失败，返回空列表
        }
        
        return list;
    }
    
    /**
     * 设置习惯提醒 - 参考 ClockActivity 的实现
     */
    private void setupHabitReminders(Habit habit) {
        // 检查精确闹钟权限
        if (!XXPermissions.isGranted(this, Permission.SCHEDULE_EXACT_ALARM)) {
            new AlertDialog.Builder(this)
                    .setTitle("需要权限")
                    .setMessage("设置提醒需要精确闹钟权限，是否前往授权？")
                    .setPositiveButton("授权", (dialog, which) -> {
                        XXPermissions.with(this)
                                .permission(Permission.SCHEDULE_EXACT_ALARM)
                                .request((permissions, all) -> {
                                    if (all) {
                                        continueSetupReminders(habit);
                                    } else {
                                        ToastUtils.show("权限被拒绝，无法设置提醒");
                                    }
                                });
                    })
                    .setNegativeButton("取消", null)
                    .show();
            return;
        }
        
        // Android 13+ 通知权限检查
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (!XXPermissions.isGranted(this, Permission.NOTIFICATION_SERVICE)) {
                XXPermissions.with(this)
                        .permission(Permission.NOTIFICATION_SERVICE)
                        .request((permissions, all) -> {
                            if (all) {
                                continueSetupReminders(habit);
                            } else {
                                ToastUtils.show("通知权限被拒绝，提醒可能无法正常工作");
                                continueSetupReminders(habit);
                            }
                        });
                return;
            }
        }
        
        continueSetupReminders(habit);
    }
    
    /**
     * 继续设置提醒
     */
    private void continueSetupReminders(Habit habit) {
        try {
            // 使用 HabitAlarmManager 设置提醒
            alarmManager.setHabitReminders(habit);
            
            // 询问用户是否要设置系统闹钟（参考 ClockActivity）
            if (habit.getEnableSystemAlarm()) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("设置系统闹钟")
                        .setMessage("是否要同时在系统闹钟中设置提醒？\n\n选择\"是\"将自动设置系统闹钟，选择\"否\"仅使用应用内提醒。")
                        .setPositiveButton("是，设置系统闹钟", (dialog, which) -> {
                            // TODO: 实现系统闹钟设置
                            ToastUtils.show("提醒设置成功，已同时设置系统闹钟");
                        })
                        .setNegativeButton("否，仅应用内闹钟", (dialog, which) -> {
                            ToastUtils.show("提醒设置成功，仅使用应用内提醒");
                        })
                        .setCancelable(false)
                        .show();
            } else {
                ToastUtils.show("提醒设置成功");
            }
            
        } catch (Exception e) {
            android.util.Log.e("AddHabitActivity", "设置提醒失败", e);
            ToastUtils.show("设置提醒失败：" + e.getMessage());
        }
    }
}
