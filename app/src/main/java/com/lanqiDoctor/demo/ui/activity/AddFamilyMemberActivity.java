package com.lanqiDoctor.demo.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;
import com.hjq.base.BaseActivity;
import com.hjq.toast.ToastUtils;
import com.hjq.widget.view.SubmitButton;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.http.api.AddFriendApi;
import com.lanqiDoctor.demo.http.model.HttpData;
import com.lanqiDoctor.demo.manager.UserStateManager;
import com.lanqiDoctor.demo.util.EmailValidator;
import com.lanqiDoctor.demo.util.NetworkErrorHandler;
import com.lanqiDoctor.demo.config.NetworkConfig;
import com.hjq.http.EasyHttp;
import com.hjq.http.listener.OnHttpListener;

import okhttp3.Call;

/**
 * 添加家庭成员Activity
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class AddFamilyMemberActivity extends BaseActivity {
    
    private static final String TAG = "AddFamilyMemberActivity";
    
    private EditText etMemberEmail;
    private EditText etMemberPassword;
    private EditText etMemberNickname;
    private SubmitButton btnAddMember;
    private TitleBar titleBar;
    
    private NetworkConfig networkConfig;
    private UserStateManager userStateManager;
    
    /**
     * 启动添加家庭成员Activity
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, AddFamilyMemberActivity.class);
        if (!(context instanceof BaseActivity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
    
    /**
     * 启动添加家庭成员Activity，并返回结果
     */
    public static void startForResult(BaseActivity activity, int requestCode) {
        Intent intent = new Intent(activity, AddFamilyMemberActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }
    
    @Override
    protected int getLayoutId() {
        return R.layout.add_family_member_activity;
    }
    
    @Override
    protected void initView() {
        // 查找视图并添加空值检查
        titleBar = findViewById(R.id.title_bar);
        etMemberEmail = findViewById(R.id.et_member_email);
        etMemberPassword = findViewById(R.id.et_member_password);
        etMemberNickname = findViewById(R.id.et_member_nickname);
        btnAddMember = findViewById(R.id.btn_add_member);
        
        // 设置标题栏返回按钮
        if (titleBar != null) {
            titleBar.setOnTitleBarListener(new OnTitleBarListener() {
                @Override
                public void onLeftClick(View view) {
                    finish();
                }
                
                @Override
                public void onTitleClick(View view) {
                    // 标题点击事件
                }
                
                @Override
                public void onRightClick(View view) {
                    // 右侧按钮点击事件
                }
            });
        }
        
        // 设置点击监听器（只对非空视图）
        if (btnAddMember != null) {
            setOnClickListener(btnAddMember);
        }
        
        // 检查关键视图是否存在
        if (etMemberEmail == null || etMemberPassword == null || 
            etMemberNickname == null || btnAddMember == null) {
            ToastUtils.show("页面加载失败，请重试");
            finish();
            return;
        }
        
        Log.d(TAG, "页面加载完成");
    }
    
    @Override
    protected void initData() {
        try {
            networkConfig = NetworkConfig.getInstance(this);
            userStateManager = UserStateManager.getInstance(this);
            
            // 检查用户是否已登录
            if (!userStateManager.isUserLoggedIn()) {
                ToastUtils.show("请先登录");
                finish();
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "初始化数据失败", e);
            ToastUtils.show("初始化失败，请重试");
            finish();
        }
    }
    
    @Override
    public void onClick(View view) {
        int id = view.getId();
        
        if (id == R.id.btn_add_member) {
            addFamilyMember();
        }
    }
    
    @Override
    public void onBackPressed() {
        // 确保返回键能正常工作
        super.onBackPressed();
    }
    
    /**
     * 添加家庭成员
     */
    private void addFamilyMember() {
        // 检查视图是否存在
        if (etMemberEmail == null || etMemberPassword == null || 
            etMemberNickname == null || btnAddMember == null) {
            ToastUtils.show("页面异常，请重试");
            return;
        }
        
        String email = etMemberEmail.getText().toString().trim();
        String password = etMemberPassword.getText().toString();
        String nickname = etMemberNickname.getText().toString().trim();
        
        // 验证邮箱格式
        if (!EmailValidator.isValidEmail(email, networkConfig)) {
            etMemberEmail.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_anim));
            btnAddMember.showError(3000);
            String errorMsg = EmailValidator.getEmailErrorMessage(email, networkConfig);
            ToastUtils.show(errorMsg != null ? errorMsg : "请输入正确的邮箱格式");
            return;
        }
        
        // 验证密码
        if (password.length() < 6) {
            etMemberPassword.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_anim));
            btnAddMember.showError(3000);
            ToastUtils.show("密码长度不能少于6位");
            return;
        }
        
        // 验证昵称
        if (nickname.isEmpty()) {
            etMemberNickname.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_anim));
            btnAddMember.showError(3000);
            ToastUtils.show("请输入家庭成员昵称");
            return;
        }
        
        // 检查是否是自己的账号
        if (userStateManager != null && email.equals(userStateManager.getUserEmail())) {
            ToastUtils.show("不能添加自己为家庭成员");
            btnAddMember.showError(3000);
            return;
        }
        
        // 隐藏软键盘
        hideKeyboard(getCurrentFocus());
        
        // 执行添加请求
        performAddFamilyMember(email, password, nickname);
    }
    
    /**
     * 执行添加家庭成员请求
     */
    private void performAddFamilyMember(String email, String password, String nickname) {
        if (btnAddMember == null) {
            ToastUtils.show("页面异常，请重试");
            return;
        }
        
        Log.d(TAG, "开始添加家庭成员: " + email);
        
        EasyHttp.post(this)
                .api(new AddFriendApi()
                        .setFriendEmail(email)
                        .setFriendPassword(password)
                        .setFriendNickname(nickname))
                .request(new OnHttpListener<HttpData<AddFriendApi.Bean>>() {
                    
                    @Override
                    public void onStart(Call call) {
                        if (btnAddMember != null) {
                            btnAddMember.showProgress();
                        }
                        Log.d(TAG, "开始网络请求");
                    }
                    
                    @Override
                    public void onEnd(Call call) {
                        Log.d(TAG, "网络请求结束");
                    }
                    
                    @Override
                    public void onSucceed(HttpData<AddFriendApi.Bean> data) {
                        Log.d(TAG, "网络请求成功: " + data);
                        
                        AddFriendApi.Bean result = new AddFriendApi.Bean(data);
                        
                        if (result != null && result.isSuccess()) {
                            Log.d(TAG, "添加家庭成员成功");
                            
                            // 确保在主线程中更新UI
                            runOnUiThread(() -> {
                                if (btnAddMember != null) {
                                    btnAddMember.showSucceed();
                                }
                                
                                postDelayed(() -> {
                                    ToastUtils.show("家庭成员添加成功");
                                    
                                    // 返回结果给调用方
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("member_added", true);
                                    resultIntent.putExtra("member_email", email);
                                    resultIntent.putExtra("member_nickname", nickname);
                                    
                                    // 如果服务器返回了用户ID，则添加到结果中
                                    if (result.getData() != null && result.getData().getFriendUserId() != null) {
                                        resultIntent.putExtra("member_id", result.getData().getFriendUserId());
                                    }
                                    
                                    setResult(RESULT_OK, resultIntent);
                                    finish();
                                }, 1000);
                            });
                        } else {
                            String errorMsg = result != null ? result.getMessage() : "添加失败";
                            Log.e(TAG, "添加家庭成员失败: " + errorMsg);
                            
                            runOnUiThread(() -> {
                                if (btnAddMember != null) {
                                    btnAddMember.showError(3000);
                                }
                                ToastUtils.show(errorMsg);
                            });
                        }
                    }
                    
                    @Override
                    public void onFail(Exception e) {
                        Log.e(TAG, "网络请求失败", e);
                        
                        // 使用工具类获取错误提示
                        String errorMessage = NetworkErrorHandler.getAddFriendErrorMessage(e);
                        
                        runOnUiThread(() -> {
                            ToastUtils.show(errorMessage);
                            
                            if (btnAddMember != null) {
                                btnAddMember.showError(3000);
                            }
                        });
                    }
                });
    }
}