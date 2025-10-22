package com.lanqiDoctor.demo.http.api;

import com.hjq.http.config.IRequestApi;

/**
 * 用户设置API
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public final class UserSettingsApi implements IRequestApi {

    @Override
    public String getApi() {
        return "user/settings";
    }

    /** 自动同步健康数据设置 */
    private Boolean autoSyncHealthData;

    public UserSettingsApi setAutoSyncHealthData(Boolean autoSyncHealthData) {
        this.autoSyncHealthData = autoSyncHealthData;
        return this;
    }

    public Boolean getAutoSyncHealthData() {
        return autoSyncHealthData;
    }

    public final static class Bean {
        private boolean autoSyncHealthData;
        private boolean medicationReminderEnabled;
        private boolean dataSharingEnabled;

        public boolean isAutoSyncHealthData() { return autoSyncHealthData; }
        public boolean isMedicationReminderEnabled() { return medicationReminderEnabled; }
        public boolean isDataSharingEnabled() { return dataSharingEnabled; }
    }
}