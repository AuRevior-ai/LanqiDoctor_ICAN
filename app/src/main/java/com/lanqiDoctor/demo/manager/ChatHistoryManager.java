package com.lanqiDoctor.demo.manager;

import android.content.Context;

import com.lanqiDoctor.demo.dao.ChatSessionDao;
import com.lanqiDoctor.demo.database.AppDatabase;
import com.lanqiDoctor.demo.entity.ChatSession;
import com.lanqiDoctor.demo.http.api.ChatMessage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 聊天历史管理器
 */
public class ChatHistoryManager {
    
    private static ChatHistoryManager instance;
    private ChatSessionDao chatSessionDao;
    private ExecutorService executor;
    
    private ChatHistoryManager(Context context) {
        // 初始化数据库实例
        chatSessionDao = AppDatabase.getInstance(context).chatSessionDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    public static synchronized ChatHistoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new ChatHistoryManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * 保存聊天会话
     */
    public void saveChatSession(ChatSession chatSession, SaveCallback callback) {
        executor.execute(() -> {
            try {
                if (chatSession.getTitle() == null || chatSession.getTitle().trim().isEmpty()) {
                    chatSession.generateTitle();
                }
                
                long id = chatSessionDao.insertChatSession(chatSession);
                chatSession.setId(id);
                
                if (callback != null) {
                    callback.onSuccess(chatSession);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 更新聊天会话
     */
    public void updateChatSession(ChatSession chatSession, UpdateCallback callback) {
        executor.execute(() -> {
            try {
                chatSession.setUpdateTime(System.currentTimeMillis());
                if (chatSession.getTitle() == null || chatSession.getTitle().trim().isEmpty()) {
                    chatSession.generateTitle();
                }
                
                chatSessionDao.updateChatSession(chatSession);
                
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取所有聊天会话
     */
    public void getAllChatSessions(String userId, LoadCallback callback) {
        executor.execute(() -> {
            try {
                List<ChatSession> sessions;
                if (userId != null && !userId.trim().isEmpty()) {
                    sessions = chatSessionDao.getChatSessionsByUserId(userId);
                } else {
                    sessions = chatSessionDao.getAllChatSessions();
                }
                
                if (callback != null) {
                    callback.onSuccess(sessions);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 删除聊天会话
     */
    public void deleteChatSession(ChatSession chatSession, DeleteCallback callback) {
        executor.execute(() -> {
            try {
                chatSessionDao.deleteChatSession(chatSession);
                
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 清空所有聊天历史
     */
    public void clearAllHistory(String userId, ClearCallback callback) {
        executor.execute(() -> {
            try {
                if (userId != null && !userId.trim().isEmpty()) {
                    chatSessionDao.deleteAllChatSessionsByUserId(userId);
                } else {
                    chatSessionDao.deleteAllChatSessions();
                }
                
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 从消息列表创建聊天会话
     */
    public ChatSession createChatSessionFromMessages(String userId, List<ChatMessage> messages) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setMessages(messages);
        session.generateTitle();
        return session;
    }
    
    // 回调接口
    public interface SaveCallback {
        void onSuccess(ChatSession chatSession);
        void onError(String error);
    }
    
    public interface UpdateCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface LoadCallback {
        void onSuccess(List<ChatSession> sessions);
        void onError(String error);
    }
    
    public interface DeleteCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface ClearCallback {
        void onSuccess();
        void onError(String error);
    }
}
