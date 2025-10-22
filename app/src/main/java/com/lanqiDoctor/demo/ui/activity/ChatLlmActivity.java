package com.lanqiDoctor.demo.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.config.AiConfig;
import com.lanqiDoctor.demo.entity.ChatSession;
import com.lanqiDoctor.demo.http.api.ChatMessage;
import com.lanqiDoctor.demo.manager.ChatHistoryManager;
import com.lanqiDoctor.demo.manager.UserStateManager;
import com.lanqiDoctor.demo.ui.adapter.ChatAdapter;
import com.lanqiDoctor.demo.ui.adapter.ChatHistoryAdapter;
import com.lanqiDoctor.demo.util.ChatLlmUtil;
import com.lanqiDoctor.demo.widget.StatusLayout;
import com.hjq.toast.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用 ChatLlmUtil 的 AI对话 Activity - 支持历史记录功能
 */
public class ChatLlmActivity extends AppCompatActivity implements View.OnClickListener {

    // 主界面组件
    private RecyclerView mRecyclerView;
    private EditText mMessageEditText;
    private ChatAdapter mAdapter;
    private List<ChatMessage> mMessages; // 完整消息列表（包含系统消息）
    private List<ChatMessage> mVisibleMessages; // 可见消息列表（排除系统消息）

    // 历史记录组件
    private DrawerLayout mDrawerLayout;
    private RecyclerView mHistoryRecyclerView;
    private ChatHistoryAdapter mHistoryAdapter;
    private LinearLayout mEmptyHistoryView;
    private List<ChatSession> mHistorySessions;

    // 工具类
    private ChatLlmUtil mLlmUtil;
    private Handler mMainHandler;
    private ChatHistoryManager mHistoryManager;
    private UserStateManager mUserStateManager;

