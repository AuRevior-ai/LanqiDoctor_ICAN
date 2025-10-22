package com.lanqiDoctor.demo.ui.activity;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.base.BaseActivity;
import com.hjq.toast.ToastUtils;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.util.ChatLlmUtil;
import com.lanqiDoctor.demo.http.api.ChatMessage;
import com.lanqiDoctor.demo.http.api.ChatMessage;
import com.lanqiDoctor.demo.database.dao.HabitDao;
import com.lanqiDoctor.demo.database.dao.MedicalHistoryDao;
import com.lanqiDoctor.demo.database.dao.HealthInfoDao;
import com.lanqiDoctor.demo.database.entity.Habit;
import com.lanqiDoctor.demo.database.entity.MedicalHistory;
import com.lanqiDoctor.demo.database.entity.HealthInfo;

import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import com.lanqiDoctor.demo.ui.adapter.RecommendedHabitAdapter;

/**
 * AI推荐习惯Activity
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class HabitAiRecommendationActivity extends BaseActivity {

    private TextView tvHealthSummary;
    private LinearLayout layoutLoading;
    private TextView tvLoadingText;
    private LinearLayout layoutError;
    private TextView tvErrorMessage;
    private Button btnRetry;
    private Button btnRegenerate;
    private Button btnAddSelected;
    private RecyclerView rvRecommendedHabits;

    private MedicalHistoryDao medicalHistoryDao;
    private HealthInfoDao healthInfoDao;
    private HabitDao habitDao;
    private ChatLlmUtil chatLlmUtil;
    private RecommendedHabitAdapter recommendedAdapter;

    private List<Habit> recommendedHabits = new ArrayList<>();
    private boolean isGenerating = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_habit_ai_recommendation;
    }

    @Override
    protected void initView() {
        tvHealthSummary = findViewById(R.id.tv_health_summary);
        layoutLoading = findViewById(R.id.layoutLoading);
        tvLoadingText = findViewById(R.id.tv_loading_text);
        layoutError = findViewById(R.id.layoutError);
        tvErrorMessage = findViewById(R.id.tv_error_message);
        btnRetry = findViewById(R.id.btn_retry);
        btnRegenerate = findViewById(R.id.btn_regenerate);
        btnAddSelected = findViewById(R.id.btn_add_selected);
        rvRecommendedHabits = findViewById(R.id.recyclerViewRecommendations);

        // 创建支持嵌套滚动的LinearLayoutManager  
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true); // 启用自动测量
        rvRecommendedHabits.setLayoutManager(layoutManager);
        rvRecommendedHabits.setHasFixedSize(false); // 允许RecyclerView根据内容调整大小
        // 在NestedScrollView中，RecyclerView应该禁用自己的滚动
        rvRecommendedHabits.setNestedScrollingEnabled(false);

        // 初始化推荐习惯适配器
        recommendedAdapter = new RecommendedHabitAdapter(this);
        rvRecommendedHabits.setAdapter(recommendedAdapter);
        
        // 添加布局监听器来监控RecyclerView的布局变化
        rvRecommendedHabits.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, 
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                android.util.Log.d("HabitAiRecommendation", "RecyclerView布局变化 - 新尺寸: " + 
                        (right - left) + "x" + (bottom - top) + ", 旧尺寸: " + 
                        (oldRight - oldLeft) + "x" + (oldBottom - oldTop));
            }
        });
        
        // 设置适配器监听器
        recommendedAdapter.setOnItemActionListener(new RecommendedHabitAdapter.OnItemActionListener() {
            @Override
            public void onItemClick(Habit habit, int position) {
                // 点击习惯项显示详情或切换选中状态
                toggleHabitSelection(position);
            }

            @Override
            public void onItemSelected(Habit habit, int position, boolean isSelected) {
                updateSelectedCount();
            }
        });

        setOnClickListener(btnRetry, btnRegenerate, btnAddSelected);
        
        // 设置标题栏返回按钮点击事件
        findViewById(R.id.tb_title).setOnClickListener(v -> finish());
        
        // 添加长按标题来切换全选的功能
        findViewById(R.id.tb_title).setOnLongClickListener(v -> {
            toggleSelectAll();
            return true;
        });
    }

    @Override
    protected void initData() {
        medicalHistoryDao = new MedicalHistoryDao(this);
        healthInfoDao = new HealthInfoDao(this);
        habitDao = new HabitDao(this);
        chatLlmUtil = new ChatLlmUtil(new Handler(Looper.getMainLooper()), this);

        loadUserHealthInfo();
        // 自动开始生成AI推荐
        generateAiRecommendations();
    }

    @Override
    public void onClick(View view) {
        if (view == btnRetry) {
            generateAiRecommendations();
        } else if (view == btnRegenerate) {
            generateAiRecommendations();
        } else if (view == btnAddSelected) {
            saveSelectedRecommendedHabits();
        }
    }

    /**
     * 加载用户健康信息
     */
    private void loadUserHealthInfo() {
        StringBuilder userInfo = new StringBuilder();
        
        // 获取最新的健康信息
        List<HealthInfo> healthInfoList = healthInfoDao.findLatest(1);
        if (!healthInfoList.isEmpty()) {
            HealthInfo latest = healthInfoList.get(0);
            userInfo.append("健康信息：\n");
            if (latest.getAge() != null) {
                userInfo.append("年龄：").append(latest.getAge()).append("岁\n");
            }
            if (latest.getHeight() != null) {
                userInfo.append("身高：").append(latest.getHeight()).append("cm\n");
            }
            if (latest.getWeight() != null) {
                userInfo.append("体重：").append(latest.getWeight()).append("kg\n");
                if (latest.getBmi() != null) {
                    userInfo.append("BMI：").append(String.format("%.1f", latest.getBmi())).append("\n");
                }
            }
            if (latest.getHeartRate() != null) {
                userInfo.append("心率：").append(latest.getHeartRate()).append("次/分钟\n");
            }
            if (latest.getSystolicPressure() != null && latest.getDiastolicPressure() != null) {
                userInfo.append("血压：").append(latest.getSystolicPressure()).append("/")
                        .append(latest.getDiastolicPressure()).append("mmHg\n");
            }
        }

        // 获取既往病史
        List<MedicalHistory> medicalHistories = medicalHistoryDao.findAll();
        if (!medicalHistories.isEmpty()) {
            userInfo.append("\n既往病史：\n");
            for (MedicalHistory history : medicalHistories) {
                userInfo.append("- ").append(history.getDiseaseName());
                if (history.getTreatmentStatus() != null) {
                    userInfo.append("（").append(history.getTreatmentStatus()).append("）");
                }
                userInfo.append("\n");
            }
        }

        if (userInfo.length() == 0) {
            userInfo.append("暂无健康信息，建议先完善个人健康档案");
        }

        tvHealthSummary.setText(userInfo.toString());
    }

    /**
     * 生成AI推荐
     */
    private void generateAiRecommendations() {
        if (isGenerating) {
            ToastUtils.show("正在生成推荐，请稍候...");
            return;
        }

        isGenerating = true;
        showLoadingState("AI正在分析您的健康状况...");

        // 清空之前的推荐和选择
        recommendedHabits.clear();
        if (recommendedAdapter != null) {
            recommendedAdapter.setData(recommendedHabits);
        }

        // 构建AI提示词
        String userHealthInfo = tvHealthSummary.getText().toString();
        String prompt = buildAiPrompt(userHealthInfo);

        // 构造对话消息
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", "你是一个专业的健康顾问，根据用户的健康信息为其推荐适合的健康习惯。"));
        messages.add(new ChatMessage("user", prompt));

        chatLlmUtil.sendSyncRequest(messages, new ChatLlmUtil.LlmCallback() {
            @Override
            public void onAssistantMessage(String content) {
                runOnUiThread(() -> {
                    handleAiResponse(content);
                    isGenerating = false;
                    showSuccessState();
                });
            }

            @Override
            public void onError(String errorMsg) {
                runOnUiThread(() -> {
                    showErrorState("生成推荐失败：" + errorMsg);
                    isGenerating = false;
                    ToastUtils.show("AI推荐生成失败，请重试");
                });
            }

            @Override
            public void onComplete() {
                // 完成回调
            }

            @Override
            public void onStreamUpdate(String content) {
                // 流式更新回调，这里不使用
            }
        });
    }

    /**
     * 构建AI提示词
     */
    private String buildAiPrompt(String userHealthInfo) {
        return "作为一名专业的健康顾问，请根据以下用户的健康信息，为其推荐3-5个适合的健康习惯。\n\n" +
                "用户健康信息：\n" + userHealthInfo + "\n\n" +
                "请按照以下格式严格输出推荐的习惯（这个格式很重要，将用于程序解析）：\n\n" +
                "HABIT_START\n" +
                "习惯名称：[习惯名称]\n" +
                "描述：[详细描述]\n" +
                "频次：[DAILY/HOURLY/WEEKLY]\n" +
                "频次值：[数字]\n" +
                "频次单位：[HOUR/DAY/WEEK]\n" +
                "持续时长：[分钟数]\n" +
                "周期天数：[天数]\n" +
                "分类：[HEALTH/EXERCISE/DIET/STUDY/OTHER]\n" +
                "优先级：[1-5]\n" +
                "提醒时间：[HH:MM,HH:MM]（多个时间用逗号分隔）\n" +
                "屏蔽时间：[HH:MM-HH:MM,HH:MM-HH:MM]（多个时间段用逗号分隔）\n" +
                "备注：[额外说明]\n" +
                "HABIT_END\n\n" +
                "请为每个推荐的习惯都使用上述格式。\n\n" +
                "推荐要求：\n" +
                "1. 根据用户的健康状况和病史，推荐最适合的习惯\n" +
                "2. 考虑用户的年龄、BMI等因素\n" +
                "3. 如果有慢性病，要特别关注相关的健康习惯\n" +
                "4. 习惯要具体可执行，时间安排要合理\n" +
                "5. 优先级要根据用户健康需求确定\n" +
                "6. 提醒时间要适合大多数人的作息\n" +
                "7. 屏蔽时间要避开休息时间";
    }

    /**
     * 处理AI响应
     */
    private void handleAiResponse(String response) {
        // 解析AI响应，提取习惯信息
        recommendedHabits = parseHabitsFromAiResponse(response);
        
        if (recommendedHabits.isEmpty()) {
            showErrorState("未能从AI响应中解析出习惯信息，请重新生成");
            return;
        }

        // 显示推荐的习惯
        displayRecommendedHabits();
    }

    /**
     * 从AI响应中解析习惯信息
     */
    private List<Habit> parseHabitsFromAiResponse(String response) {
        List<Habit> habits = new ArrayList<>();
        
        android.util.Log.d("HabitAiRecommendation", "解析AI响应: " + response);
        
        // 使用正则表达式提取习惯信息 - 匹配实际AI返回格式
        Pattern pattern = Pattern.compile("HABIT_START\\s*([\\s\\S]*?)HABIT_END", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(response);
        
        while (matcher.find()) {
            String habitInfo = matcher.group(1);
            android.util.Log.d("HabitAiRecommendation", "解析到习惯信息: " + habitInfo);
            Habit habit = parseHabitInfo(habitInfo);
            if (habit != null) {
                habits.add(habit);
                android.util.Log.d("HabitAiRecommendation", "成功解析习惯: " + habit.getHabitName());
            }
        }
        
        android.util.Log.d("HabitAiRecommendation", "总共解析到 " + habits.size() + " 个习惯");
        return habits;
    }

    /**
     * 解析单个习惯信息
     */
    private Habit parseHabitInfo(String habitInfo) {
        try {
            Habit habit = new Habit();
            
            String[] lines = habitInfo.split("\\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (line.startsWith("习惯名称：")) {
                    habit.setHabitName(line.substring("习惯名称：".length()).trim());
                } else if (line.startsWith("描述：")) {
                    habit.setDescription(line.substring("描述：".length()).trim());
                } else if (line.startsWith("频次：")) {
                    habit.setFrequency(line.substring("频次：".length()).trim());
                } else if (line.startsWith("频次值：")) {
                    try {
                        habit.setFrequencyValue(Integer.parseInt(line.substring("频次值：".length()).trim()));
                    } catch (NumberFormatException e) {
                        habit.setFrequencyValue(1);
                    }
                } else if (line.startsWith("频次单位：")) {
                    habit.setFrequencyUnit(line.substring("频次单位：".length()).trim());
                } else if (line.startsWith("持续时长：")) {
                    try {
                        habit.setDuration(Integer.parseInt(line.substring("持续时长：".length()).trim()));
                    } catch (NumberFormatException e) {
                        habit.setDuration(30);
                    }
                } else if (line.startsWith("周期天数：")) {
                    try {
                        habit.setCycleDays(Integer.parseInt(line.substring("周期天数：".length()).trim()));
                    } catch (NumberFormatException e) {
                        habit.setCycleDays(21);
                    }
                } else if (line.startsWith("分类：")) {
                    habit.setCategory(line.substring("分类：".length()).trim());
                } else if (line.startsWith("优先级：")) {
                    try {
                        habit.setPriority(Integer.parseInt(line.substring("优先级：".length()).trim()));
                    } catch (NumberFormatException e) {
                        habit.setPriority(3);
                    }
                } else if (line.startsWith("提醒时间：")) {
                    String reminderTimes = line.substring("提醒时间：".length()).trim();
                    habit.setReminderTimes(convertTimesToJson(reminderTimes));
                } else if (line.startsWith("屏蔽时间：")) {
                    String blockTimes = line.substring("屏蔽时间：".length()).trim();
                    habit.setBlockTimes(convertTimesToJson(blockTimes));
                } else if (line.startsWith("备注：")) {
                    habit.setNotes(line.substring("备注：".length()).trim());
                }
            }
            
            // 设置默认值
            if (habit.getHabitName() == null || habit.getHabitName().isEmpty()) {
                return null; // 习惯名称是必须的
            }
            
            if (habit.getFrequency() == null) habit.setFrequency("DAILY");
            if (habit.getFrequencyValue() == null) habit.setFrequencyValue(1);
            if (habit.getFrequencyUnit() == null) habit.setFrequencyUnit("DAY");
            if (habit.getDuration() == null) habit.setDuration(30);
            if (habit.getCycleDays() == null) habit.setCycleDays(21);
            if (habit.getCategory() == null) habit.setCategory("HEALTH");
            if (habit.getPriority() == null) habit.setPriority(3);
            
            // 设置开始日期
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            habit.setStartDate(dateFormat.format(java.util.Calendar.getInstance().getTime()));
            
            // 计算结束日期
            java.util.Calendar endCalendar = java.util.Calendar.getInstance();
            endCalendar.add(java.util.Calendar.DAY_OF_YEAR, habit.getCycleDays());
            habit.setEndDate(dateFormat.format(endCalendar.getTime()));
            
            return habit;
            
        } catch (Exception e) {
            android.util.Log.e("HabitAiRecommendation", "解析习惯信息失败", e);
            return null;
        }
    }

    /**
     * 将时间字符串转换为JSON格式
     */
    private String convertTimesToJson(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            org.json.JSONArray jsonArray = new org.json.JSONArray();
            String[] times = timeStr.split(",");
            for (String time : times) {
                time = time.trim();
                if (!time.isEmpty()) {
                    jsonArray.put(time);
                }
            }
            return jsonArray.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 显示推荐的习惯
     */
    private void displayRecommendedHabits() {
        if (recommendedAdapter != null) {
            android.util.Log.d("HabitAiRecommendation", "开始显示推荐习惯");
            
            // 检查RecyclerView的当前状态
            android.util.Log.d("HabitAiRecommendation", "RecyclerView - 可见性: " + rvRecommendedHabits.getVisibility() + 
                    ", 宽度: " + rvRecommendedHabits.getWidth() + ", 高度: " + rvRecommendedHabits.getHeight());
            android.util.Log.d("HabitAiRecommendation", "RecyclerView - LayoutParams: " + rvRecommendedHabits.getLayoutParams());
            
            recommendedAdapter.setData(recommendedHabits);
            
            // 动态调整RecyclerView高度以确保所有item都能显示
            rvRecommendedHabits.post(() -> {
                adjustRecyclerViewHeight();
                
                // 延迟检查最终状态
                rvRecommendedHabits.postDelayed(() -> {
                    checkRecyclerViewState();
                }, 500);
            });
            
            updateSelectedCount();
            ToastUtils.show("成功生成 " + recommendedHabits.size() + " 个推荐习惯");
        }
    }
    
    /**
     * 检查RecyclerView的最终状态
     */
    private void checkRecyclerViewState() {
        android.util.Log.d("HabitAiRecommendation", "=== RecyclerView最终状态检查 ===");
        android.util.Log.d("HabitAiRecommendation", "RecyclerView - 可见性: " + rvRecommendedHabits.getVisibility() + 
                ", 宽度: " + rvRecommendedHabits.getWidth() + ", 高度: " + rvRecommendedHabits.getHeight());
        android.util.Log.d("HabitAiRecommendation", "RecyclerView - 子View数量: " + rvRecommendedHabits.getChildCount());
        
        for (int i = 0; i < rvRecommendedHabits.getChildCount(); i++) {
            View child = rvRecommendedHabits.getChildAt(i);
            android.util.Log.d("HabitAiRecommendation", "子View " + i + " - 可见性: " + child.getVisibility() + 
                    ", 宽度: " + child.getWidth() + ", 高度: " + child.getHeight() +
                    ", 位置: (" + child.getLeft() + ", " + child.getTop() + ", " + child.getRight() + ", " + child.getBottom() + ")");
        }
        
        android.util.Log.d("HabitAiRecommendation", "适配器项目数量: " + recommendedAdapter.getItemCount());
    }

    /**
     * 动态调整RecyclerView高度
     */
    private void adjustRecyclerViewHeight() {
        if (recommendedHabits == null || recommendedHabits.isEmpty()) {
            android.util.Log.w("HabitAiRecommendation", "无习惯数据，跳过高度调整");
            return;
        }
        
        try {
            android.util.Log.d("HabitAiRecommendation", "开始调整RecyclerView高度");
            
            // 检查当前RecyclerView状态
            ViewGroup.LayoutParams currentParams = rvRecommendedHabits.getLayoutParams();
            android.util.Log.d("HabitAiRecommendation", "当前参数 - 宽度: " + currentParams.width + ", 高度: " + currentParams.height);
            android.util.Log.d("HabitAiRecommendation", "当前实际尺寸 - 宽度: " + rvRecommendedHabits.getWidth() + ", 高度: " + rvRecommendedHabits.getHeight());
            
            // 使用NestedScrollView时，应该让RecyclerView完全展开
            ViewGroup.LayoutParams params = rvRecommendedHabits.getLayoutParams();
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            rvRecommendedHabits.setLayoutParams(params);
            
            // 禁用RecyclerView的滚动，让NestedScrollView处理
            rvRecommendedHabits.setNestedScrollingEnabled(false);
            
            android.util.Log.d("HabitAiRecommendation", "设置RecyclerView为WRAP_CONTENT高度，禁用内部滚动");
            
            // 强制请求布局
            rvRecommendedHabits.requestLayout();
            
        } catch (Exception e) {
            android.util.Log.e("HabitAiRecommendation", "调整RecyclerView高度失败", e);
        }
    }

    /**
     * 切换习惯选中状态
     */
    private void toggleHabitSelection(int position) {
        // 这个方法由适配器的复选框处理，这里可以添加额外的逻辑
        updateSelectedCount();
    }

    /**
     * 更新选中数量显示
     */
    private void updateSelectedCount() {
        if (recommendedAdapter != null) {
            int selectedCount = recommendedAdapter.getSelectedCount();
            int totalCount = recommendedHabits.size();
            
            if (selectedCount > 0) {
                btnAddSelected.setText("添加选中的习惯 (" + selectedCount + "/" + totalCount + ")");
                btnAddSelected.setEnabled(true);
            } else {
                btnAddSelected.setText("添加选中的习惯");
                btnAddSelected.setEnabled(false);
            }
        }
    }

    /**
     * 切换全选状态
     */
    private void toggleSelectAll() {
        if (recommendedAdapter == null || recommendedHabits.isEmpty()) {
            ToastUtils.show("没有可选择的习惯");
            return;
        }
        
        int selectedCount = recommendedAdapter.getSelectedCount();
        int totalCount = recommendedHabits.size();
        
        boolean selectAll = selectedCount < totalCount;
        recommendedAdapter.selectAll(selectAll);
        updateSelectedCount();
        
        String message = selectAll ? "已全选所有习惯" : "已取消全选";
        ToastUtils.show(message);
    }

    /**
     * 保存所有推荐的习惯
     */
    private void saveAllRecommendedHabits() {
        if (recommendedHabits.isEmpty()) {
            ToastUtils.show("没有待保存的习惯");
            return;
        }

        int successCount = 0;
        for (Habit habit : recommendedHabits) {
            long result = habitDao.insert(habit);
            if (result != -1) {
                successCount++;
            }
        }

        if (successCount == recommendedHabits.size()) {
            ToastUtils.show("成功保存 " + successCount + " 个习惯");
            setResult(RESULT_OK);
            finish();
        } else {
            ToastUtils.show("已保存 " + successCount + "/" + recommendedHabits.size() + " 个习惯");
        }
    }

    /**
     * 保存选中的推荐习惯
     */
    private void saveSelectedRecommendedHabits() {
        if (recommendedAdapter == null) {
            ToastUtils.show("没有推荐数据");
            return;
        }
        
        List<Habit> selectedHabits = recommendedAdapter.getSelectedHabits();
        if (selectedHabits.isEmpty()) {
            ToastUtils.show("请先选择要添加的习惯");
            return;
        }

        int successCount = 0;
        int failCount = 0;
        
        for (Habit habit : selectedHabits) {
            try {
                long result = habitDao.insert(habit);
                if (result != -1) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                failCount++;
                android.util.Log.e("HabitAiRecommendation", "保存习惯失败: " + habit.getHabitName(), e);
            }
        }

        // 显示结果
        if (failCount == 0) {
            ToastUtils.show("成功添加 " + successCount + " 个习惯");
            setResult(RESULT_OK);
            finish();
        } else {
            String message = "成功添加 " + successCount + " 个习惯";
            if (failCount > 0) {
                message += "，" + failCount + " 个添加失败";
            }
            ToastUtils.show(message);
            
            if (successCount > 0) {
                setResult(RESULT_OK);
            }
        }
    }

    /**
     * 显示加载状态
     */
    private void showLoadingState(String message) {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        findViewById(R.id.cardRecommendations).setVisibility(View.GONE);
        btnRegenerate.setVisibility(View.GONE);
        btnAddSelected.setVisibility(View.GONE);
        tvLoadingText.setText(message);
    }

    /**
     * 显示成功状态
     */
    private void showSuccessState() {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        findViewById(R.id.cardRecommendations).setVisibility(View.VISIBLE);
        btnRegenerate.setVisibility(View.VISIBLE);
        btnAddSelected.setVisibility(View.VISIBLE);
    }

    /**
     * 显示错误状态
     */
    private void showErrorState(String errorMessage) {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        findViewById(R.id.cardRecommendations).setVisibility(View.GONE);
        btnRegenerate.setVisibility(View.GONE);
        btnAddSelected.setVisibility(View.GONE);
        tvErrorMessage.setText(errorMessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatLlmUtil != null) {
            chatLlmUtil.shutdown();
        }
    }
}
