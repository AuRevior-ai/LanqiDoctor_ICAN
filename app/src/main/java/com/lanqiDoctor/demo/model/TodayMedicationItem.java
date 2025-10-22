package com.lanqiDoctor.demo.model;

import com.lanqiDoctor.demo.database.entity.MedicationIntakeRecord;

/**
 * 今日服药项目数据模型
 * 
 * @author 蓝岐医童开发团队 rrrrrrzy
 * @version 1.0
 */
public class TodayMedicationItem {
    
    // 项目类型常量
    public static final int TYPE_HEADER = 0;      // 时间段标题
    public static final int TYPE_MEDICATION = 1;  // 药物项目
    
    private int itemType = TYPE_MEDICATION;       // 项目类型
    private Long medicationId;                    // 药物ID (保留字段，向前兼容)
    private String medicationName;                // 药物名称 (主要标识符)
    private String dosage;                        // 剂量
    private String unit;                          // 单位
    private Long plannedTime;                     // 计划服药时间
    private String timeString;                    // 时间字符串 (HH:mm)
    private String timeGroup;                     // 时间组 (早上/中午/晚上)
    private MedicationIntakeRecord intakeRecord;  // 关联的服药记录
    private boolean hasTakenToday = false;
    
    // 构造函数
    public TodayMedicationItem() {
    }
    
    /**
     * 基于药物名称的构造函数（推荐使用）
     */
    public TodayMedicationItem(String medicationName, String dosage, String unit, Long plannedTime) {
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.unit = unit;
        this.plannedTime = plannedTime;
    }
    
    /**
     * 向前兼容的构造函数（保留原有接口）
     * @deprecated 建议使用基于药物名称的构造函数
     */
    @Deprecated
    public TodayMedicationItem(Long medicationId, String medicationName, String dosage, String unit, Long plannedTime) {
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.unit = unit;
        this.plannedTime = plannedTime;
    }
    
    // Getter和Setter方法
    public int getItemType() { return itemType; }
    public void setItemType(int itemType) { this.itemType = itemType; }
    
    /**
     * 获取药物ID
     * @deprecated ID字段仅用于向前兼容，新代码建议使用药物名称
     */
    @Deprecated
    public Long getMedicationId() { return medicationId; }
    
    /**
     * 设置药物ID
     * @deprecated ID字段仅用于向前兼容，新代码建议使用药物名称
     */
    @Deprecated
    public void setMedicationId(Long medicationId) { this.medicationId = medicationId; }
    
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public Long getPlannedTime() { return plannedTime; }
    public void setPlannedTime(Long plannedTime) { this.plannedTime = plannedTime; }
    
    public String getTimeString() { return timeString; }
    public void setTimeString(String timeString) { this.timeString = timeString; }
    
    public String getTimeGroup() { return timeGroup; }
    public void setTimeGroup(String timeGroup) { this.timeGroup = timeGroup; }
    
    public MedicationIntakeRecord getIntakeRecord() { return intakeRecord; }
    public void setIntakeRecord(MedicationIntakeRecord intakeRecord) { this.intakeRecord = intakeRecord; }
    
    /**
     * 判断是否已服用
     */
    public boolean isTaken() {
        return intakeRecord != null && intakeRecord.getStatus() != null && intakeRecord.getStatus() == 1;
    }

    public boolean isFriendTaken() {
        return hasTakenToday;
    }

    /**
     * 设置今日是否已服用
     */
    public void setHasTakenToday(boolean hasTakenToday) {
        this.hasTakenToday = hasTakenToday;
    }

    /**
     * 获取完整的剂量信息
     */
    public String getFullDosageInfo() {
        if (dosage != null && unit != null) {
            return dosage + " " + unit;
        } else if (dosage != null) {
            return dosage;
        } else {
            return "未设置剂量";
        }
    }
    
    /**
     * 获取主要标识符（推荐使用药物名称）
     */
    public String getPrimaryIdentifier() {
        return medicationName;
    }
    
