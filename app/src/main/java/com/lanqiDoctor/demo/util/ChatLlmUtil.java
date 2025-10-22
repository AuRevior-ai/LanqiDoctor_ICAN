package com.lanqiDoctor.demo.util;
import java.util.ArrayList;
import android.os.Handler;
import android.text.TextUtils;

import androidx.lifecycle.LifecycleOwner;

import com.google.gson.Gson;
import com.lanqiDoctor.demo.http.api.AiChatApi;
import com.lanqiDoctor.demo.http.api.AiService;
import com.lanqiDoctor.demo.http.api.ChatMessage;
import com.hjq.http.listener.OnHttpListener;
import com.hjq.toast.ToastUtils;
import com.tencent.mm.opensdk.utils.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;

/**
 * LLM对话工具类，负责与大模型的对话交互
 */
public class ChatLlmUtil {

    private final ExecutorService mExecutor;
    private final Handler mMainHandler;
    private final LifecycleOwner mLifecycleOwner;

    public interface LlmCallback {
        void onAssistantMessage(String content);
        void onError(String errorMsg);
        void onComplete();
        void onStreamUpdate(String content);
    }

    public ChatLlmUtil(Handler mainHandler, LifecycleOwner lifecycleOwner) {
        this.mExecutor = Executors.newSingleThreadExecutor();
        this.mMainHandler = mainHandler;
        this.mLifecycleOwner = lifecycleOwner;
    }

    // /**
    //  * 发送同步对话请求
    //  */
    // public void sendSyncRequest(List<ChatMessage> messages, LlmCallback callback) {
    //     mExecutor.execute(() -> {
    //         OnHttpListener<AiChatApi.Bean> listener = new OnHttpListener<AiChatApi.Bean>() {
    //             @Override
    //             public void onStart(Call call) {}

    //             @Override
    //             public void onSucceed(AiChatApi.Bean result) {
    //                 Log.d("ChatLlmUtil", "Server response: " + new Gson().toJson(result)); // 添加这一行
    //                 mMainHandler.post(() -> {
    //                     if (result != null && result.getChoices() != null && !result.getChoices().isEmpty()) {
    //                         AiChatApi.Bean.Choice choice = result.getChoices().get(0);
    //                         ChatMessage message = choice.getMessage();
    //                         if (message != null && !TextUtils.isEmpty(message.getContent())) {
    //                             callback.onAssistantMessage(message.getContent());
    //                         } else {
    //                             callback.onAssistantMessage("抱歉，我暂时无法回答您的问题，请稍后再试。");
    //                         }
    //                     } else {
    //                         callback.onAssistantMessage("抱歉，我暂时无法回答您的问题，请稍后再试。");
    //                     }
    //                     callback.onComplete();
    //                 });
    //             }

    //             @Override
    //             public void onFail(Exception e) {
    //                 mMainHandler.post(() -> {
    //                     ToastUtils.show("发送失败：" + e.getMessage());
    //                     callback.onError("抱歉，发生了错误，请稍后再试。");
    //                     callback.onComplete();
    //                 });
    //             }

    //             @Override
    //             public void onEnd(Call call) {}
    //         };

    //         AiService.sendChatRequest(messages, listener, mLifecycleOwner);
    //     });
    // }
/**
     * 发送同步对话请求
     */
    public void sendSyncRequest(List<ChatMessage> messages, LlmCallback callback) {
        mExecutor.execute(() -> {
            OnHttpListener<AiChatApi.Bean> listener = new OnHttpListener<AiChatApi.Bean>() {
                @Override
                public void onStart(Call call) {}

                @Override
                public void onSucceed(AiChatApi.Bean result) {
                    Log.d("ChatLlmUtil", "Server response: " + new Gson().toJson(result));
                    mMainHandler.post(() -> {
                        // 1. 优先显示 data.content 字段
                        String content = null;
                        if (result != null && result.getData() != null) {
                            content = result.getData().getContent();
                        }
                        if (content != null && !content.trim().isEmpty()) {
                            callback.onAssistantMessage(content.trim());
                        } else if (result != null && result.getChoices() != null && !result.getChoices().isEmpty()) {
                            // 2. 兼容 choices 逻辑
                            List<String> validContents = new ArrayList<>();
                            for (AiChatApi.Bean.Choice choice : result.getChoices()) {
                                ChatMessage message = choice.getMessage();
                                if (message != null) {
                                    String msgContent = message.getContent();
                                    Log.d("ChatLlmUtil", "返回内容: [" + msgContent + "]");
                                    if (msgContent != null && !msgContent.trim().isEmpty()
                                            && !"done.".equalsIgnoreCase(msgContent.trim())
                                            && !"抱歉，done.".equals(msgContent.trim())) {
                                        validContents.add(msgContent.trim());
                                    }
                                }
                            }
                            if (!validContents.isEmpty()) {
                                callback.onAssistantMessage(validContents.get(0));
                            } else {
                                callback.onAssistantMessage("抱歉，我暂时无法回答您的问题，请稍后再试。");
                            }
                        } else {
                            callback.onAssistantMessage("抱歉，我暂时无法回答您的问题，请稍后再试。");
                        }
                        callback.onComplete();
                    });
                }
                
                @Override
                public void onFail(Exception e) {
                    mMainHandler.post(() -> {
                        ToastUtils.show("发送失败：" + e.getMessage());
                        callback.onError("抱歉，发生了错误，请稍后再试。");
                        callback.onComplete();
                    });
                }

                @Override
                public void onEnd(Call call) {}
            };

            AiService.sendChatRequest(messages, listener, mLifecycleOwner);
        });
    }
    /**
     * 发送流式对话请求
     */
    public void sendStreamRequest(List<ChatMessage> messages, LlmCallback callback) {
        mExecutor.execute(() -> {
            final StringBuilder responseBuilder = new StringBuilder();

            AiService.sendStreamChatRequest(messages, new AiService.StreamResponseCallback() {
                @Override
                public void onStreamMessage(AiChatApi.Bean deltaBean) {
                    mMainHandler.post(() -> {
                        if (deltaBean != null && deltaBean.getChoices() != null && !deltaBean.getChoices().isEmpty()) {
                            AiChatApi.Bean.Choice choice = deltaBean.getChoices().get(0);
                            ChatMessage delta = choice.getDelta();
                            if (delta != null && delta.getContent() != null && !delta.getContent().isEmpty()) {
                                responseBuilder.append(delta.getContent());
                                callback.onStreamUpdate(responseBuilder.toString());
                            }
                        }
                    });
                }

                @Override
                public void onComplete(AiChatApi.Bean completeBean) {
                    mMainHandler.post(callback::onComplete);
                }

                @Override
                public void onError(String error) {
                    mMainHandler.post(() -> {
                        ToastUtils.show("请求失败: " + error);
                        callback.onError("抱歉，我暂时无法回答您的问题，请稍后再试。");
                        callback.onComplete();
                    });
                }
            }, mLifecycleOwner);
        });
    }

    public void shutdown() {
        mExecutor.shutdown();
    }
}