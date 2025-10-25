package com.lanqiDoctor.demo.ui.fragment;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.hjq.base.BaseFragment;
import com.hjq.toast.ToastUtils;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.aop.Permissions;
import com.lanqiDoctor.demo.config.AiConfig;
import com.lanqiDoctor.demo.http.api.ChatMessage;
import com.lanqiDoctor.demo.ui.activity.*;
import com.lanqiDoctor.demo.ui.adapter.ChatAdapter;
import com.lanqiDoctor.demo.util.ChatLlmUtil;
import com.hjq.permissions.Permission;

import java.util.ArrayList;
import java.util.List;

/**
 * 健康中心Fragment - 新版本
 * 
 * 包含两个标签页：
 * 1. AI助手 - 嵌入式聊天界面
 * 2. 日常服药 - 个性目标设置
 *
 * @author 蓝岐医童开发团队
 * @version 2.0
 */
public class HealthCenterFragmentNew extends BaseFragment {

    // AI助手页面组件
    private LinearLayout llFunctionButtons; // 4个功能按钮容器
    private LinearLayout llVoiceChat;
    private LinearLayout llSymptomTracking;
    private LinearLayout llPhotoRecognition;
    private LinearLayout llMedicalHistory;
    
    // 智能推荐问题
    private LinearLayout llSuggestionsContainer;  // 推荐问题容器（用于隐藏）
    private android.widget.TextView tvSuggestion1;
    private android.widget.TextView tvSuggestion2;
    private android.widget.TextView tvSuggestion3;
    private LinearLayout llRefreshSuggestions;
    
    // AI聊天界面组件
    private RecyclerView rvChatList;
    private EditText etMessage;
    private Button btnSend;
    private ImageView ivCameraInput;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private List<ChatMessage> visibleMessages;
    private ChatLlmUtil llmUtil;
    private Handler mainHandler;
    
