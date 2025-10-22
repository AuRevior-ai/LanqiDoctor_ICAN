package com.lanqiDoctor.demo.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LifecycleOwner;

import com.hjq.http.listener.OnHttpListener;
import com.lanqiDoctor.demo.http.api.AiChatApi;
import com.lanqiDoctor.demo.http.api.AiService;
import com.lanqiDoctor.demo.http.api.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI聊天管理器 - 专用于习惯推荐等单次对话场景
 * 简化版本，不涉及流式对话和历史记录
 */
public class AiChatManager {
    
    private Context mContext;
    private Handler mMainHandler;
    private ExecutorService mExecutor;
    private LifecycleOwner mLifecycleOwner;
    
    public AiChatManager(Context context) {
        this.mContext = context;
        this.mMainHandler = new Handler(Looper.getMainLooper());
        this.mExecutor = Executors.newSingleThreadExecutor();
    }
    
    public AiChatManager(Context context, LifecycleOwner lifecycleOwner) {
        this(context);
        this.mLifecycleOwner = lifecycleOwner;
    }
    
    /**
     * 发送单次AI请求
     * @param prompt 用户提示词
     * @param callback 回调接口
     */
    public void sendSingleRequest(String prompt, AiResponseCallback callback) {
        if (prompt == null || prompt.trim().isEmpty()) {
            if (callback != null) {
                mMainHandler.post(() -> callback.onError("提示词不能为空"));
            }
            return;
        }
        
        mExecutor.execute(() -> {
            try {
                // 创建消息列表
                List<ChatMessage> messages = new ArrayList<>();
                messages.add(new ChatMessage("user", prompt.trim()));
                
                // 发送请求
                OnHttpListener<AiChatApi.Bean> listener = new OnHttpListener<AiChatApi.Bean>() {
                    @Override
                    public void onStart(okhttp3.Call call) {
                        mMainHandler.post(() -> {
                            if (callback != null) {
                                callback.onStart();
                            }
                        });
                    }

                    @Override
                    public void onSucceed(AiChatApi.Bean result) {
                        mMainHandler.post(() -> {
                            if (callback != null) {
                                String response = extractResponseContent(result);
                                if (response != null && !response.trim().isEmpty()) {
                                    callback.onSuccess(response.trim());
                                } else {
                                    callback.onError("AI返回内容为空");
                                }
                            }
                        });
                    }

                    @Override
                    public void onFail(Exception e) {
                        mMainHandler.post(() -> {
                            if (callback != null) {
                                String errorMsg = e != null ? e.getMessage() : "请求失败";
                                callback.onError("AI请求失败: " + errorMsg);
                            }
                        });
                    }

                    @Override
                    public void onEnd(okhttp3.Call call) {
                        mMainHandler.post(() -> {
                            if (callback != null) {
                                callback.onComplete();
                            }
                        });
                    }
                };
                
                // 根据是否有生命周期管理选择不同的发送方法
                if (mLifecycleOwner != null) {
                    AiService.sendChatRequest(messages, listener, mLifecycleOwner);
                } else {
                    AiService.sendChatRequest(messages, listener);
                }
                
            } catch (Exception e) {
                mMainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError("发送请求异常: " + e.getMessage());
                        callback.onComplete();
                    }
                });
            }
        });
    }
    
    /**
     * 发送带系统提示词的AI请求
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @param callback 回调接口
     */
    public void sendRequestWithSystemPrompt(String systemPrompt, String userPrompt, AiResponseCallback callback) {
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            if (callback != null) {
                mMainHandler.post(() -> callback.onError("用户提示词不能为空"));
            }
            return;
        }
        
        mExecutor.execute(() -> {
            try {
                // 创建消息列表
                List<ChatMessage> messages = new ArrayList<>();
                
                // 添加系统提示词（如果有）
                if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                    messages.add(new ChatMessage("system", systemPrompt.trim()));
                }
                
                // 添加用户消息
                messages.add(new ChatMessage("user", userPrompt.trim()));
                
                // 发送请求
                OnHttpListener<AiChatApi.Bean> listener = new OnHttpListener<AiChatApi.Bean>() {
                    @Override
                    public void onStart(okhttp3.Call call) {
                        mMainHandler.post(() -> {
                            if (callback != null) {
                                callback.onStart();
                            }
                        });
                    }

                    @Override
                    public void onSucceed(AiChatApi.Bean result) {
                        mMainHandler.post(() -> {
                            if (callback != null) {
                                String response = extractResponseContent(result);
                                if (response != null && !response.trim().isEmpty()) {
                                    callback.onSuccess(response.trim());
                                } else {
                                    callback.onError("AI返回内容为空");
                                }
                            }
                        });
                    }

                    @Override
                    public void onFail(Exception e) {
                        mMainHandler.post(() -> {
                            if (callback != null) {
                                String errorMsg = e != null ? e.getMessage() : "请求失败";
                                callback.onError("AI请求失败: " + errorMsg);
                            }
                        });
                    }

                    @Override
                    public void onEnd(okhttp3.Call call) {
                        mMainHandler.post(() -> {
                            if (callback != null) {
                                callback.onComplete();
                            }
                        });
                    }
                };
                
                // 根据是否有生命周期管理选择不同的发送方法
                if (mLifecycleOwner != null) {
                    AiService.sendChatRequest(messages, listener, mLifecycleOwner);
                } else {
                    AiService.sendChatRequest(messages, listener);
                }
                
            } catch (Exception e) {
                mMainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError("发送请求异常: " + e.getMessage());
                        callback.onComplete();
                    }
                });
            }
        });
    }
    
    /**
     * 从AI响应中提取内容
     */
    private String extractResponseContent(AiChatApi.Bean result) {
        if (result == null) {
            return null;
        }
        
        // 1. 优先尝试获取 data.content 字段
        if (result.getData() != null) {
            String content = result.getData().getContent();
            if (content != null && !content.trim().isEmpty()) {
                return content;
            }
        }
        
        // 2. 尝试从 choices 中获取内容
        if (result.getChoices() != null && !result.getChoices().isEmpty()) {
            for (AiChatApi.Bean.Choice choice : result.getChoices()) {
                if (choice.getMessage() != null) {
                    String content = choice.getMessage().getContent();
                    if (content != null && !content.trim().isEmpty()) {
                        return content;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 释放资源
     */
    public void release() {
        if (mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdown();
        }
    }
    
    /**
     * AI响应回调接口
     */
    public interface AiResponseCallback {
        /**
         * 请求开始
         */
        void onStart();
        
        /**
         * 请求成功
         * @param response AI响应内容
         */
        void onSuccess(String response);
        
        /**
         * 请求失败
         * @param error 错误信息
         */
        void onError(String error);
        
        /**
         * 请求完成（无论成功或失败都会调用）
         */
        void onComplete();
    }
}
