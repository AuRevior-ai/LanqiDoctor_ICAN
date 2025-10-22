package com.lanqiDoctor.demo.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lanqiDoctor.demo.entity.ChatSession;

import java.util.List;

/**
 * 聊天会话数据访问对象
 */
@Dao
public interface ChatSessionDao {
    
    /**
     * 插入聊天会话
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertChatSession(ChatSession chatSession);
    
    /**
     * 更新聊天会话
     */
    @Update
    void updateChatSession(ChatSession chatSession);
    
    /**
     * 删除聊天会话
     */
    @Delete
    void deleteChatSession(ChatSession chatSession);
    
    /**
     * 根据ID查询聊天会话
     */
    @Query("SELECT * FROM chat_session WHERE id = :id")
    ChatSession getChatSessionById(long id);
    
    /**
     * 查询用户的所有聊天会话，按更新时间倒序
     */
    @Query("SELECT * FROM chat_session WHERE userId = :userId ORDER BY updateTime DESC")
    List<ChatSession> getChatSessionsByUserId(String userId);
    
    /**
     * 查询所有聊天会话，按更新时间倒序（如果不区分用户）
     */
    @Query("SELECT * FROM chat_session ORDER BY updateTime DESC")
    List<ChatSession> getAllChatSessions();
    
    /**
     * 删除指定用户的所有聊天会话
     */
    @Query("DELETE FROM chat_session WHERE userId = :userId")
    void deleteAllChatSessionsByUserId(String userId);
    
    /**
     * 删除所有聊天会话
     */
    @Query("DELETE FROM chat_session")
    void deleteAllChatSessions();
    
    /**
     * 根据ID删除聊天会话
     */
    @Query("DELETE FROM chat_session WHERE id = :id")
    void deleteChatSessionById(long id);
}
