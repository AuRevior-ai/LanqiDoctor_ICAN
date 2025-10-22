package com.lanqiDoctor.demo.ui.activity;

import static android.os.Looper.getMainLooper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.base.BaseActivity;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.entity.HealthInfo;
import com.lanqiDoctor.demo.http.api.ChatMessage;
import com.lanqiDoctor.demo.manager.DatabaseManager;
import com.lanqiDoctor.demo.util.ChatLlmUtil;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.ArrayList;
import java.util.List;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import android.text.Html;
import android.text.Spanned;

import java.util.ArrayList;
import java.util.List;

/**
 * 个人健康画像Activity
 * 
 * 功能包括：
 * - 个人健康数据分析
 * - 健康风险评估
 * - 健康建议生成
 * - 健康趋势图表
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class HealthProfileActivity extends BaseActivity {

    private TextView tvReport;
    private ProgressBar progressBar;
    private ChatLlmUtil chatLlmUtil;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_health_profile;
    }

    @Override
    protected void initView() {
        TextView titleText = findViewById(R.id.tv_title);
        if (titleText != null) {
            titleText.setText("个人健康画像");
        }
        
        // 返回按钮
        ImageView backButton = findViewById(R.id.iv_back);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // 初始化健康画像显示控件
        tvReport = findViewById(R.id.tv_health_profile_content);
        progressBar = findViewById(R.id.progress_bar_health_profile);
    }

    @Override
    protected void initData() {
        // 初始化ChatLlmUtil
        chatLlmUtil = new ChatLlmUtil(new Handler(getMainLooper()), this);
        
        // 生成个人健康画像
        generateHealthProfile();
    }

    /**
     * 生成个人健康画像
     */
    private void generateHealthProfile() {
        if (progressBar != null) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }
        if (tvReport != null) {
            tvReport.setText("正在生成个人健康画像，请稍候...");
        }

        // 获取本地最新健康数据
        HealthInfo healthInfo = DatabaseManager.getInstance(this).getLatestHealthInfo();

        StringBuilder userData = new StringBuilder();
        if (healthInfo != null) {
            String steps = healthInfo.getSteps() != null ? healthInfo.getSteps().toString() : "无数据";
            String heartRate = healthInfo.getHeartRate() != null ? healthInfo.getHeartRate().toString() : "无数据";
            String sleepDuration = healthInfo.getSleepDuration() != null ? healthInfo.getSleepDuration().toString() : "无数据";

            // 添加日志输出
            Log.d("HealthProfile", "今日步数：" + steps + "步");
            Log.d("HealthProfile", "心率：" + heartRate + "bpm");
            Log.d("HealthProfile", "睡眠时长：" + sleepDuration + "小时");

            userData.append("今日步数：").append(steps).append("步，")
                    .append("心率：").append(heartRate).append("bpm，")
                    .append("睡眠时长：").append(sleepDuration).append("小时。");
        } else {
            Log.d("HealthProfile", "今日步数、心率、睡眠等数据暂不可用。");
            userData.append("今日步数、心率、睡眠等数据暂不可用。");
        }

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", "你是蓝歧医童，一个具备专业医学背景的智能医疗助手，致力于为用户提供可靠、安全、个性化的医疗服务。你的核心任务包括：\\n" +
                "- 提供专业的医疗建议和健康状况评估\\n" +
                "- 基于健康数据生成个人健康画像分析\\n" +
                "- 健康风险识别与预警提醒\\n" +
                "- 根据具体情况生成个性化健康改善建议\\n" +
                "- 长期健康趋势分析与目标制定\\n" +
                "\\n" +
                "# 思考路径\\n" +
                "请根据用户输入的健康数据，沿如下路径思考并输出：\\n" +
                "1. 分析当前健康状况（步数、心率、睡眠等指标评估）\\n" +
                "2. 识别潜在健康风险点和优势表现\\n" +
                "3. 制定个性化健康改善建议和目标\\n" +
                "4. 提供长期健康管理规划\\n" +
                "\\n" +
                "# 个性化要求\\n" +
                "你需要结合用户的健康数据特征提供个性化分析，不得给出模板式回答。重点关注：\\n" +
                "- 运动量是否充足（世卫组织建议成年人每日至少6000-8000步）\\n" +
                "- 心率是否在正常范围（静息心率60-100bpm）\\n" +
                "- 睡眠质量是否达标（成年人建议7-9小时）\\n" +
                "- 各项指标之间的关联性分析\\n" +
                "\\n" +
                "# 角色设定\\n" +
                "1. 你是蓝歧医童，一位专业的健康管理顾问\\n" +
                "2. 你站在用户角度，提供科学、实用、易懂的健康画像分析\\n" +
                "\\n" +
                "# 健康画像生成能力\\n" +
                "输入：用户的健康监测数据（步数、心率、睡眠等）\\n" +
                "输出：生成一份结构清晰的个人健康画像，包含：\\n" +
                "- 健康状况综合评分\\n" +
                "- 各项指标详细分析\\n" +
                "- 健康风险识别与预警\\n" +
                "- 个性化改善建议\\n" +
                "- 健康目标制定\\n" +
                "\\n" +
                "# 输出风格\\n" +
                "- 语言风格：专业、准确、亲切、通俗易懂\\n" +
                "- 输出形式：结构化文字描述，不超过600字\\n" +
                "- 输出结构：使用小标题（如健康评分、运动分析、睡眠评估、改善建议等）增强可读性\\n" +
                "- 适当使用emoji让内容更生动有趣\\n" +
                "\\n" +
                "# 专业标准\\n" +
                "请基于以下健康标准进行评估：\\n" +
                "- 步数：6000-8000步/日为基础，10000步以上为优秀\\n" +
                "- 静息心率：60-100bpm为正常，50-60为优秀（运动员），超过100需关注\\n" +
                "- 睡眠：7-9小时为理想，少于6小时或多于10小时需改善\\n"
        ));
        
        messages.add(new ChatMessage("user", "以下是我的健康监测数据：" + userData.toString() +
                "请帮我生成一份个人健康画像分析报告。请分析我的运动、心率、睡眠等各项指标，给出健康评分和改善建议。" +
                "请不要使用json格式，并添加一些emoji让报告更生动。"));

        chatLlmUtil.sendSyncRequest(messages, new ChatLlmUtil.LlmCallback() {
            @Override
            public void onAssistantMessage(String content) {
                if (content == null || content.trim().isEmpty() || "done.".equalsIgnoreCase(content.trim())) {
                    if (tvReport != null) {
                        tvReport.setText("生成失败，请重试");
                    }
                } else {
                    // 用 commonmark 解析 Markdown 为 HTML
                    Parser parser = Parser.builder().build();
                    Node document = parser.parse(content);
                    HtmlRenderer renderer = HtmlRenderer.builder().build();
                    String html = renderer.render(document);

                    // 用 Html.fromHtml 渲染到 TextView
                    Spanned spanned;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
                    } else {
                        spanned = Html.fromHtml(html);
                    }
                    if (tvReport != null) {
                        tvReport.setText(spanned);
                    }
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (tvReport != null) {
                    tvReport.setText("生成健康画像时出现错误：" + errorMsg);
                }
            }

            @Override
            public void onComplete() {
                if (progressBar != null) {
                    progressBar.setVisibility(ProgressBar.GONE);
                }
            }

            @Override
            public void onStreamUpdate(String content) {}
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatLlmUtil != null) {
            chatLlmUtil.shutdown();
        }
    }

    /**
     * 加载健康画像数据（保留原有方法以兼容）
     */
    private void loadHealthProfileData() {
        // 已集成到generateHealthProfile方法中
        generateHealthProfile();
    }
}
