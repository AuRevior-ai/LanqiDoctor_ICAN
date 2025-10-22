package com.lanqiDoctor.demo.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.hjq.base.BaseActivity;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.aop.Log;
import com.lanqiDoctor.demo.aop.SingleClick;
import com.lanqiDoctor.demo.app.AppActivity;
import com.lanqiDoctor.demo.ui.adapter.PermissionAdapter;
import com.lanqiDoctor.demo.ui.model.PermissionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限检查Activity
 * 统一管理所有需要用户手动授予的权限
 * 
 * @author rrrrrzy
 * @github https://github.com/rrrrrzy
 * @time 2025/8/18
 * @desc 权限检查和申请页面
 */
public final class PermissionCheckActivity extends AppActivity {

    private RecyclerView rvPermissions;
    private TextView tvPermissionStatus;
    private Button btnContinue;
    private Button btnRetry;

    private PermissionAdapter adapter;
    private List<PermissionItem> permissionList;
    
    // 用于存储跳转信息
    private String nextActivityName;
    private boolean finishCurrent;

    @Log
    public static void start(Context context) {
        Intent intent = new Intent(context, PermissionCheckActivity.class);
        if (!(context instanceof BaseActivity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.permission_check_activity;
    }

    @Override
    protected void initView() {
        rvPermissions = findViewById(R.id.rv_permissions);
        tvPermissionStatus = findViewById(R.id.tv_permission_status);
        btnContinue = findViewById(R.id.btn_continue);
        btnRetry = findViewById(R.id.btn_retry);

        // 设置RecyclerView
        rvPermissions.setLayoutManager(new LinearLayoutManager(this));
        
        // 设置点击监听器
        setOnClickListener(btnContinue, btnRetry);
    }

    @Override
    protected void initData() {
        // 获取Intent参数
        Intent intent = getIntent();
        nextActivityName = intent.getStringExtra("next_activity");
        finishCurrent = intent.getBooleanExtra("finish_current", false);
        
        initPermissionList();
        adapter = new PermissionAdapter(permissionList, this::onPermissionItemClick);
        rvPermissions.setAdapter(adapter);
        
        // 检查权限状态
        checkAllPermissions();
    }

    /**
     * 初始化权限列表
     */
    private void initPermissionList() {
        permissionList = new ArrayList<>();
        
        // 1. 精确闹钟权限（必需）
        permissionList.add(new PermissionItem(
            "精确闹钟权限",
            "用于设置精确的用药提醒时间",
            Permission.SCHEDULE_EXACT_ALARM,
            PermissionItem.Type.ESSENTIAL,
            true // 使用XXPermissions处理
        ));

        // 2. 通知权限（必需，Android 13+）
        if (Build.VERSION.SDK_INT >= 33) { // Android 13+ (API 33)
            permissionList.add(new PermissionItem(
                "通知权限",
                "用于显示用药提醒通知",
                "android.permission.POST_NOTIFICATIONS",
                PermissionItem.Type.ESSENTIAL,
                false // 需要手动处理
            ));
        }

        // 3. 悬浮窗权限（推荐，用于自启动）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionList.add(new PermissionItem(
                "悬浮窗权限",
                "用于提醒时自动启动应用到前台",
                "android.permission.SYSTEM_ALERT_WINDOW",
                PermissionItem.Type.RECOMMENDED,
                false // 需要手动处理
            ));
        }

        // 4. 相机权限（可选）
        permissionList.add(new PermissionItem(
            "相机权限",
            "用于拍摄药品照片和OCR识别",
            Permission.CAMERA,
            PermissionItem.Type.RECOMMENDED,
            true
        ));

        // 5. 存储权限（可选）
        permissionList.add(new PermissionItem(
            "存储权限",
            "用于保存图片和文件",
            Permission.WRITE_EXTERNAL_STORAGE,
            PermissionItem.Type.OPTIONAL,
            true
        ));

        // 6. 震动权限（可选）
        permissionList.add(new PermissionItem(
            "震动权限",
            "用于提醒时震动提示",
            "android.permission.VIBRATE",
            PermissionItem.Type.OPTIONAL,
            true
        ));

