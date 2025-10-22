package com.lanqiDoctor.demo.database.entity;

/**
 * 服药记录实体类
 * 记录用户实际的服药情况
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class MedicationIntakeRecord {
    private String userId;
    private Long id;
    private Long medicationId;          // 关联的用药记录ID
    private String medicationName;      // 药品名称(冗余字段，方便查询)
    private Long plannedTime;           // 计划服药时间
    private Long actualTime;            // 实际服药时间
    private String actualDosage;        // 实际服药剂量
    private Integer status;             // 服药状态: 0-未服用, 1-已服用, 2-延迟服用, 3-跳过
    private String notes;               // 备注
    private Long createTime;            // 创建时间
    private Long updateTime;            // 更新时间
    
    // 构造函数
    public MedicationIntakeRecord() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
        this.status = 0; // 默认未服用
    }
    
    public MedicationIntakeRecord(Long medicationId, String medicationName, Long plannedTime) {
        this();
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.plannedTime = plannedTime;
    }
    
    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getMedicationId() { return medicationId; }
    public void setMedicationId(Long medicationId) { this.medicationId = medicationId; }
    
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    
    public Long getPlannedTime() { return plannedTime; }
    public void setPlannedTime(Long plannedTime) { this.plannedTime = plannedTime; }
    
    public Long getActualTime() { return actualTime; }
    public void setActualTime(Long actualTime) { 
        this.actualTime = actualTime;
        this.updateTime = System.currentTimeMillis();
    }
    
    public String getActualDosage() { return actualDosage; }
    public void setActualDosage(String actualDosage) { this.actualDosage = actualDosage; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { 
        this.status = status;
        this.updateTime = System.currentTimeMillis();
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) { this.createTime = createTime; }
    
    public Long getUpdateTime() { return updateTime; }
    public void setUpdateTime(Long updateTime) { this.updateTime = updateTime; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    /**
     * 判断是否按时服药
     */
    public boolean isOnTime() {
        if (actualTime == null || plannedTime == null) return false;
        long diff = Math.abs(actualTime - plannedTime);
        return diff <= 30 * 60 * 1000; // 30分钟内算按时
    }
    
    /**
     * 获取服药状态描述
     */
    public String getStatusDescription() {
        switch (status) {
            case 0: return "未服用";
            case 1: return "已服用";
            case 2: return "延迟服用";
            case 3: return "跳过";
            default: return "未知";
        }
    }
    
    @Override
    public String toString() {
        return "MedicationIntakeRecord{" +
                "id=" + id +
                ", medicationName='" + medicationName + '\'' +
                ", plannedTime=" + plannedTime +
                ", actualTime=" + actualTime +
                ", status=" + status +
                '}';
    }
}