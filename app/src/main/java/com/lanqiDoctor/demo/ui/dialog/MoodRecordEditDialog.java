package com.lanqiDoctor.demo.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.entity.MoodRecord;
import com.lanqiDoctor.demo.manager.MoodRecordManager;
import com.lanqiDoctor.demo.model.MoodLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * 心情记录编辑弹窗
 */
public class MoodRecordEditDialog extends BottomSheetDialogFragment {

    private static final String ARG_DAY = "arg_day";
    private static final String ARG_RECORD = "arg_record";

    private MoodRecord editingRecord;
    private long dayMillis;

    private TextView tvTitle;
    private ChipGroup chipGroupMood;
    private SeekBar seekEnergy;
    private SeekBar seekStress;
    private TextView tvEnergyValue;
    private TextView tvStressValue;
    private TextInputEditText etNote;
    private TextInputEditText etActivities;
    private CheckBox cbFavorite;
    private Button btnDelete;

    private MoodRecordManager recordManager;

    private final Map<Integer, MoodLevel> chipIdToMood = new HashMap<>();
    private final Map<MoodLevel, Integer> moodToChipId = new HashMap<>();

    public static MoodRecordEditDialog newInstance(long dayMillis, @Nullable MoodRecord record) {
        MoodRecordEditDialog dialog = new MoodRecordEditDialog();
        Bundle bundle = new Bundle();
        bundle.putLong(ARG_DAY, dayMillis);
        if (record != null) {
            bundle.putSerializable(ARG_RECORD, record);
        }
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        recordManager = MoodRecordManager.getInstance(context.getApplicationContext());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        recordManager = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_mood_record_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parseArguments();
        bindViews(view);
        setupMoodChips(view);
        setupSeekBars();
        populateInitialValues();
        setupActions(view);
    }

    private void parseArguments() {
        Bundle args = getArguments();
        if (args != null) {
            dayMillis = args.getLong(ARG_DAY, System.currentTimeMillis());
            Object recordObj = args.getSerializable(ARG_RECORD);
            if (recordObj instanceof MoodRecord) {
                editingRecord = (MoodRecord) recordObj;
                if (editingRecord.getRecordDate() > 0) {
                    dayMillis = editingRecord.getRecordDate();
                }
            }
        } else {
            dayMillis = System.currentTimeMillis();
        }
    }

    private void bindViews(View view) {
        tvTitle = view.findViewById(R.id.tv_dialog_title);
        chipGroupMood = view.findViewById(R.id.chip_group_mood);
        seekEnergy = view.findViewById(R.id.seek_energy);
        seekStress = view.findViewById(R.id.seek_stress);
        tvEnergyValue = view.findViewById(R.id.tv_energy_value);
        tvStressValue = view.findViewById(R.id.tv_stress_value);
        etNote = view.findViewById(R.id.et_note);
        etActivities = view.findViewById(R.id.et_activities);
        cbFavorite = view.findViewById(R.id.cb_favorite);
        btnDelete = view.findViewById(R.id.btn_delete);
    }

    private void setupMoodChips(View view) {
        chipIdToMood.put(R.id.chip_mood_very_happy, MoodLevel.VERY_HAPPY);
        chipIdToMood.put(R.id.chip_mood_happy, MoodLevel.HAPPY);
        chipIdToMood.put(R.id.chip_mood_neutral, MoodLevel.NEUTRAL);
        chipIdToMood.put(R.id.chip_mood_sad, MoodLevel.SAD);
        chipIdToMood.put(R.id.chip_mood_very_sad, MoodLevel.VERY_SAD);
        for (Map.Entry<Integer, MoodLevel> entry : chipIdToMood.entrySet()) {
            moodToChipId.put(entry.getValue(), entry.getKey());
            Chip chip = view.findViewById(entry.getKey());
            if (chip != null) {
                chip.setCheckable(true);
            }
        }
    }

