package com.lanqiDoctor.demo.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hjq.base.BaseActivity;
import com.hjq.toast.ToastUtils;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.dao.HabitDao;
import com.lanqiDoctor.demo.database.entity.Habit;
import com.lanqiDoctor.demo.ui.adapter.HabitAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 习惯培养Activity
 * 
 * 功能包括：
 * - 健康习惯制定
 * - 习惯打卡记录
 * - 习惯统计分析
 * - 习惯提醒设置
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class HabitBuildingActivity extends BaseActivity {

    private RecyclerView rvHabitList; // 习惯列表的RecyclerView
    private LinearLayout tvEmptyView; // 空数据的提醒视图
    private FloatingActionButton fabAddHabit; // 添加习惯的浮动按钮
    private CardView cardAiRecommendation; // AI推荐习惯卡片

    private HabitAdapter adapter; // 习惯列表的适配器
    private HabitDao habitDao; // 习惯数据的信息库
    private List<Habit> habitList; // 习惯数据的列表

    private static final int REQUEST_ADD_HABIT = 1004; // 添加习惯请求码
    private static final int REQUEST_AI_RECOMMENDATION = 1005; // AI推荐请求码

    @Override
    protected int getLayoutId() {
        return R.layout.activity_habit_building;
    }

    @Override
    protected void initView() {
        rvHabitList = findViewById(R.id.rv_habit_list);
        tvEmptyView = findViewById(R.id.tv_empty_view);
        fabAddHabit = findViewById(R.id.fab_add_habit);
        cardAiRecommendation = findViewById(R.id.card_ai_recommendation);

        // 设置RecyclerView
        rvHabitList.setLayoutManager(new LinearLayoutManager(this));
        habitList = new ArrayList<>();
        adapter = new HabitAdapter(habitList);
        rvHabitList.setAdapter(adapter);

        // 设置点击监听
        setOnClickListener(fabAddHabit, cardAiRecommendation);
        
        // 设置标题栏返回按钮点击事件
        findViewById(R.id.tb_title).setOnClickListener(v -> finish());
    }

    @Override
    protected void initData() {
        habitDao = new HabitDao(this);

        // 设置适配器监听器
        adapter.setOnItemClickListener((habit, position) -> {
            Intent intent = new Intent(this, HabitDetailActivity.class);
            intent.putExtra("habit_id", habit.getId());
            startActivity(intent);
        });

        adapter.setOnItemLongClickListener((habit, position) -> {
            showHabitOperationDialog(habit, position);
            return true;
        });

        loadHabitData();
    }

    @Override
    public void onClick(View view) {
        if (view == fabAddHabit) {
            Intent intent = new Intent(this, AddHabitActivity.class);
            startActivityForResult(intent, REQUEST_ADD_HABIT);
        } else if (view == cardAiRecommendation) {
            // 点击AI推荐习惯卡片，跳转到AI推荐页面
            Intent intent = new Intent(this, HabitAiRecommendationActivity.class);
            startActivityForResult(intent, REQUEST_AI_RECOMMENDATION);
        } else if (view.getId() == R.id.tb_title) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_ADD_HABIT || requestCode == REQUEST_AI_RECOMMENDATION) 
            && resultCode == RESULT_OK) {
            // 添加习惯后刷新数据
            loadHabitData();
            ToastUtils.show("习惯记录已保存");
        }
    }

    /**
     * 加载习惯数据
     */
    private void loadHabitData() {
        List<Habit> allHabits = habitDao.findAll();
        habitList.clear();
        habitList.addAll(allHabits);
        adapter.notifyDataSetChanged();

        // 显示/隐藏空状态
        if (habitList.isEmpty()) {
            rvHabitList.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.VISIBLE);
        } else {
            rvHabitList.setVisibility(View.VISIBLE);
            tvEmptyView.setVisibility(View.GONE);
        }
    }

    /**
     * 显示习惯操作对话框
     */
    private void showHabitOperationDialog(Habit habit, int position) {
        String[] options = {"查看详情", "编辑", "打卡", habit.getIsActive() ? "暂停" : "恢复", "删除"};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(habit.getHabitName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // 查看详情
                            viewHabitDetail(habit);
                            break;
                        case 1: // 编辑
                            editHabit(habit);
                            break;
                        case 2: // 打卡
                            checkInHabit(habit);
                            break;
                        case 3: // 暂停/恢复
                            toggleHabitActive(habit);
                            break;
                        case 4: // 删除
                            showDeleteConfirmDialog(habit);
                            break;
                    }
                })
                .show();
    }

    /**
     * 查看习惯详情
     */
    private void viewHabitDetail(Habit habit) {
        Intent intent = new Intent(this, HabitDetailActivity.class);
        intent.putExtra("habit_id", habit.getId());
        startActivity(intent);
    }

    /**
     * 编辑习惯
     */
    private void editHabit(Habit habit) {
        Intent editIntent = new Intent(this, AddHabitActivity.class);
        editIntent.putExtra("habit_id", habit.getId());
        editIntent.putExtra("is_edit", true);
        startActivityForResult(editIntent, REQUEST_ADD_HABIT);
    }

    /**
     * 习惯打卡
     */
    private void checkInHabit(Habit habit) {
        if (!habit.getIsActive()) {
            ToastUtils.show("该习惯已暂停，无法打卡");
            return;
        }

        // 更新打卡次数和坚持天数
        int newCheckIns = (habit.getTotalCheckIns() != null ? habit.getTotalCheckIns() : 0) + 1;
        int newCompletedDays = habit.getCompletedDays() != null ? habit.getCompletedDays() : 0;
        
        // 简单逻辑：假设每天最多打卡一次
        if (newCheckIns > newCompletedDays) {
            newCompletedDays++;
        }

        boolean success = habitDao.updateCheckIn(habit.getId(), newCompletedDays, newCheckIns);
        if (success) {
            ToastUtils.show("打卡成功！已坚持 " + newCompletedDays + " 天");
            loadHabitData(); // 刷新数据
        } else {
            ToastUtils.show("打卡失败，请重试");
        }
    }

    /**
     * 切换习惯激活状态
     */
    private void toggleHabitActive(Habit habit) {
        habit.setIsActive(!habit.getIsActive());
        boolean success = habitDao.update(habit);
        if (success) {
            String message = habit.getIsActive() ? "习惯已恢复" : "习惯已暂停";
            ToastUtils.show(message);
            loadHabitData(); // 刷新数据
        } else {
            ToastUtils.show("操作失败，请重试");
        }
    }

    /**
     * 显示删除确认对话框
     */
    private void showDeleteConfirmDialog(Habit habit) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除 \"" + habit.getHabitName() + "\" 吗？\n\n删除后将无法恢复所有相关数据。")
                .setPositiveButton("删除", (dialog, which) -> {
                    habitDao.delete(habit.getId());
                    loadHabitData(); // 重新加载数据
                    ToastUtils.show("已删除 \"" + habit.getHabitName() + "\"");
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
