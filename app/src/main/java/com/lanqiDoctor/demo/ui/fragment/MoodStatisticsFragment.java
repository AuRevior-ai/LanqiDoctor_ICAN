package com.lanqiDoctor.demo.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.entity.MoodRecord;
import com.lanqiDoctor.demo.manager.MoodRecordManager;
import com.lanqiDoctor.demo.model.MoodLevel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 心情统计页面
 */
public class MoodStatisticsFragment extends BaseMoodFragment implements MoodTrackPage, MoodRecordManager.DataChangedListener {

    private static final int MONTH_COUNT = 12;

    private LineChart chartYearTrend;
    private BarChart chartMoodOverview;
    private BarChart chartWeeklyDistribution;
    private TextView tvYearRange;
    private TextView tvEmpty;

    private MoodRecordManager recordManager;

    private final List<MoodRecord> allRecords = new ArrayList<>();
    private final Map<RangeType, TextView> rangeOptionViews = new EnumMap<>(RangeType.class);
    private RangeType currentRange = RangeType.ONE_MONTH;
    private String[] weekLabels;

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy.MM", Locale.getDefault());
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat monthLabelFormat = new SimpleDateFormat("M月", Locale.getDefault());

    private enum RangeType {
        ONE_MONTH,
        SIX_MONTH,
        ONE_YEAR,
        ALL
    }

    public static MoodStatisticsFragment newInstance() {
        return new MoodStatisticsFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mood_statistics;
    }

    @Override
    protected void initView() {
        chartYearTrend = findViewById(R.id.chart_year_trend);
        chartMoodOverview = findViewById(R.id.chart_mood_overview);
        chartWeeklyDistribution = findViewById(R.id.chart_weekly_distribution);
        tvYearRange = findViewById(R.id.tv_year_range);
        tvEmpty = findViewById(R.id.tv_statistics_empty);

        rangeOptionViews.put(RangeType.ONE_MONTH, findViewById(R.id.tv_range_one_month));
        rangeOptionViews.put(RangeType.SIX_MONTH, findViewById(R.id.tv_range_six_month));
        rangeOptionViews.put(RangeType.ONE_YEAR, findViewById(R.id.tv_range_one_year));
        rangeOptionViews.put(RangeType.ALL, findViewById(R.id.tv_range_all));

        weekLabels = new String[]{
                getString(R.string.mood_statistics_week_sun),
                getString(R.string.mood_statistics_week_mon),
                getString(R.string.mood_statistics_week_tue),
                getString(R.string.mood_statistics_week_wed),
                getString(R.string.mood_statistics_week_thu),
                getString(R.string.mood_statistics_week_fri),
                getString(R.string.mood_statistics_week_sat)
        };

        for (Map.Entry<RangeType, TextView> entry : rangeOptionViews.entrySet()) {
            RangeType type = entry.getKey();
            TextView view = entry.getValue();
            view.setOnClickListener(v -> applyRange(type));
        }

        highlightSelectedRange(currentRange);
        initCharts();
    }

    @Override
    protected void initData() {
        recordManager = MoodRecordManager.getInstance(requireContext());
        recordManager.registerListener(this);
        loadStatistics();
    }

    @Override
    public void onPageSelected() {
        loadStatistics();
    }

    @Override
    public void onAddRecordRequested() {
        if (getMoodTrackActivity() != null) {
            getMoodTrackActivity().openCreateRecordEditor(System.currentTimeMillis());
        }
    }

    @Override
    public void onMoodDataChanged() {
        loadStatistics();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (recordManager != null) {
            recordManager.unregisterListener(this);
        }
    }

    private void initCharts() {
        configureLineChart(chartYearTrend);
        configureBarChart(chartMoodOverview);
        configureBarChart(chartWeeklyDistribution);
        chartWeeklyDistribution.getAxisLeft().setGranularity(0.5f);
    }