    /**
     * 检查是否为标题项目
     */
    public boolean isHeader() {
        return itemType == TYPE_HEADER;
    }
    
    /**
     * 检查是否为药物项目
     */
    public boolean isMedicationItem() {
        return itemType == TYPE_MEDICATION;
    }
    
    /**
     * 获取显示用的药物信息
     */
    public String getDisplayInfo() {
        StringBuilder sb = new StringBuilder();
        if (medicationName != null) {
            sb.append(medicationName);
        }
        
        String dosageInfo = getFullDosageInfo();
        if (!"未设置剂量".equals(dosageInfo)) {
            sb.append(" ").append(dosageInfo);
        }
        
        return sb.toString();
    }
    
    /**
     * 判断两个项目是否为同一个药物和时间
     * 基于药物名称和计划时间进行比较（推荐方式）
     */
    public boolean isSameByNameAndTime(TodayMedicationItem other) {
        if (other == null) return false;
        
        boolean nameMatches = (this.medicationName != null) ? 
            this.medicationName.equals(other.medicationName) : 
            other.medicationName == null;
            
        boolean timeMatches = (this.plannedTime != null) ? 
            this.plannedTime.equals(other.plannedTime) : 
            other.plannedTime == null;
            
        return nameMatches && timeMatches;
    }
    
    /**
     * 向前兼容的比较方法（基于ID）
     * @deprecated 建议使用 isSameByNameAndTime 方法
     */
    @Deprecated
    public boolean isSameById(TodayMedicationItem other) {
        if (other == null) return false;
        
        return (this.medicationId != null) ? 
            this.medicationId.equals(other.medicationId) : 
            other.medicationId == null;
    }
    
    /**
     * 创建标题项目的静态方法
     */
    public static TodayMedicationItem createHeader(String timeGroup) {
        TodayMedicationItem headerItem = new TodayMedicationItem();
        headerItem.setItemType(TYPE_HEADER);
        headerItem.setTimeGroup(timeGroup);
        return headerItem;
    }
    
    /**
     * 复制当前项目（深拷贝）
     */
    public TodayMedicationItem copy() {
        TodayMedicationItem copy = new TodayMedicationItem();
        copy.itemType = this.itemType;
        copy.medicationId = this.medicationId; // 保持向前兼容
        copy.medicationName = this.medicationName;
        copy.dosage = this.dosage;
        copy.unit = this.unit;
        copy.plannedTime = this.plannedTime;
        copy.timeString = this.timeString;
        copy.timeGroup = this.timeGroup;
        copy.intakeRecord = this.intakeRecord; // 浅拷贝引用
        return copy;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        TodayMedicationItem that = (TodayMedicationItem) obj;
        
        // 优先使用药物名称和计划时间进行比较
        if (medicationName != null && that.medicationName != null && 
            plannedTime != null && that.plannedTime != null) {
            return medicationName.equals(that.medicationName) && 
                   plannedTime.equals(that.plannedTime);
        }
        
        // 向前兼容：如果没有名称或时间信息，使用ID比较
        if (medicationId != null && that.medicationId != null) {
            return medicationId.equals(that.medicationId);
        }
        
        return false;
    }
    
    @Override
    public int hashCode() {
        // 优先使用药物名称和计划时间生成哈希码
        if (medicationName != null && plannedTime != null) {
            return medicationName.hashCode() * 31 + plannedTime.hashCode();
        }
        
        // 向前兼容：使用ID生成哈希码
        if (medicationId != null) {
            return medicationId.hashCode();
        }
        
        return super.hashCode();
    }
    
    @Override
    public String toString() {
        return "TodayMedicationItem{" +
                "itemType=" + itemType +
                ", medicationId=" + medicationId + // 保留显示以便调试
                ", medicationName='" + medicationName + '\'' +
                ", dosage='" + dosage + '\'' +
                ", unit='" + unit + '\'' +
                ", timeString='" + timeString + '\'' +
                ", timeGroup='" + timeGroup + '\'' +
                ", plannedTime=" + plannedTime +
                ", isTaken=" + isTaken() +
                '}';
    }
}