    // 当前会话
    private ChatSession mCurrentSession;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_history);

        mMainHandler = new Handler(Looper.getMainLooper());
        mLlmUtil = new ChatLlmUtil(mMainHandler, this);
        mHistoryManager = ChatHistoryManager.getInstance(this);
        mUserStateManager = UserStateManager.getInstance(this);

        initView();
        initData();
        loadChatHistory();
    }

    private void initView() {
        // 主界面组件初始化
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mRecyclerView = findViewById(R.id.rv_chat_list);
        mMessageEditText = findViewById(R.id.et_message);
        
        // 工具栏按钮
        findViewById(R.id.iv_history).setOnClickListener(this);
        findViewById(R.id.iv_new_chat).setOnClickListener(this);
        findViewById(R.id.btn_send).setOnClickListener(this);
        
        // 历史记录组件初始化
        mHistoryRecyclerView = findViewById(R.id.rv_history_list);
        mEmptyHistoryView = findViewById(R.id.ll_empty_history);
        findViewById(R.id.iv_clear_history).setOnClickListener(this);

        // 聊天消息适配器
        mMessages = new ArrayList<>();
        mVisibleMessages = new ArrayList<>();
        mAdapter = new ChatAdapter(this);
        mAdapter.setData(mVisibleMessages);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        // 历史记录
        mHistorySessions = new ArrayList<>();
        mHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // 初始化历史记录适配器
        mHistoryAdapter = new ChatHistoryAdapter(this);
        mHistoryAdapter.setData(mHistorySessions);
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);
        
        // 设置历史记录点击监听
        mHistoryAdapter.setOnItemActionListener(new ChatHistoryAdapter.OnItemActionListener() {
            @Override
            public void onItemClick(ChatSession chatSession) {
                loadChatSession(chatSession);
            }

            @Override
            public void onItemDelete(ChatSession chatSession) {
                deleteChatSession(chatSession);
            }
        });
    }

    private void initData() {
        startNewChat();
    }

    /**
     * 开始新对话
     */
    private void startNewChat() {
        // 保存当前会话（如果有消息）
        saveCurrentSessionIfNeeded();
        
        // 清空当前消息
        mMessages.clear();
        mVisibleMessages.clear();
        mAdapter.notifyDataSetChanged();
        mCurrentSession = null;
        
        // 添加系统人设消息（如果启用）
        if (AiConfig.shouldIncludeSystemPrompt()) {
            ChatMessage systemMessage = new ChatMessage("system", AiConfig.getSystemPrompt());
            mMessages.add(systemMessage);
        }
        
        // 添加欢迎消息
        String welcomeMessage = AiConfig.isLanXinModel()
                ? "您好！我是蓝岐医童，基于Vivo蓝心大模型，有什么健康问题可以帮助您吗？"
                : "您好！我是AI助手，有什么可以帮助您的吗？";
        addMessage("assistant", welcomeMessage);
    }

    /**
     * 加载聊天历史
     */
    private void loadChatHistory() {
        String userId = mUserStateManager.getUserId();
        mHistoryManager.getAllChatSessions(userId, new ChatHistoryManager.LoadCallback() {
            @Override
            public void onSuccess(List<ChatSession> sessions) {
                mMainHandler.post(() -> {
                    mHistorySessions.clear();
                    mHistorySessions.addAll(sessions);
                    // TODO: 通知适配器数据变化
                    updateHistoryEmptyState();
                });
            }

            @Override
            public void onError(String error) {
                mMainHandler.post(() -> {
                    ToastUtils.show("加载历史记录失败: " + error);
                    updateHistoryEmptyState();
                });
            }
        });
    }

    /**
     * 加载指定的聊天会话
     */
    private void loadChatSession(ChatSession chatSession) {
        // 保存当前会话
        saveCurrentSessionIfNeeded();
        
        // 加载选中的会话
        mCurrentSession = chatSession;
        mMessages.clear();
        mVisibleMessages.clear();
        
        if (chatSession.getMessages() != null) {
            mMessages.addAll(chatSession.getMessages());
            
            // 过滤系统消息，只显示用户和助手消息
            for (ChatMessage message : chatSession.getMessages()) {
                if (!"system".equals(message.getRole())) {
                    mVisibleMessages.add(message);
                }
            }
        }
        
        mAdapter.notifyDataSetChanged();
        scrollToBottom();
    }

    /**
     * 保存当前会话（如果需要）
     */
    private void saveCurrentSessionIfNeeded() {
        if (mMessages == null || mMessages.isEmpty()) {
            return;
        }
        
        // 检查是否有用户消息（排除系统消息和欢迎消息）
        boolean hasUserMessage = false;
        for (ChatMessage message : mMessages) {
            if ("user".equals(message.getRole())) {
                hasUserMessage = true;
                break;
            }
        }
        
        if (!hasUserMessage) {
            return; // 没有用户消息，不需要保存
        }
        
        String userId = mUserStateManager.getUserId();
        
        if (mCurrentSession == null) {
            // 创建新会话
            mCurrentSession = mHistoryManager.createChatSessionFromMessages(userId, new ArrayList<>(mMessages));
            mHistoryManager.saveChatSession(mCurrentSession, new ChatHistoryManager.SaveCallback() {
                @Override
                public void onSuccess(ChatSession chatSession) {
                    mCurrentSession = chatSession;
                    // 刷新历史记录
                    loadChatHistory();
                }

                @Override
                public void onError(String error) {
                    // 保存失败，可以记录日志但不影响用户体验
                }
            });
        } else {
            // 更新现有会话
            mCurrentSession.setMessages(new ArrayList<>(mMessages));
            mHistoryManager.updateChatSession(mCurrentSession, new ChatHistoryManager.UpdateCallback() {
                @Override
                public void onSuccess() {
                    // 刷新历史记录
                    loadChatHistory();
                }

                @Override
                public void onError(String error) {
                    // 更新失败，可以记录日志但不影响用户体验
                }
            });
        }
    }

    /**
     * 更新历史记录空状态显示
     */
    private void updateHistoryEmptyState() {
        if (mHistorySessions.isEmpty()) {
            mHistoryRecyclerView.setVisibility(View.GONE);
            mEmptyHistoryView.setVisibility(View.VISIBLE);
        } else {
            mHistoryRecyclerView.setVisibility(View.VISIBLE);
            mEmptyHistoryView.setVisibility(View.GONE);
        }
    }

    /**
     * 显示删除确认对话框
     */
    private void showDeleteConfirmDialog(ChatSession chatSession) {
        new AlertDialog.Builder(this)
                .setTitle("删除对话")
                .setMessage("确定要删除这个对话吗？删除后无法恢复。")
                .setPositiveButton("删除", (dialog, which) -> deleteChatSession(chatSession))
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 删除聊天会话
     */
    private void deleteChatSession(ChatSession chatSession) {
        mHistoryManager.deleteChatSession(chatSession, new ChatHistoryManager.DeleteCallback() {
            @Override
            public void onSuccess() {
                mMainHandler.post(() -> {
                    ToastUtils.show("对话已删除");
                    loadChatHistory();
                    
                    // 如果删除的是当前会话，开始新对话
                    if (mCurrentSession != null && mCurrentSession.getId() == chatSession.getId()) {
                        startNewChat();
                    }
                });
            }

            @Override
            public void onError(String error) {
                mMainHandler.post(() -> ToastUtils.show("删除失败: " + error));
            }
        });
    }

    /**
     * 显示清空历史确认对话框
     */
    private void showClearHistoryConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("清空历史")
                .setMessage("确定要清空所有聊天历史吗？清空后无法恢复。")
                .setPositiveButton("清空", (dialog, which) -> clearAllHistory())
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 清空所有历史记录
     */
    private void clearAllHistory() {
        String userId = mUserStateManager.getUserId();
        mHistoryManager.clearAllHistory(userId, new ChatHistoryManager.ClearCallback() {
            @Override
            public void onSuccess() {
                mMainHandler.post(() -> {
                    ToastUtils.show("历史记录已清空");
                    loadChatHistory();
                    startNewChat();
                });
            }

            @Override
            public void onError(String error) {
                mMainHandler.post(() -> ToastUtils.show("清空失败: " + error));
            }
        });
    }

    private void addMessage(String role, String content) {
        ChatMessage message = new ChatMessage(role, content);
        mMessages.add(message);
        if (!"system".equals(role)) {
            mVisibleMessages.add(message);
            mAdapter.notifyItemInserted(mVisibleMessages.size() - 1);
            scrollToBottom();
        }
        manageConversationHistory();
    }

    private void scrollToBottom() {
        if (!mVisibleMessages.isEmpty()) {
            mRecyclerView.post(() -> mRecyclerView.smoothScrollToPosition(mVisibleMessages.size() - 1));
        }
    }

    private void manageConversationHistory() {
        if (!AiConfig.isConversationHistoryEnabled() || mMessages == null) return;
        int maxMessages = AiConfig.MAX_CONVERSATION_ROUNDS * 2;
        boolean hasSystemMessage = !mMessages.isEmpty() && "system".equals(mMessages.get(0).getRole());
        if (hasSystemMessage) maxMessages += 1;
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
    }

    private List<ChatMessage> prepareMessagesForApi() {
        return new ArrayList<>(mMessages);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_send) {
            sendMessage();
        } else if (id == R.id.iv_history) {
            // 打开/关闭历史记录侧边栏
            if (mDrawerLayout.isDrawerOpen(findViewById(R.id.nav_history))) {
                mDrawerLayout.closeDrawers();
            } else {
                mDrawerLayout.openDrawer(findViewById(R.id.nav_history));
            }
        } else if (id == R.id.iv_new_chat) {
            // 开始新对话
            startNewChat();
        } else if (id == R.id.iv_clear_history) {
            // 清空历史记录
            showClearHistoryConfirmDialog();
        }
    }

    private void sendMessage() {
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
    }

    private void sendSyncRequest() {
        mLlmUtil.sendSyncRequest(prepareMessagesForApi(), new ChatLlmUtil.LlmCallback() {
            @Override
            public void onAssistantMessage(String content) {
                addMessage("assistant", content);
            }

            @Override
            public void onError(String errorMsg) {
                addMessage("assistant", errorMsg);
            }

            @Override
            public void onComplete() {
                // 可选：隐藏加载状态
            }

            @Override
            public void onStreamUpdate(String content) {
                // 同步模式无需实现
            }
        });
    }

    private void sendStreamRequest() {
        final int[] aiVisibleMessageIndex = {-1};
        mLlmUtil.sendStreamRequest(prepareMessagesForApi(), new ChatLlmUtil.LlmCallback() {
            @Override
            public void onAssistantMessage(String content) {
                // 流式模式下最终内容
                updateLastAIMessageStreaming(content);
            }

            @Override
            public void onError(String errorMsg) {
                if (aiVisibleMessageIndex[0] == -1) {
                    addMessage("assistant", errorMsg);
                } else {
                    updateLastAIMessageStreaming(errorMsg);
                }
            }

            @Override
            public void onComplete() {
                scrollToBottom();
            }

            @Override
            public void onStreamUpdate(String content) {
                if (aiVisibleMessageIndex[0] == -1) {
                    addMessage("assistant", content);
                    aiVisibleMessageIndex[0] = findLastAssistantVisibleMessageIndex();
                } else {
                    updateLastAIMessageStreaming(content);
                }
            }
        });
    }

    private int findLastAssistantVisibleMessageIndex() {
        for (int i = mVisibleMessages.size() - 1; i >= 0; i--) {
            if ("assistant".equals(mVisibleMessages.get(i).getRole())) {
                return i;
            }
        }
        return -1;
    }

    private void updateLastAIMessageStreaming(String content) {
        int lastVisibleAssistantIndex = findLastAssistantVisibleMessageIndex();
        if (lastVisibleAssistantIndex >= 0) {
            ChatMessage lastMessage = mVisibleMessages.get(lastVisibleAssistantIndex);
            lastMessage.setContent(content);
            updateMessageInFullList(lastMessage, content);
            mAdapter.notifyItemChanged(lastVisibleAssistantIndex);
            scrollToBottom();
        }
    }

    private void updateMessageInFullList(ChatMessage visibleMessage, String content) {
        for (ChatMessage msg : mMessages) {
            if (msg == visibleMessage) {
                msg.setContent(content);
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 保存当前会话
        saveCurrentSessionIfNeeded();
        mLlmUtil.shutdown();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 应用进入后台时保存当前会话
        saveCurrentSessionIfNeeded();
    }

    // 如需状态布局
    public StatusLayout getStatusLayout() {
        try {
            return findViewById(R.id.hl_status_hint);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}