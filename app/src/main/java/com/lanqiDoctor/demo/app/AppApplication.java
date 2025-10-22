package com.lanqiDoctor.demo.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import android.text.TextUtils;

import com.hjq.bar.TitleBar;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.aop.Log;
import com.lanqiDoctor.demo.http.glide.GlideApp;
import com.lanqiDoctor.demo.http.model.RequestHandler;
import com.lanqiDoctor.demo.http.model.RequestServer;
import com.lanqiDoctor.demo.manager.ActivityManager;
import com.lanqiDoctor.demo.manager.UserStateManager;
import com.lanqiDoctor.demo.manager.TodayMedicationManager;
import com.lanqiDoctor.demo.other.AppConfig;
import com.lanqiDoctor.demo.other.CrashHandler;
import com.lanqiDoctor.demo.other.DebugLoggerTree;
import com.lanqiDoctor.demo.other.MaterialHeader;
import com.lanqiDoctor.demo.other.SmartBallPulseFooter;
import com.lanqiDoctor.demo.other.TitleBarStyle;
import com.lanqiDoctor.demo.other.ToastLogInterceptor;
import com.lanqiDoctor.demo.other.ToastStyle;
import com.hjq.gson.factory.GsonFactory;
import com.hjq.http.EasyConfig;
import com.hjq.toast.ToastUtils;
import com.hjq.umeng.UmengClient;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mmkv.MMKV;

import okhttp3.OkHttpClient;
import timber.log.Timber;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2018/10/18
 *    desc   : 应用入口
 */
public final class AppApplication extends Application {

    @Log("启动耗时")
    @Override
    public void onCreate() {
        super.onCreate();
        initSdk(this);
        
        // 初始化用户状态
        initUserState();
        // 初始化今日服药数据
        initTodayMedicationData();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // 清理所有图片内存缓存
        GlideApp.get(this).onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // 根据手机内存剩余情况清理图片内存缓存
        GlideApp.get(this).onTrimMemory(level);
    }

    /**
     * 初始化今日服药数据
     */
    private void initTodayMedicationData() {
        // 在后台线程中初始化，避免阻塞主线程
        new Thread(() -> {
            try {
                android.util.Log.d("AppApplication", "开始初始化今日服药数据");
                TodayMedicationManager medicationManager = TodayMedicationManager.getInstance(this);
                medicationManager.initTodayMedicationData();
                android.util.Log.d("AppApplication", "今日服药数据初始化完成");
            } catch (Exception e) {
                android.util.Log.e("AppApplication", "初始化今日服药数据失败", e);
            }
        }).start();
    }

    /**
     * 初始化一些第三方框架
     */
    public static void initSdk(Application application) {
        // 设置标题栏初始化器
        TitleBar.setDefaultStyle(new TitleBarStyle());

        // 设置全局的 Header 构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator((cx, layout) ->
                new MaterialHeader(application).setColorSchemeColors(ContextCompat.getColor(application, R.color.common_accent_color)));
        // 设置全局的 Footer 构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator((cx, layout) -> new SmartBallPulseFooter(application));
        // 设置全局初始化器
        SmartRefreshLayout.setDefaultRefreshInitializer((cx, layout) -> {
            // 刷新头部是否跟随内容偏移
            layout.setEnableHeaderTranslationContent(true)
                    // 刷新尾部是否跟随内容偏移
                    .setEnableFooterTranslationContent(true)
                    // 加载更多是否跟随内容偏移
                    .setEnableFooterFollowWhenNoMoreData(true)
                    // 内容不满一页时是否可以上拉加载更多
                    .setEnableLoadMoreWhenContentNotFull(false)
                    // 仿苹果越界效果开关
                    .setEnableOverScrollDrag(false);
        });

        // 初始化吐司
        ToastUtils.init(application, new ToastStyle());
        // 设置调试模式
        ToastUtils.setDebugMode(AppConfig.isDebug());
        // 设置 Toast 拦截器
        ToastUtils.setInterceptor(new ToastLogInterceptor());

        // 本地异常捕捉
        CrashHandler.register(application);

        // 友盟统计、登录、分享 SDK
        UmengClient.init(application, AppConfig.isLogEnable());

        // Bugly 异常捕捉
        CrashReport.initCrashReport(application, AppConfig.getBuglyId(), AppConfig.isDebug());

        // Activity 栈管理初始化
        ActivityManager.getInstance().init(application);