    private void loadStatistics() {
        if (recordManager == null) {
            return;
        }
        recordManager.loadAllRecords(new MoodRecordManager.LoadCallback<List<MoodRecord>>() {
            @Override
            public void onSuccess(List<MoodRecord> data) {
                bindRecords(data);
            }

            @Override
            public void onError(String errorMessage) {
                showToast(errorMessage);
            }
        });
    }

    private void bindRecords(List<MoodRecord> records) {
        allRecords.clear();
        if (records != null) {
            allRecords.addAll(records);
        }
        boolean hasData = !allRecords.isEmpty();
        tvEmpty.setVisibility(hasData ? View.GONE : View.VISIBLE);
        renderYearTrend();
        applyRange(currentRange);
    }

    private void configureLineChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setScaleEnabled(false);
        chart.setDragEnabled(false);
        chart.setViewPortOffsets(56f, 32f, 40f, 56f);
        chart.setExtraTopOffset(12f);
        chart.setExtraBottomOffset(16f);
        chart.setExtraLeftOffset(16f);
        chart.setExtraRightOffset(16f);
        chart.setNoDataText(getString(R.string.mood_statistics_no_data));
        chart.setDrawGridBackground(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(ContextCompat.getColor(requireContext(), R.color.background_white_alpha));
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        xAxis.setGranularity(1f);

        YAxis left = chart.getAxisLeft();
        left.setAxisMinimum(1f);
        left.setAxisMaximum(5f);
        left.setLabelCount(5, true);
        left.setGranularity(1f);
        left.setDrawGridLines(true);
        left.setGridColor(ContextCompat.getColor(requireContext(), R.color.background_white_alpha));
        left.setAxisLineColor(ContextCompat.getColor(requireContext(), R.color.background_white_alpha));
        left.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));

        chart.getAxisRight().setEnabled(false);
    }

    private void configureBarChart(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setScaleEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setNoDataText(getString(R.string.mood_statistics_no_data));
        chart.setExtraTopOffset(16f);
        chart.setExtraBottomOffset(12f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(ContextCompat.getColor(requireContext(), R.color.background_white_alpha));
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        xAxis.setGranularity(1f);

        YAxis left = chart.getAxisLeft();
        left.setAxisMinimum(0f);
        left.setDrawGridLines(true);
        left.setGridColor(ContextCompat.getColor(requireContext(), R.color.background_white_alpha));
        left.setAxisLineColor(ContextCompat.getColor(requireContext(), R.color.background_white_alpha));
        left.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        left.setGranularity(1f);

        chart.getAxisRight().setEnabled(false);
    }

    private void renderYearTrend() {
        List<Entry> entries = new ArrayList<>(MONTH_COUNT);
        List<String> monthLabels = new ArrayList<>(MONTH_COUNT);
        Map<Integer, MoodLevel> moodHints = new HashMap<>();

        Calendar startCalendar = Calendar.getInstance();
        setToMonthStart(startCalendar);
        startCalendar.add(Calendar.MONTH, -(MONTH_COUNT - 1));

        boolean hasDataPoint = false;
        for (int i = 0; i < MONTH_COUNT; i++) {
            Calendar monthStart = (Calendar) startCalendar.clone();
            monthStart.add(Calendar.MONTH, i);
            Calendar monthEnd = (Calendar) monthStart.clone();
            monthEnd.add(Calendar.MONTH, 1);

            float average = calculateAverageMood(monthStart.getTimeInMillis(), monthEnd.getTimeInMillis());
            monthLabels.add(monthLabelFormat.format(monthStart.getTime()));
            if (!Float.isNaN(average)) {
                entries.add(new Entry(i, average));
                hasDataPoint = true;
                moodHints.put(i, moodFromScore(average));
            }
        }

        Calendar endCalendar = Calendar.getInstance();
        setToMonthStart(endCalendar);
        String rangeLabel = String.format(Locale.getDefault(), "%s - %s",
                yearMonthFormat.format(startCalendar.getTime()),
                yearMonthFormat.format(endCalendar.getTime()));
        tvYearRange.setText(rangeLabel);

        if (!hasDataPoint) {
            chartYearTrend.clear();
            chartYearTrend.invalidate();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, null);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawCircles(true);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.neon_blue));
        dataSet.setCircleHoleColor(ContextCompat.getColor(requireContext(), R.color.background_white));
        dataSet.setLineWidth(2f);
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.neon_blue));
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(requireContext(), R.color.neon_blue));
        dataSet.setFillAlpha(40);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPointLabel(Entry entry) {
                int index = Math.round(entry.getX());
                MoodLevel moodLevel = moodHints.get(index);
                return moodLevel != null ? moodLevel.getEmoji() : "";
            }
        });

        chartYearTrend.getXAxis().setValueFormatter(new IndexAxisValueFormatter(monthLabels));
        chartYearTrend.getXAxis().setLabelCount(monthLabels.size());
        chartYearTrend.setData(new LineData(dataSet));
        chartYearTrend.invalidate();
    }

    private void applyRange(RangeType rangeType) {
        currentRange = rangeType;
        highlightSelectedRange(rangeType);
        List<MoodRecord> filtered = filterRecords(rangeType);
        updateMoodOverviewChart(filtered);
        updateWeeklyChart(filtered);
    }

    private void highlightSelectedRange(RangeType selectedRange) {
        for (Map.Entry<RangeType, TextView> entry : rangeOptionViews.entrySet()) {
            TextView view = entry.getValue();
            boolean selected = entry.getKey() == selectedRange;
            view.setBackgroundResource(selected ? R.drawable.bg_range_option_selected : R.drawable.bg_range_option_normal);
            view.setTextColor(ContextCompat.getColor(requireContext(), selected ? R.color.neon_blue : R.color.text_secondary));
            view.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }
    }

    private List<MoodRecord> filterRecords(RangeType rangeType) {
        if (allRecords.isEmpty()) {
            return new ArrayList<>();
        }
        if (rangeType == RangeType.ALL) {
            return new ArrayList<>(allRecords);
        }
        Calendar calendar = Calendar.getInstance();
        setToDayStart(calendar);
        switch (rangeType) {
            case ONE_MONTH:
                calendar.add(Calendar.MONTH, -1);
                break;
            case SIX_MONTH:
                calendar.add(Calendar.MONTH, -6);
                break;
            case ONE_YEAR:
                calendar.add(Calendar.YEAR, -1);
                break;
            default:
                break;
        }
        long start = calendar.getTimeInMillis();
        long end = System.currentTimeMillis();
        List<MoodRecord> filtered = new ArrayList<>();
        for (MoodRecord record : allRecords) {
            long date = record.getRecordDate();
            if (date >= start && date <= end) {
                filtered.add(record);
            }
        }
        return filtered;
    }

    private void updateMoodOverviewChart(List<MoodRecord> records) {
        EnumMap<MoodLevel, Integer> counts = new EnumMap<>(MoodLevel.class);
        for (MoodLevel level : MoodLevel.values()) {
            counts.put(level, 0);
        }
        int maxCount = 0;
        for (MoodRecord record : records) {
            MoodLevel level = MoodLevel.fromStorage(record.getMoodLevel());
            int newCount = counts.get(level) + 1;
            counts.put(level, newCount);
            maxCount = Math.max(maxCount, newCount);
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> axisLabels = new ArrayList<>();
        int index = 0;
        for (MoodLevel level : MoodLevel.values()) {
            entries.add(new BarEntry(index, counts.get(level)));
            axisLabels.add(level.getEmoji());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, null);
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.neon_blue));
        dataSet.setGradientColor(ContextCompat.getColor(requireContext(), R.color.blue_gradient_start),
                ContextCompat.getColor(requireContext(), R.color.neon_blue));
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%d", (int) value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        chartMoodOverview.setData(barData);
        chartMoodOverview.getXAxis().setValueFormatter(new IndexAxisValueFormatter(axisLabels));
        chartMoodOverview.getXAxis().setLabelCount(axisLabels.size());

        YAxis leftAxis = chartMoodOverview.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(maxCount == 0 ? 4f : maxCount + 1f);
        leftAxis.setGranularity(1f);
        leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        leftAxis.setGridColor(ContextCompat.getColor(requireContext(), R.color.background_white_alpha));
        leftAxis.setAxisLineColor(ContextCompat.getColor(requireContext(), R.color.background_white_alpha));
        chartMoodOverview.invalidate();
    }

    private void updateWeeklyChart(List<MoodRecord> records) {
        float[] totalScores = new float[7];
        int[] counts = new int[7];

        Calendar calendar = Calendar.getInstance();
        for (MoodRecord record : records) {
            calendar.setTimeInMillis(record.getRecordDate());
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
            if (dayOfWeek < 0) {
                dayOfWeek = 0;
            }
            totalScores[dayOfWeek] += getMoodScore(MoodLevel.fromStorage(record.getMoodLevel()));
            counts[dayOfWeek]++;
        }

        List<BarEntry> entries = new ArrayList<>(weekLabels.length);
        for (int i = 0; i < weekLabels.length; i++) {
            boolean hasData = counts[i] > 0;
            float value = hasData ? totalScores[i] / counts[i] : 0f;
            entries.add(new BarEntry(i, value));
        }

        BarDataSet dataSet = new BarDataSet(entries, null);
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.neon_blue));
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value == 0f ? "" : String.format(Locale.getDefault(), "%.1f", value);
            }
        });
        dataSet.setGradientColor(ContextCompat.getColor(requireContext(), R.color.blue_gradient_start),
                ContextCompat.getColor(requireContext(), R.color.neon_blue));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        chartWeeklyDistribution.setData(barData);
        chartWeeklyDistribution.getXAxis().setValueFormatter(new IndexAxisValueFormatter(Arrays.asList(weekLabels)));
        chartWeeklyDistribution.getXAxis().setLabelCount(weekLabels.length);

        YAxis leftAxis = chartWeeklyDistribution.getAxisLeft();
        leftAxis.setAxisMinimum(1f);
        leftAxis.setAxisMaximum(5f);
        leftAxis.setLabelCount(5, true);
        leftAxis.setValueFormatter(createEmojiAxisFormatter());
        leftAxis.setGranularity(1f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(ContextCompat.getColor(requireContext(), R.color.background_white_alpha));
        leftAxis.setAxisLineColor(ContextCompat.getColor(requireContext(), R.color.background_white_alpha));
        leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        chartWeeklyDistribution.getAxisRight().setEnabled(false);
        chartWeeklyDistribution.invalidate();
    }

    private float calculateAverageMood(long startInclusive, long endExclusive) {
        float total = 0f;
        int count = 0;
        for (MoodRecord record : allRecords) {
            long date = record.getRecordDate();
            if (date >= startInclusive && date < endExclusive) {
                total += getMoodScore(MoodLevel.fromStorage(record.getMoodLevel()));
                count++;
            }
        }
        return count == 0 ? Float.NaN : total / count;
    }

    private MoodLevel moodFromScore(float score) {
        if (Float.isNaN(score)) {
            return null;
        }
        int rounded = Math.round(score);
        if (rounded >= 5) {
            return MoodLevel.VERY_HAPPY;
        } else if (rounded == 4) {
            return MoodLevel.HAPPY;
        } else if (rounded == 3) {
            return MoodLevel.NEUTRAL;
        } else if (rounded == 2) {
            return MoodLevel.SAD;
        } else {
            return MoodLevel.VERY_SAD;
        }
    }

    private int getMoodScore(MoodLevel level) {
        switch (level) {
            case VERY_HAPPY:
                return 5;
            case HAPPY:
                return 4;
            case NEUTRAL:
                return 3;
            case SAD:
                return 2;
            case VERY_SAD:
            default:
                return 1;
        }
    }

    private ValueFormatter createEmojiAxisFormatter() {
        return new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                MoodLevel moodLevel = moodFromScore(value);
                return moodLevel != null ? moodLevel.getEmoji() : "";
            }
        };
    }

    private void setToMonthStart(Calendar calendar) {
        setToDayStart(calendar);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
    }

    private void setToDayStart(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
