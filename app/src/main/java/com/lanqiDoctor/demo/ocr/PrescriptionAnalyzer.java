package com.lanqiDoctor.demo.ocr;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LifecycleOwner;

import com.lanqiDoctor.demo.http.api.ChatMessage;
import com.lanqiDoctor.demo.util.ChatLlmUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处方单分析器 - 使用大模型分析OCR识别的处方单文本
 */
public class PrescriptionAnalyzer {
    
    private static final String TAG = "PrescriptionAnalyzer";
    
    private Context context;
    private LifecycleOwner lifecycleOwner;
    private ChatLlmUtil chatLlmUtil;
    
    /**
     * 分析结果回调接口
     */
    public interface AnalysisCallback {
        void onAnalysisStart();
        void onAnalysisSuccess(String analysisResult);
        void onAnalysisError(String error);
    }
    
    /**
     * 结构化分析结果回调接口
     */
    public interface StructuredAnalysisCallback {
        void onAnalysisStart();
        void onAnalysisSuccess(List<MedicationInfo> medications);
        void onAnalysisError(String error);
    }
    
    /**
     * 构造函数
     */
    public PrescriptionAnalyzer(Context context, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        // 创建主线程Handler
        Handler mainHandler = new Handler(Looper.getMainLooper());
        this.chatLlmUtil = new ChatLlmUtil(mainHandler, lifecycleOwner);
    }
    
    /**
     * 分析处方单文本
     * @param ocrTexts OCR识别的文本数组
     * @param callback 结果回调
     */
    public void analyzePrescription(String[] ocrTexts, AnalysisCallback callback) {
        if (ocrTexts == null || ocrTexts.length == 0) {
            callback.onAnalysisError("没有可分析的文本内容");
            return;
        }
        
        // 直接按数组顺序组合文本（不再倒序，因为传入的数组已经是正确顺序）
        StringBuilder combinedText = new StringBuilder();
        for (String text : ocrTexts) {
            if (text != null && !text.trim().isEmpty()) {
                combinedText.append(text.trim()).append("\n");
            }
        }
        
        String prescriptionText = combinedText.toString().trim();
        Log.d(TAG, "Analyzing prescription text: " + prescriptionText);
        
        if (prescriptionText.isEmpty()) {
            callback.onAnalysisError("没有有效的文本内容");
            return;
        }
        
        callback.onAnalysisStart();
        
        // 构建消息列表
        List<ChatMessage> messages = new ArrayList<>();
        String prompt = buildAnalysisPrompt(prescriptionText);
        messages.add(new ChatMessage("user", prompt));
        
        // 发送给大模型分析
        chatLlmUtil.sendSyncRequest(messages, new ChatLlmUtil.LlmCallback() {
            @Override
            public void onAssistantMessage(String content) {
                Log.d(TAG, "LLM response: " + content);
                
                try {
                    // 直接返回自然语言分析结果
                    callback.onAnalysisSuccess(content);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to process LLM response", e);
                    callback.onAnalysisError("处理分析结果失败: " + e.getMessage());
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "LLM request error: " + errorMsg);
                callback.onAnalysisError(errorMsg);
            }
            
            @Override
            public void onComplete() {
                // 请求完成，这里不需要特别处理
                Log.d(TAG, "LLM request completed");
            }
            
            @Override
            public void onStreamUpdate(String partialContent) {
                // 同步请求不需要实现流更新
            }
        });
    }
    
    /**
     * 构建分析提示词
     */
    private String buildAnalysisPrompt(String prescriptionText) {
        return "请分析以下处方单文本，提取其中的药品信息。请按以下格式整理输出：\n\n" +
               "药品1：\n" +
               "- 药品名称：[药品名称]\n" +
               "- 规格：[规格]\n" +
               "- 用法用量：[用法用量]\n" +
               "- 包装数量：[数量]\n\n" +
               "药品2：\n" +
               "- 药品名称：[药品名称]\n" +
               "- 规格：[规格]\n" +
               "- 用法用量：[用法用量]\n" +
               "- 包装数量：[数量]\n\n" +
               "处方单文本：\n" + prescriptionText;
    }
    
