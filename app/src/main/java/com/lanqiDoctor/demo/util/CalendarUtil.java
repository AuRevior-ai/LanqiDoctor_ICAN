package com.lanqiDoctor.demo.util;
import com.lanqiDoctor.demo.model.Day;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;

public class CalendarUtil {//日期工具类
    /**
     * 获取某个月的所有日期(包括上个月补位和下个月补位)
     */
    public static List<Day> getDaysWithFill(int year, int month) {//获取所有日期函数,返回值是一个类Day的列表
        List<Day> days = new ArrayList<>();//创建列表

        // 1. 获取当月第一天是星期几
        Calendar calendar = Calendar.getInstance();//当前日期
        calendar.set(year, month, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0=周日,1=周一...

        // 2. 添加上个月末尾几天(补全第一周)
        int prevMonthDays = getMonthDays(year, month - 1);
        for (int i = firstDayOfWeek - 1; i >= 0; i--) {
            days.add(new Day(//这里是月份的构造函数
                    month - 1 < 0 ? year - 1 : year,
                    month - 1 < 0 ? 11 : month - 1,
                    prevMonthDays - i,
                    false, // 不是当前月
                    false // 不是今天
            ));
        }

        // 3. 添加当月所有天数
        int currentMonthDays = getMonthDays(year, month);
        Calendar today = Calendar.getInstance();
        for (int i = 1; i <= currentMonthDays; i++) {
            boolean isToday = (year == today.get(Calendar.YEAR)) &&
                    (month == today.get(Calendar.MONTH)) &&
                    (i == today.get(Calendar.DAY_OF_MONTH));
            days.add(new Day(year, month, i, true, isToday));
        }

        // 4. 添加下个月开头几天(补全最后一周)
        int nextMonthFill = 42 - days.size(); // 6行×7列=42格
        for (int i = 1; i <= nextMonthFill; i++) {
            days.add(new Day(
                    month + 1 > 11 ? year + 1 : year,
                    month + 1 > 11 ? 0 : month + 1,
                    i,
                    false, // 不是当前月
                    false // 不是今天
            ));
        }

        return days;
    }

    /**
     * 获取某个月的天数
     */
    private static int getMonthDays(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取星期文本
     */
    public static String getWeekText(int week) {
        String[] weeks = {"日", "一", "二", "三", "四", "五", "六"};
        return weeks[week];
    }
}