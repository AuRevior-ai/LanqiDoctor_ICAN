package com.lanqiDoctor.demo.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.hjq.toast.ToastUtils;
import com.lanqiDoctor.demo.database.entity.HealthInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

/**
 * 健康数据管理器
 * 提供健康数据的存储、获取和模拟功能
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class VivoHealthManager {
    
    private static final String TAG = "VivoHealthManager";
    private static final String PREFS_NAME = "health_data_prefs";
    private static final String KEY_HEIGHT = "user_height";
    private static final String KEY_WEIGHT = "user_weight";
    private static final String KEY_BIRTH_YEAR = "user_birth_year";
    private static final String KEY_GENDER = "user_gender"; // 0:女性, 1:男性
    private static final String KEY_DAILY_STEPS_BASE = "daily_steps_base";
    private static final String KEY_LAST_STEPS_DATE = "last_steps_date";
    private static final String KEY_LAST_STEPS_COUNT = "last_steps_count";
    
    private static VivoHealthManager instance;
    private Context context;
    private SharedPreferences sharedPreferences;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Random random = new Random();
    
    private VivoHealthManager(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized VivoHealthManager getInstance(Context context) {
        if (instance == null) {
            instance = new VivoHealthManager(context);
        }
        return instance;
    }
    
    /**
     * 检查用户是否已登录（模拟登录状态）
     */
    public boolean isUserLogin() {
        // 模拟登录状态，总是返回true
        return true;
    }
    
    /**
     * 跳转到登录页面（模拟操作）
     */
    public void jumpToLoginPage(Runnable onSuccess) {
        Log.d(TAG, "模拟跳转登录页面");
        ToastUtils.show("模拟登录成功");
        if (onSuccess != null) {
            onSuccess.run();
        }
    }
    
    /**
     * 检查健康数据权限（模拟权限检查）
     */
    public void checkHealthPermissions(HealthPermissionCallback callback) {
        Log.d(TAG, "模拟检查健康数据权限");
        // 模拟已获得所有权限
        callback.onPermissionResult(true, true, true, true);
    }
    
    /**
     * 申请健康数据权限（模拟权限申请）
     */
    public void requestHealthPermissions(HealthPermissionCallback callback) {
        Log.d(TAG, "模拟申请健康数据权限");
        ToastUtils.show("模拟权限申请成功");
        callback.onPermissionResult(true, true, true, true);
    }
    
    /**
     * 设置用户身高
     */
    public void setUserHeight(double height) {
        sharedPreferences.edit().putFloat(KEY_HEIGHT, (float) height).apply();
        Log.d(TAG, "设置用户身高: " + height + "cm");
    }
    
    /**
     * 设置用户体重
     */
    public void setUserWeight(double weight) {
        sharedPreferences.edit().putFloat(KEY_WEIGHT, (float) weight).apply();
        Log.d(TAG, "设置用户体重: " + weight + "kg");
    }
    
    /**
     * 设置用户出生年份
     */
    public void setUserBirthYear(int birthYear) {
        sharedPreferences.edit().putInt(KEY_BIRTH_YEAR, birthYear).apply();
        Log.d(TAG, "设置用户出生年份: " + birthYear);
    }
    
    /**
     * 设置用户性别
     */
    public void setUserGender(int gender) {
        sharedPreferences.edit().putInt(KEY_GENDER, gender).apply();
        Log.d(TAG, "设置用户性别: " + (gender == 1 ? "男性" : "女性"));
    }
    
    /**
     * 设置每日步数基础值
     */
    public void setDailyStepsBase(int stepsBase) {
        sharedPreferences.edit().putInt(KEY_DAILY_STEPS_BASE, stepsBase).apply();
        Log.d(TAG, "设置每日步数基础值: " + stepsBase);
    }
    
    /**
     * 获取今日步数
     */
    public void getTodaySteps(HealthDataCallback<Integer> callback) {
        String today = dayFormat.format(System.currentTimeMillis());
        String lastDate = sharedPreferences.getString(KEY_LAST_STEPS_DATE, "");
        
        int todaySteps;
        if (today.equals(lastDate)) {
            // 同一天，返回已记录的步数
            todaySteps = sharedPreferences.getInt(KEY_LAST_STEPS_COUNT, 0);
        } else {
            // 新的一天，生成新的步数
            int baseSteps = sharedPreferences.getInt(KEY_DAILY_STEPS_BASE, 8000);
            // 在基础步数的基础上加减20%的随机变化
            int variation = (int) (baseSteps * 0.2);
            todaySteps = baseSteps + random.nextInt(variation * 2) - variation;
            
            // 确保步数不小于1000
            todaySteps = Math.max(1000, todaySteps);
            
            // 保存今日步数
            sharedPreferences.edit()
                    .putString(KEY_LAST_STEPS_DATE, today)
                    .putInt(KEY_LAST_STEPS_COUNT, todaySteps)
                    .apply();
        }
        
        Log.d(TAG, "今日步数: " + todaySteps);
        callback.onSuccess(todaySteps);
    }
    
    /**
     * 获取用户身高
     */
    public void getLatestHeight(HealthDataCallback<Double> callback) {
        double height = sharedPreferences.getFloat(KEY_HEIGHT, 0);
        
        if (height == 0) {
            // 如果没有设置身高，返回根据性别的默认值
            int gender = sharedPreferences.getInt(KEY_GENDER, 1);
            if (gender == 1) { // 男性
                height = 170.0 + random.nextInt(20); // 170-190cm
            } else { // 女性
                height = 155.0 + random.nextInt(20); // 155-175cm
            }
            // 保存生成的身高
            setUserHeight(height);
        }
        
        Log.d(TAG, "用户身高: " + height + "cm");
        callback.onSuccess(height);
    }
    
    /**
     * 获取用户体重
     */
    public void getLatestWeight(HealthDataCallback<Double> callback) {
        double weight = sharedPreferences.getFloat(KEY_WEIGHT, 0);
        
        if (weight == 0) {
            // 如果没有设置体重，返回根据性别的默认值
            int gender = sharedPreferences.getInt(KEY_GENDER, 1);
            if (gender == 1) { // 男性
                weight = 60.0 + random.nextInt(30); // 60-90kg
            } else { // 女性
                weight = 45.0 + random.nextInt(30); // 45-75kg
            }
            // 保存生成的体重
            setUserWeight(weight);
        }
        
        Log.d(TAG, "用户体重: " + weight + "kg");
        callback.onSuccess(weight);
    }
    
    /**
     * 获取当前心率（基于年龄和性别的模拟数据）
     */
    public void getLatestHeartRate(HealthDataCallback<Integer> callback) {
        int birthYear = sharedPreferences.getInt(KEY_BIRTH_YEAR, 1990);
        int age = Calendar.getInstance().get(Calendar.YEAR) - birthYear;
        int gender = sharedPreferences.getInt(KEY_GENDER, 1);
        
        // 基于年龄和性别计算基础心率
        int baseHeartRate;
        if (age < 30) {
            baseHeartRate = gender == 1 ? 70 : 75; // 年轻男性70，女性75
        } else if (age < 50) {
            baseHeartRate = gender == 1 ? 72 : 78; // 中年男性72，女性78
        } else {
            baseHeartRate = gender == 1 ? 75 : 80; // 老年男性75，女性80
        }
        
        // 添加随机变化（±10bpm）
        int heartRate = baseHeartRate + random.nextInt(21) - 10;
        
        // 确保心率在合理范围内
        heartRate = Math.max(60, Math.min(100, heartRate));
        
        Log.d(TAG, "当前心率: " + heartRate + "bpm (年龄:" + age + ", 性别:" + (gender == 1 ? "男" : "女") + ")");
        callback.onSuccess(heartRate);
    }
    
    /**
     * 获取用户年龄
     */
    public int getUserAge() {
        int birthYear = sharedPreferences.getInt(KEY_BIRTH_YEAR, 1990);
        return Calendar.getInstance().get(Calendar.YEAR) - birthYear;
    }
    
    /**
     * 获取用户性别
     */
    public int getUserGender() {
        return sharedPreferences.getInt(KEY_GENDER, 1);
    }
    
    /**
     * 获取BMI值
     */
    public void getBMI(HealthDataCallback<Double> callback) {
        getLatestHeight(new HealthDataCallback<Double>() {
            @Override
            public void onSuccess(Double height) {
                getLatestWeight(new HealthDataCallback<Double>() {
                    @Override
                    public void onSuccess(Double weight) {
                        double heightInMeters = height / 100.0;
                        double bmi = weight / (heightInMeters * heightInMeters);
                        bmi = Math.round(bmi * 10.0) / 10.0; // 保留一位小数
                        
                        Log.d(TAG, "BMI值: " + bmi);
                        callback.onSuccess(bmi);
                    }
                    
                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * 获取综合健康信息
     */
    public void getComprehensiveHealthData(HealthDataCallback<HealthInfo> callback) {
        HealthInfo healthInfo = new HealthInfo();
        healthInfo.setTimestamp(System.currentTimeMillis() / 1000);
        healthInfo.setCreateTime(System.currentTimeMillis());
        healthInfo.setUpdateTime(System.currentTimeMillis());
        
        // 获取步数
        getTodaySteps(new HealthDataCallback<Integer>() {
            @Override
            public void onSuccess(Integer steps) {
                // 将步数保存到备注中
                String remarks = "今日步数: " + steps + "步";
                healthInfo.setRemarks(remarks);
                
                // 获取身高
                getLatestHeight(new HealthDataCallback<Double>() {
                    @Override
                    public void onSuccess(Double height) {
                        healthInfo.setHeight(height);
                        
                        // 获取体重
                        getLatestWeight(new HealthDataCallback<Double>() {
                            @Override
                            public void onSuccess(Double weight) {
                                healthInfo.setWeight(weight);
                                
                                // 获取心率
                                getLatestHeartRate(new HealthDataCallback<Integer>() {
                                    @Override
                                    public void onSuccess(Integer heartRate) {
                                        healthInfo.setHeartRate(heartRate);
                                        
                                        // 计算BMI
                                        getBMI(new HealthDataCallback<Double>() {
                                            @Override
                                            public void onSuccess(Double bmi) {
                                                // 将BMI信息添加到备注中
                                                String updatedRemarks = healthInfo.getRemarks() + 
                                                        ", BMI: " + bmi + 
                                                        ", 年龄: " + getUserAge() + "岁" +
                                                        ", 性别: " + (getUserGender() == 1 ? "男" : "女");
                                                healthInfo.setRemarks(updatedRemarks);
                                                
                                                Log.d(TAG, "获取综合健康数据成功");
                                                callback.onSuccess(healthInfo);
                                            }
                                            
                                            @Override
                                            public void onError(String error) {
                                                // 即使BMI计算失败，也返回其他数据
                                                callback.onSuccess(healthInfo);
                                            }
                                        });
                                    }
                                    
                                    @Override
                                    public void onError(String error) {
                                        // 即使心率获取失败，也返回其他数据
                                        Log.w(TAG, "心率数据获取失败，返回其他健康数据");
                                        callback.onSuccess(healthInfo);
                                    }
                                });
                            }
                            
                            @Override
                            public void onError(String error) {
                                Log.w(TAG, "体重数据获取失败: " + error);
                                callback.onSuccess(healthInfo);
                            }
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.w(TAG, "身高数据获取失败: " + error);
                        callback.onSuccess(healthInfo);
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                Log.w(TAG, "步数数据获取失败: " + error);
                callback.onError("无法获取健康数据");
            }
        });
    }
    
    /**
     * 重置所有用户数据
     */
    public void resetUserData() {
        sharedPreferences.edit().clear().apply();
        Log.d(TAG, "用户数据已重置");
        ToastUtils.show("用户数据已重置");
    }
    
    /**
     * 检查用户是否已设置基本信息
     */
    public boolean hasUserBasicInfo() {
        return sharedPreferences.getFloat(KEY_HEIGHT, 0) > 0 && 
               sharedPreferences.getFloat(KEY_WEIGHT, 0) > 0;
    }
    
    /**
     * 健康权限回调接口
     */
    public interface HealthPermissionCallback {
        void onPermissionResult(boolean hasSteps, boolean hasHeight, boolean hasWeight, boolean hasHeartRate);
    }
    
    /**
     * 健康数据回调接口
     */
    public interface HealthDataCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }
}