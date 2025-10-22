package com.lanqiDoctor.demo.model;

import java.util.Date;
import java.util.List;

/**
 * 每周食谱计划数据模型
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class WeeklyRecipePlan {
    
    /** 计划ID */
    private String id;
    
    /** 计划名称 */
    private String name;
    
    /** 开始日期 */
    private Date startDate;
    
    /** 结束日期 */
    private Date endDate;
    
    /** 每日食谱安排 */
    private List<DailyRecipePlan> dailyPlans;
    
    /** 用户所在地区 */
    private String userRegion;
    
    /** 创建时间 */
    private Date createTime;
    
    /** 是否激活 */
    private boolean isActive;
    
    public static class DailyRecipePlan {
        /** 日期 */
        private Date date;
        
        /** 星期几 */
        private String dayOfWeek;
        
        /** 早餐 */
        private Recipe breakfast;
        
        /** 午餐 */
        private Recipe lunch;
        
        /** 晚餐 */
        private Recipe dinner;
        
        /** 总热量 */
        private double totalCalories;
        
        public DailyRecipePlan() {}
        
        public DailyRecipePlan(Date date, String dayOfWeek) {
            this.date = date;
            this.dayOfWeek = dayOfWeek;
        }
        
        // Getters and Setters
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
        
        public String getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
        
        public Recipe getBreakfast() { return breakfast; }
        public void setBreakfast(Recipe breakfast) { 
            this.breakfast = breakfast;
            calculateTotalCalories();
        }
        
        public Recipe getLunch() { return lunch; }
        public void setLunch(Recipe lunch) { 
            this.lunch = lunch;
            calculateTotalCalories();
        }
        
        public Recipe getDinner() { return dinner; }
        public void setDinner(Recipe dinner) { 
            this.dinner = dinner;
            calculateTotalCalories();
        }
        
        public double getTotalCalories() { return totalCalories; }
        public void setTotalCalories(double totalCalories) { this.totalCalories = totalCalories; }
        
        /**
         * 计算一天的总热量
         */
        private void calculateTotalCalories() {
            totalCalories = 0;
            if (breakfast != null) totalCalories += breakfast.getCalories();
            if (lunch != null) totalCalories += lunch.getCalories();
            if (dinner != null) totalCalories += dinner.getCalories();
        }
        
        /**
         * 获取指定餐次的食谱
         */
        public Recipe getRecipeByMealType(Recipe.MealType mealType) {
            switch (mealType) {
                case BREAKFAST:
                    return breakfast;
                case LUNCH:
                    return lunch;
                case DINNER:
                    return dinner;
                default:
                    return null;
            }
        }
        
        /**
         * 设置指定餐次的食谱
         */
        public void setRecipeByMealType(Recipe.MealType mealType, Recipe recipe) {
            switch (mealType) {
                case BREAKFAST:
                    setBreakfast(recipe);
                    break;
                case LUNCH:
                    setLunch(recipe);
                    break;
                case DINNER:
                    setDinner(recipe);
                    break;
            }
        }
    }
    
    // Constructors
    public WeeklyRecipePlan() {}
    
    public WeeklyRecipePlan(String id, String name, Date startDate, Date endDate) {
        this.id = id;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createTime = new Date();
        this.isActive = true;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    
    public List<DailyRecipePlan> getDailyPlans() { return dailyPlans; }
    public void setDailyPlans(List<DailyRecipePlan> dailyPlans) { this.dailyPlans = dailyPlans; }
    
    public String getUserRegion() { return userRegion; }
    public void setUserRegion(String userRegion) { this.userRegion = userRegion; }
    
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    /**
     * 获取指定日期的每日计划
     */
    public DailyRecipePlan getDailyPlanByDate(Date date) {
        if (dailyPlans != null) {
            for (DailyRecipePlan plan : dailyPlans) {
                if (plan.getDate().equals(date)) {
                    return plan;
                }
            }
        }
        return null;
    }
    
    /**
     * 计算整周的总热量
     */
    public double getTotalWeeklyCalories() {
        double total = 0;
        if (dailyPlans != null) {
            for (DailyRecipePlan plan : dailyPlans) {
                total += plan.getTotalCalories();
            }
        }
        return total;
    }
    
    /**
     * 计算平均每日热量
     */
    public double getAverageDailyCalories() {
        if (dailyPlans == null || dailyPlans.isEmpty()) {
            return 0;
        }
        return getTotalWeeklyCalories() / dailyPlans.size();
    }
}