    /**
     * 结构化分析处方单文本，返回药品信息列表
     * @param ocrTexts OCR识别的文本数组
     * @param callback 结果回调
     */
    public void analyzeForStructuredData(String[] ocrTexts, StructuredAnalysisCallback callback) {
        if (ocrTexts == null || ocrTexts.length == 0) {
            callback.onAnalysisError("没有可分析的文本内容");
            return;
        }
        
        // 直接按数组顺序组合文本（不再倒序，因为传入的数组已经是正确顺序）
        StringBuilder combinedText = new StringBuilder();
        for (String text : ocrTexts) {
            if (text != null && !text.trim().isEmpty()) {
                combinedText.append(text.trim()).append("\n");
            }
        }
        
        String prescriptionText = combinedText.toString().trim();
        Log.d(TAG, "Analyzing prescription text for structured data: " + prescriptionText);
        
        if (prescriptionText.isEmpty()) {
            callback.onAnalysisError("没有有效的文本内容");
            return;
        }
        
        callback.onAnalysisStart();
        
        // 构建消息列表
        List<ChatMessage> messages = new ArrayList<>();
        String prompt = buildStructuredAnalysisPrompt(prescriptionText);
        messages.add(new ChatMessage("user", prompt));
        
        // 发送给大模型分析
        chatLlmUtil.sendSyncRequest(messages, new ChatLlmUtil.LlmCallback() {
            @Override
            public void onAssistantMessage(String content) {
                Log.d(TAG, "LLM structured response: " + content);
                
                try {
                    // 解析返回的结构化数据
                    List<MedicationInfo> medications = parseStructuredResponse(content);
                    callback.onAnalysisSuccess(medications);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to parse structured LLM response", e);
                    callback.onAnalysisError("解析结构化数据失败: " + e.getMessage());
                }
            }
            
            @Override
            public void onError(String errorMsg) {
                Log.e(TAG, "LLM structured request error: " + errorMsg);
                callback.onAnalysisError(errorMsg);
            }
            
            @Override
            public void onComplete() {
                Log.d(TAG, "LLM structured request completed");
            }
            
            @Override
            public void onStreamUpdate(String partialContent) {
                // 同步请求不需要实现流更新
            }
        });
    }
    
    /**
     * 构建结构化分析提示词
     */
    private String buildStructuredAnalysisPrompt(String prescriptionText) {
        return "请分析以下处方单文本，提取药品信息。请严格按照以下格式输出，每个药品用【药品开始】和【药品结束】包围：\n\n" +
               "【药品开始】\n" +
               "药品名称：[具体药品名称]\n" +
               "剂量：[每次服用剂量，如：1]\n" +
               "单位：[剂量单位，如：片、毫升、粒]\n" +
               "频率：[服药频率，如：每日2次、每日3次]\n" +
               "用法：[具体用法用量描述]\n" +
               "备注：[其他注意事项]\n" +
               "【药品结束】\n\n" +
               "注意：\n" +
               "1. 如果某项信息不明确，请填写\"未指定\"\n" +
               "2. 剂量只填数字，单位单独填写\n" +
               "3. 频率请标准化为\"每日X次\"格式\n" +
               "4. 请仔细识别每种不同的药品\n\n" +
               "处方单文本：\n" + prescriptionText;
    }
    
