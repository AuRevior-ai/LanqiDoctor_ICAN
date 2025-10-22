package com.lanqiDoctor.demo.database.entity;

/**
 * 既往病史记录实体类
 * 
 * @author 蓝旗医生开发团队
 * @version 1.0
 */
public class MedicalHistory {
    private String userId; // 用户ID
    private Long id;
    private String diseaseName;         // 疾病名称
    private String diagnosisDate;       // 诊断时间
    private String severity;            // 严重程度(轻度、中度、重度)
    private String treatmentStatus;     // 治疗状况(治疗中、已治愈、慢性病等)
    private String hospital;            // 就诊医院
    private String doctor;              // 主治医生
    private String symptoms;            // 主要症状
    private String treatment;           // 治疗方案
    private String notes;               // 备注信息
    private Integer status;             // 状态: 0-删除, 1-正常
    private Long createTime;            // 创建时间
    private Long updateTime;            // 更新时间
    
    // 构造函数
    public MedicalHistory() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
        this.status = 1; // 默认正常状态
    }
    
    public MedicalHistory(String diseaseName, String diagnosisDate, String severity, String treatmentStatus) {
        this();
        this.diseaseName = diseaseName;
        this.diagnosisDate = diagnosisDate;
        this.severity = severity;
        this.treatmentStatus = treatmentStatus;
    }
    
    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getDiseaseName() { return diseaseName; }
    public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }
    
    public String getDiagnosisDate() { return diagnosisDate; }
    public void setDiagnosisDate(String diagnosisDate) { this.diagnosisDate = diagnosisDate; }
    
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    
    public String getTreatmentStatus() { return treatmentStatus; }
    public void setTreatmentStatus(String treatmentStatus) { this.treatmentStatus = treatmentStatus; }
    
    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }
    
    public String getDoctor() { return doctor; }
    public void setDoctor(String doctor) { this.doctor = doctor; }
    
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    
    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }
    
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
    
    @Override
    public String toString() {
        return "MedicalHistory{" +
                "id=" + id +
                ", diseaseName='" + diseaseName + '\'' +
                ", diagnosisDate='" + diagnosisDate + '\'' +
                ", severity='" + severity + '\'' +
                ", treatmentStatus='" + treatmentStatus + '\'' +
                ", hospital='" + hospital + '\'' +
                ", doctor='" + doctor + '\'' +
                '}';
    }
}