        // 7. 麦克风权限（必须）
        permissionList.add(new PermissionItem(
            "麦克风权限",
                "用于语音对话功能",
                Permission.RECORD_AUDIO,
                PermissionItem.Type.ESSENTIAL,
                true
        ));
    }

    /**
     * 检查所有权限状态
     */
    private void checkAllPermissions() {
        int grantedCount = 0;
        int essentialGrantedCount = 0;
        int essentialTotalCount = 0;

        for (PermissionItem item : permissionList) {
            boolean isGranted = checkSinglePermission(item);
            item.setGranted(isGranted);
            
            if (isGranted) {
                grantedCount++;
            }
            
            if (item.getType() == PermissionItem.Type.ESSENTIAL) {
                essentialTotalCount++;
                if (isGranted) {
                    essentialGrantedCount++;
                }
            }
        }

        // 更新UI
        adapter.notifyDataSetChanged();
        updatePermissionStatus(grantedCount, permissionList.size(), essentialGrantedCount, essentialTotalCount);
    }

    /**
     * 检查单个权限
     */
    private boolean checkSinglePermission(PermissionItem item) {
        if (item.isUseXXPermissions()) {
            return XXPermissions.isGranted(this, item.getPermission());
        } else {
            // 手动检查特殊权限
            switch (item.getPermission()) {
                case "android.permission.POST_NOTIFICATIONS":
                    return checkNotificationPermission();
                case "android.permission.SYSTEM_ALERT_WINDOW":
                    return checkOverlayPermission();
                default:
                    return false;
            }
        }
    }

    /**
     * 检查通知权限
     */
    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) { // Android 13+ (API 33)
            return androidx.core.content.ContextCompat.checkSelfPermission(this, 
                "android.permission.POST_NOTIFICATIONS") == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return true; // Android 13以下默认有通知权限
    }

    /**
     * 检查悬浮窗权限
     */
    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return android.provider.Settings.canDrawOverlays(this);
        }
        return true; // Android 6.0以下默认有悬浮窗权限
    }

    /**
     * 更新权限状态显示
     */
    private void updatePermissionStatus(int grantedCount, int totalCount, int essentialGrantedCount, int essentialTotalCount) {
        if (essentialGrantedCount == essentialTotalCount) {
            tvPermissionStatus.setText(String.format("权限检查完成 (%d/%d)\n所有必需权限已授予，应用可以正常使用", 
                grantedCount, totalCount));
            tvPermissionStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnContinue.setVisibility(View.VISIBLE);
            btnContinue.setText("继续使用");
        } else {
            tvPermissionStatus.setText(String.format("权限检查 (%d/%d)\n还有 %d 个必需权限未授予，请点击下方项目进行授权", 
                grantedCount, totalCount, essentialTotalCount - essentialGrantedCount));
            tvPermissionStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnContinue.setVisibility(View.GONE);
        }
    }

    /**
     * 权限项点击事件
     */
    private void onPermissionItemClick(PermissionItem item) {
        if (item.isGranted()) {
            toast("该权限已授予");
            return;
        }

        if (item.isUseXXPermissions()) {
            // 使用XXPermissions申请权限
            XXPermissions.with(this)
                .permission(item.getPermission())
                .request((permissions, allGranted) -> {
                    if (allGranted) {
                        toast(item.getName() + " 权限已授予");
                    } else {
                        toast(item.getName() + " 权限被拒绝");
                    }
                    // 重新检查权限
                    checkAllPermissions();
                });
        } else {
            // 手动跳转到权限设置页面
            requestSpecialPermission(item);
        }
    }

    /**
     * 申请特殊权限
     */
    private void requestSpecialPermission(PermissionItem item) {
        try {
            Intent intent = null;
            switch (item.getPermission()) {
                case "android.permission.POST_NOTIFICATIONS":
                    // Android 13+ 通知权限 - 跳转到应用通知设置页面
                    if (Build.VERSION.SDK_INT >= 33) { // Android 13+ (API 33)
                        intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                        intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
                        
                        // 检查是否有应用可以处理这个Intent
                        if (intent.resolveActivity(getPackageManager()) == null) {
                            // 备用方案：跳转到应用详细信息页面
                            intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                        }
                    }
                    break;
                case "android.permission.SYSTEM_ALERT_WINDOW":
                    // 悬浮窗权限
                    intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    break;
            }

            if (intent != null) {
                startActivity(intent);
                toast("请在设置页面授予 " + item.getName());
            }
        } catch (Exception e) {
            toast("无法打开权限设置页面");
            android.util.Log.e("PermissionCheckActivity", "打开权限设置失败: " + e.getMessage());
        }
    }

    @SingleClick
    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        
        if (viewId == R.id.btn_continue) {
            // 权限检查完成，跳转到指定Activity或直接关闭
            if (nextActivityName != null) {
                try {
                    Class<?> nextClass = Class.forName(nextActivityName);
                    Intent intent = new Intent(this, nextClass);
                    startActivity(intent);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    toast("无法找到目标页面，将直接退出权限检查");
                }
            }
            
            if (finishCurrent) {
                finish();
            }
        } else if (viewId == R.id.btn_retry) {
            // 重新检查权限
            checkAllPermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从设置页面返回时重新检查权限
        checkAllPermissions();
    }
}
