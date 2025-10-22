package com.lanqiDoctor.demo.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.lanqiDoctor.demo.dao.converter.ChatMessageListConverter;
import com.lanqiDoctor.demo.http.api.ChatMessage;

import java.util.List;

/**
 * 聊天会话实体
 */
@Entity(tableName = "chat_session")
@TypeConverters(ChatMessageListConverter.class)
public class ChatSession {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String title; // 会话标题，取首条用户消息的前20个字符
    private long createTime; // 创建时间
    private long updateTime; // 最后更新时间
    private List<ChatMessage> messages; // 聊天消息列表
    private String userId; // 用户ID
    
    public ChatSession() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
    }
    
    @Ignore
    public ChatSession(String title, String userId, List<ChatMessage> messages) {
        this();
        this.title = title;
        this.userId = userId;
        this.messages = messages;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    
    public long getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
    
    public List<ChatMessage> getMessages() {
        return messages;
    }
    
    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        this.updateTime = System.currentTimeMillis();
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    /**
     * 自动生成标题，取第一条用户消息的前20个字符
     */
    public void generateTitle() {
        if (messages != null && !messages.isEmpty()) {
            for (ChatMessage message : messages) {
                if ("user".equals(message.getRole()) && message.getContent() != null) {
                    String content = message.getContent().trim();
                    if (content.length() > 20) {
                        this.title = content.substring(0, 20) + "...";
                    } else {
                        this.title = content;
                    }
                    return;
                }
            }
        }
        this.title = "新对话";
    }
}
