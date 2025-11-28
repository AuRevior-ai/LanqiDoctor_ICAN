package com.lanqiDoctor.demo.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.hjq.bar.TitleBar;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.app.AppActivity;
import com.lanqiDoctor.demo.entity.MoodRecord;
import com.lanqiDoctor.demo.ui.adapter.MoodPhotoPreviewAdapter;
import com.lanqiDoctor.demo.manager.MoodRecordManager;
import com.lanqiDoctor.demo.model.MoodLevel;
import com.lanqiDoctor.demo.other.AppConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 心情记录编辑页
 */
public class MoodRecordEditorActivity extends AppActivity {

    public static final String EXTRA_DAY = "extra_day";
    public static final String EXTRA_RECORD = "extra_record";

    private static final int REQUEST_CAMERA_PERMISSION = 201;

    private TextView tvSelectedDate;
    private RadioGroup rgMood;
    private EditText etNote;
    private ImageView ivPreview;
    private RecyclerView rvPhotos;
    private Button btnDelete;

    private MoodRecord editingRecord;
    private long dayMillis;
    @Nullable
    private File currentPhotoFile;
    @Nullable
    private Uri pendingCameraUri;

    private MoodRecordManager recordManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.CHINA);

    private ActivityResultLauncher<Uri> takePhotoLauncher;
    private ActivityResultLauncher<String> pickImagesLauncher;
    private MoodPhotoPreviewAdapter photoAdapter;
    private final List<String> selectedImageUris = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_mood_record_editor;
    }

    @Override
    protected void initView() {
        TitleBar titleBar = findViewById(R.id.tb_title);
        if (titleBar != null) {
            titleBar.setTitle(R.string.mood_track_title);
            titleBar.setLeftIcon(R.drawable.ic_arrow_left);
        }

        tvSelectedDate = findViewById(R.id.tv_selected_date);
        rgMood = findViewById(R.id.rg_mood);
        etNote = findViewById(R.id.et_note);
        ivPreview = findViewById(R.id.iv_preview);
        rvPhotos = findViewById(R.id.rv_photos);
        btnDelete = findViewById(R.id.btn_delete);
        ImageButton btnTakePhoto = findViewById(R.id.btn_take_photo);
        ImageButton btnPickImage = findViewById(R.id.btn_pick_image);
        Button btnSave = findViewById(R.id.btn_save);

        ((RadioButton) findViewById(R.id.rb_mood_very_sad)).setText(MoodLevel.VERY_SAD.getEmoji());
        ((RadioButton) findViewById(R.id.rb_mood_sad)).setText(MoodLevel.SAD.getEmoji());
        ((RadioButton) findViewById(R.id.rb_mood_neutral)).setText(MoodLevel.NEUTRAL.getEmoji());
        ((RadioButton) findViewById(R.id.rb_mood_happy)).setText(MoodLevel.HAPPY.getEmoji());
        ((RadioButton) findViewById(R.id.rb_mood_very_happy)).setText(MoodLevel.VERY_HAPPY.getEmoji());

        btnSave.setOnClickListener(v -> saveRecord());
        btnDelete.setOnClickListener(v -> deleteRecord());
        btnTakePhoto.setOnClickListener(v -> openCamera());
        btnPickImage.setOnClickListener(v -> pickImages());

        photoAdapter = new MoodPhotoPreviewAdapter();
        photoAdapter.setOnRemoveListener(this::removeImageAt);
        rvPhotos.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        rvPhotos.setAdapter(photoAdapter);

        takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result != null && result) {
                if (pendingCameraUri != null) {
                    addImageUri(pendingCameraUri.toString());
                }
            } else {
                if (currentPhotoFile != null && currentPhotoFile.exists()) {
                    // 清理未使用文件
                    //noinspection ResultOfMethodCallIgnored
                    currentPhotoFile.delete();
                }
            }
            pendingCameraUri = null;
        });

        pickImagesLauncher = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
            if (uris == null || uris.isEmpty()) {
                toast(R.string.mood_image_pick_failed);
                return;
            }
            Set<String> existing = new HashSet<>(selectedImageUris);
            boolean changed = false;
            for (Uri uri : uris) {
                if (uri == null) {
                    continue;
                }
                String value = uri.toString();
                if (!existing.contains(value)) {
                    selectedImageUris.add(value);
                    existing.add(value);
                    changed = true;
                }
            }
            if (changed) {
                refreshPhotoSection();
            } else {
                toast(R.string.mood_image_pick_failed);
            }
        });
    }

    @Override
    protected void initData() {
        recordManager = MoodRecordManager.getInstance(this);
        parseArguments();
        bindInitialValues();
    }

    private void parseArguments() {
        Intent intent = getIntent();
        dayMillis = intent.getLongExtra(EXTRA_DAY, System.currentTimeMillis());
        Object recordObj = intent.getSerializableExtra(EXTRA_RECORD);
        if (recordObj instanceof MoodRecord) {
            editingRecord = (MoodRecord) recordObj;
            if (editingRecord.getRecordDate() > 0) {
                dayMillis = editingRecord.getRecordDate();
            }
            List<String> uris = editingRecord.getImageUriList();
            selectedImageUris.clear();
            if (uris != null && !uris.isEmpty()) {
                selectedImageUris.addAll(uris);
            }
        }
    }

    private void bindInitialValues() {
        tvSelectedDate.setText(dateFormat.format(new Date(dayMillis)));
        if (editingRecord != null) {
            btnDelete.setVisibility(Button.VISIBLE);
            MoodLevel moodLevel = MoodLevel.fromStorage(editingRecord.getMoodLevel());
            checkMoodButton(moodLevel);
            etNote.setText(editingRecord.getNote());
        } else {
            btnDelete.setVisibility(Button.GONE);
            checkMoodButton(MoodLevel.NEUTRAL);
        }
        refreshPhotoSection();
    }

    private void checkMoodButton(MoodLevel moodLevel) {
        if (moodLevel == null) {
            moodLevel = MoodLevel.NEUTRAL;
        }
        switch (moodLevel) {
            case VERY_SAD:
                rgMood.check(R.id.rb_mood_very_sad);
                break;
            case SAD:
                rgMood.check(R.id.rb_mood_sad);
                break;
            case HAPPY:
                rgMood.check(R.id.rb_mood_happy);
                break;
            case VERY_HAPPY:
                rgMood.check(R.id.rb_mood_very_happy);
                break;
            case NEUTRAL:
            default:
                rgMood.check(R.id.rb_mood_neutral);
                break;
        }
    }

    private void openCamera() {
        if (!ensureCameraPermission()) {
            return;
        }
        try {
            currentPhotoFile = createImageFile();
        } catch (IOException e) {
            toast(R.string.mood_camera_failed);
            return;
        }
        if (currentPhotoFile == null) {
            toast(R.string.mood_camera_failed);
            return;
        }
        pendingCameraUri = FileProvider.getUriForFile(this,
                AppConfig.getPackageName() + ".provider",
                currentPhotoFile);
        takePhotoLauncher.launch(pendingCameraUri);
    }

    private void pickImages() {
        pickImagesLauncher.launch("image/*");
    }

    private boolean ensureCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                toast(R.string.mood_camera_failed);
            }
        }
    }

    private void saveRecord() {
        MoodLevel selectedMood = resolveSelectedMood();
        if (selectedMood == null) {
            toast(R.string.mood_select_mood_hint);
            return;
        }

        if (editingRecord == null) {
            editingRecord = new MoodRecord();
        }
        editingRecord.setMoodLevel(selectedMood.name());
        editingRecord.setRecordDate(dayMillis);
        editingRecord.setNote(getTrimmedText(etNote));
        editingRecord.setFavorite(editingRecord.isFavorite());
        editingRecord.setActivities(null);
        editingRecord.setEnergyLevel(editingRecord.getEnergyLevel() == 0 ? 3 : editingRecord.getEnergyLevel());
        editingRecord.setStressLevel(editingRecord.getStressLevel() == 0 ? 3 : editingRecord.getStressLevel());
        editingRecord.setImageUriList(new ArrayList<>(selectedImageUris));

        recordManager.saveRecord(editingRecord, new MoodRecordManager.OperationCallback() {
            @Override
            public void onSuccess() {
                toast(R.string.mood_save_success);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                String message = TextUtils.isEmpty(errorMessage) ? getString(R.string.mood_save_failed) : errorMessage;
                toast(message);
            }
        });
    }

    private void deleteRecord() {
        if (editingRecord == null) {
            finish();
            return;
        }
        recordManager.deleteRecord(editingRecord, new MoodRecordManager.OperationCallback() {
            @Override
            public void onSuccess() {
                toast(R.string.mood_delete_success);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                String message = TextUtils.isEmpty(errorMessage) ? getString(R.string.mood_delete_failed) : errorMessage;
                toast(message);
            }
        });
    }

    private void refreshPhotoSection() {
        photoAdapter.submitList(new ArrayList<>(selectedImageUris));
        rvPhotos.setVisibility(selectedImageUris.isEmpty() ? View.GONE : View.VISIBLE);
        updateCoverPreview();
    }

    private void updateCoverPreview() {
        if (selectedImageUris.isEmpty()) {
            ivPreview.setVisibility(View.GONE);
            ivPreview.setImageDrawable(null);
            return;
        }
        ivPreview.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(Uri.parse(selectedImageUris.get(0)))
                .placeholder(R.drawable.bg_image_placeholder)
                .centerCrop()
                .into(ivPreview);
    }

    private void addImageUri(String uri) {
        if (TextUtils.isEmpty(uri)) {
            return;
        }
        if (selectedImageUris.contains(uri)) {
            return;
        }
        selectedImageUris.add(uri);
        refreshPhotoSection();
    }

    private void removeImageAt(int position) {
        if (position < 0 || position >= selectedImageUris.size()) {
            return;
        }
        selectedImageUris.remove(position);
        refreshPhotoSection();
    }

    @Nullable
    private MoodLevel resolveSelectedMood() {
        int checkedId = rgMood.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_mood_very_sad) {
            return MoodLevel.VERY_SAD;
        } else if (checkedId == R.id.rb_mood_sad) {
            return MoodLevel.SAD;
        } else if (checkedId == R.id.rb_mood_happy) {
            return MoodLevel.HAPPY;
        } else if (checkedId == R.id.rb_mood_very_happy) {
            return MoodLevel.VERY_HAPPY;
        } else if (checkedId == R.id.rb_mood_neutral) {
            return MoodLevel.NEUTRAL;
        }
        return null;
    }

    @Nullable
    private String getTrimmedText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return null;
        }
        String text = editText.getText().toString().trim();
        return text.isEmpty() ? null : text;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            storageDir = new File(getExternalFilesDir(null), "mood_photos");
        } else {
            storageDir = getExternalFilesDir(null);
        }
        if (storageDir != null && !storageDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            storageDir.mkdirs();
        }
        return File.createTempFile("MOOD_" + timeStamp + "_", ".jpg", storageDir);
    }
}
