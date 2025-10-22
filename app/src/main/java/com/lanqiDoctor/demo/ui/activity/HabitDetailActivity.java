package com.lanqiDoctor.demo.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.hjq.base.BaseActivity;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.dao.HabitDao;
import com.lanqiDoctor.demo.database.entity.Habit;

/**
 * 习惯详情Activity
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class HabitDetailActivity extends BaseActivity {

    private static final String EXTRA_HABIT_ID = "habit_id";
    
    private TextView tvHabitName;
    private TextView tvDescription;
    private TextView tvFrequencyInfo;
    private TextView tvProgress;
    
    private HabitDao habitDao;
    private Habit currentHabit;
    private Long habitId;

    public static void start(BaseActivity activity, Long habitId) {
        Intent intent = new Intent(activity, HabitDetailActivity.class);
        intent.putExtra(EXTRA_HABIT_ID, habitId);
        activity.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_habit_detail;
    }

    @Override
    protected void initView() {
        tvHabitName = findViewById(R.id.tv_habit_name);
        tvDescription = findViewById(R.id.tv_description);
        tvFrequencyInfo = findViewById(R.id.tv_frequency_info);
        tvProgress = findViewById(R.id.tv_progress);
        
        // 设置标题栏返回按钮点击事件
        findViewById(R.id.tb_title).setOnClickListener(v -> finish());
    }

    @Override
    protected void initData() {
        habitDao = new HabitDao(this);
        
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_HABIT_ID)) {
            habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1);
            if (habitId != -1) {
                loadHabitDetail();
            }
        }
    }

    private void loadHabitDetail() {
        currentHabit = habitDao.findById(habitId);
        if (currentHabit != null) {
            // 显示习惯信息
            tvHabitName.setText(currentHabit.getHabitName());
            tvDescription.setText(currentHabit.getDescription());
            
            // 显示频次信息
            String frequencyText = "频次：" + currentHabit.getFrequency();
            if (currentHabit.getFrequencyValue() != null && currentHabit.getFrequencyValue() > 0) {
                frequencyText += "，" + currentHabit.getFrequencyValue() + "次";
            }
            if (currentHabit.getFrequencyUnit() != null && !currentHabit.getFrequencyUnit().isEmpty()) {
                frequencyText += "/" + currentHabit.getFrequencyUnit();
            }
            tvFrequencyInfo.setText(frequencyText);
            
            // 显示进度信息（这里可以后续扩展）
            String progressText = "进度：";
            if (currentHabit.getCompletedDays() != null) {
                progressText += currentHabit.getCompletedDays() + "天";
            } else {
                progressText += "刚开始";
            }
            tvProgress.setText(progressText);
        }
    }

    // 处理返回按钮点击
    public void onLeftClick(View view) {
        finish();
    }
}
