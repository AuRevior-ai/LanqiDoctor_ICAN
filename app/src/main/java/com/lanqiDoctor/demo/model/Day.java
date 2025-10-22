package com.lanqiDoctor.demo.model;

public class Day {//创建日期类
    private int year;
    private int month;
    private int day;
    private boolean isCurrentMonth;
    private boolean isToday;//年月日,当前时间

    // 构造方法
    public Day(int year, int month, int day, boolean isCurrentMonth, boolean isToday) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.isCurrentMonth = isCurrentMonth;
        this.isToday = isToday;
    }//构造函数

    // getter方法
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDay() { return day; }
    public boolean isCurrentMonth() { return isCurrentMonth; }
    public boolean isToday() { return isToday; }
}//得到当前的日期等方法