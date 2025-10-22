package com.lanqiDoctor.demo.database.entity;

/**
 * 习惯实体类
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class Habit {
    private Long id;                    // 习惯ID
    private String userId;              // 用户ID
    private String habitName;           // 习惯名称
    private String description;         // 习惯描述
    private String frequency;           // 频次类型：DAILY(每日), HOURLY(每小时), WEEKLY(每周), CUSTOM(自定义)
    private Integer frequencyValue;     // 频次值（如每2小时一次，则为2）
    private String frequencyUnit;       // 频次单位：HOUR(小时), DAY(天), WEEK(周)
    private Integer duration;           // 持续时长（分钟）
    private Integer cycleDays;          // 习惯周期（天数），如21天、30天等
    private String reminderTimes;       // 提醒时间（JSON格式存储多个时间点）
    private String blockTimes;          // 屏蔽时间段（JSON格式，如午睡、晚睡时间）
    private Boolean isActive;           // 是否启用
    private Boolean enableNotification; // 是否启用通知提醒
    private Boolean enableSystemAlarm;  // 是否启用系统闹钟
    private Integer completedDays;      // 已坚持天数
    private Integer totalCheckIns;      // 总打卡次数
    private String startDate;           // 开始日期
    private String endDate;             // 结束日期（根据周期计算）
    private String category;            // 习惯分类：HEALTH(健康), EXERCISE(运动), DIET(饮食), STUDY(学习), OTHER(其他)
    private Integer priority;           // 优先级：1-5
    private String notes;               // 备注
    private Integer status;             // 状态：0-删除, 1-正常, 2-已完成, 3-已暂停
    private Long createTime;            // 创建时间
    private Long updateTime;            // 更新时间

    public Habit() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
        this.status = 1; // 默认正常状态
        this.isActive = true; // 默认启用
        this.enableNotification = true; // 默认启用通知
        this.enableSystemAlarm = false; // 默认不启用系统闹钟
        this.completedDays = 0; // 默认0天
        this.totalCheckIns = 0; // 默认0次打卡
        this.priority = 3; // 默认中等优先级
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getHabitName() { return habitName; }
    public void setHabitName(String habitName) { this.habitName = habitName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public Integer getFrequencyValue() { return frequencyValue; }
    public void setFrequencyValue(Integer frequencyValue) { this.frequencyValue = frequencyValue; }

    public String getFrequencyUnit() { return frequencyUnit; }
    public void setFrequencyUnit(String frequencyUnit) { this.frequencyUnit = frequencyUnit; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public Integer getCycleDays() { return cycleDays; }
    public void setCycleDays(Integer cycleDays) { this.cycleDays = cycleDays; }

    public String getReminderTimes() { return reminderTimes; }
    public void setReminderTimes(String reminderTimes) { this.reminderTimes = reminderTimes; }

    public String getBlockTimes() { return blockTimes; }
    public void setBlockTimes(String blockTimes) { this.blockTimes = blockTimes; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive; 
        this.updateTime = System.currentTimeMillis();
    }

    public Boolean getEnableNotification() { return enableNotification; }
    public void setEnableNotification(Boolean enableNotification) { this.enableNotification = enableNotification; }

    public Boolean getEnableSystemAlarm() { return enableSystemAlarm; }
    public void setEnableSystemAlarm(Boolean enableSystemAlarm) { this.enableSystemAlarm = enableSystemAlarm; }

    public Integer getCompletedDays() { return completedDays; }
    public void setCompletedDays(Integer completedDays) { this.completedDays = completedDays; }

    public Integer getTotalCheckIns() { return totalCheckIns; }
    public void setTotalCheckIns(Integer totalCheckIns) { this.totalCheckIns = totalCheckIns; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { 
        this.status = status; 
        this.updateTime = System.currentTimeMillis();
    }

    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) { this.createTime = createTime; }

    public Long getUpdateTime() { return updateTime; }
    public void setUpdateTime(Long updateTime) { this.updateTime = updateTime; }

    /**
     * 获取频次描述
     */
    public String getFrequencyDescription() {
        if (frequency == null) return "未设置";
        
        switch (frequency) {
            case "DAILY":
                return "每日一次";
            case "HOURLY":
                return "每" + (frequencyValue != null ? frequencyValue : 1) + "小时一次";
            case "WEEKLY":
                return "每周" + (frequencyValue != null ? frequencyValue : 1) + "次";
            case "CUSTOM":
                return "自定义频次";
            default:
                return "未知频次";
        }
    }

    /**
     * 获取持续时长描述
     */
    public String getDurationDescription() {
        if (duration == null || duration <= 0) return "未设置时长";
        
        if (duration < 60) {
            return duration + "分钟";
        } else {
            int hours = duration / 60;
            int minutes = duration % 60;
            if (minutes == 0) {
                return hours + "小时";
            } else {
                return hours + "小时" + minutes + "分钟";
            }
        }
    }

    /**
     * 获取周期描述
     */
    public String getCycleDescription() {
        if (cycleDays == null || cycleDays <= 0) return "未设置周期";
        
        if (cycleDays == 21) {
            return "21天(习惯养成)";
        } else if (cycleDays == 30) {
            return "30天(一个月)";
        } else if (cycleDays == 66) {
            return "66天(深度习惯)";
        } else if (cycleDays == 90) {
            return "90天(季度挑战)";
        } else {
            return cycleDays + "天";
        }
    }

    /**
     * 获取分类描述
     */
    public String getCategoryDescription() {
        if (category == null) return "其他";
        
        switch (category) {
            case "HEALTH":
                return "健康";
            case "EXERCISE":
                return "运动";
            case "DIET":
                return "饮食";
            case "STUDY":
                return "学习";
            case "OTHER":
            default:
                return "其他";
        }
    }

    /**
     * 获取优先级描述
     */
    public String getPriorityDescription() {
        if (priority == null) return "中等";
        
        switch (priority) {
            case 1:
                return "很低";
            case 2:
                return "低";
            case 3:
                return "中等";
            case 4:
                return "高";
            case 5:
                return "很高";
            default:
                return "中等";
        }
    }

    @Override
    public String toString() {
        return "Habit{" +
                "id=" + id +
                ", habitName='" + habitName + '\'' +
                ", frequency='" + frequency + '\'' +
                ", duration=" + duration +
                ", cycleDays=" + cycleDays +
                ", isActive=" + isActive +
                ", completedDays=" + completedDays +
                ", category='" + category + '\'' +
                '}';
    }
}
