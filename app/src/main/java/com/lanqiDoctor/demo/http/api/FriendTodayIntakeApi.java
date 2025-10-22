package com.lanqiDoctor.demo.http.api;

import com.hjq.http.config.IRequestApi;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 获取亲友今日服药记录API
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public final class FriendTodayIntakeApi implements IRequestApi {

    private String friendUserId;

    public FriendTodayIntakeApi(String friendUserId) {
        this.friendUserId = friendUserId;
    }

    @Override
    public String getApi() {
        return "friends/" + friendUserId + "/today-intake";
    }

    public String getFriendUserId() {
        return friendUserId; 
    }

    public final static class Bean {
        // 移除success字段，因为服务端没有返回这个字段
        // private boolean success;
        private String message;
        
        // 使用@SerializedName注解匹配服务端字段名
        @SerializedName("todayIntakeRecords")
        private List<IntakeRecord> intakeRecords;
        
        private int totalCount;
        private int takenCount;
        private int missedCount;
        private String queryDate;
        private FriendInfo friendInfo;
        private TimeRange timeRange;

        // Getters
        public boolean isSuccess() { 
            // 根据是否有数据来判断成功
            return intakeRecords != null || totalCount >= 0;
        }
        
        public String getMessage() { 
            return message; 
        }
        
        public List<IntakeRecord> getIntakeRecords() { 
            return intakeRecords; 
        }
        
        public int getTotalCount() { 
            return totalCount; 
        }
        
        public int getTakenCount() { 
            return takenCount; 
        }
        
        public int getMissedCount() { 
            return missedCount; 
        }
        
        public String getQueryDate() { 
            return queryDate; 
        }
        
        public FriendInfo getFriendInfo() { 
            return friendInfo; 
        }
        
        public TimeRange getTimeRange() {
            return timeRange;
        }

        // 内部类：服药记录
        public static class IntakeRecord {
            private Long id;
            private Long medicationId;
            private String medicationName;
            private String dosage;
            private String unit;
            private Long plannedTime;
            private Long actualTime;
            private Integer status;
            private String actualDosage;
            private String notes;
            private String timeGroup;
            private Long createTime;
            private Long updateTime;

            // Getters
            public Long getId() { return id; }
            public Long getMedicationId() { return medicationId; }
            public String getMedicationName() { return medicationName; }
            public String getDosage() { return dosage; }
            public String getUnit() { return unit; }
            public Long getPlannedTime() { return plannedTime; }
            public Long getActualTime() { return actualTime; }
            public Integer getStatus() { return status; }
            public String getActualDosage() { return actualDosage; }
            public String getNotes() { return notes; }
            public String getTimeGroup() { return timeGroup; }
            public Long getCreateTime() { return createTime; }
            public Long getUpdateTime() { return updateTime; }
            
            /**
             * 判断是否已服用
             */
            public boolean isTaken() {
                return status != null && status == 1;
            }
            
            /**
             * 获取完整的剂量信息
             */
            public String getFullDosageInfo() {
                if (actualDosage != null && !actualDosage.trim().isEmpty()) {
                    return actualDosage + (unit != null ? " " + unit : "");
                } else if (dosage != null && !dosage.trim().isEmpty()) {
                    return dosage + (unit != null ? " " + unit : "");
                } else {
                    return "未设置剂量";
                }
            }
        }

        // 内部类：亲友信息
        public static class FriendInfo {
            private String userId;
            private String nickname;
            private String originalNickname;
            private String email;
            private String avatarUrl;

            public String getUserId() { return userId; }
            public String getNickname() { return nickname; }
            public String getOriginalNickname() { return originalNickname; }
            public String getEmail() { return email; }
            public String getAvatarUrl() { return avatarUrl; }
        }
        
        // 内部类：时间范围
        public static class TimeRange {
            private Long start;
            private Long end;
            
            public Long getStart() { return start; }
            public Long getEnd() { return end; }
        }
    }
}