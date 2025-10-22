package com.lanqiDoctor.demo.http.api;

import com.hjq.http.config.IRequestApi;
import com.lanqiDoctor.demo.aop.Log;
import com.lanqiDoctor.demo.config.AiConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * 蓝心AI聊天API适配器 - 兼容现有聊天框架
 */
public final class LanXinChatApi implements IRequestApi {

    private String model = "vivo-BlueLM-TB-Pro";
    private List<ChatMessage> messages;
    private int max_tokens = 1000;
    private double temperature = 0.9;
    private boolean stream = false;

    // LanXin客户端实例
    private static LanXin lanXinClient;
    private static final Gson gson = new Gson();

    @Override
    public String getApi() {
        return "lanxin/chat/completions"; // 标识这是蓝心API
    }

    /**
     * 初始化蓝心客户端
     */
    @Log("LanXinChatApi")
    public static void initLanXinClient(String appId, String appKey) {
        lanXinClient = new LanXin(appId, appKey);
        System.out.println("LanXinChatApi: 初始化蓝心客户端成功");
    }

/**
     * 执行同步聊天请求（支持多轮对话和系统人设）
     */
    @Log("LanXinChatApi")
    public Bean executeSyncChat() {
        try {
            if (lanXinClient == null) {
                throw new RuntimeException("LanXin客户端未初始化，请先调用initLanXinClient()");
            }

            String response;
            String systemPrompt = AiConfig.shouldIncludeSystemPrompt() ? AiConfig.getSystemPrompt() : null;
            
            if (messages != null && !messages.isEmpty()) {
                // 转换为蓝心格式并处理多轮对话
                List<Map<String, String>> chatMessages = convertToLanXinFormat(messages);
                response = lanXinClient.chatCompletionsWithSystem(chatMessages, systemPrompt);
            } else {
                throw new RuntimeException("消息列表为空");
            }

            return parseResponse(response);
        } catch (Exception e) {
            System.err.println("LanXinChatApi: 执行聊天请求失败 - " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse(e.getMessage());
        }
    }

    /**
     * 执行流式聊天请求（支持多轮对话和系统人设）
     */
    @Log("LanXinChatApi")
    public void executeStreamChat(StreamChatCallback callback) {
        try {
            if (lanXinClient == null) {
                throw new RuntimeException("LanXin客户端未初始化，请先调用initLanXinClient()");
            }

            if (messages == null || messages.isEmpty()) {
                throw new RuntimeException("消息列表为空");
            }

            String systemPrompt = AiConfig.shouldIncludeSystemPrompt() ? AiConfig.getSystemPrompt() : null;
            List<Map<String, String>> chatMessages = convertToLanXinFormat(messages);
            
            lanXinClient.streamChatCompletionsWithSystem(chatMessages, systemPrompt, new LanXin.StreamCallback() {
                @Override
                public void onMessage(String message) {
                    // 将流式消息转换为Bean格式
                    Bean streamBean = createStreamResponse(message);
                    callback.onStreamMessage(streamBean);
                }

                @Override
                public void onComplete(String fullResponse) {
                    Bean completeBean = parseResponse(fullResponse);
                    callback.onStreamComplete(completeBean);
                }

                @Override
                public void onError(String error) {
                    Bean errorBean = createErrorResponse(error);
                    callback.onStreamError(errorBean);
                }
            });
        } catch (Exception e) {
            System.err.println("LanXinChatApi: 执行流式聊天请求失败 - " + e.getMessage());
            e.printStackTrace();
            Bean errorBean = createErrorResponse(e.getMessage());
            callback.onStreamError(errorBean);
        }
    }

    /**
     * 将ChatMessage列表转换为蓝心API格式（支持系统消息）
     */
    private List<Map<String, String>> convertToLanXinFormat(List<ChatMessage> messages) {
        java.util.List<Map<String, String>> lanXinMessages = new java.util.ArrayList<>();
        
        for (ChatMessage msg : messages) {
            // 跳过系统消息，因为systemPrompt参数会单独处理
            if (!"system".equals(msg.getRole())) {
                Map<String, String> lanXinMsg = new java.util.HashMap<>();
                lanXinMsg.put("role", msg.getRole());
                lanXinMsg.put("content", msg.getContent());
                lanXinMessages.add(lanXinMsg);
            }
        }
        
        return lanXinMessages;
    }

    /**
     * 解析蓝心API响应为标准Bean格式（支持新的响应格式）
     */
    private Bean parseResponse(String response) {
        try {
            if (response.startsWith("Error:")) {
                return createErrorResponse(response);
            }

            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> responseMap = gson.fromJson(response, type);

            Bean bean = new Bean();
            Bean.Choice choice = new Bean.Choice();

            // 1. 先从 data.content 拿健康日报内容
            String dataContent = null;
            if (responseMap.containsKey("data")) {
                Object dataObj = responseMap.get("data");
                if (dataObj instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                    if (dataMap.containsKey("content")) {
                        dataContent = String.valueOf(dataMap.get("content"));
                    }
                }
            }

            // 2. 如果 data.content 为空，再用 extractContentFromResponse 兜底
            String content = dataContent;
            if (content == null || content.trim().isEmpty()) {
                content = extractContentFromResponse(responseMap, response);
            }

            // 3. choices.message.content 用 extractContentFromResponse（兼容旧逻辑）
            ChatMessage message = new ChatMessage("assistant", content);
            choice.message = message;
            bean.choices = new java.util.ArrayList<>();
            bean.choices.add(choice);

            // 4. data.content 一定用 dataContent（健康日报原文）
            Bean.Data data = new Bean.Data();
            data.setContent(dataContent != null ? dataContent : content);
            bean.setData(data);

            return bean;
        } catch (Exception e) {
            System.err.println("LanXinChatApi: 解析响应失败 - " + e.getMessage());
            return createSimpleResponse(response);
        }
    }

    /**
     * 从响应中提取内容（根据蓝心API响应格式）
     */
    private String extractContentFromResponse(Map<String, Object> responseMap, String fallback) {
        try {
            // 检查code是否为成功
            if (responseMap.containsKey("code")) {
                Object code = responseMap.get("code");
                if (!"0".equals(code.toString()) && !Integer.valueOf(0).equals(code)) {
                    // 返回错误信息
                    String msg = responseMap.containsKey("msg") ? responseMap.get("msg").toString() : "API调用失败";
                    return "抱歉，" + msg;
                }
            }
            
            // 根据蓝心API文档，检查data.content字段
            if (responseMap.containsKey("data")) {
                Object data = responseMap.get("data");
                if (data instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    if (dataMap.containsKey("content")) {
                        return dataMap.get("content").toString();
                    }
                }
            }
            
            // 备用字段检查
            if (responseMap.containsKey("content")) {
                return responseMap.get("content").toString();
            }
            if (responseMap.containsKey("message")) {
                return responseMap.get("message").toString();
            }
        } catch (Exception e) {
            System.err.println("LanXinChatApi: 提取内容失败 - " + e.getMessage());
        }
        return fallback;
    }
    
    /**
     * 创建简单响应Bean
     */
    private Bean createSimpleResponse(String content) {
        Bean bean = new Bean();
        Bean.Choice choice = new Bean.Choice();
        choice.message = new ChatMessage("assistant", content);
        bean.choices = new java.util.ArrayList<>();
        bean.choices.add(choice);
        return bean;
    }

    /**
     * 创建流式响应Bean
     */
    private Bean createStreamResponse(String deltaContent) {
        Bean bean = new Bean();
        Bean.Choice choice = new Bean.Choice();
        choice.delta = new ChatMessage("assistant", deltaContent);
        bean.choices = new java.util.ArrayList<>();
        bean.choices.add(choice);
        return bean;
    }

    /**
     * 创建错误响应Bean
     */
    private Bean createErrorResponse(String error) {
        Bean bean = new Bean();
        Bean.Choice choice = new Bean.Choice();
        choice.message = new ChatMessage("assistant", "抱歉，发生了错误：" + error);
        bean.choices = new java.util.ArrayList<>();
        bean.choices.add(choice);
        return bean;
    }

    // Getter和Setter方法
    public LanXinChatApi setModel(String model) {
        this.model = model;
        return this;
    }

    public LanXinChatApi setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        return this;
    }

    public LanXinChatApi setMaxTokens(int maxTokens) {
        this.max_tokens = maxTokens;
        return this;
    }

    public LanXinChatApi setTemperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public LanXinChatApi setStream(boolean stream) {
        this.stream = stream;
        return this;
    }

    /**
     * 流式聊天回调接口
     */
    public interface StreamChatCallback {
        void onStreamMessage(Bean deltaBean);
        void onStreamComplete(Bean completeBean);
        void onStreamError(Bean errorBean);
    }

    /**
     * 响应Bean类 - 兼容现有AiChatApi.Bean
     */
    public static final class Bean {
        private List<Choice> choices;
        private Data data; // 新增

        public List<Choice> getChoices() {
            return choices;
        }
        public Data getData() { // 新增
            return data;
        }
        public void setData(Data data) { // 新增
            this.data = data;
        }

        public static class Data { // 新增
            private String content;
            public String getContent() { return content; }
            public void setContent(String content) { this.content = content; }
        }

        public static class Choice {
            private ChatMessage message;
            private ChatMessage delta;
            public ChatMessage getMessage() { return message; }
            public ChatMessage getDelta() { return delta; }
        }
    }
}