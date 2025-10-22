package com.lanqiDoctor.demo.model;

import java.io.Serializable;

/**
 * 家庭成员数据模型
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class FamilyMember implements Serializable {
    
    private String userId;
    private String email;
    private String nickname;
    private String relationshipId;
    private String addedTime;
    private boolean isOnline;
    private String avatar;
    
    public FamilyMember() {}
    
    public FamilyMember(String userId, String email, String nickname) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId; 
    }
    
    public void setUserId(String userId) { 
        this.userId = userId; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public String getNickname() { 
        return nickname; 
    }
    
    public void setNickname(String nickname) { 
        this.nickname = nickname; 
    }
    
    public String getRelationshipId() { 
        return relationshipId; 
    }
    
    public void setRelationshipId(String relationshipId) { 
        this.relationshipId = relationshipId; 
    }
    
    public String getAddedTime() { 
        return addedTime; 
    }
    
    public void setAddedTime(String addedTime) { 
        this.addedTime = addedTime; 
    }
    
    public boolean isOnline() { 
        return isOnline; 
    }
    
    public void setOnline(boolean online) { 
        isOnline = online; 
    }
    
    public String getAvatar() { 
        return avatar; 
    }
    
    public void setAvatar(String avatar) { 
        this.avatar = avatar; 
    }
    
    /**
     * 获取显示名称（优先显示昵称，否则显示邮箱）
     */
    public String getDisplayName() {
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        return email != null ? email : "未知用户";
    }
    
    /**
     * 获取用户ID字符串
     */
    public String getUserIdString() {
        return userId != null ? String.valueOf(userId) : "";
    }
    
    @Override
    public String toString() {
        return "FamilyMember{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", relationshipId='" + relationshipId + '\'' +
                ", addedTime='" + addedTime + '\'' +
                ", isOnline=" + isOnline +
                ", avatar='" + avatar + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FamilyMember that = (FamilyMember) obj;
        return userId != null ? userId.equals(that.userId) : that.userId == null;
    }
    
    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}