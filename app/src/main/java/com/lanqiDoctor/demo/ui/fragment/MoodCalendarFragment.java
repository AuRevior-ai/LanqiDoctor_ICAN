package com.lanqiDoctor.demo.ui.fragment;

import android.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.entity.MoodRecord;
import com.lanqiDoctor.demo.manager.MoodRecordManager;
import com.lanqiDoctor.demo.ui.activity.MoodTrackActivity;
import com.lanqiDoctor.demo.ui.adapter.MoodCalendarAdapter;
import com.lanqiDoctor.demo.ui.adapter.MoodRecordAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 心情日历页面
 */
public class MoodCalendarFragment extends BaseMoodFragment implements MoodTrackPage, MoodRecordManager.DataChangedListener {

    private TextView tvSelectedDay;
    private TextView tvMonthTitle;
    private TextView tvEmptyState;
    private RecyclerView recyclerView;
    private RecyclerView rvCalendar;
    private ImageButton btnMonthPrev;
    private ImageButton btnMonthNext;
    private MoodRecordAdapter adapter;
    private MoodCalendarAdapter calendarAdapter;
    private MoodRecordManager recordManager;
    private long selectedDayStart;
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy年M月", Locale.getDefault());
    private final Calendar currentMonth = Calendar.getInstance();
    private Map<Long, List<MoodRecord>> monthRecords = new HashMap<>();

    public static MoodCalendarFragment newInstance() {
        return new MoodCalendarFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mood_calendar;
    }

    @Override
    protected void initView() {
        tvSelectedDay = findViewById(R.id.tv_selected_day);
        tvMonthTitle = findViewById(R.id.tv_month_title);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        recyclerView = findViewById(R.id.rv_mood_records);
        rvCalendar = findViewById(R.id.rv_calendar_days);
        btnMonthPrev = findViewById(R.id.btn_month_prev);
        btnMonthNext = findViewById(R.id.btn_month_next);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        adapter = new MoodRecordAdapter();
        adapter.setActionListener(new MoodRecordAdapter.MoodRecordActionListener() {
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
                        // no-op
                    }

                    @Override
                    public void onError(String errorMessage) {
                        showToast(errorMessage);
                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);

        rvCalendar.setLayoutManager(new GridLayoutManager(getContext(), 7));
        rvCalendar.setNestedScrollingEnabled(false);
        calendarAdapter = new MoodCalendarAdapter();
        calendarAdapter.setOnDayInteractionListener(new MoodCalendarAdapter.OnDayInteractionListener() {
            @Override
            public void onDaySelected(long dayStart) {
                selectedDayStart = dayStart;
                updateSelectedDayTitle();
                calendarAdapter.setSelectedDay(selectedDayStart);
                loadRecordsForSelectedDay();
            }

            @Override
            public void onDayDoubleTap(long dayStart) {
                MoodTrackActivity activity = getMoodTrackActivity();
                if (activity != null) {
                    activity.openCreateRecordEditor(dayStart);
                }
            }
        });
        rvCalendar.setAdapter(calendarAdapter);

        btnMonthPrev.setOnClickListener(v -> shiftMonth(-1));
        btnMonthNext.setOnClickListener(v -> shiftMonth(1));
    }

    @Override
    protected void initData() {
        recordManager = MoodRecordManager.getInstance(requireContext());
        recordManager.registerListener(this);

        selectedDayStart = normalizeDay(System.currentTimeMillis());
        currentMonth.setTimeInMillis(selectedDayStart);
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);
        updateSelectedDayTitle();
        updateMonthTitle();
        calendarAdapter.setSelectedDay(selectedDayStart);
        updateCalendarCells();
        loadMonthRecords();
    }

    @Override
    public void onPageSelected() {
        loadMonthRecords();
        loadRecordsForSelectedDay();
    }

    @Override
    public void onAddRecordRequested() {
        MoodTrackActivity activity = getMoodTrackActivity();
        if (activity != null) {
            activity.openCreateRecordEditor(selectedDayStart);
        }
    }

    @Override
    public void onMoodDataChanged() {
        loadMonthRecords();
        loadRecordsForSelectedDay();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (recordManager != null) {
            recordManager.unregisterListener(this);
        }
    }

