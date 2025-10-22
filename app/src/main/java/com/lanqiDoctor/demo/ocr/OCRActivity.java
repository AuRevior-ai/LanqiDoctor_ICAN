package com.lanqiDoctor.demo.ocr;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lanqiDoctor.demo.common.Utils;
import com.lanqiDoctor.demo.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class OCRActivity extends Activity implements View.OnClickListener {
    private static final String TAG = OCRActivity.class.getSimpleName();
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;

    ImageView ivPreview;
    TextView tvResult;
    TextView tvStatus;
    Button btnCamera;
    Button btnGallery;

    String currentPhotoPath;
    
    // Model settings of object detection
    protected String detModelPath = "PP-OCRv5_mobile_det.nb";
    protected String recModelPath = "PP-OCRv5_mobile_rec.nb";
    protected String clsModelPath = "ch_ppocr_mobile_v2.0_cls_slim_opt.nb";
    protected String labelPath = "ppocr_keys_ocrv5.txt";
    protected String configPath = "config.txt";
    protected int cpuThreadNum = 1;
    protected String cpuPowerMode = "LITE_POWER_HIGH";

    Native predictor = new Native();
    private boolean isModelInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_new);

        // Init UI components
        initView();

        // Check and request permissions
        if (!checkAllPermissions()) {
            requestAllPermissions();
        } else {
            // Initialize OCR models
            checkRun();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera:
                openCamera();
                break;
            case R.id.btn_gallery:
                openGallery();
                break;
        }
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestAllPermissions();
            return;
        }
        
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "创建图片文件失败", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getPackageName() + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "OCR_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            String imagePath = null;
            
            if (requestCode == REQUEST_CAMERA) {
                imagePath = currentPhotoPath;
            } else if (requestCode == REQUEST_GALLERY && data != null) {
                Uri selectedImageUri = data.getData();
                imagePath = Utils.getPathFromUri(this, selectedImageUri);
            }
            
            if (imagePath != null) {
                processImage(imagePath);
            }
        }
    }

    private void processImage(String imagePath) {
        tvStatus.setText("正在识别中...");
        
        // 显示原始图片
        displayImage(imagePath);
        
        // 检查模型是否已初始化
        if (!isModelInitialized || predictor == null) {
            tvStatus.setText("模型未初始化");
            Toast.makeText(this, "OCR模型未初始化，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            Log.d("OCR", "Processing image: " + imagePath);
            
            // 先对图片进行医嘱区域裁剪（保留25%-70%高度区域）
            String croppedImagePath = cropPrescriptionArea(imagePath);
            if (croppedImagePath == null) {
                tvResult.setText("图片裁剪失败");
                tvStatus.setText("识别失败");
                return;
            }
            
            // 对裁剪后的图片进行OCR识别
            String[] results = predictor.processImageFile(croppedImagePath);
            displayResults(results);
            tvStatus.setText("识别完成");
            
            // 可选：显示裁剪后的图片以便调试
            // displayCroppedImage(croppedImagePath);
            
            // 跳转到处方分析页面
            if (results != null && results.length > 0) {
                Intent analysisIntent = new Intent(this, PrescriptionAnalysisActivity.class);
                analysisIntent.putExtra(PrescriptionAnalysisActivity.EXTRA_OCR_RESULTS, results);
                startActivity(analysisIntent);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            tvResult.setText("识别失败：" + e.getMessage());
            tvStatus.setText("识别失败");
        }
    }

    private void displayImage(String imagePath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                ivPreview.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "显示图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayResults(String[] results) {
        if (results == null || results.length == 0) {
            tvResult.setText("未识别到医嘱内容");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("识别到的医嘱信息：\n\n");
        
        // 倒序显示结果，从上往下的顺序
        for (int i = results.length - 1; i >= 0; i--) {
            String text = results[i].trim();
            if (!text.isEmpty()) {
                sb.append("• ").append(text).append("\n");
            }
        }
        
        // 添加提示信息
        sb.append("\n提示：已自动裁剪并识别医嘱区域（25%-70%高度）");
        
        tvResult.setText(sb.toString());
        
        Log.d("OCR", "Prescription recognition results: " + sb.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Initialize OCR models if permissions are granted
        if (checkAllPermissions() && predictor != null) {
            checkRun();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (predictor != null) {
            predictor.release();
        }
        
        super.onDestroy();
    }

    public void initView() {
        ivPreview = findViewById(R.id.iv_preview);
        tvResult = findViewById(R.id.tv_result);
        tvStatus = findViewById(R.id.tv_status);
        btnCamera = findViewById(R.id.btn_camera);
        btnGallery = findViewById(R.id.btn_gallery);
        
        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
    }

    public void checkRun() {
        try {
            tvStatus.setText("正在初始化模型...");
            
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

            boolean initResult = predictor.init(
                    this,
                    detRealModelDir,
                    clsRealModelDir,
                    recRealModelDir,
                    configRealDir,
                    labelRealDir,
                    cpuThreadNum,
                    cpuPowerMode);
            
            if (initResult) {
                isModelInitialized = true;
                tvStatus.setText("准备就绪");
                Toast.makeText(this, "OCR模型初始化成功", Toast.LENGTH_SHORT).show();
            } else {
                isModelInitialized = false;
                tvStatus.setText("模型初始化失败");
                Toast.makeText(this, "OCR模型初始化失败", Toast.LENGTH_LONG).show();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            tvStatus.setText("初始化出错");
            Toast.makeText(this, "初始化出错: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(OCRActivity.this)
                    .setTitle("Permission denied")
                    .setMessage("Click to force quit the app, then open Settings->Apps & notifications->Target " +
                            "App->Permissions to grant all of the permissions.")
                    .setCancelable(false)
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            OCRActivity.this.finish();
                        }
                    }).show();
        }
    }

    private void requestAllPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA}, 0);
    }

    private boolean checkAllPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * 裁剪处方单图片，保留医嘱区域（垂直方向25%-70%的区域）
     * @param originalImagePath 原始图片路径
     * @return 裁剪后的图片路径，失败返回null
     */
    private String cropPrescriptionArea(String originalImagePath) {
        try {
            // 读取原始图片
            Bitmap originalBitmap = BitmapFactory.decodeFile(originalImagePath);
            if (originalBitmap == null) {
                Log.e("OCR", "Failed to load original image: " + originalImagePath);
                return null;
            }
            
            int originalWidth = originalBitmap.getWidth();
            int originalHeight = originalBitmap.getHeight();
            
            Log.d("OCR", "Original image size: " + originalWidth + "x" + originalHeight);
            
            // 计算裁剪区域（保留25%-70%高度，宽度保持不变）
            int cropStartY = (int) (originalHeight * 0.25f);  // 从25%高度开始
            int cropEndY = (int) (originalHeight * 0.70f);    // 到70%高度结束
            int cropHeight = cropEndY - cropStartY;           // 裁剪高度为45%
            
            // 确保裁剪区域有效
            if (cropHeight <= 0 || cropStartY >= originalHeight) {
                Log.e("OCR", "Invalid crop area");
                originalBitmap.recycle();
                return null;
            }
            
            Log.d("OCR", "Crop area: startY=" + cropStartY + ", height=" + cropHeight);
            
            // 进行裁剪
            Bitmap croppedBitmap = Bitmap.createBitmap(
                originalBitmap, 
                0,              // x坐标从0开始（保留整个宽度）
                cropStartY,     // y坐标从25%高度开始
                originalWidth,  // 保留整个宽度
                cropHeight      // 裁剪高度为45%
            );
            
            // 释放原始bitmap内存
            originalBitmap.recycle();
            
            if (croppedBitmap == null) {
                Log.e("OCR", "Failed to create cropped bitmap");
                return null;
            }
            
            // 保存裁剪后的图片
            String croppedImagePath = createCroppedImageFile();
            if (croppedImagePath == null) {
                croppedBitmap.recycle();
                return null;
            }
            
            // 将裁剪后的bitmap保存到文件
            java.io.FileOutputStream out = null;
            try {
                out = new java.io.FileOutputStream(croppedImagePath);
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                Log.d("OCR", "Cropped image saved to: " + croppedImagePath);
            } catch (Exception e) {
                Log.e("OCR", "Failed to save cropped image", e);
                croppedBitmap.recycle();
                return null;
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            // 释放裁剪后的bitmap内存
            croppedBitmap.recycle();
            
            return croppedImagePath;
            
        } catch (Exception e) {
            Log.e("OCR", "Error during image cropping", e);
            return null;
        }
    }
    
    /**
     * 创建裁剪后图片的文件路径
     */
    private String createCroppedImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "OCR_Cropped_" + timeStamp + ".jpg";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile = new File(storageDir, imageFileName);
            return imageFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e("OCR", "Failed to create cropped image file path", e);
            return null;
        }
    }
    
    /**
     * 显示裁剪后的图片（用于调试和验证裁剪效果）
     */
    private void displayCroppedImage(String croppedImagePath) {
        try {
            Bitmap croppedBitmap = BitmapFactory.decodeFile(croppedImagePath);
            if (croppedBitmap != null) {
                ivPreview.setImageBitmap(croppedBitmap);
                Log.d("OCR", "Displaying cropped image: " + croppedImagePath);
            }
        } catch (Exception e) {
            Log.e("OCR", "Failed to display cropped image", e);
        }
    }
}
