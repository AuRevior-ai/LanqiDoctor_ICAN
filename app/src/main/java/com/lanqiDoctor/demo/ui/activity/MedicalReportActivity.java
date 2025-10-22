package com.lanqiDoctor.demo.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.bar.TitleBar;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.app.AppActivity;
import com.lanqiDoctor.demo.database.dao.MedicalHistoryDao;
import com.lanqiDoctor.demo.database.entity.MedicalHistory;
import com.lanqiDoctor.demo.http.api.ChatMessage;
import com.lanqiDoctor.demo.manager.UserStateManager;
import com.lanqiDoctor.demo.util.ChatLlmUtil;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 医疗报告生成页面
 * 基于既往病史生成专业的医疗报告，供医生参考
 */
public class MedicalReportActivity extends AppActivity {

    private TitleBar tbTitle;
    private TextView tvReport;
    private ProgressBar progressBar;
    private ChatLlmUtil chatLlmUtil;
    private MedicalHistoryDao medicalHistoryDao;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_medical_report;
    }

    @Override
    protected void initView() {
        tbTitle = findViewById(R.id.tb_title);
        tvReport = findViewById(R.id.tv_report);
        progressBar = findViewById(R.id.progress_bar);
    }

    @Override
    protected void initData() {
        // 设置标题栏返回按钮点击事件
        findViewById(R.id.tb_title).setOnClickListener(v -> finish());

        // 初始化数据访问对象
        medicalHistoryDao = new MedicalHistoryDao(this);
        
        // 初始化ChatLlmUtil
        chatLlmUtil = new ChatLlmUtil(new Handler(Looper.getMainLooper()), this);

        // 生成医疗报告
        generateMedicalReport();
    }

    private void generateMedicalReport() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        tvReport.setText("正在基于您的既往病史生成专业医疗报告，请稍候...");

        // 获取当前用户的既往病史记录
        String userId = UserStateManager.getInstance(this).getUserId();
        List<MedicalHistory> medicalHistories = medicalHistoryDao.findAll();

        StringBuilder medicalData = new StringBuilder();
        if (medicalHistories != null && !medicalHistories.isEmpty()) {
            medicalData.append("患者既往病史记录如下：\n\n");
            
            for (int i = 0; i < medicalHistories.size(); i++) {
                MedicalHistory history = medicalHistories.get(i);
                medicalData.append("【病史记录 ").append(i + 1).append("】\n");
                medicalData.append("疾病名称：").append(history.getDiseaseName()).append("\n");
                
                if (history.getDiagnosisDate() != null && !history.getDiagnosisDate().isEmpty()) {
                    medicalData.append("诊断日期：").append(history.getDiagnosisDate()).append("\n");
                }
                
                if (history.getSeverity() != null && !history.getSeverity().isEmpty()) {
                    medicalData.append("严重程度：").append(history.getSeverity()).append("\n");
                }
                
                if (history.getTreatmentStatus() != null && !history.getTreatmentStatus().isEmpty()) {
                    medicalData.append("治疗状态：").append(history.getTreatmentStatus()).append("\n");
                }
                
                if (history.getHospital() != null && !history.getHospital().isEmpty()) {
                    medicalData.append("诊疗医院：").append(history.getHospital()).append("\n");
                }
                
                if (history.getDoctor() != null && !history.getDoctor().isEmpty()) {
                    medicalData.append("负责医生：").append(history.getDoctor()).append("\n");
                }
                
                if (history.getSymptoms() != null && !history.getSymptoms().isEmpty()) {
                    medicalData.append("主要症状：").append(history.getSymptoms()).append("\n");
                }
                
                if (history.getTreatment() != null && !history.getTreatment().isEmpty()) {
                    medicalData.append("治疗方案：").append(history.getTreatment()).append("\n");
                }
                
                if (history.getNotes() != null && !history.getNotes().isEmpty()) {
                    medicalData.append("备注信息：").append(history.getNotes()).append("\n");
                }
                
                medicalData.append("\n");
            }
        } else {
            medicalData.append("患者目前暂无既往病史记录。");
        }

        // 添加日志输出
        Log.d("MedicalReportActivity", "既往病史数据：" + medicalData.toString());

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", "你是\"蓝岐医童\"，一位资深的临床医学AI助手。你需要基于患者的既往病史，生成一份专业、实用的医疗综合报告。\\n\\n" +
                "# 核心要求\\n" +
                "- 以临床医生的视角撰写，语言专业但易懂\\n" +
                "- 重点关注疾病间的关联性和潜在风险\\n" +
                "- 提供具有临床指导价值的建议\\n" +
                "- 避免简单罗列，要有逻辑分析和综合判断\\n\\n" +
                "# 报告结构\\n" +
                "## 既往病史概览\\n" +
                "简要总结患者的主要疾病史，突出关键时间节点和疾病特点\\n\\n" +
                "## 临床风险评估\\n" +
                "分析现有病史可能导致的健康风险，包括：\\n" +
                "- 疾病复发风险\\n" +
                "- 并发症可能性\\n" +
                "- 药物相互作用风险\\n" +
                "- 新发疾病倾向\\n\\n" +
                "## 诊疗建议\\n" +
                "基于病史提供针对性的医疗建议：\\n" +
                "- 重点监测指标\\n" +
                "- 推荐检查项目\\n" +
                "- 生活方式调整\\n" +
                "- 预防措施\\n\\n" +
                "## 随访计划\\n" +
                "制定个性化的随访策略和时间安排\\n\\n" +
                "# 写作风格\\n" +
                "- 使用医学术语但保持可读性\\n" +
                "- 突出重要信息，如高风险因素\\n" +
                "- 提供具体可操作的建议\\n" +
                "- 体现专业判断和临床经验\\n" +
                "- 控制篇幅在600-800字之间\\n" +
                "- 使用适当的emoji增强可读性"
        ));
        
        messages.add(new ChatMessage("user", medicalData.toString() + 
                "\n\n请基于以上既往病史信息，生成一份专业的医疗报告供医生参考。报告生成日期：" + 
                new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(new Date())));

        chatLlmUtil.sendSyncRequest(messages, new ChatLlmUtil.LlmCallback() {
            @Override
            public void onAssistantMessage(String content) {
                if (content == null || content.trim().isEmpty() || "done.".equalsIgnoreCase(content.trim())) {
                    tvReport.setText("报告生成失败，请重试");
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
                tvReport.setText("报告生成失败：" + errorMsg);
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
}
