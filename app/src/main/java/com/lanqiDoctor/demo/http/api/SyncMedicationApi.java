package com.lanqiDoctor.demo.http.api;

import com.hjq.http.config.IRequestApi;
import com.lanqiDoctor.demo.database.entity.MedicationRecord;

import java.util.List;

/**
 * 用药信息同步API
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public final class SyncMedicationApi implements IRequestApi {

    @Override
    public String getApi() {
        return "medication/sync";
    }

    /** 用药记录列表 */
    private List<MedicationRecord> medications;
    /** 最后同步时间 */
    private Long lastSyncTime;
    /** 操作类型：upload-上传, download-下载 */
    private String operationType = "upload";
    /** 是否首次同步 */
    private boolean isFirstSync;

    public SyncMedicationApi setIsFirstSync(boolean isFirstSync) {
        this.isFirstSync = isFirstSync;
        return this;
    }

    public boolean isFirstSync() {
        return isFirstSync;
    }


    public SyncMedicationApi setMedications(List<MedicationRecord> medications) {
        this.medications = medications;
        return this;
    }

    public SyncMedicationApi setLastSyncTime(Long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
        return this;
    }

    public SyncMedicationApi setOperationType(String operationType) {
        this.operationType = operationType;
        return this;
    }

    public List<MedicationRecord> getMedications() {
        return medications;
    }

    public Long getLastSyncTime() {
        return lastSyncTime;
    }

    public String getOperationType() {
        return operationType;
    }

    public final static class Bean {
        private boolean success;
        private String message;
        private List<MedicationRecord> medications;
        private Long serverTime;
        private int totalCount;

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<MedicationRecord> getMedications() { return medications; }
        public Long getServerTime() { return serverTime; }
        public int getTotalCount() { return totalCount; }
    }
}