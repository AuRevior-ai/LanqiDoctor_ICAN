package com.lanqiDoctor.demo.ui.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.gyf.immersionbar.ImmersionBar;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.aop.Log;
import com.lanqiDoctor.demo.aop.SingleClick;
import com.lanqiDoctor.demo.app.AppActivity;
import com.lanqiDoctor.demo.config.NetworkConfig;
import com.lanqiDoctor.demo.http.api.LoginApi;
import com.lanqiDoctor.demo.http.glide.GlideApp;
import com.lanqiDoctor.demo.http.model.HttpData;
import com.lanqiDoctor.demo.manager.InputTextManager;
import com.lanqiDoctor.demo.manager.UserStateManager;
import com.lanqiDoctor.demo.other.KeyboardWatcher;
import com.lanqiDoctor.demo.ui.fragment.MineFragment;
import com.lanqiDoctor.demo.util.EmailValidator;
import com.lanqiDoctor.demo.util.NetworkErrorHandler;
import com.lanqiDoctor.demo.wxapi.WXEntryActivity;
import com.hjq.http.EasyConfig;
import com.hjq.http.EasyHttp;
import com.hjq.http.listener.HttpCallback;
import com.hjq.umeng.Platform;
import com.hjq.umeng.UmengClient;
import com.hjq.umeng.UmengLogin;
import com.hjq.widget.view.SubmitButton;

import okhttp3.Call;

/**
 * 登录界面 - 邮箱版本
 * 
 * @author 蓝岐医童开发团队
 * @version 2.0
 */
