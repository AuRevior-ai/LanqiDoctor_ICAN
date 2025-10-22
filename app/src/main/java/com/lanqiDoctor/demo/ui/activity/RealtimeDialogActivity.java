package com.lanqiDoctor.demo.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.app.AppActivity;
import com.lanqiDoctor.demo.manager.RealtimeDialogManager;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 实时语音对话Activity
 */
public class RealtimeDialogActivity extends AppActivity {
    
    private static final int REQUEST_PERMISSIONS = 1001;
    
    private TextView tvStatus;
    private Button btnStart, btnStop;
    
    private RealtimeDialogManager dialogManager;
    private ExecutorService executorService;
    private Handler mainHandler;
    private boolean isDialogActive = false;
    private AudioManager audioManager;
    
    public static void start(Context context) {
        Intent intent = new Intent(context, RealtimeDialogActivity.class);
        context.startActivity(intent);
    }
    
    @Override
    protected int getLayoutId() {
        return R.layout.realtime_dialog_activity;
    }
    
    @Override
    protected void initView() {
        tvStatus = findViewById(R.id.tv_status);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        
        btnStart.setOnClickListener(v -> checkHeadphonesAndStart());
        btnStop.setOnClickListener(v -> stopRealtimeDialog());
        
        btnStop.setEnabled(false);
    }
    
    @Override
    protected void initData() {
        executorService = Executors.newFixedThreadPool(3);
        mainHandler = new Handler(Looper.getMainLooper());
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
        // 检查权限
        checkPermissions();
        
        // 初始化对话管理器
        dialogManager = new RealtimeDialogManager(this);
        dialogManager.setStatusListener(this::updateStatus);
    }
    
    private void checkPermissions() {
        String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.INTERNET
        };
        
        boolean hasPermission = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                hasPermission = false;
                break;
            }
        }
        
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (!allGranted) {
                Toast.makeText(this, "需要录音权限才能使用语音对话功能", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    private void checkHeadphonesAndStart() {
        if (isDialogActive) {
            return;
        }
        
        // 检查是否连接耳机
        boolean isHeadphonesConnected = audioManager.isWiredHeadsetOn() || audioManager.isBluetoothA2dpOn();
        
        if (!isHeadphonesConnected) {
            // 显示耳机提醒对话框
            new AlertDialog.Builder(this)
                .setTitle("耳机提醒")
                .setMessage("检测到您未佩戴耳机。\n\n为了获得最佳体验并避免回声问题，强烈建议您佩戴耳机后再开始对话。\n\n是否继续？")
                .setIcon(R.drawable.ic_headphones)
                .setPositiveButton("继续使用", (dialog, which) -> {
                    startRealtimeDialog();
                })
                .setNegativeButton("取消", null)
                .setNeutralButton("我已佩戴耳机", (dialog, which) -> {
                    startRealtimeDialog();
                })
                .show();
        } else {
            startRealtimeDialog();
        }
    }
    
    private void startRealtimeDialog() {
        if (isDialogActive) {
            return;
        }
        
        updateStatus("正在启动实时对话...");
        btnStart.setEnabled(false);
        
        executorService.execute(() -> {
            try {
                String sessionId = UUID.randomUUID().toString();
                boolean success = dialogManager.startDialog(sessionId);
                
                mainHandler.post(() -> {
                    if (success) {
                        isDialogActive = true;
                        btnStop.setEnabled(true);
                        updateStatus("实时对话已启动，请开始说话");
                        Toast.makeText(this, "开始语音对话", Toast.LENGTH_SHORT).show();
                    } else {
                        btnStart.setEnabled(true);
                        updateStatus("启动失败，请重试");
                        Toast.makeText(this, "启动失败", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    btnStart.setEnabled(true);
                    updateStatus("启动失败: " + e.getMessage());
                    Toast.makeText(this, "启动失败", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void stopRealtimeDialog() {
        if (!isDialogActive) {
            return;
        }
        
        updateStatus("正在停止对话...");
        btnStop.setEnabled(false);
        
        executorService.execute(() -> {
            try {
                dialogManager.stopDialog();
                
                mainHandler.post(() -> {
                    isDialogActive = false;
                    btnStart.setEnabled(true);
                    updateStatus("对话已结束");
                    Toast.makeText(this, "对话已结束", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    btnStart.setEnabled(true);
                    updateStatus("停止失败: " + e.getMessage());
                });
            }
        });
    }
    
    private void updateStatus(String status) {
        runOnUiThread(() -> tvStatus.setText(status));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isDialogActive) {
            dialogManager.stopDialog();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}