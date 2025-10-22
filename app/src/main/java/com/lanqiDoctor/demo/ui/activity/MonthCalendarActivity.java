package com.lanqiDoctor.demo.ui.activity;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import main.java.com.lanqiDoctor.demo.ui.adapter.CalendarDayRecordAdapter;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.View.CalendarView;
import com.lanqiDoctor.demo.database.dao.MedicationIntakeRecordDao;
import com.lanqiDoctor.demo.database.entity.MedicationIntakeRecord;
import com.lanqiDoctor.demo.manager.UserStateManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MonthCalendarActivity extends AppCompatActivity {
    private String userId; // 新增成员变量
    private CalendarView calendarView;
    private TextView monthTitle;
    private int currentYear;
    private int currentMonth;

    // 存储日期状态：0=未标记，1=全部完成(绿)，2=未完成(红)
    private Map<String, Integer> dateStatusMap = new HashMap<>();
    private MedicationIntakeRecordDao intakeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.month_calendar_activity);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        calendarView = findViewById(R.id.calendar_view);
        monthTitle = findViewById(R.id.month_title);
        intakeDao = new MedicationIntakeRecordDao(this);

        // 1. 获取当前用户ID（请根据你的实际用户管理类调整）
        this.userId = UserStateManager.getInstance(this).getUserId();

        // 添加调试日志
        android.util.Log.d("MonthCalendarActivity", "获取到用户ID: " + this.userId);
        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);

        // 初始化日历
        refreshCalendar();

        // 设置月份切换按钮
        findViewById(R.id.btn_prev).setOnClickListener(v -> {
            currentMonth--;
            if (currentMonth < 0) {
                currentMonth = 11;
                currentYear--;
            }
            refreshCalendar();
        });

        findViewById(R.id.btn_next).setOnClickListener(v -> {
            currentMonth++;
            if (currentMonth > 11) {
                currentMonth = 0;
                currentYear++;
            }
            refreshCalendar();
        });

        // 设置日期点击监听
        calendarView.setOnDayClickListener(day -> {
            // 1. 计算当天的起止时间
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, day.getYear());
            cal.set(Calendar.MONTH, day.getMonth());
            cal.set(Calendar.DAY_OF_MONTH, day.getDay());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long dayStart = cal.getTimeInMillis();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            long dayEnd = cal.getTimeInMillis();

            // 2. 查询当天所有服药记录
            List<MedicationIntakeRecord> records = intakeDao.findByTimeRange(this.userId,dayStart, dayEnd);

            // 3. 弹窗显示详情
            showDayMedicationDialog(day, records);
        });
    }   


    private void refreshCalendar() {
        // 更新标题
        monthTitle.setText(String.format("%d年%d月", currentYear, currentMonth + 1));

        // 加载并计算本月用药状态
        loadMonthMedicationStatus();

        // 刷新日历并传递状态数据
        calendarView.refreshCalendar(currentYear, currentMonth, dateStatusMap);
    }

 /**
     * 加载并计算本月的用药状态
     */
    private void loadMonthMedicationStatus() {
        dateStatusMap.clear();
        
        // 添加调试日志
        android.util.Log.d("MonthCalendarActivity", "开始加载本月用药状态，userId: " + this.userId);

        // 获取本月开始和结束时间
        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfMonth = cal.getTimeInMillis();

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long endOfMonth = cal.getTimeInMillis();

        android.util.Log.d("MonthCalendarActivity", "查询时间范围: " + 
                formatDateTime(startOfMonth) + " 至 " + formatDateTime(endOfMonth));

        // 查询本月所有用药记录（使用成员变量userId）
        List<MedicationIntakeRecord> records = intakeDao.findByTimeRange(this.userId, startOfMonth, endOfMonth);
        
        android.util.Log.d("MonthCalendarActivity", "查询到本月服药记录数量: " + records.size());

        // 按日期分组记录
        Map<String, List<MedicationIntakeRecord>> recordsByDate = new HashMap<>();
        for (MedicationIntakeRecord record : records) {
            String dateKey = getDateKey(record.getPlannedTime());
            recordsByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(record);
            
            android.util.Log.d("MonthCalendarActivity", "分组记录: " + 
                    record.getMedicationName() + " 日期: " + dateKey + " 状态: " + record.getStatus());
        }

        // 计算每日状态
        Calendar todayCal = Calendar.getInstance();
        long currentTime = System.currentTimeMillis();

        for (int day = 1; day <= cal.getActualMaximum(Calendar.DAY_OF_MONTH); day++) {
            cal.set(currentYear, currentMonth, day, 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long dayStart = cal.getTimeInMillis();
            long dayEnd = dayStart + 24 * 60 * 60 * 1000 - 1;

            String dateKey = String.format("%d-%d-%d", currentYear, currentMonth, day);
            List<MedicationIntakeRecord> dayRecords = recordsByDate.get(dateKey);

            if (dayRecords == null || dayRecords.isEmpty()) {
                // 如果当天没有记录且已经过了当天
                if (currentTime > dayEnd) {
                    dateStatusMap.put(dateKey, 2); // 红色（未服药）
                }
                continue;
            }

            // 计算当天应服药次数和已服药次数
            int totalCount = dayRecords.size();
            int takenCount = 0;

            for (MedicationIntakeRecord record : dayRecords) {
                if (record.getStatus() != null && record.getStatus() == 1) {
                    takenCount++;
                }
            }

            // 判断当天状态
            int status = 0; // 默认无状态
            if (takenCount == totalCount) {
                status = 1; // 绿色（全部完成）
            } else if (currentTime > dayEnd) {
                status = 2; // 红色（未完成）
            }
            
            if (status > 0) {
                dateStatusMap.put(dateKey, status);
                android.util.Log.d("MonthCalendarActivity", "日期状态: " + dateKey + 
                        " 总计:" + totalCount + " 已服:" + takenCount + " 状态:" + status);
            }
        }
        
        android.util.Log.d("MonthCalendarActivity", "本月状态统计完成，有状态的日期数: " + dateStatusMap.size());
    }

    /**
     * 格式化日期时间用于日志显示
     */
    private String formatDateTime(Long timestamp) {
        if (timestamp == null) {
            return "null";
        }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(timestamp));
        } catch (Exception e) {
            return "时间格式错误";
        }
    }

    /**
     * 生成日期键（格式：年-月-日）
     */
    private String getDateKey(long timeMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeMillis);
        return String.format("%d-%d-%d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
    }

    private void showDayMedicationDialog(com.lanqiDoctor.demo.model.Day day, List<MedicationIntakeRecord> records) {
        // 加载自定义布局
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_day_medication_detail, null);

        TextView tvDate = dialogView.findViewById(R.id.tv_dialog_date);
        androidx.recyclerview.widget.RecyclerView rvList = dialogView.findViewById(R.id.rv_medication_list);
        LinearLayout llEmptyHint = dialogView.findViewById(R.id.ll_empty_hint);
        Button btnClose = dialogView.findViewById(R.id.btn_dialog_close);

        // 设置日期
        tvDate.setText(String.format("%d年%d月%d日", day.getYear(), day.getMonth() + 1, day.getDay()));

        // 检查是否有记录
        if (records == null || records.isEmpty()) {
            // 显示空状态
            rvList.setVisibility(android.view.View.GONE);
            llEmptyHint.setVisibility(android.view.View.VISIBLE);
        } else {
            // 显示记录列表
            rvList.setVisibility(android.view.View.VISIBLE);
            llEmptyHint.setVisibility(android.view.View.GONE);
            
            // 设置RecyclerView
            rvList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            CalendarDayRecordAdapter adapter = new CalendarDayRecordAdapter(records);
            rvList.setAdapter(adapter);
        }

        // 构建Dialog
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private String formatTime(Long time) {
        if (time == null) return "无";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new java.util.Date(time));
    }

    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "已服用";
            case 0: return "未服用";
            default: return "未知";
        }
    }
}