    private void setupSeekBars() {
        seekEnergy.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvEnergyValue.setText(String.valueOf(progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekStress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvStressValue.setText(String.valueOf(progress + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void populateInitialValues() {
        if (editingRecord != null) {
            tvTitle.setText(R.string.mood_edit_title);
            MoodLevel moodLevel = MoodLevel.fromStorage(editingRecord.getMoodLevel());
            Integer chipId = moodToChipId.get(moodLevel);
            if (chipId != null) {
                chipGroupMood.check(chipId);
            }
            int energy = Math.max(1, editingRecord.getEnergyLevel());
            int stress = Math.max(1, editingRecord.getStressLevel());
            seekEnergy.setProgress(energy - 1);
            seekStress.setProgress(stress - 1);
            tvEnergyValue.setText(String.valueOf(energy));
            tvStressValue.setText(String.valueOf(stress));
            if (!TextUtils.isEmpty(editingRecord.getNote())) {
                etNote.setText(editingRecord.getNote());
            }
            if (!TextUtils.isEmpty(editingRecord.getActivities())) {
                etActivities.setText(editingRecord.getActivities());
            }
            cbFavorite.setChecked(editingRecord.isFavorite());
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            tvTitle.setText(R.string.mood_create_title);
            chipGroupMood.check(R.id.chip_mood_neutral);
            seekEnergy.setProgress(2);
            seekStress.setProgress(2);
            tvEnergyValue.setText(String.valueOf(seekEnergy.getProgress() + 1));
            tvStressValue.setText(String.valueOf(seekStress.getProgress() + 1));
            cbFavorite.setChecked(false);
            btnDelete.setVisibility(View.GONE);
        }
    }

    private void setupActions(View view) {
        view.findViewById(R.id.btn_save).setOnClickListener(v -> saveRecord());
        btnDelete.setOnClickListener(v -> deleteRecord());
    }

    private void saveRecord() {
        MoodLevel selectedMood = getSelectedMood();
        if (selectedMood == null) {
            showToast(getString(R.string.mood_select_mood_hint));
            return;
        }
        if (editingRecord == null) {
            editingRecord = new MoodRecord();
        }
        editingRecord.setMoodLevel(selectedMood.name());
        editingRecord.setEnergyLevel(seekEnergy.getProgress() + 1);
        editingRecord.setStressLevel(seekStress.getProgress() + 1);
        editingRecord.setNote(getTextSafely(etNote));
        editingRecord.setActivities(getTextSafely(etActivities));
        editingRecord.setFavorite(cbFavorite.isChecked());
        editingRecord.setRecordDate(dayMillis);

        if (recordManager == null) {
            showToast(getString(R.string.mood_save_failed));
            return;
        }

        recordManager.saveRecord(editingRecord, new MoodRecordManager.OperationCallback() {
            @Override
            public void onSuccess() {
                showToast(getString(R.string.mood_save_success));
                dismissAllowingStateLoss();
            }

            @Override
            public void onError(String errorMessage) {
                String message = TextUtils.isEmpty(errorMessage) ? getString(R.string.mood_delete_failed) : errorMessage;
                showToast(message);
            }
        });
    }

    private void deleteRecord() {
        if (editingRecord == null || recordManager == null) {
            return;
        }
        recordManager.deleteRecord(editingRecord, new MoodRecordManager.OperationCallback() {
            @Override
            public void onSuccess() {
                showToast(getString(R.string.mood_delete_success));
                dismissAllowingStateLoss();
            }

            @Override
            public void onError(String errorMessage) {
                String message = TextUtils.isEmpty(errorMessage) ? getString(R.string.mood_save_failed) : errorMessage;
                showToast(message);
            }
        });
    }

    @Nullable
    private MoodLevel getSelectedMood() {
        int checkedId = chipGroupMood.getCheckedChipId();
        return chipIdToMood.get(checkedId);
    }

    private String getTextSafely(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) {
            return null;
        }
        String value = editText.getText().toString().trim();
        return value.isEmpty() ? null : value;
    }

    private void showToast(String message) {
        if (TextUtils.isEmpty(message) || !isAdded()) {
            return;
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