public final class LoginActivity extends AppActivity
        implements UmengLogin.OnLoginListener,
        KeyboardWatcher.SoftKeyboardStateListener,
        TextView.OnEditorActionListener {

    private static final String INTENT_KEY_IN_EMAIL = "email";
    private static final String INTENT_KEY_IN_PASSWORD = "password";

    @Log
    public static void start(Context context, String email, String password) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(INTENT_KEY_IN_EMAIL, email);
        intent.putExtra(INTENT_KEY_IN_PASSWORD, password);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    private ImageView mLogoView;
    private ViewGroup mBodyLayout;
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mForgetView;
    private SubmitButton mCommitView;
    private View mOtherView;
    private View mQQView;
    private View mWeChatView;

    private NetworkConfig networkConfig;

    /** logo 缩放比例 */
    private final float mLogoScale = 0.8f;
    /** 动画时间 */
    private final int mAnimTime = 300;

    @Override
    protected int getLayoutId() {
        return R.layout.login_activity;
    }

    @Override
    protected void initView() {
        mLogoView = findViewById(R.id.iv_login_logo);
        mBodyLayout = findViewById(R.id.ll_login_body);
        mEmailView = findViewById(R.id.et_login_email);
        mPasswordView = findViewById(R.id.et_login_password);
        mForgetView = findViewById(R.id.tv_login_forget);
        mCommitView = findViewById(R.id.btn_login_commit);
        mOtherView = findViewById(R.id.ll_login_other);
        mQQView = findViewById(R.id.iv_login_qq);
        mWeChatView = findViewById(R.id.iv_login_wechat);

        // 添加空指针检查
        if (mForgetView == null) {
            android.util.Log.e("LoginActivity", "mForgetView is null");
        }
        if (mCommitView == null) {
            android.util.Log.e("LoginActivity", "mCommitView is null");
        }
        if (mQQView == null) {
            android.util.Log.e("LoginActivity", "mQQView is null");
        }
        if (mWeChatView == null) {
            android.util.Log.e("LoginActivity", "mWeChatView is null");
        }

        // 只为非空的 View 设置点击监听器
        if (mForgetView != null && mCommitView != null && mQQView != null && mWeChatView != null) {
            setOnClickListener(mForgetView, mCommitView, mQQView, mWeChatView);
        } else {
            // 分别设置非空的 View
            if (mForgetView != null) setOnClickListener(mForgetView);
            if (mCommitView != null) setOnClickListener(mCommitView);
            if (mQQView != null) setOnClickListener(mQQView);
            if (mWeChatView != null) setOnClickListener(mWeChatView);
        }

        if (mPasswordView != null) {
            mPasswordView.setOnEditorActionListener(this);
        }

        if (mEmailView != null && mPasswordView != null && mCommitView != null) {
            InputTextManager.with(this)
                    .addView(mEmailView)
                    .addView(mPasswordView)
                    .setMain(mCommitView)
                    .build();
        }
    }

    @Override
    protected void initData() {
        networkConfig = NetworkConfig.getInstance(this);
        
        postDelayed(() -> {
            KeyboardWatcher.with(LoginActivity.this)
                    .setListener(LoginActivity.this);
        }, 500);

        // 判断用户当前有没有安装 QQ
        if (mQQView != null && !UmengClient.isAppInstalled(this, Platform.QQ)) {
            mQQView.setVisibility(View.GONE);
        }

        // 判断用户当前有没有安装微信
        if (mWeChatView != null && !UmengClient.isAppInstalled(this, Platform.WECHAT)) {
            mWeChatView.setVisibility(View.GONE);
        }

        // 如果这两个都没有安装就隐藏提示
        if (mOtherView != null && mQQView != null && mWeChatView != null &&
            mQQView.getVisibility() == View.GONE && mWeChatView.getVisibility() == View.GONE) {
            mOtherView.setVisibility(View.GONE);
        }

        // 自动填充邮箱和密码
        if (mEmailView != null) {
            mEmailView.setText(getString(INTENT_KEY_IN_EMAIL));
        }
        if (mPasswordView != null) {
            mPasswordView.setText(getString(INTENT_KEY_IN_PASSWORD));
        }
    }

    @Override
    public void onRightClick(View view) {
        // 跳转到注册界面
        String email = mEmailView != null ? mEmailView.getText().toString() : "";
        String password = mPasswordView != null ? mPasswordView.getText().toString() : "";
        
        RegisterActivity.start(this, email, password, (newEmail, newPassword) -> {
            // 如果已经注册成功，就执行登录操作
            if (mEmailView != null) {
                mEmailView.setText(newEmail);
            }
            if (mPasswordView != null) {
                mPasswordView.setText(newPassword);
                mPasswordView.requestFocus();
                mPasswordView.setSelection(mPasswordView.getText().length());
            }
            if (mCommitView != null) {
                onClick(mCommitView);
            }
        });
    }

    @SingleClick
    @Override
    public void onClick(View view) {
        if (view == mForgetView) {
            startActivity(PasswordForgetActivity.class);
            return;
        }
    
        if (view == mCommitView) {
            String email = mEmailView.getText().toString().trim();
            String password = mPasswordView.getText().toString();
    
            // 验证邮箱格式
            if (!EmailValidator.isValidEmail(email, networkConfig)) {
                mEmailView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim));
                mCommitView.showError(3000);
                String errorMsg = EmailValidator.getEmailErrorMessage(email, networkConfig);
                toast(errorMsg != null ? errorMsg : "请输入正确的邮箱格式");
                return;
            }
    
            // 验证密码
            if (password.length() < 6) {
                mPasswordView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_anim));
                mCommitView.showError(3000);
                toast("密码长度不能少于6位");
                return;
            }
    
            // 隐藏软键盘
            hideKeyboard(getCurrentFocus());
    
            // 执行登录请求
            performLogin(email, password);
            return;
        }
    
        if (view == mQQView || view == mWeChatView) {
            toast("记得改好第三方 AppID 和 Secret，否则会调不起来哦");
            Platform platform;
            if (view == mQQView) {
                platform = Platform.QQ;
            } else if (view == mWeChatView) {
                platform = Platform.WECHAT;
                toast("也别忘了改微信 " + WXEntryActivity.class.getSimpleName() + " 类所在的包名哦");
            } else {
                throw new IllegalStateException("are you ok?");
            }
            UmengClient.login(this, platform, this);
        }
    }
    
    /**
     * 检查网络连接
     */
    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 执行登录请求
     */
    private void performLogin(String email, String password) {
        // 添加网络检查
        if (!isNetworkAvailable()) {
            toast("网络连接不可用，请检查网络设置");
            mCommitView.showError(3000);
            return;
        }

        EasyHttp.post(this)
                .api(new LoginApi()
                        .setEmail(email)
                        .setPassword(password)
                        .setDeviceInfo(getDeviceInfo()))
                .request(new HttpCallback<HttpData<LoginApi.Bean>>(this) {
    
                    @Override
                    public void onStart(Call call) {
                        mCommitView.showProgress();
                    }
    
                    @Override
                    public void onEnd(Call call) {}
    
                    @Override
                    public void onSucceed(HttpData<LoginApi.Bean> data) {
                        LoginApi.Bean loginData = data.getData();
                        
                        // 修改：直接传入LoginApi.Bean对象
                        saveUserInfo(loginData);
                        
                        postDelayed(() -> {
                            mCommitView.showSucceed();
                            postDelayed(() -> {
                                // 跳转到权限检查页面，完成后再进入主页
                                checkPermissionsAndGoToHome();
                            }, 1000);
                        }, 1000);
                    }
    
                    @Override
                    public void onFail(Exception e) {
                        super.onFail(e);

                        // 使用工具类获取错误提示
                        String errorMessage = NetworkErrorHandler.getLoginErrorMessage(e);
                        toast(errorMessage);
                        
                        postDelayed(() -> {
                            mCommitView.showError(3000);
                        }, 1000);
                    }
                });
    }
    
    /**
     * 保存用户信息到SharedPreferences
     */
    private void saveUserInfo(LoginApi.Bean loginData) {
        // 使用UserStateManager保存登录信息
        UserStateManager.getInstance(this).saveLoginInfo(loginData);
        
        // 同时更新EasyHttp的Token配置
        EasyConfig.getInstance()
                .addParam("token", loginData.getToken())
                .addParam("refreshToken", loginData.getRefreshToken());
    }

    /**
     * 获取设备信息
     */
    private String getDeviceInfo() {
        return android.os.Build.MODEL + " " + android.os.Build.VERSION.RELEASE;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 友盟回调
        UmengClient.onActivityResult(this, requestCode, resultCode, data);
    }

    /**
     * {@link UmengLogin.OnLoginListener}
     */

    /**
     * 第三方登录成功的回调
     *
     * @param platform      平台名称
     * @param data          用户资料返回
     */
    @Override
    public void onSucceed(Platform platform, UmengLogin.LoginData data) {
        if (isFinishing() || isDestroyed()) {
            // Glide：You cannot start a load for a destroyed activity
            return;
        }

        // 判断第三方登录的平台
        switch (platform) {
            case QQ:
                break;
            case WECHAT:
                break;
            default:
                break;
        }

        GlideApp.with(this)
                .load(data.getAvatar())
                .circleCrop()
                .into(mLogoView);

        toast("昵称：" + data.getName() + "\n" +
                "性别：" + data.getSex() + "\n" +
                "id：" + data.getId() + "\n" +
                "token：" + data.getToken());
    }

    /**
     * 授权失败的回调
     *
     * @param platform      平台名称
     * @param t             错误原因
     */
    @Override
    public void onError(Platform platform, Throwable t) {
        toast("第三方登录出错：" + t.getMessage());
    }

    /**
     * {@link KeyboardWatcher.SoftKeyboardStateListener}
     */

    @Override
    public void onSoftKeyboardOpened(int keyboardHeight) {
        // 执行位移动画
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mBodyLayout, "translationY", 0, -mCommitView.getHeight());
        objectAnimator.setDuration(mAnimTime);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.start();

        // 执行缩小动画
        mLogoView.setPivotX(mLogoView.getWidth() / 2f);
        mLogoView.setPivotY(mLogoView.getHeight());
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mLogoView, "scaleX", 1f, mLogoScale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mLogoView, "scaleY", 1f, mLogoScale);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(mLogoView, "translationY", 0f, -mCommitView.getHeight());
        animatorSet.play(translationY).with(scaleX).with(scaleY);
        animatorSet.setDuration(mAnimTime);
        animatorSet.start();
    }

    @Override
    public void onSoftKeyboardClosed() {
        // 执行位移动画
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mBodyLayout, "translationY", mBodyLayout.getTranslationY(), 0f);
        objectAnimator.setDuration(mAnimTime);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.start();

        if (mLogoView.getTranslationY() == 0) {
            return;
        }

        // 执行放大动画
        mLogoView.setPivotX(mLogoView.getWidth() / 2f);
        mLogoView.setPivotY(mLogoView.getHeight());
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mLogoView, "scaleX", mLogoScale, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mLogoView, "scaleY", mLogoScale, 1f);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(mLogoView, "translationY", mLogoView.getTranslationY(), 0f);
        animatorSet.play(translationY).with(scaleX).with(scaleY);
        animatorSet.setDuration(mAnimTime);
        animatorSet.start();
    }

    /**
     * {@link TextView.OnEditorActionListener}
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE && mCommitView.isEnabled()) {
            // 模拟点击登录按钮
            onClick(mCommitView);
            return true;
        }
        return false;
    }

    @NonNull
    @Override
    protected ImmersionBar createStatusBarConfig() {
        return super.createStatusBarConfig()
                // 指定导航栏背景颜色
                .navigationBarColor(R.color.white);
    }

    /**
     * 检查权限并跳转到主页
     */
    private void checkPermissionsAndGoToHome() {
        // 启动权限检查Activity，完成后自动跳转到主页
        Intent intent = new Intent(this, PermissionCheckActivity.class);
        intent.putExtra("next_activity", HealthMainActivity.class.getName());
        intent.putExtra("finish_current", true);
        startActivity(intent);
        finish();
    }
}