package com.lanqiDoctor.demo.ocr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.ui.activity.AddMedicationActivity;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 医嘱分析Activity - 显示OCR识别结果，调用大模型分析，并允许用户编辑
 */
public class PrescriptionAnalysisActivity extends Activity implements LifecycleOwner {
    
    private static final String TAG = "PrescriptionAnalysis";
    public static final String EXTRA_OCR_RESULTS = "ocr_results";
    
    private TextView tvOcrResults;
    private EditText etOcrResults;
    private Button btnAnalyze;
    private Button btnEdit;
    private Button btnSave;
    private Button btnBatchAdd;
    private ProgressBar progressBar;
    private TextView tvAnalysisResults;
    
    private String[] ocrResults;
    private PrescriptionAnalyzer analyzer;
    private boolean isEditMode = false;
    private List<MedicationInfo> extractedMedications;
    private int currentMedicationIndex = 0;
    private String displayText; // 用于保存当前显示的文本
    
    private static final int REQUEST_ADD_MEDICATION = 1001;
    
    private LifecycleRegistry lifecycleRegistry;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_analysis);
        
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.markState(Lifecycle.State.CREATED);
        
        initViews();
        initData();
        setupAnalyzer();
    }
    
    private void initViews() {
        tvOcrResults = findViewById(R.id.tv_ocr_results);
        etOcrResults = findViewById(R.id.et_ocr_results);
        btnAnalyze = findViewById(R.id.btn_analyze);
        btnEdit = findViewById(R.id.btn_edit);
        btnSave = findViewById(R.id.btn_save);
        btnBatchAdd = findViewById(R.id.btn_batch_add);
        progressBar = findViewById(R.id.progress_bar);
        tvAnalysisResults = findViewById(R.id.tv_analysis_results);
        
        btnAnalyze.setOnClickListener(v -> startStructuredAnalysis());
        btnEdit.setOnClickListener(v -> toggleEditMode());
        btnSave.setOnClickListener(v -> saveChanges());
        btnBatchAdd.setOnClickListener(v -> startBatchAdd());
        
        // 初始状态下隐藏批量添加按钮
        btnBatchAdd.setVisibility(View.GONE);
        
        // 监听编辑文本变化
        etOcrResults.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                btnSave.setEnabled(true);
            }
        });
    }
    
    private void initData() {
        Intent intent = getIntent();
        ocrResults = intent.getStringArrayExtra(EXTRA_OCR_RESULTS);
        
        if (ocrResults != null) {
            displayOcrResults();
        } else {
            Toast.makeText(this, "未接收到OCR识别结果", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void setupAnalyzer() {
        analyzer = new PrescriptionAnalyzer(this, this);
    }
    
    private void displayOcrResults() {
        StringBuilder sb = new StringBuilder();
        // 倒序显示结果，从上往下的顺序（与OCRActivity保持一致）
        for (int i = ocrResults.length - 1; i >= 0; i--) {
            if (ocrResults[i] != null && !ocrResults[i].trim().isEmpty()) {
                sb.append(ocrResults[i].trim()).append("\n");
            }
        }
        
        displayText = sb.toString().trim(); // 保存显示文本
        tvOcrResults.setText(displayText);
        etOcrResults.setText(displayText);
    }
    
    private void toggleEditMode() {
        isEditMode = !isEditMode;
        
        if (isEditMode) {
            tvOcrResults.setVisibility(View.GONE);
            etOcrResults.setVisibility(View.VISIBLE);
            btnEdit.setText("取消编辑");
            btnSave.setVisibility(View.VISIBLE);
            // 进入编辑模式时，使用当前显示的文本
            etOcrResults.setText(displayText != null ? displayText : tvOcrResults.getText().toString());
        } else {
            tvOcrResults.setVisibility(View.VISIBLE);
            etOcrResults.setVisibility(View.GONE);
            btnEdit.setText("编辑文本");
            btnSave.setVisibility(View.GONE);
            
            // 取消编辑时，恢复到之前保存的显示文本
            if (displayText != null) {
                tvOcrResults.setText(displayText);
                etOcrResults.setText(displayText);
            }
        }
    }
    
    private void saveChanges() {
        String editedText = etOcrResults.getText().toString();
        
        // 保存用户编辑的文本作为新的显示文本
        displayText = editedText;
        
        // 将编辑后的文本重新分割为数组，用于后续分析
        String[] lines = editedText.split("\n");
        ocrResults = lines; // 这里不需要倒序，因为editedText已经是用户想要的顺序
        
        // 更新显示
        tvOcrResults.setText(editedText);
        
        // 退出编辑模式
        toggleEditMode();
        
        btnSave.setEnabled(false);
        Toast.makeText(this, "修改已保存", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 开始结构化分析
     */
    private void startStructuredAnalysis() {
        String textToAnalyze = getCurrentText();
        if (textToAnalyze == null || textToAnalyze.trim().isEmpty()) {
            Toast.makeText(this, "没有可分析的内容", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 如果在编辑模式，先保存更改
        if (isEditMode) {
            saveChanges();
            textToAnalyze = getCurrentText(); // 重新获取保存后的文本
        }
        
        // 将文本转换为行数组格式，供分析器使用
        String[] textLines = textToAnalyze.split("\n");
        
        analyzer.analyzeForStructuredData(textLines, new PrescriptionAnalyzer.StructuredAnalysisCallback() {
            @Override
            public void onAnalysisStart() {
                progressBar.setVisibility(View.VISIBLE);
                btnAnalyze.setEnabled(false);
                btnBatchAdd.setVisibility(View.GONE);
                tvAnalysisResults.setText("正在智能提取药品信息...");
            }
            
            @Override
            public void onAnalysisSuccess(List<MedicationInfo> medications) {
                Log.d(TAG, "Structured analysis success, found " + medications.size() + " medications");
                extractedMedications = medications;
                displayStructuredResults(medications);
                progressBar.setVisibility(View.GONE);
                btnAnalyze.setEnabled(true);
                
                // 如果解析出药品信息，显示批量添加按钮
                if (medications != null && !medications.isEmpty()) {
                    btnBatchAdd.setVisibility(View.VISIBLE);
                    btnBatchAdd.setText("批量添加药品 (" + medications.size() + "个)");
                }
            }
            
            @Override
            public void onAnalysisError(String error) {
                Log.e(TAG, "Structured analysis error: " + error);
                tvAnalysisResults.setText("提取失败：" + error);
                Toast.makeText(PrescriptionAnalysisActivity.this, "提取失败：" + error, Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                btnAnalyze.setEnabled(true);
            }
        });
    }
    
    /**
     * 显示结构化分析结果
     */
    private void displayStructuredResults(List<MedicationInfo> medications) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 提取的药品信息 ===\n\n");
        
        if (medications == null || medications.isEmpty()) {
            sb.append("未能识别出有效的药品信息。\n");
            sb.append("建议检查处方单图片清晰度，或手动添加药品信息。");
        } else {
            for (int i = 0; i < medications.size(); i++) {
                MedicationInfo med = medications.get(i);
                sb.append("药品 ").append(i + 1).append("：\n");
                sb.append("  药品名称：").append(med.getMedicationName() != null ? med.getMedicationName() : "未识别").append("\n");
                sb.append("  剂量：").append(med.getDosage() != null ? med.getDosage() : "未指定").append("\n");
                sb.append("  单位：").append(med.getUnit() != null ? med.getUnit() : "未指定").append("\n");
                sb.append("  频率：").append(med.getFrequency() != null ? med.getFrequency() : "未指定").append("\n");
                if (med.getUsage() != null) {
                    sb.append("  用法：").append(med.getUsage()).append("\n");
                }
                if (med.getNotes() != null) {
                    sb.append("  备注：").append(med.getNotes()).append("\n");
                }
                sb.append("\n");
            }
            sb.append("提示：请仔细核对以上信息，确认无误后可点击\"批量添加\"按钮。");
        }
        
        tvAnalysisResults.setText(sb.toString());
    }
    
    /**
     * 开始批量添加药品
     */
    private void startBatchAdd() {
        if (extractedMedications == null || extractedMedications.isEmpty()) {
            Toast.makeText(this, "没有可添加的药品信息", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 重置索引
        currentMedicationIndex = 0;
        addNextMedication();
    }
    
    /**
     * 添加下一个药品
     */
    private void addNextMedication() {
        if (currentMedicationIndex >= extractedMedications.size()) {
            // 所有药品都已添加完成
            Toast.makeText(this, "所有药品已添加完成", Toast.LENGTH_LONG).show();
            
            // 询问是否返回用药管理页面
            showBatchCompleteDialog();
            return;
        }
        
        MedicationInfo medication = extractedMedications.get(currentMedicationIndex);
        Intent intent = new Intent(this, AddMedicationActivity.class);
        
        // 解析用法中的每次剂量信息
        DosageInfo dosageInfo = parseUsageDosage(medication);
        
        // 传递药品信息到AddMedicationActivity
        intent.putExtra("medication_name", medication.getMedicationName());
        intent.putExtra("dosage", dosageInfo.dosage);  // 每次服用的剂量
        intent.putExtra("frequency", medication.getFrequency());
        intent.putExtra("unit", dosageInfo.unit);      // 每次服用的单位
        intent.putExtra("notes", buildNotesFromMedication(medication, dosageInfo));
        intent.putExtra("is_batch_mode", true);
        intent.putExtra("current_index", currentMedicationIndex + 1);
        intent.putExtra("total_count", extractedMedications.size());
        
        startActivityForResult(intent, REQUEST_ADD_MEDICATION);
    }
    
    /**
     * 解析用法中的每次剂量信息
     */
    private DosageInfo parseUsageDosage(MedicationInfo medication) {
        DosageInfo dosageInfo = new DosageInfo();
        
        // 默认值
        dosageInfo.dosage = "1";
        dosageInfo.unit = "片";
        
        String usage = medication.getUsage();
        String originalDosage = medication.getDosage();
        String originalUnit = medication.getUnit();
        
        // 保存原始规格信息
        dosageInfo.originalSpec = buildOriginalSpec(originalDosage, originalUnit);
        
        if (usage != null && !usage.trim().isEmpty()) {
            // 尝试从用法中提取"每次X片/粒/毫升"等信息
            dosageInfo = extractDosageFromUsage(usage, dosageInfo);
        }
        
        // 如果用法中没有明确的剂量信息，尝试从原始剂量推断
        if ("1".equals(dosageInfo.dosage) && originalDosage != null) {
            dosageInfo = inferDosageFromOriginal(originalDosage, originalUnit, dosageInfo);
        }
        
        return dosageInfo;
    }
    
    /**
     * 从用法文本中提取剂量信息
     */
    private DosageInfo extractDosageFromUsage(String usage, DosageInfo defaultInfo) {
        DosageInfo result = new DosageInfo();
        result.dosage = defaultInfo.dosage;
        result.unit = defaultInfo.unit;
        result.originalSpec = defaultInfo.originalSpec;
        
        String lowerUsage = usage.toLowerCase();
        
        // 匹配模式：每次X片、每次X粒、每次Xml等
        Pattern pattern = Pattern.compile(
            "每次\\s*(\\d+(?:\\.\\d+)?)\\s*(片|粒|毫升|毫克|克|袋|支|滴|ml|mg|g)"
        );
        Matcher matcher = pattern.matcher(usage);
        
        if (matcher.find()) {
            result.dosage = matcher.group(1);
            String extractedUnit = matcher.group(2);
            
            // 标准化单位
            switch (extractedUnit.toLowerCase()) {
                case "ml":
                    result.unit = "毫升";
                    break;
                case "mg":
                    result.unit = "毫克";
                    break;
                case "g":
                    result.unit = "克";
                    break;
                default:
                    result.unit = extractedUnit;
                    break;
            }
            return result;
        }
        
        // 匹配其他模式：一次X片、1次X粒等
        pattern = Pattern.compile(
            "[一1]次\\s*(\\d+(?:\\.\\d+)?)\\s*(片|粒|毫升|毫克|克|袋|支|滴|ml|mg|g)"
        );
        matcher = pattern.matcher(usage);
        
        if (matcher.find()) {
            result.dosage = matcher.group(1);
            String extractedUnit = matcher.group(2);
            
            // 标准化单位
            switch (extractedUnit.toLowerCase()) {
                case "ml":
                    result.unit = "毫升";
                    break;
                case "mg":
                    result.unit = "毫克";
                    break;
                case "g":
                    result.unit = "克";
                    break;
                default:
                    result.unit = extractedUnit;
                    break;
            }
        }
        
        return result;
    }
    
    /**
     * 从原始剂量推断每次服用剂量
     */
    private DosageInfo inferDosageFromOriginal(String originalDosage, String originalUnit, DosageInfo defaultInfo) {
        DosageInfo result = new DosageInfo();
        result.dosage = defaultInfo.dosage;
        result.unit = defaultInfo.unit;
        result.originalSpec = defaultInfo.originalSpec;
        
        // 如果原始单位是片、粒、袋、支、滴，通常每次服用1个
        if (originalUnit != null) {
            String lowerUnit = originalUnit.toLowerCase();
            if (lowerUnit.contains("片") || lowerUnit.contains("粒") || 
                lowerUnit.contains("袋") || lowerUnit.contains("支") || 
                lowerUnit.contains("滴")) {
                result.dosage = "1";
                result.unit = originalUnit;
            }
        }
        
        return result;
    }
    
    /**
     * 构建原始规格信息
     */
    private String buildOriginalSpec(String dosage, String unit) {
        if (dosage != null && unit != null) {
            return dosage + unit;
        } else if (dosage != null) {
            return dosage;
        } else if (unit != null) {
            return unit;
        }
        return null;
    }
    
    /**
     * 剂量信息类
     */
    private static class DosageInfo {
        String dosage = "1";        // 每次服用剂量
        String unit = "片";         // 每次服用单位
        String originalSpec;        // 原始规格信息
    }
    
    /**
     * 从药品信息构建备注文本
     */
    private String buildNotesFromMedication(MedicationInfo medication, DosageInfo dosageInfo) {
        StringBuilder notes = new StringBuilder();
        
        // 添加原始规格信息
        if (dosageInfo.originalSpec != null && !dosageInfo.originalSpec.trim().isEmpty()) {
            notes.append("药品规格：").append(dosageInfo.originalSpec).append("\n");
        }
        
        if (medication.getUsage() != null) {
            notes.append("用法：").append(medication.getUsage()).append("\n");
        }
        
        if (medication.getNotes() != null) {
            notes.append("备注：").append(medication.getNotes()).append("\n");
        }
        
        notes.append("来源：处方单OCR识别");
        
        return notes.toString().trim();
    }
    
    /**
     * 显示批量添加完成对话框
     */
    private void showBatchCompleteDialog() {
        // 这里可以添加一个对话框询问用户是否返回用药管理页面
        // 简化实现：直接finish当前页面
        setResult(RESULT_OK);
        finish();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_ADD_MEDICATION) {
            if (resultCode == RESULT_OK) {
                // 当前药品添加成功，继续添加下一个
                currentMedicationIndex++;
                addNextMedication();
            } else {
                // 用户取消或添加失败
                Toast.makeText(this, "药品添加已取消", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        lifecycleRegistry.markState(Lifecycle.State.STARTED);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        lifecycleRegistry.markState(Lifecycle.State.RESUMED);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        lifecycleRegistry.markState(Lifecycle.State.STARTED);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        lifecycleRegistry.markState(Lifecycle.State.CREATED);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED);
    }
    
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }
    
    /**
     * 获取当前要分析的文本
     */
    private String getCurrentText() {
        if (isEditMode) {
            return etOcrResults.getText().toString().trim();
        } else if (displayText != null) {
            return displayText.trim();
        } else {
            return tvOcrResults.getText().toString().trim();
        }
    }
}
