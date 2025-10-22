package com.lanqiDoctor.demo.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.hjq.base.BaseActivity;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.dao.MedicalHistoryDao;
import com.lanqiDoctor.demo.database.entity.MedicalHistory;

/**
 * 既往病史详情Activity
 * 
 * @author 蓝旗医生开发团队
 * @version 1.0
 */
public class MedicalHistoryDetailActivity extends BaseActivity {
    
    private TextView tvDiseaseName;
    private TextView tvDiagnosisDate;
    private TextView tvSeverity;
    private TextView tvTreatmentStatus;
    private TextView tvHospital;
    private TextView tvDoctor;
    private TextView tvSymptoms;
    private TextView tvTreatment;
    private TextView tvNotes;
    
    private MedicalHistoryDao medicalHistoryDao;
    private MedicalHistory medicalHistory;
    private Long historyId;
    
    @Override
    protected int getLayoutId() {
        return R.layout.activity_medical_history_detail;
    }
    
    @Override
    protected void initView() {
        tvDiseaseName = findViewById(R.id.tv_disease_name);
        tvDiagnosisDate = findViewById(R.id.tv_diagnosis_date);
        tvSeverity = findViewById(R.id.tv_severity);
        tvTreatmentStatus = findViewById(R.id.tv_treatment_status);
        tvHospital = findViewById(R.id.tv_hospital);
        tvDoctor = findViewById(R.id.tv_doctor);
        tvSymptoms = findViewById(R.id.tv_symptoms);
        tvTreatment = findViewById(R.id.tv_treatment);
        tvNotes = findViewById(R.id.tv_notes);
        
        // 设置标题栏返回按钮点击事件
        findViewById(R.id.tb_title).setOnClickListener(v -> finish());
    }
    
    @Override
    protected void initData() {
        medicalHistoryDao = new MedicalHistoryDao(this);
        
        // 获取传递的病史ID
        Intent intent = getIntent();
        historyId = intent.getLongExtra("medical_history_id", -1);
        
        if (historyId != -1) {
            loadMedicalHistoryDetail();
        } else {
            finish(); // 如果没有有效ID，直接关闭
        }
    }
    
    private void loadMedicalHistoryDetail() {
        medicalHistory = medicalHistoryDao.findById(historyId);
        
        if (medicalHistory != null) {
            displayMedicalHistoryInfo();
        } else {
            finish(); // 如果找不到数据，直接关闭
        }
    }
    
    private void displayMedicalHistoryInfo() {
        tvDiseaseName.setText(medicalHistory.getDiseaseName());
        tvDiagnosisDate.setText(medicalHistory.getDiagnosisDate());
        
        // 显示严重程度
        String severity = medicalHistory.getSeverity();
        if (severity != null && !severity.isEmpty()) {
            tvSeverity.setText(severity);
            tvSeverity.setVisibility(View.VISIBLE);
        } else {
            tvSeverity.setVisibility(View.GONE);
        }
        
        // 显示治疗状况
        String treatmentStatus = medicalHistory.getTreatmentStatus();
        if (treatmentStatus != null && !treatmentStatus.isEmpty()) {
            tvTreatmentStatus.setText(treatmentStatus);
            setTreatmentStatusColor(treatmentStatus);
        }
        
        // 显示就诊医院
        String hospital = medicalHistory.getHospital();
        if (hospital != null && !hospital.isEmpty()) {
            tvHospital.setText(hospital);
            findViewById(R.id.layout_hospital).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_hospital).setVisibility(View.GONE);
        }
        
        // 显示主治医生
        String doctor = medicalHistory.getDoctor();
        if (doctor != null && !doctor.isEmpty()) {
            tvDoctor.setText(doctor);
            findViewById(R.id.layout_doctor).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_doctor).setVisibility(View.GONE);
        }
        
        // 显示主要症状
        String symptoms = medicalHistory.getSymptoms();
        if (symptoms != null && !symptoms.isEmpty()) {
            tvSymptoms.setText(symptoms);
            findViewById(R.id.layout_symptoms).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_symptoms).setVisibility(View.GONE);
        }
        
        // 显示治疗方案
        String treatment = medicalHistory.getTreatment();
        if (treatment != null && !treatment.isEmpty()) {
            tvTreatment.setText(treatment);
            findViewById(R.id.layout_treatment).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_treatment).setVisibility(View.GONE);
        }
        
        // 显示备注信息
        String notes = medicalHistory.getNotes();
        if (notes != null && !notes.isEmpty()) {
            tvNotes.setText(notes);
            findViewById(R.id.layout_notes).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_notes).setVisibility(View.GONE);
        }
    }
    
    private void setTreatmentStatusColor(String treatmentStatus) {
        int color;
        switch (treatmentStatus) {
            case "已治愈":
                color = getResources().getColor(android.R.color.holo_green_dark);
                break;
            case "治疗中":
                color = getResources().getColor(android.R.color.holo_orange_dark);
                break;
            case "慢性病":
                color = getResources().getColor(android.R.color.holo_red_dark);
                break;
            default:
                color = getResources().getColor(android.R.color.darker_gray);
                break;
        }
        tvTreatmentStatus.setTextColor(color);
    }
}