    /**
     * 解析结构化响应文本，提取药品信息
     */
    private List<MedicationInfo> parseStructuredResponse(String response) {
        List<MedicationInfo> medications = new ArrayList<>();
        
        // 使用正则表达式匹配药品信息块
        Pattern pattern = Pattern.compile(
            "【药品开始】(.*?)【药品结束】", 
            Pattern.DOTALL
        );
        
        Matcher matcher = pattern.matcher(response);
        
        while (matcher.find()) {
            String medicationBlock = matcher.group(1).trim();
            MedicationInfo medication = parseSingleMedication(medicationBlock);
            if (medication != null && isValidMedication(medication)) {
                medications.add(medication);
            }
        }
        
        // 如果没有找到标准格式，尝试备用解析方法
        if (medications.isEmpty()) {
            medications = parseAlternativeFormat(response);
        }
        
        return medications;
    }
    
    /**
     * 解析单个药品信息块
     */
    private MedicationInfo parseSingleMedication(String medicationBlock) {
        MedicationInfo medication = new MedicationInfo();
        
        // 解析各个字段
        medication.setMedicationName(extractField(medicationBlock, "药品名称"));
        medication.setDosage(extractField(medicationBlock, "剂量"));
        medication.setUnit(extractField(medicationBlock, "单位"));
        medication.setFrequency(extractField(medicationBlock, "频率"));
        medication.setUsage(extractField(medicationBlock, "用法"));
        medication.setNotes(extractField(medicationBlock, "备注"));
        
        return medication;
    }
    
    /**
     * 从文本块中提取指定字段的值
     */
    private String extractField(String text, String fieldName) {
        Pattern pattern = Pattern.compile(fieldName + "：(.*)");
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            String value = matcher.group(1).trim();
            // 清理换行符和多余空白
            value = value.replaceAll("\n.*", "").trim();
            return value.isEmpty() || "未指定".equals(value) ? null : value;
        }
        
        return null;
    }
    
    /**
     * 验证药品信息是否有效
     */
    private boolean isValidMedication(MedicationInfo medication) {
        return medication.getMedicationName() != null && 
               !medication.getMedicationName().trim().isEmpty();
    }
    
    /**
     * 备用解析方法，处理非标准格式的响应
     */
    private List<MedicationInfo> parseAlternativeFormat(String response) {
        List<MedicationInfo> medications = new ArrayList<>();
        
        // 尝试解析药品1、药品2格式
        Pattern drugPattern = Pattern.compile("药品\\d+[：:]", Pattern.CASE_INSENSITIVE);
        String[] parts = drugPattern.split(response);
        
        for (int i = 1; i < parts.length; i++) { // 跳过第一个空白部分
            String part = parts[i].trim();
            if (!part.isEmpty()) {
                MedicationInfo medication = parseAlternativeMedication(part);
                if (medication != null && isValidMedication(medication)) {
                    medications.add(medication);
                }
            }
        }
        
        return medications;
    }
    
    /**
     * 解析备用格式的单个药品信息
     */
    private MedicationInfo parseAlternativeMedication(String text) {
        MedicationInfo medication = new MedicationInfo();
        
        // 尝试提取药品名称（通常在开头）
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("-") || line.startsWith("•")) {
                line = line.substring(1).trim();
            }
            
            if (line.contains("药品名称") || line.contains("名称")) {
                medication.setMedicationName(extractValueAfterColon(line));
            } else if (line.contains("剂量") || line.contains("用量")) {
                medication.setDosage(extractValueAfterColon(line));
            } else if (line.contains("频率") || line.contains("次数")) {
                medication.setFrequency(extractValueAfterColon(line));
            } else if (line.contains("单位")) {
                medication.setUnit(extractValueAfterColon(line));
            } else if (line.contains("用法")) {
                medication.setUsage(extractValueAfterColon(line));
            }
        }
        
        return medication;
    }
    
    /**
     * 提取冒号后的值
     */
    private String extractValueAfterColon(String text) {
        int colonIndex = text.indexOf('：');
        if (colonIndex == -1) {
            colonIndex = text.indexOf(':');
        }
        
        if (colonIndex != -1 && colonIndex + 1 < text.length()) {
            String value = text.substring(colonIndex + 1).trim();
            return value.isEmpty() ? null : value;
        }
        
        return null;
    }
}
