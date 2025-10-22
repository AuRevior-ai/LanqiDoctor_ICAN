package com.lanqiDoctor.demo.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hjq.base.BaseActivity;
import com.lanqiDoctor.demo.R;

/**
 * 健康功能详情页面
 * 
 * 通用的功能详情显示页面，用于展示各种健康功能的具体信息
 * 包括：语音对话、文字输入、药品图片识别、症状轨迹可视化、个性目标等
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class HealthFunctionActivity extends BaseActivity {
    
    private ImageView ivBack;
    private TextView tvTitle;
    private TextView tvFunctionTitle;
    private TextView tvDescription;
    private Button btnBack;
    
    private String functionType;
    private String title;
    private String description;
    
    @Override
    protected int getLayoutId() {
        return R.layout.health_function_activity;
    }
    
    @Override
    protected void initView() {
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        tvFunctionTitle = findViewById(R.id.tv_function_title);
        tvDescription = findViewById(R.id.tv_description);
        btnBack = findViewById(R.id.btn_back);
    }
    
    @Override
    protected void initData() {
        Intent intent = getIntent();
        functionType = intent.getStringExtra("function_type");
        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");
        
        if (title != null) {
            tvTitle.setText(title);
            tvFunctionTitle.setText(title);
        }
        
        if (description != null) {
            tvDescription.setText(description);
        }
        
        initListener();
    }
    
    private void initListener() {
        // 返回按钮点击事件
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        // 返回首页按钮点击事件
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
} 