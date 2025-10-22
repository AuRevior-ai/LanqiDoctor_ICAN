package com.lanqiDoctor.demo.View;

import java.util.Calendar;
import java.util.Map;



import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.graphics.Typeface;
import android.widget.TextView;
import java.util.List;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.model.Day;
import com.lanqiDoctor.demo.util.CalendarUtil;

public class CalendarView extends LinearLayout {

    private int currentYear;
    private int currentMonth;
    private OnDayClickListener listener;
    private Map<String, Integer> dateStatusMap; // 存储日期状态

    // 状态常量
    public static final int STATUS_NONE = 0;
    public static final int STATUS_COMPLETED = 1; // 全部完成
    public static final int STATUS_MISSED = 2;    // 未完成

    public CalendarView(Context context) {
        super(context);
        init();
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        setPadding(dp2px(8), dp2px(8), dp2px(8), dp2px(8));

        // 初始化当前年月
        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);
    }

    /**
     * 刷新日历显示
     */
    public void refreshCalendar(int year, int month) {
        this.currentYear = year;
        this.currentMonth = month;
        removeAllViews();

        // 1. 添加星期标题行
        addWeekTitleRow();

        // 2. 添加日期行
        List<Day> days = CalendarUtil.getDaysWithFill(year, month);
        for (int i = 0; i < 6; i++) { // 6行
            addDayRow(days.subList(i * 7, (i + 1) * 7));
        }
    }

    /**
     * 刷新日历显示（带状态数据）
     */
    public void refreshCalendar(int year, int month, Map<String, Integer> dateStatusMap) {
        this.dateStatusMap = dateStatusMap;
        refreshCalendar(year, month);
    }

    /**
     * 添加星期标题行
     */
    private void addWeekTitleRow() {
        LinearLayout weekLayout = new LinearLayout(getContext());
        weekLayout.setOrientation(HORIZONTAL);
        weekLayout.setWeightSum(7);

        int weekTextColor = getResources().getColor(R.color.text_secondary);
        int weekTextSize = dp2px(14);

        for (int i = 0; i < 7; i++) {
            TextView weekText = new TextView(getContext());
            weekText.setLayoutParams(new LayoutParams(0, dp2px(36), 1));
            weekText.setGravity(Gravity.CENTER);
            weekText.setText(CalendarUtil.getWeekText(i));
            weekText.setTextColor(weekTextColor);
            weekText.setTextSize(weekTextSize);
            weekText.setTypeface(null, Typeface.BOLD);
            weekLayout.addView(weekText);
        }

        addView(weekLayout);
    }

    /**
     * 添加日期行
     */

    private void addDayRow(List<Day> days) {
        LinearLayout rowLayout = new LinearLayout(getContext());
        rowLayout.setOrientation(HORIZONTAL);
        rowLayout.setWeightSum(7);
        rowLayout.setBackgroundColor(Color.TRANSPARENT);

        for (Day day : days) {
            // 日期卡片容器
            LinearLayout dayContainer = new LinearLayout(getContext());
            dayContainer.setOrientation(VERTICAL);
            dayContainer.setLayoutParams(new LayoutParams(0, dp2px(48), 1));
            dayContainer.setGravity(Gravity.CENTER);
            dayContainer.setPadding(dp2px(2), dp2px(2), dp2px(2), dp2px(2));

            // 卡片背景
            LinearLayout card = new LinearLayout(getContext());
            card.setOrientation(VERTICAL);
            card.setGravity(Gravity.CENTER);
            card.setBackgroundResource(R.drawable.bg_calendar_card); // 需自定义圆角白底卡片
            card.setPadding(0, dp2px(4), 0, dp2px(4));

            // 日期数字
            TextView dayNum = new TextView(getContext());
            dayNum.setText(String.valueOf(day.getDay()));
            dayNum.setGravity(Gravity.CENTER);
            dayNum.setTextSize(16);
            dayNum.setTypeface(null, Typeface.BOLD);

            // 今日高亮
            if (day.isToday()) {
                card.setBackgroundResource(R.drawable.bg_calendar_today_card); // 需自定义高亮卡片
                dayNum.setTextColor(getResources().getColor(R.color.green_500));
            } else if (!day.isCurrentMonth()) {
                dayNum.setTextColor(getResources().getColor(R.color.text_hint));
            } else {
                dayNum.setTextColor(getResources().getColor(R.color.text_primary));
            }

            // 状态图标和文字
            LinearLayout statusLayout = new LinearLayout(getContext());
            statusLayout.setOrientation(HORIZONTAL);
            statusLayout.setGravity(Gravity.CENTER);
            statusLayout.setPadding(0, dp2px(2), 0, 0);

            ImageView statusIcon = new ImageView(getContext());
            TextView statusText = new TextView(getContext());
            statusText.setTextSize(12);
            statusText.setPadding(dp2px(2), 0, 0, 0);

            if (day.isCurrentMonth()) {
                String dateKey = String.format("%d-%d-%d", currentYear, currentMonth, day.getDay());
                Integer status = dateStatusMap != null ? dateStatusMap.get(dateKey) : null;

                if (status != null) {
                    if (status == STATUS_COMPLETED) {
                        statusIcon.setImageResource(R.drawable.ic_check_circle);
                        statusIcon.setColorFilter(getResources().getColor(R.color.green_500));
                        statusText.setText("已服用");
                        statusText.setTextColor(getResources().getColor(R.color.green_500));
                    } else if (status == STATUS_MISSED) {
                        statusIcon.setImageResource(R.drawable.ic_clock);
                        statusIcon.setColorFilter(getResources().getColor(R.color.orange_500));
                        statusText.setText("待服用");
                        statusText.setTextColor(getResources().getColor(R.color.orange_500));
                    } else {
                        statusIcon.setImageDrawable(null);
                        statusText.setText("");
                    }
                    statusLayout.addView(statusIcon, new LayoutParams(dp2px(14), dp2px(14)));
                    statusLayout.addView(statusText);
                }
            }

            // 点击事件
            card.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDayClick(day);
                }
            });

            // 只读效果
            card.setAlpha(0.92f);

            // 组装
            card.addView(dayNum);
            card.addView(statusLayout);
            dayContainer.addView(card);
            rowLayout.addView(dayContainer);
        }

        addView(rowLayout);
    }

    /**
     * dp转px
     */
    private int dp2px(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * 日期点击监听接口
     */
    public interface OnDayClickListener {
        void onDayClick(Day day);
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.listener = listener;
    }
}