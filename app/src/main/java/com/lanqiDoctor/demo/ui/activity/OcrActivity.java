package com.lanqiDoctor.demo.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.hjq.base.BaseActivity;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.common.CameraSurfaceView;
import com.lanqiDoctor.demo.common.Utils;
import com.lanqiDoctor.demo.ocr.Native;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * OCR识别Activity - 医嘱识别功能
 * 
 * 集成了PaddleLite OCR功能，用于识别医嘱文本
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class OcrActivity extends BaseActivity implements View.OnClickListener, CameraSurfaceView.OnTextureChangedListener {
    private static final String TAG = OcrActivity.class.getSimpleName();

    CameraSurfaceView svPreview;
    TextView tvStatus;
    ImageButton btnSwitch;
    ImageButton btnShutter;

    String savedImagePath = "images/save.jpg";
    int lastFrameIndex = 0;
    long lastFrameTime;

    // Model settings of OCR detection
    protected String detModelPath = "PP-OCRv5_mobile_det.nb";
    protected String recModelPath = "PP-OCRv5_mobile_rec.nb";
    protected String clsModelPath = "ch_ppocr_mobile_v2.0_cls_slim_opt.nb";
    protected String labelPath = "ppocr_keys_ocrv5.txt";
    protected String configPath = "config.txt";
    protected int cpuThreadNum = 1;
    protected String cpuPowerMode = "LITE_POWER_HIGH";

    Native predictor = new Native();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_ocr;
    }

    @Override
    protected void initView() {
        // Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Init the camera preview and UI components
        svPreview = (CameraSurfaceView) findViewById(R.id.sv_preview);
        svPreview.setOnTextureChangedListener(this);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        btnSwitch = (ImageButton) findViewById(R.id.btn_switch);
        btnSwitch.setOnClickListener(this);
        btnShutter = (ImageButton) findViewById(R.id.btn_shutter);
        btnShutter.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        // Check and request CAMERA and WRITE_EXTERNAL_STORAGE permissions
        if (!checkAllPermissions()) {
            requestAllPermissions();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch:
                svPreview.switchCamera();
                break;
            case R.id.btn_shutter:
                SimpleDateFormat date = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                synchronized (this) {
                    savedImagePath = Utils.getDCIMDirectory() + File.separator + date.format(new Date()).toString() + ".png";
                }
                Toast.makeText(OcrActivity.this, "保存快照到 " + savedImagePath, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onTextureChanged(int inTextureId, int outTextureId, int textureWidth, int textureHeight) {
        String savedImagePath = "";
        synchronized (this) {
            savedImagePath = OcrActivity.this.savedImagePath;
        }
        savedImagePath = Utils.getDCIMDirectory() + File.separator + "result.jpg";
        boolean modified = predictor.process(inTextureId, outTextureId, textureWidth, textureHeight, savedImagePath);
        if (!savedImagePath.isEmpty()) {
            synchronized (this) {
                OcrActivity.this.savedImagePath = "";
            }
        }
        lastFrameIndex++;
        if (lastFrameIndex >= 30) {
            final int fps = (int) (lastFrameIndex * 1e9 / (System.nanoTime() - lastFrameTime));
            runOnUiThread(new Runnable() {
                public void run() {
                    tvStatus.setText(Integer.toString(fps) + "fps");
                }
            });
            lastFrameIndex = 0;
            lastFrameTime = System.nanoTime();
        }
        return modified;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload settings and re-initialize the predictor
        checkRun();
        // Open camera until the permissions have been granted
        if (!checkAllPermissions()) {
            svPreview.disableCamera();
        }
        svPreview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        svPreview.onPause();
    }

    @Override
    protected void onDestroy() {
        if (predictor != null) {
            predictor.release();
        }
        super.onDestroy();
    }

    public void checkRun() {
        try {
            Utils.copyAssets(this, labelPath);
            String labelRealDir = new File(
                    this.getExternalFilesDir(null),
                    labelPath).getAbsolutePath();

            Utils.copyAssets(this, configPath);
            String configRealDir = new File(
                    this.getExternalFilesDir(null),
                    configPath).getAbsolutePath();

            Utils.copyAssets(this, detModelPath);
            String detRealModelDir = new File(
                    this.getExternalFilesDir(null),
                    detModelPath).getAbsolutePath();

            Utils.copyAssets(this, clsModelPath);
            String clsRealModelDir = new File(
                    this.getExternalFilesDir(null),
                    clsModelPath).getAbsolutePath();

            Utils.copyAssets(this, recModelPath);
            String recRealModelDir = new File(
                    this.getExternalFilesDir(null),
                    recModelPath).getAbsolutePath();

            predictor.init(
                    this,
                    detRealModelDir,
                    clsRealModelDir,
                    recRealModelDir,
                    configRealDir,
                    labelRealDir,
                    cpuThreadNum,
                    cpuPowerMode);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(OcrActivity.this)
                    .setTitle("权限被拒绝")
                    .setMessage("点击强制退出应用，然后打开设置->应用和通知->目标应用->权限来授予所有权限。")
                    .setCancelable(false)
                    .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            OcrActivity.this.finish();
                        }
                    }).show();
        }
    }

    private void requestAllPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA}, 0);
    }

    private boolean checkAllPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
}
