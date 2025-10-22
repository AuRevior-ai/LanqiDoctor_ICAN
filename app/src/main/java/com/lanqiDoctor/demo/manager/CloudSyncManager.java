package com.lanqiDoctor.demo.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.hjq.toast.ToastUtils;
import com.lanqiDoctor.demo.database.dao.MedicationIntakeRecordDao;
import com.lanqiDoctor.demo.database.dao.MedicationRecordDao;
import com.lanqiDoctor.demo.database.entity.MedicationIntakeRecord;
import com.lanqiDoctor.demo.database.entity.MedicationRecord;
import com.lanqiDoctor.demo.http.api.SyncMedicationApi;
import com.lanqiDoctor.demo.http.api.SyncMedicationIntakeApi;

import com.lanqiDoctor.demo.http.model.HttpData;

import java.util.Calendar;
import java.util.List;

import okhttp3.Call;

/**
 * 云端数据同步管理器
 * 负责用药信息和每日服药数据的云端同步
 *
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class CloudSyncManager {

    private static final String TAG = "CloudSyncManager";

    private static final String PREFS_NAME = "cloud_sync";
    private static final String KEY_LAST_MEDICATION_SYNC = "last_medication_sync";
    private static final String KEY_LAST_INTAKE_SYNC = "last_intake_sync";
    private static final String KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled";

    private static volatile CloudSyncManager instance;

    private Context context;
    private SharedPreferences syncPrefs;
    private IUserState userStateManager;
    private MedicationRecordDao medicationDao;
    private MedicationIntakeRecordDao intakeDao;
    // private String userId; // 新增

    private CloudSyncManager(Context context) {
        Log.d(TAG, "初始化 CloudSyncManager");
        this.context = context.getApplicationContext();
        this.syncPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        try {
            // 使用适配器包装UserStateManager
            UserStateManager realUserStateManager = UserStateManager.getInstance(context);
            this.userStateManager = new UserStateManagerAdapter(realUserStateManager);
            // 新增：初始化userId
            // this.userId = realUserStateManager.getUserId();//删掉这一行,不要再初始化的时候固定它
            Log.d(TAG, "UserStateManager 初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "UserStateManager 初始化失败", e);
            // 创建一个默认实现，避免崩溃
            this.userStateManager = createDefaultUserStateManager();
        }

        try {
            this.medicationDao = new MedicationRecordDao(context);
            this.intakeDao = new MedicationIntakeRecordDao(context);
            Log.d(TAG, "数据库DAO初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "数据库DAO初始化失败", e);
        }
    }
    /**
     * 动态获取当前用户ID
     */
    private String getCurrentUserId() {
        try {
            UserStateManager realUserStateManager = UserStateManager.getInstance(context);
            String userId = realUserStateManager.getUserId();
            Log.d(TAG, "获取当前用户ID: " + userId);
            return userId;
        } catch (Exception e) {
            Log.e(TAG, "获取当前用户ID失败", e);
            return null;
        }
    }
    /**
     * 创建默认的用户状态管理器（防止崩溃）
     */
    private IUserState createDefaultUserStateManager() {
        return new IUserState() {
            @Override
            public boolean isUserLoggedIn() {
                Log.d(TAG, "使用默认UserStateManager - 返回未登录状态");
                return false;  // 默认未登录
            }

            @Override
            public boolean isAutoSyncHealthData() {
                return false;  // 默认不自动同步
            }

            @Override
            public boolean isTokenExpired() {
                return true;   // 默认token过期
            }
        };
    }

    public static synchronized CloudSyncManager getInstance(Context context) {
        Log.d(TAG, "获取CloudSyncManager实例");
        if (instance == null) {
            try {
                instance = new CloudSyncManager(context);
                Log.d(TAG, "CloudSyncManager实例创建成功");
            } catch (Exception e) {
                Log.e(TAG, "CloudSyncManager实例创建失败", e);
                throw e;
            }
        }
        return instance;
    }

    /**
     * 检查是否可以进行云端同步
     */
    public boolean canSyncToCloud() {
        Log.d(TAG, "检查是否可以进行云端同步");

        try {
            // 检查用户是否已登录
            if (userStateManager == null) {
                Log.e(TAG, "userStateManager 为 null");
                return false;
            }

            if (!userStateManager.isUserLoggedIn()) {
                Log.d(TAG, "用户未登录，无法同步到云端");
                return false;
            }

            // 检查用户是否开启了自动同步健康数据
            if (!userStateManager.isAutoSyncHealthData()) {
                Log.d(TAG, "用户未开启自动同步健康数据，跳过云端同步");
                return false;
            }

            // 检查Token是否过期
            if (userStateManager.isTokenExpired()) {
                Log.d(TAG, "Token已过期，无法同步到云端");
                return false;
            }

            Log.d(TAG, "云端同步条件检查通过");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "检查云端同步条件时发生异常", e);
            return false;
        }
    }

    /**
     * 执行完整的数据同步（上传和下载） - 修复首次同步逻辑
     */
    public void performFullSync(SyncCallback callback) {
        Log.d(TAG, "开始执行完整数据同步");

        if (!canSyncToCloud()) {
            String errorMsg = "无法进行云端同步：用户未登录或未开启自动同步";
            Log.w(TAG, errorMsg);
            if (callback != null) {
                callback.onError(errorMsg);
            }
            return;
        }

        boolean isFirstSync = isFirstSync();
        Log.d(TAG, "执行同步流程，是否首次同步: " + isFirstSync);

        if (isFirstSync) {
            // 首次同步：先下载服务器数据，再上传本地数据
            performFirstTimeSync(callback);
        } else {
            // 增量同步：先上传，再下载
            performIncrementalSync(callback);
        }
    }

    /**
     * 执行首次同步
     */
    private void performFirstTimeSync(SyncCallback callback) {
        Log.d(TAG, "执行首次同步流程");

        // 首次同步：先下载服务器的所有数据
        downloadAllServerData(new SyncCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "首次同步：服务器数据下载成功: " + message);

                // 然后上传本地数据（如果有的话）
                uploadLocalDataForFirstSync(callback);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "首次同步：服务器数据下载失败: " + error);
                if (callback != null) callback.onError("首次同步失败: " + error);
            }
        });
    }

    /**
     * 执行增量同步
     */
    private void performIncrementalSync(SyncCallback callback) {
        Log.d(TAG, "执行增量同步流程");

        // 增量同步：先上传本地变更，再下载服务器变更
        uploadMedicationData(new SyncCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "增量同步：用药信息上传成功: " + message);
                uploadIntakeData(new SyncCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Log.d(TAG, "增量同步：服药记录上传成功: " + message);
                        downloadServerData(callback);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "增量同步：服药记录上传失败: " + error);
                        if (callback != null) callback.onError(error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "增量同步：用药信息上传失败: " + error);
                if (callback != null) callback.onError(error);
            }
        });
    }

    /**
     * 下载所有服务器数据（首次同步专用）
     */
    private void downloadAllServerData(SyncCallback callback) {
        Log.d(TAG, "开始下载所有服务器数据（首次同步）");

        // 首次同步时，不传递lastSyncTime，让服务器返回所有数据
        downloadMedicationDataForFirstSync(new SyncCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "首次同步：用药信息下载成功: " + message);
                downloadIntakeDataForFirstSync(callback);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "首次同步：用药信息下载失败: " + error);
                if (callback != null) callback.onError(error);
            }
        });
    }

    /**
     * 首次同步专用：下载用药信息
     */
    private void downloadMedicationDataForFirstSync(SyncCallback callback) {
        Log.d(TAG, "开始下载用药信息（首次同步）");

        EasyHttp.post(new ApplicationLifecycle())
                .api(new SyncMedicationApi()
                        .setLastSyncTime(null) // 关键：首次同步不传递时间戳
                        .setOperationType("download_all") // 使用特殊操作类型
                        .setIsFirstSync(true)) // 标记为首次同步
                .request(new OnHttpListener<HttpData<SyncMedicationApi.Bean>>() {

                    @Override
                    public void onStart(Call call) {
                        Log.d(TAG, "开始下载用药信息网络请求（首次同步）");
                    }

                    @Override
                    public void onEnd(Call call) {
                        Log.d(TAG, "用药信息下载网络请求结束（首次同步）");
                    }

                    @Override
                    public void onSucceed(HttpData<SyncMedicationApi.Bean> data) {
                        Log.d(TAG, "用药信息下载网络请求成功（首次同步）");
                        try {
                            SyncMedicationApi.Bean result = data.getData();
                            if (result != null && result.isSuccess()) {

                                if (result.getMedications() != null && !result.getMedications().isEmpty()) {
                                    int savedCount = 0;
                                    for (MedicationRecord medication : result.getMedications()) {
                                        try {
                                            // 首次同步：直接插入，不检查是否存在
                                            medicationDao.insert(medication);
                                            savedCount++;
                                            Log.d(TAG, "首次同步：插入用药记录: " + medication.getMedicationName());
                                        } catch (Exception e) {
                                            Log.e(TAG, "首次同步：保存用药记录失败: " + medication.getMedicationName(), e);
                                        }
                                    }

                                    Log.d(TAG, "首次同步：用药信息下载完成，保存了 " + savedCount + " 条记录");
                                    if (callback != null) {
                                        callback.onSuccess("下载了 " + savedCount + " 条用药信息");
                                    }
                                } else {
                                    Log.d(TAG, "首次同步：服务器没有用药信息");
                                    if (callback != null) {
                                        callback.onSuccess("服务器没有用药信息");
                                    }
                                }
                            } else {
                                String errorMsg = result != null ? result.getMessage() : "服务器返回空数据";
                                Log.e(TAG, "首次同步：用药信息下载失败: " + errorMsg);
                                if (callback != null) {
                                    callback.onError(errorMsg);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "首次同步：处理用药信息下载响应时发生异常", e);
                            if (callback != null) {
                                callback.onError("响应处理失败: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        Log.e(TAG, "首次同步：用药信息下载网络请求失败", e);
                        if (callback != null) {
                            callback.onError("下载失败: " + e.getMessage());
                        }
                    }
                });
    }

    /**
     * 首次同步专用：下载服药记录 - 修复时间范围计算
     */
    private void downloadIntakeDataForFirstSync(SyncCallback callback) {
        Log.d(TAG, "开始下载服药记录（首次同步）");

        // 修复：获取当前用户ID
        String currentUserId = getCurrentUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "无法获取当前用户ID，跳过首次下载");
            if (callback != null) {
                callback.onError("用户ID获取失败");
            }
            return;
        }

        // 修复：正确计算最近30天的时间范围
        Calendar calendar = Calendar.getInstance();

        // 设置结束时间为今天的23:59:59
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endDate = calendar.getTimeInMillis();

        // 设置开始时间为30天前的00:00:00
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startDate = calendar.getTimeInMillis();

        Log.d(TAG, "首次同步服药记录时间范围: " + formatDateTime(startDate) + " 至 " + formatDateTime(endDate));

        EasyHttp.post(new ApplicationLifecycle())
                .api(new SyncMedicationIntakeApi()
                        .setLastSyncTime(null) // 关键：首次同步不传递时间戳
                        .setOperationType("download_all") // 使用特殊操作类型
                        .setStartDate(startDate)
                        .setEndDate(endDate)
                        //.setUserId(currentUserId) // 修复：传递当前用户ID
                        .setIsFirstSync(true)) // 标记为首次同步
                .request(new OnHttpListener<HttpData<SyncMedicationIntakeApi.Bean>>() {

                    @Override
                    public void onStart(Call call) {
                        Log.d(TAG, "开始下载服药记录网络请求（首次同步）");
                    }

                    @Override
                    public void onEnd(Call call) {
                        Log.d(TAG, "服药记录下载网络请求结束（首次同步）");
                    }

                    @Override
                    public void onSucceed(HttpData<SyncMedicationIntakeApi.Bean> data) {
                        Log.d(TAG, "服药记录下载网络请求成功（首次同步）");
                        try {
                            SyncMedicationIntakeApi.Bean result = data.getData();
                            if (result != null && result.isSuccess()) {

                                if (result.getIntakeRecords() != null && !result.getIntakeRecords().isEmpty()) {
                                    int savedCount = 0;
                                    for (MedicationIntakeRecord intake : result.getIntakeRecords()) {
                                        try {
                                            // 🔥 关键修复：确保设置正确的用户ID
                                            if (intake.getUserId() == null || intake.getUserId().isEmpty()) {
                                                Log.w(TAG, "服务器返回的服药记录userId为空，设置为当前用户: " + intake.getMedicationName());
                                                intake.setUserId(currentUserId);
                                            }
                                            
                                            // 🔥 进一步验证：只保存属于当前用户的数据
                                            if (currentUserId.equals(intake.getUserId())) {
                                                intakeDao.insert(intake);
                                                savedCount++;
                                                Log.d(TAG, "首次同步：插入服药记录: " + intake.getMedicationName() +
                                                        " userId=" + intake.getUserId() + 
                                                        " (" + formatDateTime(intake.getPlannedTime()) + ")");
                                            } else {
                                                Log.w(TAG, "跳过其他用户的服药记录: userId=" + intake.getUserId() + 
                                                        ", 当前用户=" + currentUserId);
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "首次同步：保存服药记录失败: " + intake.getMedicationName(), e);
                                        }
                                    }

                                    Log.d(TAG, "首次同步：服药记录下载完成，保存了 " + savedCount + " 条记录");
                                    if (callback != null) {
                                        callback.onSuccess("首次同步完成，下载了 " + savedCount + " 条服药记录");
                                    }
                                } else {
                                    Log.d(TAG, "首次同步：服务器没有服药记录");
                                    if (callback != null) {
                                        callback.onSuccess("首次同步完成，服务器没有服药记录");
                                    }
                                }
                            } else {
                                String errorMsg = result != null ? result.getMessage() : "服务器返回空数据";
                                Log.e(TAG, "首次同步：服药记录下载失败: " + errorMsg);
                                if (callback != null) {
                                    callback.onError(errorMsg);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "首次同步：处理服药记录下载响应时发生异常", e);
                            if (callback != null) {
                                callback.onError("响应处理失败: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        Log.e(TAG, "首次同步：服药记录下载网络请求失败", e);
                        if (callback != null) {
                            callback.onError("下载失败: " + e.getMessage());
                        }
                    }
                });
}

    /**
     * 首次同步完成后上传本地数据
     */
    private void uploadLocalDataForFirstSync(SyncCallback callback) {
        Log.d(TAG, "首次同步：开始上传本地数据");

        try {
            // 检查本地是否有数据需要上传
            List<MedicationRecord> localMedications = medicationDao.findAll();

            if (localMedications.isEmpty()) {
                Log.d(TAG, "首次同步：本地没有数据需要上传，标记同步完成");
                markFirstSyncCompleted(callback);
                return;
            }

            Log.d(TAG, "首次同步：发现本地有 " + localMedications.size() + " 条数据需要上传");

            // 上传本地数据
            uploadMedicationData(new SyncCallback() {
                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "首次同步：本地数据上传成功: " + message);
                    markFirstSyncCompleted(callback);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "首次同步：本地数据上传失败: " + error);
                    // 即使上传失败，也标记首次同步完成，避免重复下载服务器数据
                    markFirstSyncCompleted(callback);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "首次同步：检查本地数据时发生异常", e);
            markFirstSyncCompleted(callback);
        }
    }

    /**
     * 标记首次同步完成
     */
    private void markFirstSyncCompleted(SyncCallback callback) {
        try {
            // 设置一个初始的同步时间戳，标记首次同步已完成
            long currentTime = System.currentTimeMillis();
            updateLastMedicationSyncTime(currentTime);
            updateLastIntakeSyncTime(currentTime);

            Log.d(TAG, "首次同步完成，设置初始同步时间: " + currentTime);

            if (callback != null) {
                callback.onSuccess("首次同步完成");
            }
        } catch (Exception e) {
            Log.e(TAG, "标记首次同步完成时发生异常", e);
            if (callback != null) {
                callback.onError("同步完成，但状态更新失败");
            }
        }
    }

    /**
     * 上传用药信息到云端 - 优化版本
     */
    public void uploadMedicationData(SyncCallback callback) {
        Log.d(TAG, "开始上传用药信息到云端");

        if (!canSyncToCloud()) {
            String errorMsg = "无法进行云端同步";
            Log.w(TAG, errorMsg);
            if (callback != null) {
                callback.onError(errorMsg);
            }
            return;
        }

        try {
            if (medicationDao == null) {
                Log.e(TAG, "medicationDao 为 null，无法获取用药记录");
                if (callback != null) {
                    callback.onError("数据库访问失败");
                }
                return;
            }

            List<MedicationRecord> medications = medicationDao.findAll();
            Long lastSyncTime = getLastMedicationSyncTime();

            Log.d(TAG, "准备上传 " + medications.size() + " 条用药记录");
            Log.d(TAG, "上次同步时间: " + (lastSyncTime != null ? lastSyncTime : "首次同步"));

            // 使用 ApplicationLifecycle 进行网络请求
            EasyHttp.post(new ApplicationLifecycle())
                    .api(new SyncMedicationApi()
                            .setMedications(medications)
                            .setLastSyncTime(lastSyncTime) // 首次同步时为null
                            .setOperationType("upload"))
                    .request(new OnHttpListener<HttpData<SyncMedicationApi.Bean>>() {

                        @Override
                        public void onStart(Call call) {
                            Log.d(TAG, "开始上传用药信息网络请求");
                        }

                        @Override
                        public void onEnd(Call call) {
                            Log.d(TAG, "用药信息上传网络请求结束");
                        }

                        @Override
                        public void onSucceed(HttpData<SyncMedicationApi.Bean> data) {
                            Log.d(TAG, "用药信息上传网络请求成功");
                            try {
                                SyncMedicationApi.Bean result = data.getData();
                                if (result != null && result.isSuccess()) {
                                    // 关键：保存服务器返回的时间戳
                                    if (result.getServerTime() != null) {
                                        updateLastMedicationSyncTime(result.getServerTime());
                                        Log.d(TAG, "用药信息上传成功，更新同步时间: " + result.getServerTime());
                                    } else {
                                        Log.w(TAG, "服务器未返回同步时间，使用当前时间");
                                        updateLastMedicationSyncTime(System.currentTimeMillis());
                                    }

                                    if (callback != null) {
                                        callback.onSuccess(result.getMessage());
                                    }
                                } else {
                                    String errorMsg = result != null ? result.getMessage() : "服务器返回空数据";
                                    Log.e(TAG, "用药信息上传失败: " + errorMsg);
                                    if (callback != null) {
                                        callback.onError(errorMsg);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "处理用药信息上传响应时发生异常", e);
                                if (callback != null) {
                                    callback.onError("响应处理失败: " + e.getMessage());
                                }
                            }
                        }

                        @Override
                        public void onFail(Exception e) {
                            Log.e(TAG, "用药信息上传网络请求失败", e);
                            if (callback != null) {
                                callback.onError("网络错误: " + e.getMessage());
                            }
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "上传用药信息时发生异常", e);
            if (callback != null) {
                callback.onError("上传失败: " + e.getMessage());
            }
        }
    }

    /**
     * 上传服药记录到云端 - 修复时间范围计算
     */
    public void uploadIntakeData(SyncCallback callback) {
        Log.d(TAG, "开始上传服药记录到云端");

        if (!canSyncToCloud()) {
            String errorMsg = "无法进行云端同步";
            Log.w(TAG, errorMsg);
            if (callback != null) {
                callback.onError(errorMsg);
            }
            return;
        }

        try {
            if (intakeDao == null) {
                Log.e(TAG, "intakeDao 为 null，无法获取服药记录");
                if (callback != null) {
                    callback.onError("数据库访问失败");
                }
                return;
            }

            // 修复：动态获取当前用户ID
            String currentUserId = getCurrentUserId();
            if (currentUserId == null || currentUserId.isEmpty()) {
                Log.e(TAG, "无法获取当前用户ID");
                if (callback != null) {
                    callback.onError("用户ID获取失败");
                }
                return;
            }

            // 修复：正确计算最近30天的时间范围
            Calendar calendar = Calendar.getInstance();

            // 设置结束时间为今天的23:59:59
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            long endDate = calendar.getTimeInMillis();

            // 设置开始时间为30天前的00:00:00
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startDate = calendar.getTimeInMillis();

            // 使用当前用户ID过滤数据
            List<MedicationIntakeRecord> intakeRecords = intakeDao.findByTimeRange(currentUserId,startDate, endDate);
            Long lastSyncTime = getLastIntakeSyncTime();

            Log.d(TAG, "准备上传服药记录 - 数量: " + intakeRecords.size());
            Log.d(TAG, "时间范围: " + formatDateTime(startDate) + " 至 " + formatDateTime(endDate));
            Log.d(TAG, "上次同步时间: " + (lastSyncTime != null ? formatDateTime(lastSyncTime) : "首次同步"));

            EasyHttp.post(new ApplicationLifecycle())
                    .api(new SyncMedicationIntakeApi()
                            .setIntakeRecords(intakeRecords)
                            .setLastSyncTime(lastSyncTime)
                            .setOperationType("upload")
                            .setStartDate(startDate)
                            .setEndDate(endDate))
                    .request(new OnHttpListener<HttpData<SyncMedicationIntakeApi.Bean>>() {

                        @Override
                        public void onStart(Call call) {
                            Log.d(TAG, "开始上传服药记录网络请求");
                        }

                        @Override
                        public void onEnd(Call call) {
                            Log.d(TAG, "服药记录上传网络请求结束");
                        }

                        @Override
                        public void onSucceed(HttpData<SyncMedicationIntakeApi.Bean> data) {
                            Log.d(TAG, "服药记录上传网络请求成功");
                            try {
                                SyncMedicationIntakeApi.Bean result = data.getData();
                                if (result != null && result.isSuccess()) {
                                    // 关键：保存服务器返回的时间戳
                                    if (result.getServerTime() != null) {
                                        updateLastIntakeSyncTime(result.getServerTime());
                                        Log.d(TAG, "服药记录上传成功，更新同步时间: " + formatDateTime(result.getServerTime()));
                                    } else {
                                        Log.w(TAG, "服务器未返回同步时间，使用当前时间");
                                        updateLastIntakeSyncTime(System.currentTimeMillis());
                                    }

                                    if (callback != null) {
                                        callback.onSuccess(result.getMessage());
                                    }
                                } else {
                                    String errorMsg = result != null ? result.getMessage() : "服务器返回空数据";
                                    Log.e(TAG, "服药记录上传失败: " + errorMsg);
                                    if (callback != null) {
                                        callback.onError(errorMsg);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "处理服药记录上传响应时发生异常", e);
                                if (callback != null) {
                                    callback.onError("响应处理失败: " + e.getMessage());
                                }
                            }
                        }

                        @Override
                        public void onFail(Exception e) {
                            Log.e(TAG, "服药记录上传网络请求失败", e);
                            if (callback != null) {
                                callback.onError("网络错误: " + e.getMessage());
                            }
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "上传服药记录时发生异常", e);
            if (callback != null) {
                callback.onError("上传失败: " + e.getMessage());
            }
        }
    }
    /**
     * 从服务器下载数据
     */
    public void downloadServerData(SyncCallback callback) {
        Log.d(TAG, "开始从服务器下载数据");

        if (!canSyncToCloud()) {
            String errorMsg = "无法进行云端同步";
            Log.w(TAG, errorMsg);
            if (callback != null) {
                callback.onError(errorMsg);
            }
            return;
        }

        Log.d(TAG, "开始下载服务器数据流程");

        downloadMedicationData(new SyncCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "用药信息下载成功: " + message);
                downloadIntakeData(callback);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "用药信息下载失败: " + error);
                if (callback != null) callback.onError(error);
            }
        });
    }

    /**
     * 下载用药信息 - 优化版本
     */
    private void downloadMedicationData(SyncCallback callback) {
        Log.d(TAG, "开始下载用药信息");

        Long lastSyncTime = getLastMedicationSyncTime();
        Log.d(TAG, "用药信息下载，上次同步时间: " + (lastSyncTime != null ? lastSyncTime : "首次同步"));

        EasyHttp.post(new ApplicationLifecycle())
                .api(new SyncMedicationApi()
                        .setLastSyncTime(lastSyncTime) // 首次同步时为null
                        .setOperationType("download"))
                .request(new OnHttpListener<HttpData<SyncMedicationApi.Bean>>() {

                    @Override
                    public void onStart(Call call) {
                        Log.d(TAG, "开始下载用药信息网络请求");
                    }

                    @Override
                    public void onEnd(Call call) {
                        Log.d(TAG, "用药信息下载网络请求结束");
                    }

                    @Override
                    public void onSucceed(HttpData<SyncMedicationApi.Bean> data) {
                        Log.d(TAG, "用药信息下载网络请求成功");
                        try {
                            SyncMedicationApi.Bean result = data.getData();
                            if (result != null && result.isSuccess()) {

                                // 关键：先更新同步时间，再处理数据
                                if (result.getServerTime() != null) {
                                    updateLastMedicationSyncTime(result.getServerTime());
                                    Log.d(TAG, "更新用药信息同步时间: " + result.getServerTime());
                                }

                                if (result.getMedications() != null && !result.getMedications().isEmpty()) {
                                    int savedCount = 0;
                                    for (MedicationRecord medication : result.getMedications()) {
                                        try {
                                            MedicationRecord existing = medicationDao.findById(medication.getId());
                                            if (existing == null) {
                                                medicationDao.insert(medication);
                                                savedCount++;
                                                Log.d(TAG, "插入新的用药记录: " + medication.getMedicationName());
                                            } else {
                                                medicationDao.update(medication);
                                                savedCount++;
                                                Log.d(TAG, "更新现有用药记录: " + medication.getMedicationName());
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "保存用药记录时发生异常: " + medication.getMedicationName(), e);
                                        }
                                    }

                                    Log.d(TAG, "用药信息下载完成，保存了 " + savedCount + " 条记录");
                                    if (callback != null) {
                                        callback.onSuccess("下载了 " + savedCount + " 条用药信息");
                                    }
                                } else {
                                    Log.d(TAG, "服务器没有新的用药信息");
                                    if (callback != null) {
                                        callback.onSuccess("没有新的用药信息");
                                    }
                                }
                            } else {
                                String errorMsg = result != null ? result.getMessage() : "服务器返回空数据";
                                Log.e(TAG, "用药信息下载失败: " + errorMsg);
                                if (callback != null) {
                                    callback.onError(errorMsg);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "处理用药信息下载响应时发生异常", e);
                            if (callback != null) {
                                callback.onError("响应处理失败: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        Log.e(TAG, "用药信息下载网络请求失败", e);
                        if (callback != null) {
                            callback.onError("下载失败: " + e.getMessage());
                        }
                    }
                });
    }

    /**
     * 下载服药记录 - 修复时间范围计算
     */
    private void downloadIntakeData(SyncCallback callback) {
        Log.d(TAG, "开始下载服药记录");

        String currentUserId = getCurrentUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "无法获取当前用户ID");
            if (callback != null) {
                callback.onError("用户ID获取失败");
            }
            return;
        }

        Long lastSyncTime = getLastIntakeSyncTime();

        // 修复：正确计算最近30天的时间范围
        Calendar calendar = Calendar.getInstance();

        // 设置结束时间为今天的23:59:59
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endDate = calendar.getTimeInMillis();

        // 设置开始时间为30天前的00:00:00
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startDate = calendar.getTimeInMillis();

        Log.d(TAG, "服药记录下载时间范围: " + formatDateTime(startDate) + " 至 " + formatDateTime(endDate));
        Log.d(TAG, "上次同步时间: " + (lastSyncTime != null ? formatDateTime(lastSyncTime) : "首次同步"));

        EasyHttp.post(new ApplicationLifecycle())
                .api(new SyncMedicationIntakeApi()
                        .setLastSyncTime(lastSyncTime)
                        .setOperationType("download")
                        .setStartDate(startDate)
                        .setEndDate(endDate))
                .request(new OnHttpListener<HttpData<SyncMedicationIntakeApi.Bean>>() {

                    @Override
                    public void onStart(Call call) {
                        Log.d(TAG, "开始下载服药记录网络请求");
                    }

                    @Override
                    public void onEnd(Call call) {
                        Log.d(TAG, "服药记录下载网络请求结束");
                    }

                    @Override
                    public void onSucceed(HttpData<SyncMedicationIntakeApi.Bean> data) {
                        Log.d(TAG, "服药记录下载网络请求成功");
                        try {
                            SyncMedicationIntakeApi.Bean result = data.getData();
                            if (result != null && result.isSuccess() && result.getIntakeRecords() != null) {
                                int savedCount = 0;
                                for (MedicationIntakeRecord intake : result.getIntakeRecords()) {
                                    try {
                                        // 🔥 关键修复：确保设置正确的用户ID
                                        if (intake.getUserId() == null || intake.getUserId().isEmpty()) {
                                            Log.w(TAG, "服务器返回的服药记录userId为空，设置为当前用户: " + intake.getMedicationName());
                                            intake.setUserId(currentUserId);
                                        }
                                        
                                        // 🔥 进一步验证：只保存属于当前用户的数据
                                        if (currentUserId.equals(intake.getUserId())) {
                                            MedicationIntakeRecord existing = intakeDao.findById(intake.getId());
                                            if (existing == null) {
                                                intakeDao.insert(intake);
                                                savedCount++;
                                                Log.d(TAG, "插入新的服药记录: " + intake.getMedicationName() +
                                                        " userId=" + intake.getUserId() + 
                                                        " (" + formatDateTime(intake.getPlannedTime()) + ")");
                                            } else {
                                                intakeDao.update(intake);
                                                savedCount++;
                                                Log.d(TAG, "更新现有服药记录: " + intake.getMedicationName() +
                                                        " userId=" + intake.getUserId() + 
                                                        " (" + formatDateTime(intake.getPlannedTime()) + ")");
                                            }
                                        } else {
                                            Log.w(TAG, "跳过其他用户的服药记录: userId=" + intake.getUserId() + 
                                                    ", 当前用户=" + currentUserId);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "保存服药记录时发生异常: " + intake.getMedicationName(), e);
                                    }
                                }

                                updateLastIntakeSyncTime(result.getServerTime());
                                Log.d(TAG, "服药记录下载完成，保存了 " + savedCount + " 条记录");
                                if (callback != null) {
                                    callback.onSuccess("同步完成，下载了 " + savedCount + " 条服药记录");
                                }
                            } else {
                                Log.d(TAG, "服务器没有新的服药记录");
                                if (callback != null) {
                                    callback.onSuccess("同步完成，没有新的服药记录");
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "处理服药记录下载响应时发生异常", e);
                            if (callback != null) {
                                callback.onError("响应处理失败: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        Log.e(TAG, "服药记录下载网络请求失败", e);
                        if (callback != null) {
                            callback.onError("下载失败: " + e.getMessage());
                        }
                    }
                });
}
    

    /**
     * 自动同步数据（在后台静默执行）
     */
    public void autoSyncInBackground() {
        Log.d(TAG, "尝试开始后台自动同步");

        if (!canSyncToCloud()) {
            Log.d(TAG, "后台自动同步条件不满足，跳过");
            return;
        }

        Log.d(TAG, "开始后台自动同步");
        performFullSync(new SyncCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "后台自动同步成功: " + message);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "后台自动同步失败: " + error);
            }
        });
    }

    /**
     * 格式化日期时间用于日志显示
     */
    private String formatDateTime(Long timestamp) {
        if (timestamp == null) {
            return "null";
        }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(timestamp));
        } catch (Exception e) {
            return "时间格式错误";
        }
    }

    /**
     * 手动触发同步（带用户提示）
     */
    public void manualSync() {
        Log.d(TAG, "用户手动触发同步");

        if (!canSyncToCloud()) {
            String msg = "无法同步：请先登录并开启自动同步健康数据";
            Log.w(TAG, msg);
            ToastUtils.show(msg);
            return;
        }

        ToastUtils.show("开始同步数据到云端...");
        performFullSync(new SyncCallback() {
            @Override
            public void onSuccess(String message) {
                Log.d(TAG, "手动同步成功: " + message);
                ToastUtils.show("数据同步成功");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "手动同步失败: " + error);
                ToastUtils.show("数据同步失败: " + error);
            }
        });
    }

    /**
     * 获取用药信息上次同步时间
     */
    private Long getLastMedicationSyncTime() {
        try {
            long time = syncPrefs.getLong(KEY_LAST_MEDICATION_SYNC, 0);
            Log.d(TAG, "获取用药信息上次同步时间: " + time + (time == 0 ? " (首次同步)" : ""));
            return time == 0 ? null : time; // 首次同步返回null
        } catch (Exception e) {
            Log.e(TAG, "获取用药信息同步时间失败", e);
            return null;
        }
    }

    /**
     * 更新用药信息同步时间
     */
    private void updateLastMedicationSyncTime(Long serverTime) {
        try {
            if (serverTime != null && serverTime > 0) {
                syncPrefs.edit().putLong(KEY_LAST_MEDICATION_SYNC, serverTime).apply();
                Log.d(TAG, "更新用药信息同步时间: " + serverTime);
            } else {
                Log.w(TAG, "服务器返回的同步时间无效: " + serverTime);
            }
        } catch (Exception e) {
            Log.e(TAG, "更新用药信息同步时间失败", e);
        }
    }

    /**
     * 获取服药记录上次同步时间
     */
    private Long getLastIntakeSyncTime() {
        try {
            long time = syncPrefs.getLong(KEY_LAST_INTAKE_SYNC, 0);
            Log.d(TAG, "获取服药记录上次同步时间: " + time + (time == 0 ? " (首次同步)" : ""));
            return time == 0 ? null : time; // 首次同步返回null
        } catch (Exception e) {
            Log.e(TAG, "获取服药记录同步时间失败", e);
            return null;
        }
    }

    /**
     * 更新服药记录同步时间
     */
    private void updateLastIntakeSyncTime(Long serverTime) {
        try {
            if (serverTime != null && serverTime > 0) {
                syncPrefs.edit().putLong(KEY_LAST_INTAKE_SYNC, serverTime).apply();
                Log.d(TAG, "更新服药记录同步时间: " + serverTime);
            } else {
                Log.w(TAG, "服务器返回的同步时间无效: " + serverTime);
            }
        } catch (Exception e) {
            Log.e(TAG, "更新服药记录同步时间失败", e);
        }
    }


    /**
     * 检查是否为首次同步
     */
    public boolean isFirstSync() {
        try {
            long medicationSyncTime = syncPrefs.getLong(KEY_LAST_MEDICATION_SYNC, 0);
            long intakeSyncTime = syncPrefs.getLong(KEY_LAST_INTAKE_SYNC, 0);
            boolean isFirst = (medicationSyncTime == 0 && intakeSyncTime == 0);
            Log.d(TAG, "检查是否首次同步: " + isFirst);
            return isFirst;
        } catch (Exception e) {
            Log.e(TAG, "检查首次同步状态失败", e);
            return true; // 异常情况下认为是首次同步
        }
    }

    /**
     * 重置同步状态（用于重新开始同步）
     */
    public void resetSyncState() {
        try {
            syncPrefs.edit()
                    .remove(KEY_LAST_MEDICATION_SYNC)
                    .remove(KEY_LAST_INTAKE_SYNC)
                    .apply();
            Log.d(TAG, "重置同步状态完成");
        } catch (Exception e) {
            Log.e(TAG, "重置同步状态失败", e);
        }
    }

    /**
     * 获取同步状态信息
     */
    public SyncStatus getSyncStatus() {
        SyncStatus status = new SyncStatus();

        try {
            status.lastMedicationSyncTime = getLastMedicationSyncTime();
            status.lastIntakeSyncTime = getLastIntakeSyncTime();
            status.isFirstSync = isFirstSync();
            status.canSync = canSyncToCloud();

            Log.d(TAG, "同步状态: " + status.toString());
        } catch (Exception e) {
            Log.e(TAG, "获取同步状态失败", e);
        }

        return status;
    }

    /**
     * 同步状态信息类
     */
    public static class SyncStatus {
        public Long lastMedicationSyncTime;
        public Long lastIntakeSyncTime;
        public boolean isFirstSync;
        public boolean canSync;

        @Override
        public String toString() {
            return "SyncStatus{" +
                    "lastMedicationSyncTime=" + lastMedicationSyncTime +
                    ", lastIntakeSyncTime=" + lastIntakeSyncTime +
                    ", isFirstSync=" + isFirstSync +
                    ", canSync=" + canSync +
                    '}';
        }
    }

    /**
     * 同步回调接口
     */
    public interface SyncCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    /**
     * 用户状态接口（避免命名冲突）
     */
    private interface IUserState {
        boolean isUserLoggedIn();
        boolean isAutoSyncHealthData();
        boolean isTokenExpired();
    }

    /**
     * UserStateManager的适配器类
     */
    private static class UserStateManagerAdapter implements IUserState {
        private final UserStateManager realUserStateManager;

        public UserStateManagerAdapter(UserStateManager userStateManager) {
            this.realUserStateManager = userStateManager;
        }

        @Override
        public boolean isUserLoggedIn() {
            try {
                return realUserStateManager.isUserLoggedIn();
            } catch (Exception e) {
                Log.e(TAG, "检查用户登录状态时发生异常", e);
                return false;
            }
        }

        @Override
        public boolean isAutoSyncHealthData() {
            try {
                return realUserStateManager.isAutoSyncHealthData();
            } catch (Exception e) {
                Log.e(TAG, "检查自动同步设置时发生异常", e);
                return false;
            }
        }

        @Override
        public boolean isTokenExpired() {
            try {
                return realUserStateManager.isTokenExpired();
            } catch (Exception e) {
                Log.e(TAG, "检查Token过期状态时发生异常", e);
                return true;
            }
        }
        /**
         * 获取当前用户ID
         */
        public String getUserId() {
            try {
                return realUserStateManager.getUserId();
            } catch (Exception e) {
                Log.e(TAG, "获取用户ID时发生异常", e);
                return null;
            }
        }
    }
}