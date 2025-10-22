package com.lanqiDoctor.demo.ocr;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 药品信息数据类
 * 用于存储从处方单中解析出的药品信息
 */
public class MedicationInfo implements Parcelable {
    
    private String medicationName;      // 药品名称
    private String dosage;              // 剂量
    private String frequency;           // 服药频率
    private String unit;                // 单位(片、ml等)
    private String usage;               // 用法用量描述
    private String notes;               // 备注
    
    public MedicationInfo() {}
    
    public MedicationInfo(String medicationName, String dosage, String frequency, String unit) {
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.unit = unit;
    }
    
    // Parcelable 实现
    protected MedicationInfo(Parcel in) {
        medicationName = in.readString();
        dosage = in.readString();
        frequency = in.readString();
        unit = in.readString();
        usage = in.readString();
        notes = in.readString();
    }
    
    public static final Creator<MedicationInfo> CREATOR = new Creator<MedicationInfo>() {
        @Override
        public MedicationInfo createFromParcel(Parcel in) {
            return new MedicationInfo(in);
        }
        
        @Override
        public MedicationInfo[] newArray(int size) {
            return new MedicationInfo[size];
        }
    };
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(medicationName);
        dest.writeString(dosage);
        dest.writeString(frequency);
        dest.writeString(unit);
        dest.writeString(usage);
        dest.writeString(notes);
    }
    
    // Getter 和 Setter 方法
    public String getMedicationName() {
        return medicationName;
    }
    
    public void setMedicationName(String medicationName) {
        this.medicationName = medicationName;
    }
    
    public String getDosage() {
        return dosage;
    }
    
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
    
    public String getFrequency() {
        return frequency;
    }
    
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public String getUsage() {
        return usage;
    }
    
    public void setUsage(String usage) {
        this.usage = usage;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return "MedicationInfo{" +
                "medicationName='" + medicationName + '\'' +
                ", dosage='" + dosage + '\'' +
                ", frequency='" + frequency + '\'' +
                ", unit='" + unit + '\'' +
                ", usage='" + usage + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
