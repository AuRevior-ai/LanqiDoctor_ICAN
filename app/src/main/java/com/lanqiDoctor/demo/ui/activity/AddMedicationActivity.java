package com.lanqiDoctor.demo.ui.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.lanqiDoctor.demo.database.dao.MedicationRecordDao;
import com.lanqiDoctor.demo.database.entity.MedicationRecord;
import com.lanqiDoctor.demo.manager.TodayMedicationManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 添加/编辑用药Activity
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class AddMedicationActivity extends BaseActivity {
    
    private EditText etMedicationName;
    private EditText etDosage;
    private Spinner spinnerFrequency;
    private Spinner spinnerUnit;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private EditText etNotes;
    private Button btnSave;
    private Button btnCancel;
    
    private MedicationRecordDao medicationDao;
    private MedicationRecord currentMedication;
    private boolean isEditMode = false;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    
    @Override
    protected int getLayoutId() {
        return R.layout.add_medication_activity;
    }
    
    @Override
    protected void initView() {
        etMedicationName = findViewById(R.id.et_medication_name);
        etDosage = findViewById(R.id.et_dosage);
        spinnerFrequency = findViewById(R.id.spinner_frequency);
        spinnerUnit = findViewById(R.id.spinner_unit);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);
        etNotes = findViewById(R.id.et_notes);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        
        setOnClickListener(tvStartDate, tvEndDate, btnSave, btnCancel);
        
        // 设置频率下拉框
        String[] frequencies = {"每日1次", "每日2次", "每日3次", "按需服用"};
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, frequencies);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(frequencyAdapter);
        
        // 设置单位下拉框
        String[] units = {"片", "粒", "毫升", "毫克", "克", "袋", "支", "滴"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, units);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);
    }
    
    @Override
    protected void initData() {
        medicationDao = new MedicationRecordDao(this);
        
        // 检查是否为编辑模式
        long medicationId = getIntent().getLongExtra("medication_id", -1);
        isEditMode = getIntent().getBooleanExtra("is_edit", false);
        
        if (isEditMode && medicationId != -1) {
            currentMedication = medicationDao.findById(medicationId);
            if (currentMedication != null) {
                fillDataForEdit();
            }
        } else {
            // 设置默认开始时间为今天
            tvStartDate.setText(dateFormat.format(new Date()));
            startCalendar.setTime(new Date());
            
            // 检查是否为批量模式，如果是则自动填充数据
            fillDataFromIntent();
        }
        
        // 如果是批量模式，更新标题
        updateTitleForBatchMode();
    }
    
    /**
     * 从Intent中填充药品数据（批量模式）
     */
    private void fillDataFromIntent() {
        Intent intent = getIntent();
        
        // 填充药品名称
        String medicationName = intent.getStringExtra("medication_name");
        if (medicationName != null && !medicationName.trim().isEmpty()) {
            etMedicationName.setText(medicationName.trim());
        }
        
        // 填充剂量
        String dosage = intent.getStringExtra("dosage");
        if (dosage != null && !dosage.trim().isEmpty()) {
            etDosage.setText(dosage.trim());
        }
        
        // 填充频率
        String frequency = intent.getStringExtra("frequency");
        if (frequency != null && !frequency.trim().isEmpty()) {
            setSpinnerSelection(spinnerFrequency, frequency);
        }
        
        // 填充单位
        String unit = intent.getStringExtra("unit");
        if (unit != null && !unit.trim().isEmpty()) {
            setSpinnerSelection(spinnerUnit, unit);
        }
        
        // 检查剂量和单位的组合，看是否需要毫克换算提示
        if (dosage != null && unit != null) {
            String combinedText = dosage.toLowerCase() + " " + unit.toLowerCase();
            if (combinedText.contains("mg") || combinedText.contains("毫克")) {
                // 延迟执行，确保UI已经更新
                etDosage.post(this::handleMilligramConversion);
            }
        }
        
        // 填充备注
        String notes = intent.getStringExtra("notes");
        if (notes != null && !notes.trim().isEmpty()) {
            etNotes.setText(notes.trim());
        }
    }
    
    /**
     * 设置Spinner的选中项
     */
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }
        
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        
        // 首先尝试精确匹配
        int position = adapter.getPosition(value.trim());
        if (position >= 0) {
            spinner.setSelection(position);
            return;
        }
        
        // 如果精确匹配失败，尝试模糊匹配
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = adapter.getItem(i);
            if (item != null && (item.contains(value) || value.contains(item))) {
                spinner.setSelection(i);
                return;
            }
        }
        
        // 针对不同的Spinner做特殊处理
        if (spinner == spinnerFrequency) {
            handleFrequencySelection(value, adapter, spinner);
        } else if (spinner == spinnerUnit) {
            handleUnitSelection(value, adapter, spinner);
        }
    }
    
    /**
     * 处理频率选择的特殊逻辑
     */
    private void handleFrequencySelection(String value, ArrayAdapter<String> adapter, Spinner spinner) {
        String lowerValue = value.toLowerCase();
        
        if (lowerValue.contains("1") || lowerValue.contains("一") || lowerValue.contains("once")) {
            spinner.setSelection(0); // 每日1次
        } else if (lowerValue.contains("2") || lowerValue.contains("二") || lowerValue.contains("twice")) {
            spinner.setSelection(1); // 每日2次
        } else if (lowerValue.contains("3") || lowerValue.contains("三") || lowerValue.contains("three")) {
            spinner.setSelection(2); // 每日3次
        } else if (lowerValue.contains("按需") || lowerValue.contains("需要") || lowerValue.contains("prn")) {
            spinner.setSelection(3); // 按需服用
        }
    }
    
    /**
     * 处理单位选择的特殊逻辑
     */
    private void handleUnitSelection(String value, ArrayAdapter<String> adapter, Spinner spinner) {
        String lowerValue = value.toLowerCase();
        
        // 常见单位的映射关系
        if (lowerValue.contains("片") || lowerValue.contains("tablet")) {
            setSpinnerByValue(spinner, "片");
        } else if (lowerValue.contains("粒") || lowerValue.contains("capsule") || lowerValue.contains("胶囊")) {
            setSpinnerByValue(spinner, "粒");
        } else if (lowerValue.contains("ml") || lowerValue.contains("毫升") || lowerValue.contains("液")) {
            setSpinnerByValue(spinner, "毫升");
        } else if (lowerValue.contains("mg") || lowerValue.contains("毫克") || lowerValue.contains("milligram")) {
            setSpinnerByValue(spinner, "毫克");
            // 如果剂量字段有值且包含毫克信息，可以考虑换算
            handleMilligramConversion();
        } else if (lowerValue.contains("g") || lowerValue.contains("克") || lowerValue.contains("gram")) {
            // 确保不是毫克（mg）
            if (!lowerValue.contains("mg") && !lowerValue.contains("毫克")) {
                setSpinnerByValue(spinner, "克");
            } else {
                setSpinnerByValue(spinner, "毫克");
                handleMilligramConversion();
            }
        } else if (lowerValue.contains("袋") || lowerValue.contains("包") || lowerValue.contains("bag")) {
            setSpinnerByValue(spinner, "袋");
        } else if (lowerValue.contains("支") || lowerValue.contains("管") || lowerValue.contains("tube")) {
            setSpinnerByValue(spinner, "支");
        } else if (lowerValue.contains("滴") || lowerValue.contains("drop")) {
            setSpinnerByValue(spinner, "滴");
        }
        // 如果都不匹配，保持默认选择（片）
    }
    
    /**
     * 根据值设置Spinner选择
     */
    private void setSpinnerByValue(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }
    
    /**
     * 处理毫克换算逻辑
     * 如果剂量字段中有大于1000的毫克数值，提示用户是否换算为克
     */
    private void handleMilligramConversion() {
        String dosageText = etDosage.getText().toString().trim();
        if (dosageText.isEmpty()) {
            return;
        }
        
        try {
            // 尝试从剂量文本中提取数字
            String numberStr = dosageText.replaceAll("[^0-9.]", "");
            if (!numberStr.isEmpty()) {
                double dosageValue = Double.parseDouble(numberStr);
                
                // 如果毫克数量大于等于1000，建议换算为克
                if (dosageValue >= 1000) {
                    double gramValue = dosageValue / 1000;
                    
                    // 使用Toast提示用户，也可以用对话框
                    String message = String.format("检测到 %.0f毫克，建议换算为 %.1f克", dosageValue, gramValue);
                    ToastUtils.show(message);
                    
                    // 可以选择自动换算（注释掉如果不需要自动换算）
                    // etDosage.setText(String.valueOf(gramValue));
                    // setSpinnerByValue(spinnerUnit, "克");
                }
            }
        } catch (NumberFormatException e) {
            // 如果无法解析数字，忽略换算
        }
    }
    
    /**
     * 更新批量模式的标题
     */
    private void updateTitleForBatchMode() {
        boolean isBatchMode = getIntent().getBooleanExtra("is_batch_mode", false);
        if (isBatchMode) {
            int currentIndex = getIntent().getIntExtra("current_index", 1);
            int totalCount = getIntent().getIntExtra("total_count", 1);
            
            // 这里可以设置ActionBar标题，如果有的话
            // setTitle("添加药品 (" + currentIndex + "/" + totalCount + ")");
            
            // 或者在btnSave按钮上显示进度
            btnSave.setText("保存并继续 (" + currentIndex + "/" + totalCount + ")");
        }
    }
    
    @Override
    public void onClick(View view) {
        if (view == tvStartDate) {
            showDatePicker(true);
        } else if (view == tvEndDate) {
            showDatePicker(false);
        } else if (view == btnSave) {
            saveMedication();
        } else if (view == btnCancel) {
            finish();
        }
    }
    
    /**
     * 填充编辑数据
     */
    private void fillDataForEdit() {
        etMedicationName.setText(currentMedication.getMedicationName());
        etDosage.setText(currentMedication.getDosage());
        
        // 设置频率
        ArrayAdapter<String> frequencyAdapter = (ArrayAdapter<String>) spinnerFrequency.getAdapter();
        int frequencyPosition = frequencyAdapter.getPosition(currentMedication.getFrequency());
        if (frequencyPosition >= 0) {
            spinnerFrequency.setSelection(frequencyPosition);
        }
        
        // 设置单位
        ArrayAdapter<String> unitAdapter = (ArrayAdapter<String>) spinnerUnit.getAdapter();
        int unitPosition = unitAdapter.getPosition(currentMedication.getUnit());
        if (unitPosition >= 0) {
            spinnerUnit.setSelection(unitPosition);
        }
        
        // 设置开始时间
        if (currentMedication.getStartDate() != null) {
            startCalendar.setTimeInMillis(currentMedication.getStartDate());
            tvStartDate.setText(dateFormat.format(new Date(currentMedication.getStartDate())));
        }
        
        // 设置结束时间
        if (currentMedication.getEndDate() != null) {
            endCalendar.setTimeInMillis(currentMedication.getEndDate());
            tvEndDate.setText(dateFormat.format(new Date(currentMedication.getEndDate())));
        }
        
        // 设置备注
        if (currentMedication.getNotes() != null) {
            etNotes.setText(currentMedication.getNotes());
        }
    }
    
    /**
     * 显示日期选择器
     */
    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = isStartDate ? startCalendar : endCalendar;
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String dateString = dateFormat.format(calendar.getTime());
                    
                    if (isStartDate) {
                        tvStartDate.setText(dateString);
                    } else {
                        tvEndDate.setText(dateString);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    /**
     * 保存用药记录
     */
    private void saveMedication() {
        // 验证输入
        String medicationName = etMedicationName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String frequency = spinnerFrequency.getSelectedItem().toString();
        String unit = spinnerUnit.getSelectedItem().toString();
        String notes = etNotes.getText().toString().trim();
        
        if (medicationName.isEmpty()) {
            ToastUtils.show("请输入药品名称");
            etMedicationName.requestFocus();
            return;
        }
        
        if (dosage.isEmpty()) {
            ToastUtils.show("请输入用药剂量");
            etDosage.requestFocus();
            return;
        }
        
        // 获取当前用户ID
        String userId = com.lanqiDoctor.demo.manager.UserStateManager.getInstance(this).getUserId();
        if (userId == null || userId.isEmpty()) {
            ToastUtils.show("用户信息获取失败，请重新登录");
            return;
        }
        // 创建或更新用药记录
        MedicationRecord medication;
        if (isEditMode && currentMedication != null) {
            medication = currentMedication;
            medication.setUpdateTime(System.currentTimeMillis()); // 更新时间
        } else {
            medication = new MedicationRecord();
            medication.setCreateTime(System.currentTimeMillis()); // 创建时间
            medication.setUpdateTime(System.currentTimeMillis()); // 更新时间
            medication.setStatus(1); // 设置为活跃状态
        }
        
        // 设置用户ID（重要！）
        medication.setUserId(userId);
        
        medication.setMedicationName(medicationName);
        medication.setDosage(dosage);
        medication.setFrequency(frequency);
        medication.setUnit(unit);
        medication.setStartDate(startCalendar.getTimeInMillis());
        
        // 设置结束时间（如果选择了）
        if (!tvEndDate.getText().toString().trim().isEmpty() && 
            !tvEndDate.getText().toString().equals("点击选择结束时间")) {
            medication.setEndDate(endCalendar.getTimeInMillis());
        }
        
        if (!notes.isEmpty()) {
            medication.setNotes(notes);
        }
        
        // 保存到数据库
        long result;
        if (isEditMode) {
            // 使用基于名称的更新方法，而非基于ID
            result = medicationDao.updateByName(medication);
            if (result > 0) {
                ToastUtils.show("用药记录更新成功");
            } else {
                ToastUtils.show("用药记录更新失败");
                return;
            }
        } else {
            // 修改这里：使用新的insertOrUpdateByName方法，而非老旧的insert
            result = medicationDao.insertOrUpdateByName(medication);
            if (result > 0) {
                ToastUtils.show("用药记录添加成功");
            } else {
                ToastUtils.show("用药记录添加失败");
                return;
            }
        }
        // 每次添加药品结束后，强制刷新一次
        TodayMedicationManager.getInstance(this).forceReinitTodayMedicationData();
        setResult(RESULT_OK);
        finish();
    }}