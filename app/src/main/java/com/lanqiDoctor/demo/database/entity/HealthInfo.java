package com.lanqiDoctor.demo.database.entity;

/**
 *    author : rrrrrzy
 *    github : https://github.com/rrrrrzy
 *    time   : 2025/6/19
 *    desc   : 健康信息实体类
 */
public class HealthInfo {
    private Long id;
    private Long timestamp; // 时间戳（精确到秒）
    private Integer age; // 年龄
    private Double height; // 身高（cm）
    private Double weight; // 体重（kg）
    private Integer heartRate; // 心率（次/分钟）
    private Double systolicPressure; // 收缩压（mmHg）
    private Double diastolicPressure; // 舒张压（mmHg）
    private Double bloodSugar; // 血糖（mmol/L）
    private String remarks; // 备注
    private Long createTime; // 创建时间
    private Long updateTime; // 更新时间
    
    private Integer steps; // 步数
    private Double sleepDuration; // 睡眠时长（小时）
    public HealthInfo() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }

    public Double getSystolicPressure() { return systolicPressure; }
    public void setSystolicPressure(Double systolicPressure) { this.systolicPressure = systolicPressure; }

    public Double getDiastolicPressure() { return diastolicPressure; }
    public void setDiastolicPressure(Double diastolicPressure) { this.diastolicPressure = diastolicPressure; }

    public Double getBloodSugar() { return bloodSugar; }
    public void setBloodSugar(Double bloodSugar) { this.bloodSugar = bloodSugar; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Long getCreateTime() { return createTime; }
    public void setCreateTime(Long createTime) { this.createTime = createTime; }

    public Long getUpdateTime() { return updateTime; }
    public void setUpdateTime(Long updateTime) { this.updateTime = updateTime; }

    public Integer getSteps() { return steps; }
    public void setSteps(Integer steps) { this.steps = steps; }

    public Double getSleepDuration() { return sleepDuration; }
    public void setSleepDuration(Double sleepDuration) { this.sleepDuration = sleepDuration; }
    /**
     * 计算BMI
     */
    public Double getBmi() {
        if (height != null && weight != null && height > 0) {
            double heightInMeters = height / 100.0;
            return weight / (heightInMeters * heightInMeters);
        }
        return null;
    }

    @Override
    public String toString() {
        return "HealthInfo{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", age=" + age +
                ", height=" + height +
                ", weight=" + weight +
                ", heartRate=" + heartRate +
                ", systolicPressure=" + systolicPressure +
                ", diastolicPressure=" + diastolicPressure +
                ", bloodSugar=" + bloodSugar +
                ", remarks='" + remarks + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}