    // 推荐问题库 - 常见症状/疾病
    private String[][] suggestionQuestions = {
        {"感冒咳嗽怎么办？", "发烧如何快速退烧？", "头痛的缓解方法？"},
        {"胃痛是什么原因？", "失眠怎么调理？", "过敏症状如何处理？"},
        {"高血压注意事项？", "糖尿病饮食建议？", "心脏不适怎么办？"}
    };
    private int currentSuggestionSet = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.page_ai_assistant;
    }

    @Override
    protected void initView() {
        // 功能按钮容器
        llFunctionButtons = (LinearLayout) findViewById(R.id.ll_function_buttons);
        
        // 功能按钮
        llVoiceChat = (LinearLayout) findViewById(R.id.ll_voice_chat);
        llSymptomTracking = (LinearLayout) findViewById(R.id.ll_symptom_tracking);
        llPhotoRecognition = (LinearLayout) findViewById(R.id.ll_photo_recognition);
        llMedicalHistory = (LinearLayout) findViewById(R.id.ll_medical_history);
        
        // 智能推荐问题
        llSuggestionsContainer = (LinearLayout) findViewById(R.id.ll_suggestions_container);
        tvSuggestion1 = (android.widget.TextView) findViewById(R.id.tv_suggestion_1);
        tvSuggestion2 = (android.widget.TextView) findViewById(R.id.tv_suggestion_2);
        tvSuggestion3 = (android.widget.TextView) findViewById(R.id.tv_suggestion_3);
        llRefreshSuggestions = (LinearLayout) findViewById(R.id.ll_refresh_suggestions);
        
        // 聊天界面组件
        rvChatList = (RecyclerView) findViewById(R.id.rv_chat_list);
        etMessage = (EditText) findViewById(R.id.et_message);
        btnSend = (Button) findViewById(R.id.btn_send);
        ivCameraInput = (ImageView) findViewById(R.id.iv_camera_input);
        
        // 设置聊天列表
        messages = new ArrayList<>();
        visibleMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext());
        chatAdapter.setData(visibleMessages);
        rvChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChatList.setAdapter(chatAdapter);
        
        // 添加滚动监听，隐藏/显示功能按钮
        setupScrollListener();
    }

    @Override
    protected void initData() {
        // 初始化聊天工具
        mainHandler = new Handler(Looper.getMainLooper());
        llmUtil = new ChatLlmUtil(mainHandler, getActivity());
        
        // 初始化事件监听
        initListener();
        
        // 启动新对话并添加欢迎消息
        startNewChat();
    }

    /**
     * 设置滚动监听，实现滚动隐藏/显示功能按钮
     */
    private void setupScrollListener() {
        rvChatList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                if (dy > 20 && llFunctionButtons.getVisibility() == android.view.View.VISIBLE) {
                    // 向上滚动，隐藏功能按钮
                    llFunctionButtons.animate()
                        .translationY(-llFunctionButtons.getHeight())
                        .alpha(0.0f)
                        .setDuration(200)
                        .withEndAction(() -> llFunctionButtons.setVisibility(android.view.View.GONE));
                } else if (dy < -20 && llFunctionButtons.getVisibility() == android.view.View.GONE) {
                    // 向下滚动，显示功能按钮
                    llFunctionButtons.setVisibility(android.view.View.VISIBLE);
                    llFunctionButtons.animate()
                        .translationY(0)
                        .alpha(1.0f)
                        .setDuration(200);
                }
            }
        });
    }

    @Permissions(Permission.RECORD_AUDIO)
    private void initListener() {
        // AI助手页面 - 功能按钮事件
        if (llVoiceChat != null) {
            llVoiceChat.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), RealtimeDialogActivity.class);
                startActivity(intent);
            });
        }
        
        if (llSymptomTracking != null) {
            llSymptomTracking.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), MonthCalendarActivity.class);
                startActivity(intent);
            });
        }
        
        if (llPhotoRecognition != null) {
            llPhotoRecognition.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), MobileHealthDaily.class);
                startActivity(intent);
            });
        }
        
        if (llMedicalHistory != null) {
            llMedicalHistory.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), MedicalHistoryActivity.class);
                startActivity(intent);
            });
        }
        
        // AI聊天界面事件
        if (btnSend != null) {
            btnSend.setOnClickListener(v -> sendMessage());
        }
        
        if (ivCameraInput != null) {
            ivCameraInput.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), com.lanqiDoctor.demo.ocr.OCRActivity.class);
                startActivity(intent);
            });
        }
        
        // 推荐问题点击事件
        if (tvSuggestion1 != null) {
            tvSuggestion1.setOnClickListener(v -> {
                sendQuestionToChat(tvSuggestion1.getText().toString());
            });
        }
        if (tvSuggestion2 != null) {
            tvSuggestion2.setOnClickListener(v -> {
                sendQuestionToChat(tvSuggestion2.getText().toString());
            });
        }
        if (tvSuggestion3 != null) {
            tvSuggestion3.setOnClickListener(v -> {
                sendQuestionToChat(tvSuggestion3.getText().toString());
            });
        }
        
        // 换一换按钮
        if (llRefreshSuggestions != null) {
            llRefreshSuggestions.setOnClickListener(v -> {
                refreshSuggestions();
            });
        }
    }
    
    /**
     * 发送推荐问题到聊天
     */
    private void sendQuestionToChat(String question) {
        if (etMessage != null) {
            etMessage.setText(question);
            sendMessage();
            hideSuggestionsContainer();  // 发送后隐藏推荐框
        }
    }
    
    /**
     * 隐藏推荐问题容器（带动画）
     */
    private void hideSuggestionsContainer() {
        if (llSuggestionsContainer != null && llSuggestionsContainer.getVisibility() == android.view.View.VISIBLE) {
            llSuggestionsContainer.animate()
                .alpha(0f)
                .translationY(-llSuggestionsContainer.getHeight())
                .setDuration(200)
                .withEndAction(() -> llSuggestionsContainer.setVisibility(android.view.View.GONE))
                .start();
        }
    }
    
    /**
     * 刷新推荐问题
     */
    private void refreshSuggestions() {
        currentSuggestionSet = (currentSuggestionSet + 1) % suggestionQuestions.length;
        String[] questions = suggestionQuestions[currentSuggestionSet];
        
        if (tvSuggestion1 != null) tvSuggestion1.setText(questions[0]);
        if (tvSuggestion2 != null) tvSuggestion2.setText(questions[1]);
        if (tvSuggestion3 != null) tvSuggestion3.setText(questions[2]);
        
        // 简单的淡入淡出效果
        if (llSuggestionsContainer != null) {
            llSuggestionsContainer.animate()
                .alpha(0.5f)
                .setDuration(150)
                .withEndAction(() -> {
                    llSuggestionsContainer.animate().alpha(1f).setDuration(150).start();
                })
                .start();
        }
    }

    /**
     * 开始新对话
     */
    private void startNewChat() {
        messages.clear();
        visibleMessages.clear();
        
        // 添加系统人设消息（如果启用）
        if (AiConfig.shouldIncludeSystemPrompt()) {
            ChatMessage systemMessage = new ChatMessage("system", AiConfig.getSystemPrompt());
            messages.add(systemMessage);
        }
        
        // 添加欢迎消息
        String welcomeMessage = "您好！我是小蓝，你的智能健康助手~ 有什么健康问题可以帮助您吗？";
        addMessage("assistant", welcomeMessage);
    }

    /**
     * 发送消息
     */
    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            ToastUtils.show("请输入消息");
            return;
        }
        
        addMessage("user", message);
        etMessage.setText("");
        ToastUtils.show("请稍候...");
        
        // 根据配置选择同步或流式请求
        if (AiConfig.USE_STREAM_MODE && AiConfig.isLanXinModel()) {
            sendStreamRequest();
        } else {
            sendSyncRequest();
        }
    }

    /**
     * 添加消息
     */
    private void addMessage(String role, String content) {
        ChatMessage message = new ChatMessage(role, content);
        messages.add(message);
        if (!"system".equals(role)) {
            visibleMessages.add(message);
            chatAdapter.notifyItemInserted(visibleMessages.size() - 1);
            scrollToBottom();
        }
        manageConversationHistory();
    }

    /**
     * 滚动到底部
     */
    private void scrollToBottom() {
        if (!visibleMessages.isEmpty()) {
            rvChatList.post(() -> rvChatList.smoothScrollToPosition(visibleMessages.size() - 1));
        }
    }

    /**
     * 管理对话历史
     */
    private void manageConversationHistory() {
        if (!AiConfig.isConversationHistoryEnabled() || messages == null) return;
        int maxMessages = AiConfig.MAX_CONVERSATION_ROUNDS * 2;
        boolean hasSystemMessage = !messages.isEmpty() && "system".equals(messages.get(0).getRole());
        if (hasSystemMessage) maxMessages += 1;
        while (messages.size() > maxMessages) {
            int removeIndex = hasSystemMessage ? 1 : 0;
            ChatMessage removedMessage = messages.remove(removeIndex);
            if (!"system".equals(removedMessage.getRole())) {
                for (int i = 0; i < visibleMessages.size(); i++) {
                    if (visibleMessages.get(i) == removedMessage) {
                        visibleMessages.remove(i);
                        chatAdapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 准备API消息
     */
    private List<ChatMessage> prepareMessagesForApi() {
        return new ArrayList<>(messages);
    }

    /**
     * 同步请求
     */
    private void sendSyncRequest() {
        llmUtil.sendSyncRequest(prepareMessagesForApi(), new ChatLlmUtil.LlmCallback() {
            @Override
            public void onAssistantMessage(String content) {
                addMessage("assistant", content);
            }

            @Override
            public void onError(String errorMsg) {
                addMessage("assistant", errorMsg);
            }

            @Override
            public void onComplete() {}

            @Override
            public void onStreamUpdate(String content) {}
        });
    }

    /**
     * 流式请求
     */
    private void sendStreamRequest() {
        final int[] aiVisibleMessageIndex = {-1};
        llmUtil.sendStreamRequest(prepareMessagesForApi(), new ChatLlmUtil.LlmCallback() {
            @Override
            public void onAssistantMessage(String content) {
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

    /**
     * 查找最后一条AI消息索引
     */
    private int findLastAssistantVisibleMessageIndex() {
        for (int i = visibleMessages.size() - 1; i >= 0; i--) {
            if ("assistant".equals(visibleMessages.get(i).getRole())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 更新最后一条AI消息（流式）
     */
    private void updateLastAIMessageStreaming(String content) {
        int lastVisibleAssistantIndex = findLastAssistantVisibleMessageIndex();
        if (lastVisibleAssistantIndex >= 0) {
            ChatMessage lastMessage = visibleMessages.get(lastVisibleAssistantIndex);
            lastMessage.setContent(content);
            updateMessageInFullList(lastMessage, content);
            chatAdapter.notifyItemChanged(lastVisibleAssistantIndex);
            scrollToBottom();
        }
    }

    /**
     * 在完整消息列表中更新对应的消息
     */
    private void updateMessageInFullList(ChatMessage visibleMessage, String content) {
        for (ChatMessage msg : messages) {
            if (msg == visibleMessage) {
                msg.setContent(content);
                break;
            }
        }
    }

    /**
     * 处理返回键
     * @return true表示已处理，false表示未处理
     */
    public boolean onBackPressed() {
        // 新版本没有Fragment容器切换，直接返回false让Activity处理
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (llmUtil != null) {
            llmUtil.shutdown();
        }
    }
}

