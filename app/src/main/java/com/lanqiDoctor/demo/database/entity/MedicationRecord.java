package com.lanqiDoctor.demo.database.entity;

/**
 * 用药记录实体类
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class MedicationRecord {
    private String userId; // 新增字段
    private Long id;
    private String medicationName;      // 药品名称
    private String dosage;              // 剂量
    private String frequency;           // 服药频率
    private String unit;                // 单位(片、ml等)
    private Long startDate;             // 开始服药时间
    private Long endDate;               // 结束服药时间
    private String reminderTimes;       // 提醒时间(JSON格式存储多个时间)
    private String notes;               // 备注
    private Integer status;             // 状态: 0-停用, 1-正在服用, 2-已完成
    private Long createTime;            // 创建时间
    private Long updateTime;            // 更新时间
    
    // 构造函数
    public MedicationRecord() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
        this.status = 1; // 默认正在服用
    }
    
    public MedicationRecord(String medicationName, String dosage, String frequency, String unit) {
        this();
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.unit = unit;
    }
    
    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public Long getStartDate() { return startDate; }
    public void setStartDate(Long startDate) { this.startDate = startDate; }
    
    public Long getEndDate() { return endDate; }
    public void setEndDate(Long endDate) { this.endDate = endDate; }
    
    public String getReminderTimes() { return reminderTimes; }
    public void setReminderTimes(String reminderTimes) { this.reminderTimes = reminderTimes; }
    
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
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    @Override
    public String toString() {
        return "MedicationRecord{" +
                "id=" + id +
                ", medicationName='" + medicationName + '\'' +
                ", dosage='" + dosage + '\'' +
                ", frequency='" + frequency + '\'' +
                ", unit='" + unit + '\'' +
                ", status=" + status +
                '}';
    }
}