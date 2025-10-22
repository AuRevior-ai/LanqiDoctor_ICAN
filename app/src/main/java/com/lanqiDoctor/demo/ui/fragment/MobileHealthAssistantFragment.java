package com.lanqiDoctor.demo.ui.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.hjq.base.BaseFragment;
import com.hjq.toast.ToastUtils;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.entity.HealthInfo;
import com.lanqiDoctor.demo.manager.DatabaseManager;
import com.lanqiDoctor.demo.manager.VivoHealthManager;
import com.lanqiDoctor.demo.ui.activity.HabitBuildingActivity;
import com.lanqiDoctor.demo.ui.activity.HealthFunctionActivity;
import com.lanqiDoctor.demo.ui.activity.HealthProfileActivity;
import com.lanqiDoctor.demo.ui.activity.MobileHealthDaily;
import com.lanqiDoctor.demo.ui.activity.MyRecipesActivity;
import com.lanqiDoctor.demo.ui.activity.UserHealthInfoActivity;

/**
 * 联动手机健康助手Fragment
 * 
 * 迁移自myapplication2项目的手机健康助手功能，包含：
 * - 步数统计同步
 * - 心率监测数据
 * - 睡眠质量分析
 * - 运动健身记录
 * - 健康数据导入
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class MobileHealthAssistantFragment extends BaseFragment {
    //心率睡眠和步数
    private Integer latestSteps = null;
    private Integer latestHeartRate = null;
    private Double latestSleep = null;
    // 健康数据卡片
    private CardView cardStepsData;
    private CardView cardHeartRateData;
    private CardView cardSleepData;
    
    // 功能按钮 - 2x2网格
    private LinearLayout llDataSync;
    private LinearLayout llHealthProfile;
    private LinearLayout llMyRecipes;
    private LinearLayout llHabitBuilding;
    
    // 数据显示文本
    private TextView tvStepsData;
    private TextView tvHeartRateData;
    private TextView tvSleepData;
    
    private VivoHealthManager vivoHealthManager;
    private DatabaseManager databaseManager;
    
    @Override
    protected int getLayoutId() {
        return R.layout.mobile_health_assistant_fragment;
    }
    
    @Override
    protected void initView() {
        // 健康数据卡片 - 添加显式类型转换
        cardStepsData = (CardView) findViewById(R.id.card_steps_data);
        cardHeartRateData = (CardView) findViewById(R.id.card_heart_rate_data);
        cardSleepData = (CardView) findViewById(R.id.card_sleep_data);
        
        // 功能按钮 - 2x2网格
        llDataSync = (LinearLayout) findViewById(R.id.ll_data_sync);
        llHealthProfile = (LinearLayout) findViewById(R.id.ll_health_profile);
        llMyRecipes = (LinearLayout) findViewById(R.id.ll_my_recipes);
        llHabitBuilding = (LinearLayout) findViewById(R.id.ll_habit_building);
        
        // 数据显示文本 - 添加显式类型转换
        tvStepsData = (TextView) findViewById(R.id.tv_steps_data);
        tvHeartRateData = (TextView) findViewById(R.id.tv_heart_rate_data);
        tvSleepData = (TextView) findViewById(R.id.tv_sleep_data);
    }
    
    @Override
    protected void initData() {
        // 初始化管理器
        vivoHealthManager = VivoHealthManager.getInstance(getContext());
        databaseManager = DatabaseManager.getInstance(getContext());
        
        // 初始化数据和监听器
        initListener();
        checkHealthPermissionsAndLoadData();
    }
    
    /**
     * 检查健康权限并加载数据
     */
    private void checkHealthPermissionsAndLoadData() {
        // 首先检查是否登录
        if (!vivoHealthManager.isUserLogin()) {
            ToastUtils.show("请先登录vivo账号");
            vivoHealthManager.jumpToLoginPage(() -> {
                // 登录成功后检查权限
                checkAndRequestPermissions();
            });
            return;
        }
        
        checkAndRequestPermissions();
    }
    
    /**
     * 检查并申请权限
     */
    private void checkAndRequestPermissions() {
        vivoHealthManager.checkHealthPermissions(new VivoHealthManager.HealthPermissionCallback() {
            @Override
            public void onPermissionResult(boolean hasSteps, boolean hasHeight, boolean hasWeight, boolean hasHeartRate) {
                if (hasSteps || hasHeight || hasWeight || hasHeartRate) {
                    // 有权限，加载数据
                    loadHealthData();
                } else {
                    // 没有权限，申请权限
                    requestHealthPermissions();
                }
            }
        });
    }
    
    /**
     * 申请健康权限
     */
    private void requestHealthPermissions() {
        vivoHealthManager.requestHealthPermissions(new VivoHealthManager.HealthPermissionCallback() {
            @Override
            public void onPermissionResult(boolean hasSteps, boolean hasHeight, boolean hasWeight, boolean hasHeartRate) {
                if (hasSteps || hasHeight || hasWeight || hasHeartRate) {
                    ToastUtils.show("权限获取成功，正在加载健康数据...");
                    loadHealthData();
                } else {
                    ToastUtils.show("无法获取健康数据权限，部分功能可能无法使用");
                    // 即使没有权限，也显示模拟数据
                    loadMockHealthData();
                }
            }
        });
    }

    //这是有权限的时候进行的调用
    private void loadHealthData() {
        latestSteps = null;
        latestHeartRate = null;
        latestSleep = 7.33; // 7小时20分，示例值

        // 获取今日步数
        vivoHealthManager.getTodaySteps(new VivoHealthManager.HealthDataCallback<Integer>() {
            @Override
            public void onSuccess(Integer steps) {
                if (tvStepsData != null) {
                    tvStepsData.setText(steps + "步");
                }
                latestSteps = steps;
                trySaveHealthInfoToDb();
            }

            @Override
            public void onError(String error) {
                if (tvStepsData != null) {
                    tvStepsData.setText("获取失败");
                }
                latestSteps = null;
                trySaveHealthInfoToDb();
            }
        });

        // 获取最新心率
        vivoHealthManager.getLatestHeartRate(new VivoHealthManager.HealthDataCallback<Integer>() {
            @Override
            public void onSuccess(Integer heartRate) {
                if (tvHeartRateData != null) {
                    tvHeartRateData.setText(heartRate + "bpm");
                }
                latestHeartRate = heartRate;
                trySaveHealthInfoToDb();
            }

            @Override
            public void onError(String error) {
                if (tvHeartRateData != null) {
                    tvHeartRateData.setText("获取失败");
                }
                latestHeartRate = null;
                trySaveHealthInfoToDb();
            }
        });

        // 睡眠数据暂时显示占位文本
        if (tvSleepData != null) {
            tvSleepData.setText("7小时20分");
        }
        // 这里直接赋值模拟
        latestSleep = 7.33; // 7小时20分
        trySaveHealthInfoToDb();
    }

    /**
     * 加载模拟健康数据（当无权限时使用）
     */
    private void loadMockHealthData() {
        int mockSteps = (int) (Math.random() * 5000) + 5000;
        int mockHeartRate = (int) (Math.random() * 40) + 60;
        double mockSleep = 7.5; // 7小时30分

        if (tvStepsData != null) {
            tvStepsData.setText(mockSteps + "步（模拟）");
        }
        if (tvHeartRateData != null) {
            tvHeartRateData.setText(mockHeartRate + "bpm（模拟）");
        }
        if (tvSleepData != null) {
            tvSleepData.setText("7小时20分（模拟）");
        }

        // 保存到数据库
        if (databaseManager != null) {
            HealthInfo mockInfo = new HealthInfo();
            mockInfo.setSteps(mockSteps);
            mockInfo.setHeartRate(mockHeartRate);
            mockInfo.setSleepDuration(mockSleep);
            mockInfo.setTimestamp(System.currentTimeMillis() / 1000);
            databaseManager.saveHealthInfo(mockInfo);
        }
    }
    
    private void initListener() {
        // // 步数数据卡片点击事件
        // if (cardStepsData != null) {
        //     cardStepsData.setOnClickListener(v -> {
        //         Intent intent = new Intent(getContext(), HealthFunctionActivity.class);
        //         intent.putExtra("function_type", "STEPS_DATA");
        //         intent.putExtra("title", "步数统计详情");
        //         intent.putExtra("description", "手机步数数据模拟功能\n\n• 今日步数：模拟数据\n• 每日基础步数可调整\n• 自动生成合理变化\n• 点击设置可修改参数");
        //         startActivity(intent);
        //     });
        // }
        
        // // 心率数据卡片点击事件
        // if (cardHeartRateData != null) {
        //     cardHeartRateData.setOnClickListener(v -> {
        //         Intent intent = new Intent(getContext(), HealthFunctionActivity.class);
        //         intent.putExtra("function_type", "HEART_RATE_DATA");
        //         intent.putExtra("title", "心率监测详情");
        //         intent.putExtra("description", "心率数据模拟功能\n\n• 基于年龄性别计算\n• 静息心率范围60-100bpm\n• 自动生成合理变化\n• 可在设置中修改个人信息");
        //         startActivity(intent);
        //     });
        // }
        
        // // 睡眠数据卡片点击事件
        // if (cardSleepData != null) {
        //     cardSleepData.setOnClickListener(v -> {
        //         Intent intent = new Intent(getContext(), HealthFunctionActivity.class);
        //         intent.putExtra("function_type", "SLEEP_DATA");
        //         intent.putExtra("title", "睡眠质量详情");
        //         intent.putExtra("description", "睡眠质量模拟功能\n\n• 模拟睡眠时长\n• 深浅睡眠比例\n• 睡眠质量评分\n• 基于个人习惯生成");
        //         startActivity(intent);
        //     });
        // }
        
        // 数据同步功能
        if (llDataSync != null) {
            llDataSync.setOnClickListener(v -> {
                ToastUtils.show("正在同步健康数据...");
                syncHealthData();
            });
        }
        
        // 个人健康画像功能
        if (llHealthProfile != null) {
            llHealthProfile.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), HealthProfileActivity.class);
                startActivity(intent);
            });
        }
        
        // 我的食谱功能
        if (llMyRecipes != null) {
            llMyRecipes.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), MyRecipesActivity.class);
                startActivity(intent);
            });
        }
        
        // 习惯培养功能
        if (llHabitBuilding != null) {
            llHabitBuilding.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), HabitBuildingActivity.class);
                startActivity(intent);
            });
        }
        
        // // 健身追踪功能
        // if (llFitnessTracker != null) {
        //     llFitnessTracker.setOnClickListener(v -> {
        //         Intent intent = new Intent(getContext(), HealthFunctionActivity.class);
        //         intent.putExtra("function_type", "FITNESS_TRACKER");
        //         intent.putExtra("title", "运动健身追踪");
        //         intent.putExtra("description", "运动健身模拟功能\n\n• 基于步数计算运动量\n• 估算卡路里消耗\n• 运动强度分析\n• 个性化建议生成");
        //         startActivity(intent);
        //     });
        // }
        
        // // 健康报告功能
        // if (llHealthReport != null) {
        //     llHealthReport.setOnClickListener(v -> {
        //         Intent intent = new Intent(getContext(), MobileHealthDaily.class);
        //         startActivity(intent);
        //     });
        // }
        
        // // 数据导出功能
        // if (llDataExport != null) {
        //     llDataExport.setOnClickListener(v -> {
        //         Intent intent = new Intent(getContext(), HealthFunctionActivity.class);
        //         intent.putExtra("function_type", "DATA_EXPORT");
        //         intent.putExtra("title", "健康数据导出");
        //         intent.putExtra("description", "健康数据导出功能\n\n• 支持多种格式导出\n• 自定义时间段\n• 数据可视化图表\n• 隐私数据保护");
        //         startActivity(intent);
        //     });
        // }
        
        // 添加长按事件打开设置页面
        if (cardStepsData != null) {
            cardStepsData.setOnLongClickListener(v -> {
                Intent intent = new Intent(getContext(), UserHealthInfoActivity.class);
                startActivity(intent);
                return true;
            });
        }
    }
    
    /**
     * 同步健康数据到本地数据库
     */
    private void syncHealthData() {
        vivoHealthManager.getComprehensiveHealthData(new VivoHealthManager.HealthDataCallback<HealthInfo>() {
            @Override
            public void onSuccess(HealthInfo healthInfo) {
                // 保存到本地数据库
                if (databaseManager != null) {
                    long id = databaseManager.saveHealthInfo(healthInfo);
                    if (id > 0) {
                        ToastUtils.show("健康数据同步成功");
                        // 刷新显示的数据
                        loadHealthData();
                    } else {
                        ToastUtils.show("数据保存失败");
                    }
                } else {
                    ToastUtils.show("数据库管理器未初始化");
                }
            }
            
            @Override
            public void onError(String error) {
                ToastUtils.show("数据同步失败: " + error);
            }
        });
    }
    
    // 新增方法：三项都不为null时保存,但是还是没有插入成功
    private void trySaveHealthInfoToDb() {
        if (latestSteps != null && latestHeartRate != null && latestSleep != null && databaseManager != null) {
            HealthInfo info = new HealthInfo();
            info.setSteps(latestSteps);
            info.setHeartRate(latestHeartRate);
            info.setSleepDuration(latestSleep);
            info.setTimestamp(System.currentTimeMillis() / 1000);
            databaseManager.saveHealthInfo(info);
            // 防止重复插入
            latestSteps = null;
            latestHeartRate = null;
            latestSleep = null;
        }
    }
}