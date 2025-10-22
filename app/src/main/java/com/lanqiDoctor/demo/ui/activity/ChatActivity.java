package com.lanqiDoctor.demo.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.action.StatusAction;
import com.lanqiDoctor.demo.aop.Log;
import com.lanqiDoctor.demo.aop.SingleClick;
import com.lanqiDoctor.demo.config.AiConfig;
import com.lanqiDoctor.demo.http.api.AiChatApi;
import com.lanqiDoctor.demo.http.api.AiService;
import com.lanqiDoctor.demo.http.api.ChatMessage;
import com.lanqiDoctor.demo.ui.adapter.ChatAdapter;
import com.lanqiDoctor.demo.widget.StatusLayout;
import com.hjq.http.listener.OnHttpListener;
import com.hjq.toast.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;

/**
 * AI对话 Activity - 支持多轮对话和系统人设
 */
public class ChatActivity extends AppCompatActivity implements StatusAction, View.OnClickListener {

    private RecyclerView mRecyclerView;
    private EditText mMessageEditText;
    private ChatAdapter mAdapter;
    private List<ChatMessage> mMessages; // 完整消息列表（包含系统消息）
    private List<ChatMessage> mVisibleMessages; // 可见消息列表（排除系统消息）

    // 线程池和Handler
    private ExecutorService mExecutor;
    private Handler mMainHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_fragment);

        initView();
        initData();
    }

    private void initView() {
        try {
            mRecyclerView = findViewById(R.id.rv_chat_list);
            mMessageEditText = findViewById(R.id.et_message);

            findViewById(R.id.btn_send).setOnClickListener(this);

            mMessages = new ArrayList<>();
            mVisibleMessages = new ArrayList<>();

            mAdapter = new ChatAdapter(this);
            mAdapter.setData(mVisibleMessages);

            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(mAdapter);

            mExecutor = Executors.newSingleThreadExecutor();
            mMainHandler = new Handler(Looper.getMainLooper());

        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.show("界面初始化失败");
        }
    }

    private void initData() {
        try {
            AiService.initialize();
            showComplete();

            if (AiConfig.shouldIncludeSystemPrompt()) {
                ChatMessage systemMessage = new ChatMessage("system", AiConfig.getSystemPrompt());
                mMessages.add(systemMessage);
            }

            String welcomeMessage = AiConfig.isLanXinModel()
                    ? "您好！我是蓝歧医童，基于Vivo蓝心大模型，有什么健康问题可以帮助您吗？"
                    : "您好！我是AI助手，有什么可以帮助您的吗？";
            addMessage("assistant", welcomeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addMessage(String role, String content) {
        try {
            if (mMessages != null && mVisibleMessages != null && mAdapter != null) {
                ChatMessage message = new ChatMessage(role, content);
                mMessages.add(message);

                if (!"system".equals(role)) {
                    mVisibleMessages.add(message);
                    mAdapter.notifyItemInserted(mVisibleMessages.size() - 1);
                    scrollToBottom();
                }
                manageConversationHistory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scrollToBottom() {
        if (mRecyclerView != null && mVisibleMessages != null && !mVisibleMessages.isEmpty()) {
            mRecyclerView.post(() -> mRecyclerView.smoothScrollToPosition(mVisibleMessages.size() - 1));
        }
    }

    private void manageConversationHistory() {
        if (!AiConfig.isConversationHistoryEnabled() || mMessages == null) {
            return;
        }
        try {
            int maxMessages = AiConfig.MAX_CONVERSATION_ROUNDS * 2;
            boolean hasSystemMessage = !mMessages.isEmpty() && "system".equals(mMessages.get(0).getRole());
            if (hasSystemMessage) {
                maxMessages += 1;
            }
            while (mMessages.size() > maxMessages) {
                int removeIndex = hasSystemMessage ? 1 : 0;
                ChatMessage removedMessage = mMessages.remove(removeIndex);
                if (!"system".equals(removedMessage.getRole())) {
                    for (int i = 0; i < mVisibleMessages.size(); i++) {
                        if (mVisibleMessages.get(i) == removedMessage) {
                            mVisibleMessages.remove(i);
                            mAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("ChatActivity: 管理对话历史失败 - " + e.getMessage());
        }
    }

    private List<ChatMessage> prepareMessagesForApi() {
        if (mMessages == null || mMessages.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(mMessages);
    }

    @Log("ChatActivity")
    private void sendSyncRequest() {
        mExecutor.execute(() -> {
            OnHttpListener<AiChatApi.Bean> listener = new OnHttpListener<AiChatApi.Bean>() {
                @Override
                public void onStart(Call call) {}

                @Override
                public void onSucceed(AiChatApi.Bean result) {
                    mMainHandler.post(() -> {
                        showComplete();
                        if (result != null && result.getChoices() != null && !result.getChoices().isEmpty()) {
                            AiChatApi.Bean.Choice choice = result.getChoices().get(0);
                            ChatMessage message = choice.getMessage();
                            if (message != null && !TextUtils.isEmpty(message.getContent())) {
                                addMessage("assistant", message.getContent());
                            }
                        } else {
                            addMessage("assistant", "抱歉，我暂时无法回答您的问题，请稍后再试。");
                        }
                    });
                }

                @Override
                public void onFail(Exception e) {
                    mMainHandler.post(() -> {
                        showComplete();
                        ToastUtils.show("发送失败：" + e.getMessage());
                        addMessage("assistant", "抱歉，发生了错误，请稍后再试。");
                    });
                }

                @Override
                public void onEnd(Call call) {}
            };

            List<ChatMessage> apiMessages = prepareMessagesForApi();
            AiService.sendChatRequest(apiMessages, listener, this);
        });
    }

    @Log("ChatActivity")
    private void sendStreamRequest() {
        mExecutor.execute(() -> {
            final int[] aiVisibleMessageIndex = {-1};
            final StringBuilder responseBuilder = new StringBuilder();

            List<ChatMessage> apiMessages = prepareMessagesForApi();

            AiService.sendStreamChatRequest(apiMessages, new AiService.StreamResponseCallback() {
                @Override
                public void onStreamMessage(AiChatApi.Bean deltaBean) {
                    mMainHandler.post(() -> {
                        try {
                            if (deltaBean != null && deltaBean.getChoices() != null && !deltaBean.getChoices().isEmpty()) {
                                AiChatApi.Bean.Choice choice = deltaBean.getChoices().get(0);
                                ChatMessage delta = choice.getDelta();
                                if (delta != null && delta.getContent() != null && !delta.getContent().isEmpty()) {
                                    responseBuilder.append(delta.getContent());
                                    if (aiVisibleMessageIndex[0] == -1) {
                                        addMessage("assistant", responseBuilder.toString());
                                        aiVisibleMessageIndex[0] = findLastAssistantVisibleMessageIndex();
                                    } else {
                                        updateLastAIMessageStreaming(responseBuilder.toString());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onComplete(AiChatApi.Bean completeBean) {
                    mMainHandler.post(() -> {
                        showComplete();
                        if (aiVisibleMessageIndex[0] >= 0 && responseBuilder.length() > 0) {
                            updateLastAIMessageStreaming(responseBuilder.toString());
                        }
                        scrollToBottom();
                    });
                }

                @Override
                public void onError(String error) {
                    mMainHandler.post(() -> {
                        showComplete();
                        ToastUtils.show("请求失败: " + error);
                        if (aiVisibleMessageIndex[0] == -1) {
                            addMessage("assistant", "抱歉，我暂时无法回答您的问题，请稍后再试。");
                        } else {
                            String currentContent = responseBuilder.toString();
                            if (currentContent.isEmpty()) {
                                updateLastAIMessageStreaming("抱歉，我暂时无法回答您的问题，请稍后再试。");
                            }
                        }
                        scrollToBottom();
                    });
                }
            }, this);
        });
    }

    private int findLastAssistantVisibleMessageIndex() {
        if (mVisibleMessages == null) return -1;
        for (int i = mVisibleMessages.size() - 1; i >= 0; i--) {
            if ("assistant".equals(mVisibleMessages.get(i).getRole())) {
                return i;
            }
        }
        return -1;
    }

    private void updateLastAIMessageStreaming(String content) {
        try {
            int lastVisibleAssistantIndex = findLastAssistantVisibleMessageIndex();
            if (lastVisibleAssistantIndex >= 0 && mAdapter != null && mVisibleMessages != null) {
                ChatMessage lastMessage = mVisibleMessages.get(lastVisibleAssistantIndex);
                if (lastMessage != null) {
                    lastMessage.setContent(content);
                    updateMessageInFullList(lastMessage, content);
                    mAdapter.notifyItemChanged(lastVisibleAssistantIndex);
                    scrollToBottom();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateMessageInFullList(ChatMessage visibleMessage, String content) {
        try {
            if (mMessages != null) {
                for (ChatMessage msg : mMessages) {
                    if (msg == visibleMessage) {
                        msg.setContent(content);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SingleClick
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_send) {
            sendMessage();
        }
    }

    @Log("ChatActivity")
    private void sendMessage() {
        try {
            String message = mMessageEditText.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                ToastUtils.show("请输入消息");
                return;
            }
            addMessage("user", message);
            mMessageEditText.setText("");
            ToastUtils.show("请稍候...");
            if (AiConfig.USE_STREAM_MODE && AiConfig.isLanXinModel()) {
                sendStreamRequest();
            } else {
                sendSyncRequest();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.show("发送消息失败");
            showComplete();
        }
    }

    @Override
    public StatusLayout getStatusLayout() {
        try {
            return findViewById(R.id.hl_status_hint);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    @Override
    public boolean isStatusBarEnabled() {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mExecutor != null) {
            mExecutor.shutdown();
        }
    }
}