    private void loadRecordsForSelectedDay() {
        if (recordManager == null) {
            return;
        }
        recordManager.loadRecordsForDay(selectedDayStart, new MoodRecordManager.LoadCallback<List<MoodRecord>>() {
            @Override
            public void onSuccess(List<MoodRecord> data) {
                adapter.submitList(data);
                tvEmptyState.setVisibility(data == null || data.isEmpty() ? View.VISIBLE : View.GONE);
                if (data != null) {
                    monthRecords.put(selectedDayStart, data);
                    updateCalendarCells();
                }
            }

            @Override
            public void onError(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private void updateSelectedDayTitle() {
        tvSelectedDay.setText(getString(R.string.mood_selected_day, dayFormat.format(selectedDayStart)));
    }

    private void updateMonthTitle() {
        tvMonthTitle.setText(monthFormat.format(currentMonth.getTime()));
    }

    private void shiftMonth(int offset) {
        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.setTimeInMillis(selectedDayStart);
        int desiredDay = selectedCalendar.get(Calendar.DAY_OF_MONTH);

        currentMonth.add(Calendar.MONTH, offset);
        int maxDay = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        int resolvedDay = Math.min(desiredDay, maxDay);

        Calendar newSelected = (Calendar) currentMonth.clone();
        newSelected.set(Calendar.DAY_OF_MONTH, resolvedDay);
        selectedDayStart = normalizeCalendar(newSelected);

        currentMonth.set(Calendar.DAY_OF_MONTH, 1);
        monthRecords.clear();

        updateMonthTitle();
        updateSelectedDayTitle();
        calendarAdapter.setSelectedDay(selectedDayStart);
        updateCalendarCells();
        loadMonthRecords();
        loadRecordsForSelectedDay();
    }

    private void updateCalendarCells() {
        Calendar calendar = (Calendar) currentMonth.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int leadingDays = firstDayOfWeek - Calendar.SUNDAY;
        if (leadingDays < 0) {
            leadingDays += 7;
        }

        List<MoodCalendarAdapter.DayCell> dayCells = new ArrayList<>();
        Calendar cursor = (Calendar) calendar.clone();
        cursor.add(Calendar.DAY_OF_MONTH, -leadingDays);

        int totalCells = 42; // 6行
        for (int i = 0; i < totalCells; i++) {
            MoodCalendarAdapter.DayCell cell = new MoodCalendarAdapter.DayCell();
            cell.dayStart = normalizeCalendar(cursor);
            cell.dayOfMonth = cursor.get(Calendar.DAY_OF_MONTH);
            cell.inCurrentMonth = cursor.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)
                    && cursor.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR);
            cell.isToday = isSameDay(cursor.getTimeInMillis(), System.currentTimeMillis());

            List<MoodRecord> records = monthRecords.get(cell.dayStart);
            if (records != null && !records.isEmpty()) {
                cell.coverImageUri = records.get(0).getImageUri();
            }

            dayCells.add(cell);
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }

        calendarAdapter.submitList(dayCells);
        calendarAdapter.setSelectedDay(selectedDayStart);
    }

    private boolean isSameDay(long time1, long time2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTimeInMillis(time1);
        c2.setTimeInMillis(time2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private void loadMonthRecords() {
        if (recordManager == null) {
            return;
        }
        long start = getMonthStartMillis();
        long end = getMonthEndMillis();
        recordManager.loadRecordsBetween(start, end, new MoodRecordManager.LoadCallback<List<MoodRecord>>() {
            @Override
            public void onSuccess(List<MoodRecord> data) {
                monthRecords.clear();
                if (data != null) {
                    for (MoodRecord record : data) {
                        long day = normalizeDay(record.getRecordDate());
                        List<MoodRecord> list = monthRecords.get(day);
                        if (list == null) {
                            list = new ArrayList<>();
                            monthRecords.put(day, list);
                        }
                        list.add(record);
                    }
                }
                updateCalendarCells();
            }

            @Override
            public void onError(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private long getMonthStartMillis() {
        Calendar calendar = (Calendar) currentMonth.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return normalizeCalendar(calendar);
    }

    private long getMonthEndMillis() {
        Calendar calendar = (Calendar) currentMonth.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        return normalizeCalendar(calendar);
    }

    private long normalizeDay(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        return normalizeCalendar(calendar);
    }

    private long normalizeCalendar(@NonNull Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
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
