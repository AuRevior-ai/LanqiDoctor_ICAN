package com.lanqiDoctor.demo.http.api;

/**
 * 聊天消息实体
 */
public class ChatMessage {
    private String role;
    private String content;
    
    public ChatMessage(String role, String content) {
        this.role = role != null ? role : "user";
        this.content = content != null ? content : "";
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    @Override
    public String toString() {
        return "ChatMessage{" +
                "role='" + (role != null ? role : "null") + "'" +
                ", content='" + (content != null ? content.substring(0, Math.min(content.length(), 50)) + "..." : "null") + "'" +
                "}";
    }
}