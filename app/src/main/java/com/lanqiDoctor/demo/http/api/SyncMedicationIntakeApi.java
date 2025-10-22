package com.lanqiDoctor.demo.http.api;

import com.hjq.http.config.IRequestApi;
import com.lanqiDoctor.demo.database.entity.MedicationIntakeRecord;

import java.util.List;

/**
 * 每日服药记录同步API
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public final class SyncMedicationIntakeApi implements IRequestApi {

    @Override
    public String getApi() {
        return "medication/intake/sync";
    }

    /** 服药记录列表 */
    private List<MedicationIntakeRecord> intakeRecords;
    /** 最后同步时间 */
    private Long lastSyncTime;
    /** 操作类型：upload-上传, download-下载 */
    private String operationType = "upload";
    /** 同步日期范围（开始） */
    private Long startDate;
    /** 同步日期范围（结束） */
    private Long endDate;
    /** 是否首次同步 */
    private boolean isFirstSync;
    
    public SyncMedicationIntakeApi setIsFirstSync(boolean isFirstSync) {
        this.isFirstSync = isFirstSync;
        return this;
    }

    public boolean isFirstSync() {
        return isFirstSync;
    }

    public SyncMedicationIntakeApi setIntakeRecords(List<MedicationIntakeRecord> intakeRecords) {
        this.intakeRecords = intakeRecords;
        return this;
    }

    public SyncMedicationIntakeApi setLastSyncTime(Long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
        return this;
    }

    public SyncMedicationIntakeApi setOperationType(String operationType) {
        this.operationType = operationType;
        return this;
    }

    public SyncMedicationIntakeApi setStartDate(Long startDate) {
        this.startDate = startDate;
        return this;
    }

    public SyncMedicationIntakeApi setEndDate(Long endDate) {
        this.endDate = endDate;
        return this;
    }

    public List<MedicationIntakeRecord> getIntakeRecords() { return intakeRecords; }
    public Long getLastSyncTime() { return lastSyncTime; }
    public String getOperationType() { return operationType; }
    public Long getStartDate() { return startDate; }
    public Long getEndDate() { return endDate; }

    public final static class Bean {
        private boolean success;
        private String message;
        private List<MedicationIntakeRecord> intakeRecords;
        private Long serverTime;
        private int totalCount;

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<MedicationIntakeRecord> getIntakeRecords() { return intakeRecords; }
        public Long getServerTime() { return serverTime; }
        public int getTotalCount() { return totalCount; }
    }
}