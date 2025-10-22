package com.lanqiDoctor.demo.ui.activity;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.gyf.immersionbar.ImmersionBar;
import com.hjq.base.BaseActivity;
import com.hjq.base.BaseDialog;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.aop.Log;
import com.lanqiDoctor.demo.aop.SingleClick;
import com.lanqiDoctor.demo.app.AppActivity;
import com.lanqiDoctor.demo.config.NetworkConfig;
import com.lanqiDoctor.demo.http.api.RegisterApi;
import com.lanqiDoctor.demo.http.api.SendEmailCodeApi;
import com.lanqiDoctor.demo.http.model.HttpData;
import com.lanqiDoctor.demo.manager.InputTextManager;
import com.lanqiDoctor.demo.util.EmailValidator;
import com.lanqiDoctor.demo.util.NetworkErrorHandler;
import com.hjq.http.EasyHttp;
import com.hjq.http.listener.HttpCallback;
import com.hjq.widget.view.CountdownView;
import com.hjq.widget.view.SubmitButton;

import okhttp3.Call;

/**
 * 注册界面 - 邮箱版本
 * 
 * @author 蓝岐医童开发团队
 * @version 2.0
 */
public final class RegisterActivity extends AppActivity
        implements TextView.OnEditorActionListener {

    private static final String INTENT_KEY_EMAIL = "email";
    private static final String INTENT_KEY_PASSWORD = "password";

    @Log
    public static void start(BaseActivity activity, String email, String password, OnRegisterListener listener) {
        Intent intent = new Intent(activity, RegisterActivity.class);
        intent.putExtra(INTENT_KEY_EMAIL, email);
        intent.putExtra(INTENT_KEY_PASSWORD, password);
        activity.startActivityForResult(intent, (resultCode, data) -> {

            if (listener == null || data == null) {
                return;
            }

            if (resultCode == RESULT_OK) {
                listener.onSucceed(data.getStringExtra(INTENT_KEY_EMAIL), data.getStringExtra(INTENT_KEY_PASSWORD));
            } else {
                listener.onCancel();
            }
        });
    }

    private EditText mEmailView;
    private CountdownView mCountdownView;
    private EditText mCodeView;
    private EditText mFirstPassword;
    private EditText mSecondPassword;
    private EditText mNicknameView;
    private SubmitButton mCommitView;

    private NetworkConfig networkConfig;

    @Override
    protected int getLayoutId() {
        return R.layout.register_activity;
    }

    @Override
    protected void initView() {
        mEmailView = findViewById(R.id.et_register_email);
        mCountdownView = findViewById(R.id.cv_register_countdown);
        mCodeView = findViewById(R.id.et_register_code);
        mFirstPassword = findViewById(R.id.et_register_password1);
        mSecondPassword = findViewById(R.id.et_register_password2);
        mNicknameView = findViewById(R.id.et_register_nickname);
        mCommitView = findViewById(R.id.btn_register_commit);
    
        setOnClickListener(mCountdownView, mCommitView);

        // 设置倒计时秒数
        mCountdownView.setTotalTime(60);

        mSecondPassword.setOnEditorActionListener(this);
    
        // 给这个 View 设置沉浸式，避免状态栏遮挡
        ImmersionBar.setTitleBar(this, findViewById(R.id.tv_register_title));
    
        InputTextManager.with(this)
                .addView(mEmailView)
                .addView(mCodeView)
                .addView(mFirstPassword)
                .addView(mSecondPassword)
                .addView(mNicknameView)
                .setMain(mCommitView)
                .build();
    }

    @Override
    protected void initData() {
        networkConfig = NetworkConfig.getInstance(this);
        
        // 自动填充邮箱和密码 - 修复：使用 getIntent().getStringExtra
        mEmailView.setText(getIntent().getStringExtra(INTENT_KEY_EMAIL));
        mFirstPassword.setText(getIntent().getStringExtra(INTENT_KEY_PASSWORD));
        mSecondPassword.setText(getIntent().getStringExtra(INTENT_KEY_PASSWORD));
    }

    @SingleClick
    @Override
    public void onClick(View view) {
        if (view == mCountdownView) {
            String email = mEmailView.getText().toString().trim();
            
            // 验证邮箱格式
            if (!EmailValidator.isValidEmail(email, networkConfig)) {
                mEmailView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim));
                String errorMsg = EmailValidator.getEmailErrorMessage(email, networkConfig);
                toast(errorMsg != null ? errorMsg : "请输入正确的邮箱格式");
                return;
            }
    
            // 修改：移除模拟逻辑，直接发送邮箱验证码
            sendEmailVerificationCode(email);
            
        } else if (view == mCommitView) {
            String email = mEmailView.getText().toString().trim();
            String code = mCodeView.getText().toString().trim();
            String password1 = mFirstPassword.getText().toString();
            String password2 = mSecondPassword.getText().toString();
            String nickname = mNicknameView.getText().toString().trim();
    
            // 验证邮箱格式
            if (!EmailValidator.isValidEmail(email, networkConfig)) {
                mEmailView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim));
                mCommitView.showError(3000);
                String errorMsg = EmailValidator.getEmailErrorMessage(email, networkConfig);
                toast(errorMsg != null ? errorMsg : "请输入正确的邮箱格式");
                return;
            }
    
            // 验证验证码长度
            int codeLength = networkConfig.getVerificationCodeLength();
            if (code.length() != codeLength) {
                mCodeView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim));
                mCommitView.showError(3000);
                toast("请输入" + codeLength + "位验证码");
                return;
            }
    
            // 验证密码长度
            if (password1.length() < 6) {
                mFirstPassword.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim));
                mCommitView.showError(3000);
                toast("密码长度不能少于6位");
                return;
            }
    
            // 验证密码一致性
            if (!password1.equals(password2)) {
                mFirstPassword.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim));
                mSecondPassword.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim));
                mCommitView.showError(3000);
                toast("两次输入的密码不一致");
                return;
            }
    
            // 隐藏软键盘
            hideKeyboard(getCurrentFocus());
    
            // 修改：移除模拟逻辑，直接提交注册
            performRegister(email, code, password1, password2, nickname);
        }
    }
    
    /**
     * 发送邮箱验证码
     */
    private void sendEmailVerificationCode(String email) {
        EasyHttp.post(this)
                .api(new SendEmailCodeApi()
                        .setEmail(email)
                        .setType("register")
                        .setClientType("android"))
                .request(new HttpCallback<HttpData<SendEmailCodeApi.Bean>>(this) {
    
                    @Override
                    public void onStart(Call call) {
                        // 显示发送中状态
//                        mCountdownView.setText("发送中...");
//                        mCountdownView.setEnabled(false);
                        mCountdownView.start();
                    }
    
                    @Override
                    public void onEnd(Call call) {
                        // 恢复按钮状态（如果请求失败）
//                        mCountdownView.stop();
                    }
    
                    @Override
                    public void onSucceed(HttpData<SendEmailCodeApi.Bean> data) {
                        // 这里也得恢复状态，倒计时结束后自动恢复
                        toast("验证码已发送到您的邮箱，请注意查收");
//                        mCountdownView.start();
//                        mCountdownView.setEnabled(false);
                    }
    
                    @Override
                    public void onFail(Exception e) {
                        super.onFail(e);

                        // 使用工具类获取错误提示
                        String errorMessage = NetworkErrorHandler.getEmailCodeErrorMessage(e);
                        toast(errorMessage);

//                        mCountdownView.setText("获取验证码");
//                        mCountdownView.setEnabled(true);
                        mCountdownView.stop();
                    }
                });
    }
    
    /**
     * 执行注册
     */
    private void performRegister(String email, String code, String password, String confirmPassword, String nickname) {
        // 修改：使用 RegisterApi 而不是 EmailRegisterApi
        EasyHttp.post(this)
                .api(new RegisterApi()
                        .setEmail(email)
                        .setCode(code)  // 注意：RegisterApi 使用 setCode，不是 setVerificationCode
                        .setPassword(password)
                        .setConfirmPassword(confirmPassword)
                        .setNickname(nickname.isEmpty() ? null : nickname)
                        .setDeviceInfo(getDeviceInfo()))
                .request(new HttpCallback<HttpData<RegisterApi.Bean>>(this) {
    
                    @Override
                    public void onStart(Call call) {
                        mCommitView.showProgress();
                    }
    
                    @Override
                    public void onEnd(Call call) {}
    
                    @Override
                    public void onSucceed(HttpData<RegisterApi.Bean> data) {
                        postDelayed(() -> {
                            mCommitView.showSucceed();
                            postDelayed(() -> {
                                setResult(RESULT_OK, new Intent()
                                        .putExtra(INTENT_KEY_EMAIL, email)
                                        .putExtra(INTENT_KEY_PASSWORD, password));
                                finish();
                            }, 1000);
                        }, 1000);
                    }
    
                    @Override
                    public void onFail(Exception e) {
                        super.onFail(e);

                        // 使用工具类获取错误提示
                        String errorMessage = NetworkErrorHandler.getRegisterErrorMessage(e);
                        toast(errorMessage);

                        postDelayed(() -> {
                            mCommitView.showError(3000);
                        }, 1000);
                    }
                });
    }
    /**
     * 获取设备信息
     */
    private String getDeviceInfo() {
        return android.os.Build.MODEL + " " + android.os.Build.VERSION.RELEASE;
    }

    @NonNull
    @Override
    protected ImmersionBar createStatusBarConfig() {
        return super.createStatusBarConfig()
                // 指定导航栏背景颜色
                .navigationBarColor(R.color.white)
                // 不要把整个布局顶上去
                .keyboardEnable(true);
    }

    /**
     * {@link TextView.OnEditorActionListener}
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE && mCommitView.isEnabled()) {
            // 模拟点击注册按钮
            onClick(mCommitView);
            return true;
        }
        return false;
    }

    /**
     * 注册监听
     */
    public interface OnRegisterListener {

        /**
         * 注册成功
         *
         * @param email             邮箱
         * @param password          密码
         */
        void onSucceed(String email, String password);

        /**
         * 取消注册
         */
        default void onCancel() {}
    }
}