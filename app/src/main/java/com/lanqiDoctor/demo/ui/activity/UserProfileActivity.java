package com.lanqiDoctor.demo.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import com.hjq.base.BaseActivity;
import com.hjq.http.EasyConfig;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.manager.UserStateManager;
import com.lanqiDoctor.demo.util.LoginCheckUtil;

/**
 * 用户信息页面
 * 
 * 显示用户个人信息、健康档案、设置选项等
 * 迁移自myapplication2项目的用户信息功能
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class UserProfileActivity extends BaseActivity {
    
    private ImageView ivBack;
    private TextView tvTitle;
    private ImageView ivUserAvatar;
    private TextView tvUserName;
    private TextView tvUserInfo;
    
    // 功能选项
    private LinearLayout llPersonalInfo;
    private LinearLayout llHealthRecord;
    // 移除：private LinearLayout llSettings; - 布局中不存在此ID
    private LinearLayout llPreferences; // 新增：偏好设置
    private LinearLayout llPrivacy;
    private LinearLayout llAbout;
    private LinearLayout llLogout;
    
    private UserStateManager userStateManager;
    
    @Override
    protected int getLayoutId() {
        return R.layout.user_profile_activity;
    }
    
    @Override
    protected void initView() {
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        ivUserAvatar = findViewById(R.id.iv_user_avatar);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserInfo = findViewById(R.id.tv_user_info);
        
        // 功能选项
        llPersonalInfo = findViewById(R.id.ll_personal_info);
        llPreferences = findViewById(R.id.ll_preferences); // 新增
        llPrivacy = findViewById(R.id.ll_privacy);
        llAbout = findViewById(R.id.ll_about);
        llLogout = findViewById(R.id.ll_logout);
    }
    
    @Override
    protected void initData() {
        userStateManager = UserStateManager.getInstance(this);
        
        tvTitle.setText("用户信息");
        
        // 加载用户信息
        loadUserInfo();
        
        initListener();
    }
    
    private void loadUserInfo() {
        // 从UserStateManager获取用户信息
        String nickname = userStateManager.getUserNickname();
        String email = userStateManager.getUserEmail();
        
        if (!nickname.isEmpty()) {
            tvUserName.setText(nickname);
        } else if (!email.isEmpty()) {
            tvUserName.setText(email);
        } else {
            tvUserName.setText("用户");
        }
        
        // 显示用户详细信息
        long loginTime = userStateManager.getLastLoginTime();
        String lastLoginText = "";
        if (loginTime > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年MM月", java.util.Locale.CHINA);
            lastLoginText = "注册时间: " + sdf.format(new java.util.Date(loginTime));
        }
        
        String emailVerified = userStateManager.isEmailVerified() ? "已验证" : "未验证";
        tvUserInfo.setText(lastLoginText + " • 邮箱: " + emailVerified);
    }
    
    private void initListener() {
        // 返回按钮点击事件
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        // 个人资料点击事件
        llPersonalInfo.setOnClickListener(v -> {
            startActivity(new Intent(this, PersonalDataActivity.class));
        });
                
        // 偏好设置点击事件（替代原来的应用设置）
        llPreferences.setOnClickListener(v -> {
            startActivity(new Intent(this, UserPreferencesActivity.class));
        });
        
        // 隐私安全点击事件
        llPrivacy.setOnClickListener(v -> {
            Intent intent = new Intent(this, PrivateActivity.class);
            startActivity(intent);
        });
        
        // 关于我们点击事件
        llAbout.setOnClickListener(v -> {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        });
        
        // 退出登录点击事件
        llLogout.setOnClickListener(v -> {
            showLogoutDialog();
        });
    }
    
    /**
     * 显示退出登录确认对话框
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("确认退出")
                .setMessage("确定要退出当前账户吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    /**
     * 执行退出登录操作
     */
    private void performLogout() {
        // 清除用户状态
        userStateManager.logout();
        
        // 清除HTTP配置中的Token
        EasyConfig.getInstance()
                .removeParam("token")
                .removeParam("refreshToken");
        
        Toast.makeText(this, "已成功退出", Toast.LENGTH_SHORT).show();
        
        // 跳转到登录页面
        LoginCheckUtil.forceRelogin(this, "用户主动退出");
        finish();
    }
}