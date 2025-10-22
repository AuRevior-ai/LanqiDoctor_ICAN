package com.lanqiDoctor.demo.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import java.security.MessageDigest;

import com.google.gson.Gson;
import com.lanqiDoctor.demo.http.api.LoginApi;
import com.lanqiDoctor.demo.http.api.UserSettingsApi;
import com.lanqiDoctor.demo.http.api.RefreshTokenApi;
import com.lanqiDoctor.demo.http.model.HttpData;
import com.hjq.http.EasyHttp;
import com.hjq.http.listener.OnHttpListener;
import com.hjq.http.lifecycle.ApplicationLifecycle;

import okhttp3.Call;

/**
 * 用户状态管理器
 * 负责用户登录状态、用户信息和偏好设置的持久化存储
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class UserStateManager {
    
    private static final String PREFS_NAME = "user_state_prefs";
    private static final String KEY_IS_LOGIN = "is_login";
    private static final String KEY_USER_TOKEN = "user_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOKEN_EXPIRE_TIME = "token_expire_time";
    private static final String KEY_USER_INFO = "user_info";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NICKNAME = "user_nickname";
    private static final String KEY_USER_AVATAR = "user_avatar";
    private static final String KEY_EMAIL_VERIFIED = "email_verified";
    private static final String KEY_LAST_LOGIN_TIME = "last_login_time";
    
    // 地区信息相关
    private static final String KEY_USER_PROVINCE = "user_province";
    private static final String KEY_USER_CITY = "user_city";
    private static final String KEY_USER_AREA = "user_area";
    
    // 偏好设置相关
    private static final String KEY_ENABLE_PUSH_NOTIFICATION = "enable_push_notification";
    private static final String KEY_ENABLE_HEALTH_REMINDER = "enable_health_reminder";
    private static final String KEY_ENABLE_MEDICATION_REMINDER = "enable_medication_reminder";
    private static final String KEY_PREFERRED_LANGUAGE = "preferred_language";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_AUTO_SYNC_HEALTH_DATA = "auto_sync_health_data";
    private static final String KEY_PRIVACY_LEVEL = "privacy_level";
    private static final String TAG = "UserStateManager";
    
    private static UserStateManager instance;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    private Context context;
    
    private UserStateManager(Context context) {
        Log.d(TAG, "初始化UserStateManager");
        this.context = context.getApplicationContext();
        this.sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        Log.d(TAG, "UserStateManager初始化完成");
    }
    
    public static synchronized UserStateManager getInstance(Context context) {
        Log.d(TAG, "获取UserStateManager实例");
        if (instance == null) {
            try {
                instance = new UserStateManager(context);
                Log.d(TAG, "UserStateManager实例创建成功");
            } catch (Exception e) {
                Log.e(TAG, "UserStateManager实例创建失败", e);
                throw e;
            }
        }
        return instance;
    }
    
    /**
     * 保存登录信息
     */
    public void saveLoginInfo(LoginApi.Bean loginData) {
        Log.d(TAG, "保存用户登录信息");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        try {
            // 保存登录状态
            editor.putBoolean(KEY_IS_LOGIN, true);
            
            // 保存Token信息
            editor.putString(KEY_USER_TOKEN, loginData.getToken());
            editor.putString(KEY_REFRESH_TOKEN, loginData.getRefreshToken());
            
            // 计算Token过期时间（假设Token有效期为24小时）
            long expireTime = System.currentTimeMillis() + (loginData.getExpiresIn() * 1000);
            editor.putLong(KEY_TOKEN_EXPIRE_TIME, expireTime);
            
            // 保存用户信息
            if (loginData.getUserInfo() != null) {
                String userInfoJson = gson.toJson(loginData.getUserInfo());
                editor.putString(KEY_USER_INFO, userInfoJson);
                
                // 单独保存常用字段便于快速访问
                editor.putString(KEY_USER_EMAIL, loginData.getUserInfo().getEmail());
                editor.putString(KEY_USER_NICKNAME, loginData.getUserInfo().getNickname());
                editor.putString(KEY_USER_AVATAR, loginData.getUserInfo().getAvatar());
                editor.putBoolean(KEY_EMAIL_VERIFIED, loginData.getUserInfo().isEmailVerified());
            }
            
            // 记录登录时间
            editor.putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis());
            
            editor.apply();
            Log.d(TAG, "用户登录信息保存成功");

            // 登录后从服务器加载用户设置
            loadUserSettingsFromServer();
        } catch (Exception e) {
            Log.e(TAG, "保存用户登录信息失败", e);
        }
    }
    
    /**
     * Token刷新回调接口
     */
    public interface TokenRefreshCallback {
        void onSuccess(String newToken, String newRefreshToken);
        void onError(String error);
    }

    /**
     * 检查Token是否即将过期（提前1小时检查）
     */
    public boolean isTokenNearExpiry() {
        try {
            long expireTime = sharedPreferences.getLong(KEY_TOKEN_EXPIRE_TIME, 0);
            long currentTime = System.currentTimeMillis();
            // 提前1小时检查过期
            long oneHourInMillis = 60 * 60 * 1000;
            boolean nearExpiry = (currentTime + oneHourInMillis) > expireTime;
            Log.d(TAG, "Token即将过期检查: 过期时间=" + expireTime + ", 当前时间=" + currentTime + ", 是否即将过期=" + nearExpiry);
            return nearExpiry;
        } catch (Exception e) {
            Log.e(TAG, "检查Token即将过期状态时发生异常", e);
            return true; // 出现异常时认为即将过期
        }
    }

    /**
     * 刷新Token
     */
    public void refreshToken(String refreshToken, TokenRefreshCallback callback) {
        Log.d(TAG, "开始刷新Token");
        
        if (TextUtils.isEmpty(refreshToken)) {
            Log.e(TAG, "RefreshToken为空，无法刷新");
            if (callback != null) {
                callback.onError("RefreshToken为空");
            }
            return;
        }
        
        try {
            EasyHttp.post(new ApplicationLifecycle())
                    .api(new RefreshTokenApi().setRefreshToken(refreshToken))
                    .request(new OnHttpListener<HttpData<RefreshTokenApi.Bean>>() {
                        @Override
                        public void onStart(Call call) {
                            Log.d(TAG, "开始刷新Token网络请求");
                        }
                        
                        @Override
                        public void onEnd(Call call) {
                            Log.d(TAG, "刷新Token网络请求结束");
                        }
                        
                        @Override
                        public void onSucceed(HttpData<RefreshTokenApi.Bean> data) {
                            Log.d(TAG, "刷新Token网络请求成功");
                            try {
                                RefreshTokenApi.Bean result = data.getData();
                                if (result != null && result.isSuccess()) {
                                    // 保存新的Token
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(KEY_USER_TOKEN, result.getToken());
                                    editor.putString(KEY_REFRESH_TOKEN, result.getRefreshToken());
                                    
                                    // 计算新的过期时间
                                    long expireTime = System.currentTimeMillis() + (result.getExpiresIn() * 1000);
                                    editor.putLong(KEY_TOKEN_EXPIRE_TIME, expireTime);
                                    
                                    editor.apply();
                                    
                                    Log.d(TAG, "Token刷新成功，新Token已保存");
                                    if (callback != null) {
                                        callback.onSuccess(result.getToken(), result.getRefreshToken());
                                    }
                                } else {
                                    String errorMsg = result != null ? result.getMessage() : "服务器返回空数据";
                                    Log.e(TAG, "Token刷新失败: " + errorMsg);
                                    if (callback != null) {
                                        callback.onError(errorMsg);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "处理Token刷新响应时发生异常", e);
                                if (callback != null) {
                                    callback.onError("响应处理失败: " + e.getMessage());
                                }
                            }
                        }
                        
                        @Override
                        public void onFail(Exception e) {
                            Log.e(TAG, "刷新Token网络请求失败", e);
                            if (callback != null) {
                                callback.onError("网络错误: " + e.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "刷新Token时发生异常", e);
            if (callback != null) {
                callback.onError("刷新失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 检查用户是否已登录
     */
    public boolean isUserLoggedIn() {
        try {
            if (!sharedPreferences.getBoolean(KEY_IS_LOGIN, false)) {
                Log.d(TAG, "用户未登录");
                return false;
            }
            
            // 检查Token是否过期
            boolean isExpired = isTokenExpired();
            if (isExpired) {
                Log.d(TAG, "用户Token已过期");
            } else {
                Log.d(TAG, "用户已登录且Token有效");
            }
            return !isExpired;
        } catch (Exception e) {
            Log.e(TAG, "检查用户登录状态时发生异常", e);
            return false;
        }
    }
    
    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired() {
        try {
            long expireTime = sharedPreferences.getLong(KEY_TOKEN_EXPIRE_TIME, 0);
            boolean expired = System.currentTimeMillis() > expireTime;
            Log.d(TAG, "Token过期检查: 过期时间=" + expireTime + ", 当前时间=" + System.currentTimeMillis() + ", 是否过期=" + expired);
            return expired;
        } catch (Exception e) {
            Log.e(TAG, "检查Token过期状态时发生异常", e);
            return true; // 出现异常时认为已过期
        }
    }
    
    /**
     * 获取用户Token
     */
    public String getUserToken() {
        return sharedPreferences.getString(KEY_USER_TOKEN, "");
    }
    
    /**
     * 获取刷新Token
     */
    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, "");
    }
    
    /**
     * 获取用户信息
     */
    public LoginApi.Bean.UserInfo getUserInfo() {
        String userInfoJson = sharedPreferences.getString(KEY_USER_INFO, "");
        if (TextUtils.isEmpty(userInfoJson)) {
            return null;
        }
        try {
            return gson.fromJson(userInfoJson, LoginApi.Bean.UserInfo.class);
        } catch (Exception e) {
            Log.e(TAG, "解析用户信息JSON失败", e);
            return null;
        }
    }
    
    /**
     * 获取用户邮箱
     */
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }
    
    /**
     * 获取用户昵称
     */
    public String getUserNickname() {
        return sharedPreferences.getString(KEY_USER_NICKNAME, "");
    }
    
    /**
     * 获取用户头像URL
     */
    public String getUserAvatar() {
        return sharedPreferences.getString(KEY_USER_AVATAR, "");
    }
    
    /**
     * 获取邮箱验证状态
     */
    public boolean isEmailVerified() {
        return sharedPreferences.getBoolean(KEY_EMAIL_VERIFIED, false);
    }
    
    /**
     * 获取最后登录时间
     */
    public long getLastLoginTime() {
        return sharedPreferences.getLong(KEY_LAST_LOGIN_TIME, 0);
    }
    
    /**
     * 生成用户ID（邮箱MD5的前6位）
     */
    public String getUserId() {
        String email = getUserEmail();
        if (TextUtils.isEmpty(email)) {
            Log.w(TAG, "用户邮箱为空，使用默认用户ID");
            return "DEFAULT";  // 使用默认用户ID而不是空字符串
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(email.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String md5 = sb.toString();
            return md5.substring(0, 6).toUpperCase();
        } catch (Exception e) {
            Log.e(TAG, "生成用户ID失败", e);
            return "DEFAULT";  // 异常时也使用默认用户ID
        }
    }
    
    /**
     * 更新用户信息
     */
    public void updateUserInfo(String nickname, String avatar) {
        Log.d(TAG, "更新用户信息: nickname=" + nickname + ", avatar=" + avatar);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        try {
            if (!TextUtils.isEmpty(nickname)) {
                editor.putString(KEY_USER_NICKNAME, nickname);
            }
            
            if (!TextUtils.isEmpty(avatar)) {
                editor.putString(KEY_USER_AVATAR, avatar);
            }
            
            // 同时更新完整的用户信息JSON
            LoginApi.Bean.UserInfo userInfo = getUserInfo();
            if (userInfo != null) {
                String updatedUserInfoJson = gson.toJson(userInfo);
                editor.putString(KEY_USER_INFO, updatedUserInfoJson);
            }
            
            editor.apply();
            Log.d(TAG, "用户信息更新成功");
        } catch (Exception e) {
            Log.e(TAG, "更新用户信息失败", e);
        }
    }
    
    /**
     * 用户登出
     */
    public void logout() {
        Log.d(TAG, "用户登出");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        
        try {
            // 清除登录状态和Token信息
            editor.remove(KEY_IS_LOGIN);
            editor.remove(KEY_USER_TOKEN);
            editor.remove(KEY_REFRESH_TOKEN);
            editor.remove(KEY_TOKEN_EXPIRE_TIME);
            editor.remove(KEY_USER_INFO);
            editor.remove(KEY_USER_EMAIL);
            editor.remove(KEY_USER_NICKNAME);
            editor.remove(KEY_USER_AVATAR);
            editor.remove(KEY_EMAIL_VERIFIED);
            editor.remove(KEY_LAST_LOGIN_TIME);
            
            // 注意：不清除偏好设置和地区信息，让用户设置保持
            
            editor.apply();
            Log.d(TAG, "用户登出成功");
        } catch (Exception e) {
            Log.e(TAG, "用户登出失败", e);
        }
    }
    
    /**
     * 清除所有数据（包括偏好设置）
     */
    public void clearAllData() {
        Log.d(TAG, "清除所有用户数据");
        try {
            sharedPreferences.edit().clear().apply();
            Log.d(TAG, "所有用户数据清除成功");
        } catch (Exception e) {
            Log.e(TAG, "清除用户数据失败", e);
        }
    }
    
    // ==================== 地区信息相关方法 ====================
    
    /**
     * 保存地区信息
     */
    public void saveAddressInfo(String province, String city, String area) {
        Log.d(TAG, "保存地区信息: " + province + " " + city + " " + area);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_PROVINCE, province);
        editor.putString(KEY_USER_CITY, city);
        editor.putString(KEY_USER_AREA, area);
        editor.apply();
    }
    
    /**
     * 获取省份
     */
    public String getUserProvince() {
        return sharedPreferences.getString(KEY_USER_PROVINCE, "陕西省");
    }
    
    /**
     * 获取城市
     */
    public String getUserCity() {
        return sharedPreferences.getString(KEY_USER_CITY, "西安市");
    }
    
    /**
     * 获取区域
     */
    public String getUserArea() {
        return sharedPreferences.getString(KEY_USER_AREA, "长安区");
    }
    
    /**
     * 获取完整地址
     */
    public String getFullAddress() {
        return getUserProvince() + getUserCity() + getUserArea();
    }
    
    // ==================== 偏好设置相关方法 ====================
    
    /**
     * 设置推送通知开关
     */
    public void setEnablePushNotification(boolean enable) {
        Log.d(TAG, "设置推送通知: " + enable);
        sharedPreferences.edit().putBoolean(KEY_ENABLE_PUSH_NOTIFICATION, enable).apply();
    }
    
    /**
     * 获取推送通知开关状态
     */
    public boolean isEnablePushNotification() {
        return sharedPreferences.getBoolean(KEY_ENABLE_PUSH_NOTIFICATION, true);
    }
    
    /**
     * 设置健康提醒开关
     */
    public void setEnableHealthReminder(boolean enable) {
        Log.d(TAG, "设置健康提醒: " + enable);
        sharedPreferences.edit().putBoolean(KEY_ENABLE_HEALTH_REMINDER, enable).apply();
    }
    
    /**
     * 获取健康提醒开关状态
     */
    public boolean isEnableHealthReminder() {
        return sharedPreferences.getBoolean(KEY_ENABLE_HEALTH_REMINDER, true);
    }
    
    /**
     * 设置用药提醒开关
     */
    public void setEnableMedicationReminder(boolean enable) {
        Log.d(TAG, "设置用药提醒: " + enable);
        sharedPreferences.edit().putBoolean(KEY_ENABLE_MEDICATION_REMINDER, enable).apply();
    }
    
    /**
     * 获取用药提醒开关状态
     */
    public boolean isEnableMedicationReminder() {
        return sharedPreferences.getBoolean(KEY_ENABLE_MEDICATION_REMINDER, true);
    }
    
    /**
     * 设置首选语言
     */
    public void setPreferredLanguage(String language) {
        Log.d(TAG, "设置首选语言: " + language);
        sharedPreferences.edit().putString(KEY_PREFERRED_LANGUAGE, language).apply();
    }
    
    /**
     * 获取首选语言
     */
    public String getPreferredLanguage() {
        return sharedPreferences.getString(KEY_PREFERRED_LANGUAGE, "zh_CN");
    }
    
    /**
     * 设置主题模式
     */
    public void setThemeMode(String themeMode) {
        Log.d(TAG, "设置主题模式: " + themeMode);
        sharedPreferences.edit().putString(KEY_THEME_MODE, themeMode).apply();
    }
    
    /**
     * 获取主题模式
     */
    public String getThemeMode() {
        return sharedPreferences.getString(KEY_THEME_MODE, "auto");
    }
    
    /**
     * 设置自动同步健康数据开关
     */
    public void setAutoSyncHealthData(boolean enable) {
        Log.d(TAG, "设置自动同步健康数据: " + enable);
        sharedPreferences.edit().putBoolean(KEY_AUTO_SYNC_HEALTH_DATA, enable).apply();

        // 同步到服务器
        syncAutoSyncSettingToServer(enable);
    }
    
    /**
     * 获取自动同步健康数据开关状态
     */
    public boolean isAutoSyncHealthData() {
        boolean enabled = sharedPreferences.getBoolean(KEY_AUTO_SYNC_HEALTH_DATA, true);
        Log.d(TAG, "获取自动同步健康数据设置: " + enabled);
        return enabled;
    }
    
    /**
     * 设置隐私级别
     */
    public void setPrivacyLevel(int level) {
        Log.d(TAG, "设置隐私级别: " + level);
        sharedPreferences.edit().putInt(KEY_PRIVACY_LEVEL, level).apply();
    }
    
    /**
     * 获取隐私级别
     */
    public int getPrivacyLevel() {
        return sharedPreferences.getInt(KEY_PRIVACY_LEVEL, 1); // 默认中等隐私级别
    }
    
    /**
     * 导出用户偏好设置
     */
    public UserPreferences exportPreferences() {
        Log.d(TAG, "导出用户偏好设置");
        UserPreferences preferences = new UserPreferences();
        preferences.enablePushNotification = isEnablePushNotification();
        preferences.enableHealthReminder = isEnableHealthReminder();
        preferences.enableMedicationReminder = isEnableMedicationReminder();
        preferences.preferredLanguage = getPreferredLanguage();
        preferences.themeMode = getThemeMode();
        preferences.autoSyncHealthData = isAutoSyncHealthData();
        preferences.privacyLevel = getPrivacyLevel();
        return preferences;
    }
    
    /**
     * 导入用户偏好设置
     */
    public void importPreferences(UserPreferences preferences) {
        Log.d(TAG, "导入用户偏好设置");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_ENABLE_PUSH_NOTIFICATION, preferences.enablePushNotification);
        editor.putBoolean(KEY_ENABLE_HEALTH_REMINDER, preferences.enableHealthReminder);
        editor.putBoolean(KEY_ENABLE_MEDICATION_REMINDER, preferences.enableMedicationReminder);
        editor.putString(KEY_PREFERRED_LANGUAGE, preferences.preferredLanguage);
        editor.putString(KEY_THEME_MODE, preferences.themeMode);
        editor.putBoolean(KEY_AUTO_SYNC_HEALTH_DATA, preferences.autoSyncHealthData);
        editor.putInt(KEY_PRIVACY_LEVEL, preferences.privacyLevel);
        editor.apply();
    }
    
    /**
     * 用户偏好设置数据类
     */
    public static class UserPreferences {
        public boolean enablePushNotification = true;
        public boolean enableHealthReminder = true;
        public boolean enableMedicationReminder = true;
        public String preferredLanguage = "zh_CN";
        public String themeMode = "auto";
        public boolean autoSyncHealthData = true;
        public int privacyLevel = 1;
    }

    // ==================== 新增的服务器同步方法 ====================
    
    /**
     * 从服务器加载用户设置
     */
    private void loadUserSettingsFromServer() {
        Log.d(TAG, "尝试从服务器加载用户设置");
        
        if (!isUserLoggedIn()) {
            Log.d(TAG, "用户未登录，跳过从服务器加载设置");
            return;
        }
        
        try {
            EasyHttp.get(new ApplicationLifecycle())
                    .api(new UserSettingsApi())
                    .request(new OnHttpListener<HttpData<UserSettingsApi.Bean>>() {
                        @Override
                        public void onStart(Call call) {
                            Log.d(TAG, "开始从服务器加载用户设置网络请求");
                        }
                        
                        @Override
                        public void onEnd(Call call) {
                            Log.d(TAG, "从服务器加载用户设置网络请求结束");
                        }
                        
                        @Override
                        public void onSucceed(HttpData<UserSettingsApi.Bean> data) {
                            Log.d(TAG, "从服务器加载用户设置网络请求成功");
                            try {
                                UserSettingsApi.Bean settings = data.getData();
                                if (settings != null) {
                                    // 更新本地设置
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean(KEY_AUTO_SYNC_HEALTH_DATA, settings.isAutoSyncHealthData());
                                    editor.apply();
                                    
                                    Log.d(TAG, "用户设置从服务器加载成功: autoSync=" + settings.isAutoSyncHealthData());
                                } else {
                                    Log.w(TAG, "服务器返回的用户设置为空");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "处理服务器用户设置响应时发生异常", e);
                            }
                        }
                        
                        @Override
                        public void onFail(Exception e) {
                            Log.e(TAG, "从服务器加载用户设置失败", e);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "从服务器加载用户设置时发生异常", e);
        }
    }

    /**
     * 同步自动同步设置到服务器
     */
    private void syncAutoSyncSettingToServer(boolean enable) {
        Log.d(TAG, "尝试同步自动同步设置到服务器: " + enable);
        
        if (!isUserLoggedIn()) {
            Log.d(TAG, "用户未登录，跳过同步设置到服务器");
            return;
        }
        
        try {
            EasyHttp.post(new ApplicationLifecycle())
                    .api(new UserSettingsApi().setAutoSyncHealthData(enable))
                    .request(new OnHttpListener<HttpData<Object>>() {
                        @Override
                        public void onStart(Call call) {
                            Log.d(TAG, "开始同步自动同步设置到服务器网络请求");
                        }
                        
                        @Override
                        public void onEnd(Call call) {
                            Log.d(TAG, "同步自动同步设置到服务器网络请求结束");
                        }
                        
                        @Override
                        public void onSucceed(HttpData<Object> data) {
                            Log.d(TAG, "自动同步设置已成功同步到服务器: " + enable);
                        }
                        
                        @Override
                        public void onFail(Exception e) {
                            Log.e(TAG, "同步设置到服务器失败", e);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "同步自动同步设置到服务器时发生异常", e);
        }
    }    
    
    /**
     * 手动同步所有用户设置到服务器
     */
    public void syncAllSettingsToServer() {
        Log.d(TAG, "手动同步所有用户设置到服务器");
        
        if (!isUserLoggedIn()) {
            Log.d(TAG, "用户未登录，无法同步设置到服务器");
            return;
        }
        
        // 可以根据需要扩展，目前只同步自动同步设置
        syncAutoSyncSettingToServer(isAutoSyncHealthData());
    }
}