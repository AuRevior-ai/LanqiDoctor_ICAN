package com.lanqiDoctor.demo.ui.fragment;

import android.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.entity.MoodRecord;
import com.lanqiDoctor.demo.manager.MoodRecordManager;
import com.lanqiDoctor.demo.ui.activity.MoodTrackActivity;
import com.lanqiDoctor.demo.ui.adapter.MoodMemoriesAdapter;

import java.util.List;

/**
 * 忆迹页面
 */
public class MoodMemoriesFragment extends BaseMoodFragment implements MoodTrackPage, MoodRecordManager.DataChangedListener {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private MoodMemoriesAdapter adapter;
    private MoodRecordManager recordManager;

    public static MoodMemoriesFragment newInstance() {
        return new MoodMemoriesFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mood_memories;
    }

    @Override
    protected void initView() {
        recyclerView = findViewById(R.id.rv_memories);
        tvEmpty = findViewById(R.id.tv_memories_empty);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new MoodMemoriesAdapter();
        adapter.setActionListener(new MoodMemoriesAdapter.MemoryActionListener() {
            @Override
            public void onRecordClick(MoodRecord record) {
                MoodTrackActivity activity = getMoodTrackActivity();
                if (activity != null) {
                    activity.openEditRecordEditor(record);
                }
            }

            @Override
            public void onRecordLongPress(MoodRecord record) {
                showDeleteConfirm(record);
            }

            @Override
            public void onToggleFavorite(MoodRecord record) {
                if (recordManager == null) {
                    return;
                }
                recordManager.toggleFavorite(record, !record.isFavorite(), new MoodRecordManager.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        // 刷新列表
                        loadMemories();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        showToast(errorMessage);
                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void initData() {
        recordManager = MoodRecordManager.getInstance(requireContext());
        recordManager.registerListener(this);
        loadMemories();
    }

    @Override
    public void onPageSelected() {
        loadMemories();
    }

    @Override
    public void onAddRecordRequested() {
        MoodTrackActivity activity = getMoodTrackActivity();
        if (activity != null) {
                    activity.openCreateRecordEditor(System.currentTimeMillis());
        }
    }

    @Override
    public void onMoodDataChanged() {
        loadMemories();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (recordManager != null) {
            recordManager.unregisterListener(this);
        }
    }

    private void loadMemories() {
        if (recordManager == null) {
            return;
        }
        recordManager.loadMemoryRecords(new MoodRecordManager.LoadCallback<List<MoodRecord>>() {
            @Override
            public void onSuccess(List<MoodRecord> data) {
                adapter.submitList(data);
                tvEmpty.setVisibility(data == null || data.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private void showDeleteConfirm(final MoodRecord record) {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.mood_delete_confirm)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.mood_delete, (dialog, which) -> deleteRecord(record))
                .show();
    }

    private void deleteRecord(MoodRecord record) {
        if (recordManager == null) {
            return;
        }
        recordManager.deleteRecord(record, new MoodRecordManager.OperationCallback() {
            @Override
            public void onSuccess() {
                showToast(getString(R.string.mood_delete_success));
            }

            @Override
            public void onError(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }
}
