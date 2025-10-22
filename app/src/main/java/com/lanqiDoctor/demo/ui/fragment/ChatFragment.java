package com.lanqiDoctor.demo.ui.fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.action.StatusAction;
import com.lanqiDoctor.demo.aop.Log;
import com.lanqiDoctor.demo.aop.SingleClick;
import com.lanqiDoctor.demo.app.TitleBarFragment;
import com.lanqiDoctor.demo.config.AiConfig;
import com.lanqiDoctor.demo.http.api.AiChatApi;
import com.lanqiDoctor.demo.http.api.AiService;
import com.lanqiDoctor.demo.http.api.ChatMessage;
import com.lanqiDoctor.demo.ui.activity.HomeActivity;
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
 * AI对话 Fragment - 支持多轮对话和系统人设
 */
public final class ChatFragment extends TitleBarFragment<HomeActivity> implements StatusAction {

    private RecyclerView mRecyclerView;
    private EditText mMessageEditText;
    private ChatAdapter mAdapter;
    private List<ChatMessage> mMessages; // 完整消息列表（包含系统消息）
    private List<ChatMessage> mVisibleMessages; // 可见消息列表（排除系统消息）

    // 线程池和Handler
    private ExecutorService mExecutor;
    private Handler mMainHandler;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.chat_fragment;
    }

    @Override
    protected void initView() {
        try {
            mRecyclerView = findViewById(R.id.rv_chat_list);
            mMessageEditText = findViewById(R.id.et_message);

            setOnClickListener(R.id.btn_send);

            mMessages = new ArrayList<>();
            mVisibleMessages = new ArrayList<>();

            // 适配器使用可见消息列表
            mAdapter = new ChatAdapter(getAttachActivity());
            mAdapter.setData(mVisibleMessages);

            mRecyclerView.setLayoutManager(new LinearLayoutManager(getAttachActivity()));
            mRecyclerView.setAdapter(mAdapter);

            // 初始化线程池和Handler
            mExecutor = Executors.newSingleThreadExecutor();
            mMainHandler = new Handler(Looper.getMainLooper());

        } catch (Exception e) {
            e.printStackTrace();
            ToastUtils.show("界面初始化失败");
        }
    }

    @Override
    protected void initData() {
        try {
            // 初始化AI服务
            AiService.initialize();

            // 确保状态布局被正确隐藏
            showComplete();

            // 添加系统人设消息（如果启用）
            if (AiConfig.shouldIncludeSystemPrompt()) {
                // 系统消息只添加到完整消息列表，不显示在UI中
                ChatMessage systemMessage = new ChatMessage("system", AiConfig.getSystemPrompt());
                mMessages.add(systemMessage);
            }

            // 添加欢迎消息，根据当前模型显示不同的欢迎语
            String welcomeMessage = AiConfig.isLanXinModel()
                    ? "您好！我是蓝歧医童，基于Vivo蓝心大模型，有什么健康问题可以帮助您吗？"
                    : "您好！我是AI助手，有什么可以帮助您的吗？";
            addMessage("assistant", welcomeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加消息到列表
     */
    private void addMessage(String role, String content) {
        try {
            if (mMessages != null && mVisibleMessages != null && mAdapter != null) {
                ChatMessage message = new ChatMessage(role, content);

                // 添加到完整消息列表
                mMessages.add(message);

                // 只有非系统消息才添加到可见消息列表和UI
                if (!"system".equals(role)) {
                    mVisibleMessages.add(message);
                    mAdapter.notifyItemInserted(mVisibleMessages.size() - 1);
                    scrollToBottom();
                }

                // 管理历史对话长度
                manageConversationHistory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 滚动到底部
     */
    private void scrollToBottom() {
        if (mRecyclerView != null && mVisibleMessages != null && !mVisibleMessages.isEmpty()) {
            mRecyclerView.post(() -> {
                mRecyclerView.smoothScrollToPosition(mVisibleMessages.size() - 1);
            });
        }
    }

    /**
     * 管理对话历史，保持在合理范围内
     */
    private void manageConversationHistory() {
        if (!AiConfig.isConversationHistoryEnabled() || mMessages == null) {
            return;
        }

        try {
            // 计算需要保留的消息数量
            int maxMessages = AiConfig.MAX_CONVERSATION_ROUNDS * 2; // 每轮包含用户和助手两条消息

            // 如果有系统消息，需要额外保留
            boolean hasSystemMessage = !mMessages.isEmpty() && "system".equals(mMessages.get(0).getRole());
            if (hasSystemMessage) {
                maxMessages += 1;
            }

            // 如果消息数量超过限制，删除最旧的对话（保留系统消息）
            while (mMessages.size() > maxMessages) {
                int removeIndex = hasSystemMessage ? 1 : 0; // 跳过系统消息
                ChatMessage removedMessage = mMessages.remove(removeIndex);

                // 如果删除的不是系统消息，也需要从可见列表中删除
                if (!"system".equals(removedMessage.getRole())) {
                    // 在可见列表中找到对应消息并删除
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
            System.err.println("ChatFragment: 管理对话历史失败 - " + e.getMessage());
        }
    }

    /**
     * 准备发送的消息列表（用于API请求）
     */
    private List<ChatMessage> prepareMessagesForApi() {
        if (mMessages == null || mMessages.isEmpty()) {
            return new ArrayList<>();
        }

        // 返回完整的消息列表，包括系统消息
        return new ArrayList<>(mMessages);
    }

    /**
     * 发送同步请求
     */
    @Log("ChatFragment")
    private void sendSyncRequest() {
        mExecutor.execute(() -> {
            // 创建HTTP监听器
            OnHttpListener<AiChatApi.Bean> listener = new OnHttpListener<AiChatApi.Bean>() {
                @Override
                public void onStart(Call call) {
                    // 请求开始
                }

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
                public void onEnd(Call call) {
                    // 请求结束
                }
            };

            // 使用完整的消息历史（包括系统消息）
            List<ChatMessage> apiMessages = prepareMessagesForApi();
            AiService.sendChatRequest(apiMessages, listener, this);
        });
    }

    /**
     * 发送流式请求
     */
    @Log("ChatFragment")
    private void sendStreamRequest() {
        mExecutor.execute(() -> {
            final int[] aiVisibleMessageIndex = {-1};
            final StringBuilder responseBuilder = new StringBuilder();

            // 使用完整的消息历史（包括系统消息）
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
                                    // 累积内容
                                    responseBuilder.append(delta.getContent());

                                    System.out.println("ChatFragment: 收到流式片段: '" + delta.getContent() +
                                            "', 累积长度: " + responseBuilder.length());

                                    if (aiVisibleMessageIndex[0] == -1) {
                                        // 第一次收到消息，添加AI消息
                                        addMessage("assistant", responseBuilder.toString());
                                        aiVisibleMessageIndex[0] = findLastAssistantVisibleMessageIndex();
                                        System.out.println("ChatFragment: 创建新AI消息，可见索引: " + aiVisibleMessageIndex[0]);
                                    } else {
                                        // 流式更新现有AI消息
                                        updateLastAIMessageStreaming(responseBuilder.toString());
                                        System.out.println("ChatFragment: 更新AI消息，当前内容长度: " + responseBuilder.length());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.err.println("ChatFragment: 处理流式消息异常: " + e.getMessage());
                        }
                    });
                }

                @Override
                public void onComplete(AiChatApi.Bean completeBean) {
                    mMainHandler.post(() -> {
                        showComplete();
                        System.out.println("ChatFragment: 流式请求完成，最终内容长度: " + responseBuilder.length());

                        // 确保最终内容正确显示
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
                        System.err.println("ChatFragment: 流式请求错误: " + error);

                        if (aiVisibleMessageIndex[0] == -1) {
                            addMessage("assistant", "抱歉，我暂时无法回答您的问题，请稍后再试。");
                        } else {
                            // 如果已经有部分内容，保留已显示的内容
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

    /**
     * 查找最后一条助手消息在可见消息列表中的索引
     */
    private int findLastAssistantVisibleMessageIndex() {
        if (mVisibleMessages == null) return -1;

        for (int i = mVisibleMessages.size() - 1; i >= 0; i--) {
            if ("assistant".equals(mVisibleMessages.get(i).getRole())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 流式更新最后一条AI消息
     */
    private void updateLastAIMessageStreaming(String content) {
        try {
            int lastVisibleAssistantIndex = findLastAssistantVisibleMessageIndex();
            System.out.println("ChatFragment: 准备更新AI消息，可见索引: " + lastVisibleAssistantIndex + ", 内容长度: " + content.length());

            if (lastVisibleAssistantIndex >= 0 && mAdapter != null && mVisibleMessages != null) {
                ChatMessage lastMessage = mVisibleMessages.get(lastVisibleAssistantIndex);
                if (lastMessage != null) {
                    String oldContent = lastMessage.getContent();
                    lastMessage.setContent(content);

                    // 同时更新完整消息列表中对应的消息
                    updateMessageInFullList(lastMessage, content);

                    System.out.println("ChatFragment: 内容更新 - 旧长度: " + (oldContent != null ? oldContent.length() : 0) +
                            ", 新长度: " + content.length());

                    mAdapter.notifyItemChanged(lastVisibleAssistantIndex);
                    System.out.println("ChatFragment: 已通知适配器更新位置: " + lastVisibleAssistantIndex);

                    scrollToBottom();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ChatFragment: 更新AI消息异常: " + e.getMessage());
        }
    }

    /**
     * 在完整消息列表中更新对应的消息
     */
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

    /**
     * 发送消息
     */
    @Log("ChatFragment")
    private void sendMessage() {
        try {
            String message = mMessageEditText.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                ToastUtils.show("请输入消息");
                return;
            }

            // 添加用户消息
            addMessage("user", message);
            mMessageEditText.setText("");

            // 提示用户请稍候
            ToastUtils.show("请稍候...");

            // 根据配置选择同步或流式请求
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

    @Override
    public boolean isStatusBarEnabled() {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 关闭线程池
        if (mExecutor != null) {
            mExecutor.shutdown();
        }
    }
}