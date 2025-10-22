package com.lanqiDoctor.demo.ui.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.hjq.base.BaseActivity;
import com.hjq.toast.ToastUtils;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.dao.MedicalHistoryDao;
import com.lanqiDoctor.demo.database.entity.MedicalHistory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * 添加/编辑既往病史Activity
 * 
 * @author 蓝旗医生开发团队
 * @version 1.0
 */
public class AddMedicalHistoryActivity extends BaseActivity {
    
    private EditText etDiseaseName;
    private TextView tvDiagnosisDate;
    private Spinner spinnerSeverity;
    private Spinner spinnerTreatmentStatus;
    private EditText etHospital;
    private EditText etDoctor;
    private EditText etSymptoms;
    private EditText etTreatment;
    private EditText etNotes;
    private Button btnSave;
    private Button btnCancel;
    
    private MedicalHistoryDao medicalHistoryDao;
    private MedicalHistory currentHistory;
    private boolean isEditMode = false;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    private Calendar diagnosisCalendar = Calendar.getInstance();
    
    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_medical_history;
    }
    
    @Override
    protected void initView() {
        etDiseaseName = findViewById(R.id.et_disease_name);
        tvDiagnosisDate = findViewById(R.id.tv_diagnosis_date);
        spinnerSeverity = findViewById(R.id.spinner_severity);
        spinnerTreatmentStatus = findViewById(R.id.spinner_treatment_status);
        etHospital = findViewById(R.id.et_hospital);
        etDoctor = findViewById(R.id.et_doctor);
        etSymptoms = findViewById(R.id.et_symptoms);
        etTreatment = findViewById(R.id.et_treatment);
        etNotes = findViewById(R.id.et_notes);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        
        // 设置下拉框数据
        setupSpinners();
        
        // 设置点击监听
        setOnClickListener(tvDiagnosisDate, btnSave, btnCancel);
        findViewById(R.id.tb_title).setOnClickListener(v -> finish());
    }
    
    @Override
    protected void initData() {
        medicalHistoryDao = new MedicalHistoryDao(this);
        
        // 检查是否为编辑模式
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("is_edit", false);
        
        if (isEditMode) {
            Long historyId = intent.getLongExtra("medical_history_id", -1);
            if (historyId != -1) {
                loadMedicalHistoryData(historyId);
            }
        } else {
            // 设置默认诊断日期为今天
            tvDiagnosisDate.setText(dateFormat.format(diagnosisCalendar.getTime()));
        }
    }
    
    private void setupSpinners() {
        // 严重程度选项
        String[] severityOptions = {"轻度", "中度", "重度"};
        ArrayAdapter<String> severityAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, severityOptions);
        severityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeverity.setAdapter(severityAdapter);
        
        // 治疗状况选项
        String[] treatmentStatusOptions = {"治疗中", "已治愈", "慢性病", "观察中"};
        ArrayAdapter<String> treatmentAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, treatmentStatusOptions);
        treatmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTreatmentStatus.setAdapter(treatmentAdapter);
    }
    
    @Override
    public void onClick(View view) {
        if (view == tvDiagnosisDate) {
            showDiagnosisDatePicker();
        } else if (view == btnSave) {
            saveMedicalHistory();
        } else if (view == btnCancel) {
            finish();
        }
    }
    
    private void showDiagnosisDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
            (view, year, month, dayOfMonth) -> {
                diagnosisCalendar.set(year, month, dayOfMonth);
                tvDiagnosisDate.setText(dateFormat.format(diagnosisCalendar.getTime()));
            },
            diagnosisCalendar.get(Calendar.YEAR),
            diagnosisCalendar.get(Calendar.MONTH),
            diagnosisCalendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }
    
    private void saveMedicalHistory() {
        // 验证必填字段
        String diseaseName = etDiseaseName.getText().toString().trim();
        if (diseaseName.isEmpty()) {
            ToastUtils.show("请输入疾病名称");
            etDiseaseName.requestFocus();
            return;
        }
        
        String diagnosisDate = tvDiagnosisDate.getText().toString().trim();
        if (diagnosisDate.isEmpty()) {
            ToastUtils.show("请选择诊断时间");
            return;
        }
        
        // 创建或更新病史记录
        if (currentHistory == null) {
            currentHistory = new MedicalHistory();
        }
        
        currentHistory.setDiseaseName(diseaseName);
        currentHistory.setDiagnosisDate(diagnosisDate);
        currentHistory.setSeverity(spinnerSeverity.getSelectedItem().toString());
        currentHistory.setTreatmentStatus(spinnerTreatmentStatus.getSelectedItem().toString());
        currentHistory.setHospital(etHospital.getText().toString().trim());
        currentHistory.setDoctor(etDoctor.getText().toString().trim());
        currentHistory.setSymptoms(etSymptoms.getText().toString().trim());
        currentHistory.setTreatment(etTreatment.getText().toString().trim());
        currentHistory.setNotes(etNotes.getText().toString().trim());
        
        boolean success;
        if (isEditMode) {
            success = medicalHistoryDao.update(currentHistory);
        } else {
            long id = medicalHistoryDao.insert(currentHistory);
            success = id != -1;
        }
        
        if (success) {
            setResult(RESULT_OK);
            finish();
        } else {
            ToastUtils.show("保存失败，请重试");
        }
    }
    
    private void loadMedicalHistoryData(Long historyId) {
        currentHistory = medicalHistoryDao.findById(historyId);
        if (currentHistory != null) {
            etDiseaseName.setText(currentHistory.getDiseaseName());
            tvDiagnosisDate.setText(currentHistory.getDiagnosisDate());
            
            // 设置严重程度选择
            String severity = currentHistory.getSeverity();
            if (severity != null) {
                ArrayAdapter<String> severityAdapter = (ArrayAdapter<String>) spinnerSeverity.getAdapter();
                int severityPosition = severityAdapter.getPosition(severity);
                if (severityPosition >= 0) {
                    spinnerSeverity.setSelection(severityPosition);
                }
            }
            
            // 设置治疗状况选择
            String treatmentStatus = currentHistory.getTreatmentStatus();
            if (treatmentStatus != null) {
                ArrayAdapter<String> treatmentAdapter = (ArrayAdapter<String>) spinnerTreatmentStatus.getAdapter();
                int treatmentPosition = treatmentAdapter.getPosition(treatmentStatus);
                if (treatmentPosition >= 0) {
                    spinnerTreatmentStatus.setSelection(treatmentPosition);
                }
            }
            
            etHospital.setText(currentHistory.getHospital());
            etDoctor.setText(currentHistory.getDoctor());
            etSymptoms.setText(currentHistory.getSymptoms());
            etTreatment.setText(currentHistory.getTreatment());
            etNotes.setText(currentHistory.getNotes());
        }
    }
}
