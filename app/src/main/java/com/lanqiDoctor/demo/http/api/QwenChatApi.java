package com.lanqiDoctor.demo.http.api;

import com.hjq.http.config.IRequestApi;
import com.lanqiDoctor.demo.aop.Log;
import com.lanqiDoctor.demo.config.AiConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;
import okhttp3.*;

/**
 * 阿里通义千问API适配器 - 兼容现有聊天框架
 */
public final class QwenChatApi implements IRequestApi {

    private String model = "qwen-turbo";
    private List<ChatMessage> messages;
    private int max_tokens = 1000;
    private double temperature = 0.9;
    private boolean stream = false;

    private static final Gson gson = new Gson();
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    @Override
    public String getApi() {
        return "qwen/chat/completions"; // 标识这是通义千问API
    }

    /**
     * 执行同步聊天请求（支持多轮对话和系统人设）
     */
    @Log("QwenChatApi")
    public Bean executeSyncChat() {
        try {
            String apiKey = AiConfig.getQwenApiKey();
            if (apiKey == null || apiKey.trim().isEmpty() || "your-qwen-api-key-here".equals(apiKey)) {
                throw new RuntimeException("通义千问API Key未配置，请在AiConfig中设置QWEN_API_KEY");
            }

            if (messages == null || messages.isEmpty()) {
                throw new RuntimeException("消息列表为空");
            }

            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody();
            String jsonBody = gson.toJson(requestBody);

            // 构建HTTP请求
            Request request = new Request.Builder()
                    .url(AiConfig.QWEN_API_BASE_URL)
                    .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // 执行请求
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("HTTP请求失败: " + response.code() + " " + response.message());
                }

                String responseBody = response.body().string();
                return parseResponse(responseBody);
            }

        } catch (Exception e) {
            System.err.println("QwenChatApi: 执行聊天请求失败 - " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse(e.getMessage());
        }
    }

    /**
     * 执行流式聊天请求（支持多轮对话和系统人设）
     */
    @Log("QwenChatApi")
    public void executeStreamChat(StreamChatCallback callback) {
        try {
            String apiKey = AiConfig.getQwenApiKey();
            if (apiKey == null || apiKey.trim().isEmpty() || "your-qwen-api-key-here".equals(apiKey)) {
                throw new RuntimeException("通义千问API Key未配置，请在AiConfig中设置QWEN_API_KEY");
            }

            if (messages == null || messages.isEmpty()) {
                throw new RuntimeException("消息列表为空");
            }

            // 构建流式请求体
            Map<String, Object> requestBody = buildRequestBody();
            requestBody.put("stream", true);
            requestBody.put("incremental_output", true);
            String jsonBody = gson.toJson(requestBody);

            // 构建HTTP请求
            Request request = new Request.Builder()
                    .url(AiConfig.QWEN_API_BASE_URL)
                    .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "text/event-stream")
                    .build();

            // 执行流式请求
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("流式请求失败: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError("HTTP请求失败: " + response.code() + " " + response.message());
                        return;
                    }

                    StringBuilder fullResponse = new StringBuilder();
                    try (ResponseBody body = response.body()) {
                        String line;
                        java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(body.byteStream(), "UTF-8")
                        );
                        
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6);
                                if ("[DONE]".equals(data)) {
                                    // 流结束
                                    Bean completeBean = createStreamResponse(fullResponse.toString());
                                    callback.onStreamComplete(completeBean);
                                    break;
                                }
                                
                                try {
                                    // 解析流式数据
                                    Type type = new TypeToken<Map<String, Object>>(){}.getType();
                                    Map<String, Object> streamData = gson.fromJson(data, type);
                                    
                                    String deltaContent = extractStreamContent(streamData);
                                    if (deltaContent != null && !deltaContent.isEmpty()) {
                                        fullResponse.append(deltaContent);
                                        Bean deltaBean = createStreamResponse(deltaContent);
                                        callback.onStreamMessage(deltaBean);
                                    }
                                } catch (Exception e) {
                                    // 忽略解析错误，继续处理下一行
                                }
                            }
                        }
                    }
                }
            });

        } catch (Exception e) {
            System.err.println("QwenChatApi: 执行流式聊天请求失败 - " + e.getMessage());
            e.printStackTrace();
            callback.onStreamError(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody() {
        Map<String, Object> requestBody = new HashMap<>();
        
        // 模型参数
        Map<String, Object> model = new HashMap<>();
        model.put("model", this.model);
        requestBody.put("model", this.model);

        // 消息转换
        List<Map<String, String>> qwenMessages = convertToQwenFormat(messages);
        
        // 如果需要系统人设，添加系统消息
        if (AiConfig.shouldIncludeSystemPrompt()) {
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", AiConfig.getSystemPrompt());
            qwenMessages.add(0, systemMessage);
        }
        
        requestBody.put("input", createInputMap(qwenMessages));
        
        // 参数设置
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("max_tokens", max_tokens);
        parameters.put("temperature", temperature);
        parameters.put("top_p", AiConfig.TOP_P);
        parameters.put("repetition_penalty", AiConfig.REPETITION_PENALTY);
        parameters.put("result_format", "message");
        
        requestBody.put("parameters", parameters);
        
        return requestBody;
    }

    /**
     * 创建输入映射
     */
    private Map<String, Object> createInputMap(List<Map<String, String>> messages) {
        Map<String, Object> input = new HashMap<>();
        input.put("messages", messages);
        return input;
    }

    /**
     * 将ChatMessage列表转换为通义千问API格式
     */
    private List<Map<String, String>> convertToQwenFormat(List<ChatMessage> messages) {
        List<Map<String, String>> qwenMessages = new ArrayList<>();
        
        for (ChatMessage msg : messages) {
            // 跳过系统消息，因为会在buildRequestBody中单独处理
            if (!"system".equals(msg.getRole())) {
                Map<String, String> qwenMsg = new HashMap<>();
                qwenMsg.put("role", msg.getRole());
                qwenMsg.put("content", msg.getContent());
                qwenMessages.add(qwenMsg);
            }
        }
        
        return qwenMessages;
    }

    /**
     * 解析通义千问API响应为标准Bean格式
     */
    private Bean parseResponse(String response) {
        try {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> responseMap = gson.fromJson(response, type);

            Bean bean = new Bean();
            Bean.Choice choice = new Bean.Choice();

            // 检查响应状态
            if (responseMap.containsKey("code")) {
                String code = responseMap.get("code").toString();
                if (!"200".equals(code)) {
                    String message = responseMap.containsKey("message") ? 
                        responseMap.get("message").toString() : "API调用失败";
                    return createErrorResponse("API错误 " + code + ": " + message);
                }
            }

            // 提取响应内容
            String content = extractContentFromResponse(responseMap, response);
            
            ChatMessage message = new ChatMessage("assistant", content);
            choice.message = message;
            bean.choices = new ArrayList<>();
            bean.choices.add(choice);

            // 设置data字段
            Bean.Data data = new Bean.Data();
            data.setContent(content);
            bean.setData(data);

            return bean;
        } catch (Exception e) {
            System.err.println("QwenChatApi: 解析响应失败 - " + e.getMessage());
            return createSimpleResponse(response);
        }
    }

    /**
     * 从响应中提取内容
     */
    private String extractContentFromResponse(Map<String, Object> responseMap, String fallback) {
        try {
            // 根据通义千问API文档，检查output.text字段
            if (responseMap.containsKey("output")) {
                Object output = responseMap.get("output");
                if (output instanceof Map) {
                    Map<String, Object> outputMap = (Map<String, Object>) output;
                    if (outputMap.containsKey("text")) {
                        return outputMap.get("text").toString();
                    }
                    // 备用字段：choices[0].message.content
                    if (outputMap.containsKey("choices")) {
                        Object choices = outputMap.get("choices");
                        if (choices instanceof List && !((List<?>) choices).isEmpty()) {
                            Object firstChoice = ((List<?>) choices).get(0);
                            if (firstChoice instanceof Map) {
                                Map<String, Object> choiceMap = (Map<String, Object>) firstChoice;
                                if (choiceMap.containsKey("message")) {
                                    Object message = choiceMap.get("message");
                                    if (message instanceof Map) {
                                        Map<String, Object> messageMap = (Map<String, Object>) message;
                                        if (messageMap.containsKey("content")) {
                                            return messageMap.get("content").toString();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 备用字段检查
            if (responseMap.containsKey("text")) {
                return responseMap.get("text").toString();
            }
            if (responseMap.containsKey("content")) {
                return responseMap.get("content").toString();
            }
        } catch (Exception e) {
            System.err.println("QwenChatApi: 提取内容失败 - " + e.getMessage());
        }
        return fallback;
    }

    /**
     * 从流式数据中提取内容
     */
    private String extractStreamContent(Map<String, Object> streamData) {
        try {
            if (streamData.containsKey("output")) {
                Object output = streamData.get("output");
                if (output instanceof Map) {
                    Map<String, Object> outputMap = (Map<String, Object>) output;
                    if (outputMap.containsKey("text")) {
                        return outputMap.get("text").toString();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("QwenChatApi: 提取流式内容失败 - " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 创建简单响应Bean
     */
    private Bean createSimpleResponse(String content) {
        Bean bean = new Bean();
        Bean.Choice choice = new Bean.Choice();
        choice.message = new ChatMessage("assistant", content);
        bean.choices = new ArrayList<>();
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
        bean.choices = new ArrayList<>();
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
        bean.choices = new ArrayList<>();
        bean.choices.add(choice);
        return bean;
    }

    // Getter和Setter方法
    public QwenChatApi setModel(String model) {
        this.model = model;
        return this;
    }

    public QwenChatApi setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        return this;
    }

    public QwenChatApi setMaxTokens(int maxTokens) {
        this.max_tokens = maxTokens;
        return this;
    }

    public QwenChatApi setTemperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public QwenChatApi setStream(boolean stream) {
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
        void onError(String error);
    }

    /**
     * 响应Bean类 - 兼容现有AiChatApi.Bean
     */
    public static final class Bean {
        private List<Choice> choices;
        private Data data;

        public List<Choice> getChoices() {
            return choices;
        }
        
        public Data getData() {
            return data;
        }
        
        public void setData(Data data) {
            this.data = data;
        }

        public static class Data {
            private String content;
            
            public String getContent() { 
                return content; 
            }
            
            public void setContent(String content) { 
                this.content = content; 
            }
        }

        public static class Choice {
            private ChatMessage message;
            private ChatMessage delta;
            
            public ChatMessage getMessage() { 
                return message; 
            }
            
            public ChatMessage getDelta() { 
                return delta; 
            }
        }
    }
}