        // MMKV 初始化
        MMKV.initialize(application);

        // 网络请求框架初始化
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();

        EasyConfig.with(okHttpClient)
                // 是否打印日志
                .setLogEnabled(AppConfig.isLogEnable())
                // 设置服务器配置
                .setServer(new RequestServer())
                // 设置请求处理策略
                .setHandler(new RequestHandler(application))
                // 设置请求重试次数
                .setRetryCount(1)
                .setInterceptor((api, params, headers) -> {
                    // 动态获取当前用户的JWT token
                    UserStateManager userStateManager = UserStateManager.getInstance(application);
                    if (userStateManager.isUserLoggedIn()) {
                        String token = userStateManager.getUserToken();
                        if (!TextUtils.isEmpty(token)) {
                            // 设置JWT token到Authorization头
                            headers.put("Authorization", "Bearer " + token);
                        }
                    }
                    
                    // 添加其他全局请求头
                    headers.put("deviceOaid", UmengClient.getDeviceOaid());
                    headers.put("versionName", AppConfig.getVersionName());
                    headers.put("versionCode", String.valueOf(AppConfig.getVersionCode()));
                    headers.put("Content-Type", "application/json");
                    // 添加全局请求参数
                    // params.put("6666666", "6666666");
                })
                .into();

        // 设置 Json 解析容错监听
        GsonFactory.setJsonCallback((typeToken, fieldName, jsonToken) -> {
            // 上报到 Bugly 错误列表
            CrashReport.postCatchedException(new IllegalArgumentException(
                    "类型解析异常：" + typeToken + "#" + fieldName + "，后台返回的类型为：" + jsonToken));
        });

        // 初始化日志打印
        if (AppConfig.isLogEnable()) {
            Timber.plant(new DebugLoggerTree());
        }

        // 注册网络状态变化监听
        ConnectivityManager connectivityManager = ContextCompat.getSystemService(application, ConnectivityManager.class);
        if (connectivityManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
                @Override
                public void onLost(@NonNull Network network) {
                    Activity topActivity = ActivityManager.getInstance().getTopActivity();
                    if (!(topActivity instanceof LifecycleOwner)) {
                        return;
                    }

                    LifecycleOwner lifecycleOwner = ((LifecycleOwner) topActivity);
                    if (lifecycleOwner.getLifecycle().getCurrentState() != Lifecycle.State.RESUMED) {
                        return;
                    }

                    ToastUtils.show(R.string.common_network_error);
                }
            });
        }
    }

    /**
     * 初始化用户状态
     */
    private void initUserState() {
        UserStateManager userStateManager = UserStateManager.getInstance(this);
        
        // 检查用户登录状态
        if (userStateManager.isUserLoggedIn()) {
            android.util.Log.d("AppApplication", "用户已登录，初始化Token状态");
                    
            // 检查用户ID是否有效
            String userId = userStateManager.getUserId();
            String email = userStateManager.getUserEmail();
            android.util.Log.d("AppApplication", "用户ID: " + userId + ", 邮箱: " + email);
            
            if (TextUtils.isEmpty(userId) || "".equals(userId)) {
            android.util.Log.w("AppApplication", "用户ID无效，可能需要重新登录");
            }
            
            // 检查Token是否即将过期（提前1小时检查）
            if (userStateManager.isTokenNearExpiry()) {
                String refreshToken = userStateManager.getRefreshToken();
                if (!TextUtils.isEmpty(refreshToken)) {
                    android.util.Log.d("AppApplication", "Token即将过期，自动刷新");
                    refreshTokenIfNeeded(refreshToken);
                }
            }
        } else {
            android.util.Log.d("AppApplication", "用户未登录");
        }
    }

    /**
     * 刷新Token（如果需要）
     */
    private void refreshTokenIfNeeded(String refreshToken) {
        android.util.Log.d("AppApplication", "开始刷新Token");
        
        // 实现Token刷新逻辑
        UserStateManager userStateManager = UserStateManager.getInstance(this);
        userStateManager.refreshToken(refreshToken, new UserStateManager.TokenRefreshCallback() {
            @Override
            public void onSuccess(String newToken, String newRefreshToken) {
                android.util.Log.d("AppApplication", "Token刷新成功");
            }
            
            @Override
            public void onError(String error) {
                android.util.Log.e("AppApplication", "Token刷新失败: " + error);
                // Token刷新失败，可能需要重新登录
                userStateManager.logout();
            }
        });
    }
}