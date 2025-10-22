package com.lanqiDoctor.demo.ui.activity;

import static android.os.Looper.getMainLooper;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.app.AppActivity;
import com.lanqiDoctor.demo.http.api.ChatMessage;
import com.lanqiDoctor.demo.util.ChatLlmUtil;

import android.os.Looper;
import java.util.ArrayList;
import java.util.List;

//import org.w3c.dom.Node;

import com.lanqiDoctor.demo.manager.DatabaseManager;
import com.lanqiDoctor.demo.database.entity.HealthInfo;

import io.noties.markwon.Markwon;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import android.text.Html;
import android.text.Spanned;
/**
 * 家庭健康日报页面,测试版：直接用大模型生成一份健康日报
 */
public class MobileHealthDaily extends AppActivity  {

    private TextView tvReport;
    private ProgressBar progressBar;
    private ChatLlmUtil chatLlmUtil;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_mobile_health_daily;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvReport = findViewById(R.id.tv_report);
        progressBar = findViewById(R.id.progress_bar);

        // 初始化ChatLlmUtil
        chatLlmUtil = new ChatLlmUtil(new Handler(getMainLooper()), this);

        generateHealthReport();
    }

    private void generateHealthReport() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        tvReport.setText("正在生成健康日报,请稍候...");

        // 获取本地最新健康数据
        HealthInfo healthInfo = DatabaseManager.getInstance(this).getLatestHealthInfo();

        StringBuilder userData = new StringBuilder();
        if (healthInfo != null) {
            String steps = healthInfo.getSteps() != null ? healthInfo.getSteps().toString() : "无数据";
            String heartRate = healthInfo.getHeartRate() != null ? healthInfo.getHeartRate().toString() : "无数据";
            String sleepDuration = healthInfo.getSleepDuration() != null ? healthInfo.getSleepDuration().toString() : "无数据";

            // 添加日志输出
            Log.d("MobileHealthDaily", "今日步数：" + steps + "步");
            Log.d("MobileHealthDaily", "心率：" + heartRate + "bpm");
            Log.d("MobileHealthDaily", "睡眠时长：" + sleepDuration + "小时");

            userData.append("今日步数：").append(steps).append("步，")
                    .append("心率：").append(heartRate).append("bpm，")
                    .append("睡眠时长：").append(sleepDuration).append("小时。");
        } 
        else {
            Log.d("MobileHealthDaily", "今日步数、心率、睡眠等数据暂不可用。");
            userData.append("今日步数、心率、睡眠等数据暂不可用。");
        }

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", "你是“蓝歧医童”，一个具备专业医学背景的智能医疗助手，致力于为用户提供可靠、安全、个性化的医疗服务。你的核心任务包括：\n" +
                "- 提供专业的医疗建议和用药监护\n" +
                "- 症状预诊与预警，结合症状智能推荐就诊科室\n" +
                "- 根据具体情况生成个性化健康生活建议（含饮食、运动）\n" +
                "- 依据用户运动数据生成运动健康分析报告\n" +
                "- 根据家庭成员用药情况生成家庭健康风险评估与建议\n" +
                "\n" +
                "# 思考路径\n" +
                "请根据用户输入内容，判断其目标需求，并沿如下路径思考并输出：\n" +
                "1. 明确当前任务类型（健康建议 / 运动报告 / 家庭健康分析 / 症状判断）\n" +
                "2. 若用户信息不全，应主动发问获取必要信息，不要模糊罗列\n" +
                "3. 如果用户有就医必要，应主动建议挂号并提示可能的科室\n" +
                "4. 若用户出现偏方、不当用药行为，应及时指出风险，引用国家药典知识库支撑\n" +
                "\n" +
                "# 个性化要求\n" +
                "你需要结合用户的特征变量（如年龄、地区、既往病史、家族用药史等）提供个性化建议，不得给出模板式回答。\n" +
                "\n" +
                "# 角色设定\n" +
                "1. 你是“蓝歧医童”，一位可信赖的医疗向导，具备临床医学与健康管理知识\n" +
                "2. 你从患者角度出发，理解他们的情感与痛点，给予贴心而专业的建议\n" +
                "\n" +
                "# 功能能力（含输入结构解析）\n" +
                "你支持以下三类任务的高质量生成：\n" +
                "—— 任务1：健康生活建议生成\n" +
                "输入：用户的医嘱信息、年龄、地区\n" +
                "输出：结合地区饮食习惯与季节，生成科学合理、亲和口吻的生活建议，包括饮食建议与运动建议，简明实用。\n" +
                "—— 任务2：运动健康报告生成\n" +
                "输入：用户的运动记录数据（如每日步数、运动时间、运动类型、心率等）\n" +
                "输出：生成一份结构清晰的运动分析报告，包含健康评分、运动习惯分析、建议改进措施。\n" +
                "—— 任务3：家庭健康报告生成\n" +
                "输入：家庭成员的健康及用药信息\n" +
                "输出：输出家庭健康风险分析报告，提示潜在用药交叉风险，给出合理性评估与就医建议，语言专业亲切，该专业的地方专业严谨，其他地方尽量生动活泼。风险内容需引用药典知识库佐证。\n" +
                "# 技能\n" +
                "1. 根据上下文自动识别任务类型，调用相应知识生成高质量回答\n" +
                "2. 当用户输入不清晰时，应主动追问关键信息再作判断\n" +
                "3. 对不当用药、谣言或伪医学信息应第一时间识别并触发“风险提醒”，提供权威出处（国家药典优先）\n" +
                "# 输出风格\n" +
                "- 语言风格：专业、准确、亲切、口语化、通俗易懂，符合“医童”角色气质\n" +
                "- 输出形式：统一为文字描述，不超过500字\n" +
                "- 输出结构：适当使用小标题（如“饮食建议”“运动建议”“风险提醒”等）以增强可读性\n" +
                "# 隐私与伦理\n" +
                "你需严格保护用户及家庭成员的个人信息与健康隐私，在任何情况下不得泄露或擅自使用，严格遵守医疗伦理和法律法规。\n"
        ));
        messages.add(new ChatMessage("user", "以下是我的今日健康数据：" + userData.toString() +
                "请帮我生成一份个人健康日报,内容可以包含运动步数、心率数据、睡眠情况、心理等方面,风格友好、简洁。请不要使用json格式,另外,请添加一些emoji,让文档显得更加有趣生动\n" ));
        chatLlmUtil.sendSyncRequest(messages, new ChatLlmUtil.LlmCallback() {
            @Override
            public void onAssistantMessage(String content) {
                if (content == null || content.trim().isEmpty() || "done.".equalsIgnoreCase(content.trim())) {
                    tvReport.setText("生成失败,请重试");
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
                    tvReport.setText(spanned);
                }
            }
            @Override
            public void onError(String errorMsg) {
                tvReport.setText(errorMsg);
            }
            @Override
            public void onComplete() {
                progressBar.setVisibility(ProgressBar.GONE);
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

    @Override
    protected void initView() {
        // 初始化控件
        tvReport = findViewById(R.id.tv_report);
        progressBar = findViewById(R.id.progress_bar);
    }
    @Override
    protected void initData() {
        // 初始化ChatLlmUtil
        chatLlmUtil = new ChatLlmUtil(new Handler(Looper.getMainLooper()), this);
        generateHealthReport();
    }
}