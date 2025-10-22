package com.lanqiDoctor.demo.ui.activity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.hjq.base.BaseActivity;
import com.hjq.toast.ToastUtils;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.manager.VivoHealthManager;

/**
 * 用户健康信息设置页面
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class UserHealthInfoActivity extends BaseActivity {
    
    private EditText etHeight;
    private EditText etWeight;
    private EditText etBirthYear;
    private EditText etDailySteps;
    private RadioGroup rgGender;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private Button btnSave;
    private Button btnReset;
    
    private VivoHealthManager vivoHealthManager;
    
    @Override
    protected int getLayoutId() {
        return R.layout.user_health_info_activity;
    }
    
    @Override
    protected void initView() {
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        etBirthYear = findViewById(R.id.et_birth_year);
        etDailySteps = findViewById(R.id.et_daily_steps);
        rgGender = findViewById(R.id.rg_gender);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        btnSave = findViewById(R.id.btn_save);
        btnReset = findViewById(R.id.btn_reset);
        
        setOnClickListener(btnSave, btnReset);
    }
    
    @Override
    protected void initData() {
        vivoHealthManager = VivoHealthManager.getInstance(this);
        
        // 设置默认值
        etBirthYear.setText("1990");
        etDailySteps.setText("8000");
        rbMale.setChecked(true);
    }
    
    @Override
    public void onClick(View view) {
        if (view == btnSave) {
            saveUserInfo();
        } else if (view == btnReset) {
            resetUserInfo();
        }
    }
    
    private void saveUserInfo() {
        try {
            String heightStr = etHeight.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();
            String birthYearStr = etBirthYear.getText().toString().trim();
            String dailyStepsStr = etDailySteps.getText().toString().trim();
            
            if (heightStr.isEmpty()) {
                ToastUtils.show("请输入身高");
                return;
            }
            
            if (weightStr.isEmpty()) {
                ToastUtils.show("请输入体重");
                return;
            }
            
            if (birthYearStr.isEmpty()) {
                ToastUtils.show("请输入出生年份");
                return;
            }
            
            double height = Double.parseDouble(heightStr);
            double weight = Double.parseDouble(weightStr);
            int birthYear = Integer.parseInt(birthYearStr);
            int dailySteps = dailyStepsStr.isEmpty() ? 8000 : Integer.parseInt(dailyStepsStr);
            int gender = rbMale.isChecked() ? 1 : 0;
            
            // 数据验证
            if (height < 100 || height > 250) {
                ToastUtils.show("身高应在100-250cm之间");
                return;
            }
            
            if (weight < 30 || weight > 200) {
                ToastUtils.show("体重应在30-200kg之间");
                return;
            }
            
            if (birthYear < 1900 || birthYear > 2020) {
                ToastUtils.show("出生年份应在1900-2020之间");
                return;
            }
            
            // 保存数据
            vivoHealthManager.setUserHeight(height);
            vivoHealthManager.setUserWeight(weight);
            vivoHealthManager.setUserBirthYear(birthYear);
            vivoHealthManager.setUserGender(gender);
            vivoHealthManager.setDailyStepsBase(dailySteps);
            
            ToastUtils.show("用户信息保存成功");
            finish();
            
        } catch (NumberFormatException e) {
            ToastUtils.show("请输入有效的数字");
        }
    }
    
    private void resetUserInfo() {
        vivoHealthManager.resetUserData();
        
        // 清空输入框
        etHeight.setText("");
        etWeight.setText("");
        etBirthYear.setText("1990");
        etDailySteps.setText("8000");
        rbMale.setChecked(true);
    }
}