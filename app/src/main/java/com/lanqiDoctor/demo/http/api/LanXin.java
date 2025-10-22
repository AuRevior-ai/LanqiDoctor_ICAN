package com.lanqiDoctor.demo.http.api;

import com.lanqiDoctor.demo.aop.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.*;
import java.io.IOException;
import java.util.*;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

/**
 * 蓝心大模型API客户端 - 使用OkHttp
 */
public class LanXin {
    private static final String DOMAIN = "api-ai.vivo.com.cn";
    private static final String SYNC_URI = "/vivogpt/completions";
    private static final String STREAM_URI = "/vivogpt/completions/stream";
    private static final String METHOD = "POST";

    private final String appId;
    private final String appKey;
    private final Gson gson;
    private final OkHttpClient httpClient;

    /**
     * 构造函数
     *
     * @param appId AIGC官网分配的app_id
     * @param appKey AIGC官网分配的app_key
     */
    public LanXin(String appId, String appKey) {
        this.appId = appId;
        this.appKey = appKey;
        this.gson = new Gson();
        
        // 创建OkHttp客户端
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 同步调用蓝心大模型API
     *
     * @param prompt 提问内容
     * @return API响应结果
     */
    @Log("LanXin")
    public String syncCompletions(String prompt) {
        try {
            // 生成唯一请求ID和会话ID
            String requestId = UUID.randomUUID().toString();
            String sessionId = UUID.randomUUID().toString();
            System.out.println("LanXin: requestId: " + requestId + ", sessionId: " + sessionId);

            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("requestId", requestId);
            String queryStr = mapToQueryString(params);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("prompt", prompt);
            requestBody.put("model", "vivo-BlueLM-TB-Pro");
            requestBody.put("sessionId", sessionId);

            // 可选：添加超参数
            Map<String, Object> extra = new HashMap<>();
            extra.put("temperature", 0.9);
            extra.put("top_p", 0.7);
            requestBody.put("extra", extra);

            // 生成鉴权头信息
            VivoAuth.Headers authHeaders = VivoAuth.generateAuthHeaders(appId, appKey, METHOD, SYNC_URI, queryStr);

            // 构建URL
            String url = String.format("https://%s%s?%s", DOMAIN, SYNC_URI, queryStr);

            // 创建请求体
            String requestBodyString = gson.toJson(requestBody);
                        // 对于 OkHttp 3.12.13，使用正确的参数顺序
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    requestBodyString
            );

            // 构建请求头
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json");

            // 添加认证头
            for (Map.Entry<String, String> entry : authHeaders.getHeaders().entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }

            Request request = requestBuilder.build();

            long startTime = System.currentTimeMillis();
            Response response = httpClient.newCall(request).execute();
            long endTime = System.currentTimeMillis();

            System.out.println("LanXin: 请求耗时: " + (endTime - startTime) + "ms");

            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                System.out.println("LanXin: Response: " + responseBody);
                response.close();
                return responseBody;
            } else {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                System.err.println("LanXin: Error: " + response.code() + ", " + errorBody);
                response.close();
                return "Error: " + response.code();
            }
        } catch (Exception e) {
            System.err.println("LanXin: 调用蓝心大模型API异常 - " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 流式调用蓝心大模型API
     *
     * @param prompt 提问内容
     * @param callback 流式响应回调接口
     */
    @Log("LanXin")
    public void streamCompletions(String prompt, StreamCallback callback) {
        try {
            // 生成唯一请求ID和会话ID
            String requestId = UUID.randomUUID().toString();
            String sessionId = UUID.randomUUID().toString();
            System.out.println("LanXin: Stream requestId: " + requestId + ", sessionId: " + sessionId);

            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("requestId", requestId);
            String queryStr = mapToQueryString(params);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("prompt", prompt);
            requestBody.put("model", "vivo-BlueLM-TB-Pro");
            requestBody.put("sessionId", sessionId);

            // 生成鉴权头信息
            VivoAuth.Headers authHeaders = VivoAuth.generateAuthHeaders(appId, appKey, METHOD, STREAM_URI, queryStr);

            // 构建URL
            String url = String.format("https://%s%s?%s", DOMAIN, STREAM_URI, queryStr);

            // 创建请求体
            String requestBodyString = gson.toJson(requestBody);
                        // 对于 OkHttp 3.12.13，使用正确的参数顺序
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    requestBodyString
            );

            // 构建请求头
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json");

            // 添加认证头
            for (Map.Entry<String, String> entry : authHeaders.getHeaders().entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }

            Request request = requestBuilder.build();

            // 异步执行流式请求
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.err.println("LanXin: 流式请求失败 - " + e.getMessage());
                    if (callback != null) {
                        callback.onError("Request failed: " + e.getMessage());
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    long startTime = System.currentTimeMillis();
                    boolean firstLine = true;
                    StringBuilder resultBuilder = new StringBuilder();

                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseBody = response.body().string();
                            String[] lines = responseBody.split("\n");
                            
                            for (String line : lines) {
                                if (firstLine) {
                                    long firstLineTime = System.currentTimeMillis();
                                    System.out.println("LanXin: 首字耗时: " + (firstLineTime - startTime) + "ms");
                                    firstLine = false;
                                }

                                if (line.startsWith("data:")) {
                                    String content = line.substring(5).trim();
                                    System.out.println("LanXin: 接收到: " + content);

                                    try {
                                        // 跳过空内容或特殊标记
                                        if (!content.isEmpty() && !content.equals("[DONE]")) {
                                            // 解析JSON
                                            Type type = new TypeToken<Map<String, Object>>(){}.getType();
                                            Map<String, Object> jsonMap = gson.fromJson(content, type);

                                            // 只提取message字段
                                            if (jsonMap.containsKey("message")) {
                                                String messageText = jsonMap.get("message").toString();
                                                resultBuilder.append(messageText);

                                                // 回调通知新的消息片段
                                                if (callback != null) {
                                                    callback.onMessage(messageText);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        System.err.println("LanXin: 解析JSON出错: " + e.getMessage());
                                    }
                                } else if (line.startsWith("event:")) {
                                    System.out.println("LanXin: 事件: " + line);
                                }
                            }

                            long endTime = System.currentTimeMillis();
                            System.out.println("LanXin: 流式请求总耗时: " + (endTime - startTime) + "ms");

                            String result = resultBuilder.toString();
                            System.out.println("LanXin: 结果长度: " + result.length() +
                                ", 前20个字符: " + (result.length() > 20 ? result.substring(0, 20) : result));

                            if (callback != null) {
                                callback.onComplete(result);
                            }
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                            System.err.println("LanXin: Stream Error: " + response.code() + ", " + errorBody);
                            if (callback != null) {
                                callback.onError("HTTP Error: " + response.code());
                            }
                        }
                    } finally {
                        response.close();
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("LanXin: 流式调用蓝心大模型API异常 - " + e.getMessage());
            e.printStackTrace();
            if (callback != null) {
                callback.onError("Exception: " + e.getMessage());
            }
        }
    }

/**
     * 多轮对话调用蓝心大模型API（支持系统人设）
     *
     * @param messages 多轮对话消息列表
     * @param systemPrompt 系统人设（可选）
     * @return API响应结果
     */
    @Log("LanXin")
    public String chatCompletionsWithSystem(List<Map<String, String>> messages, String systemPrompt) {
        try {
            // 生成唯一请求ID和会话ID
            String requestId = UUID.randomUUID().toString();
            String sessionId = UUID.randomUUID().toString();
            System.out.println("LanXin: Multi-chat requestId: " + requestId + ", sessionId: " + sessionId);

            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("requestId", requestId);
            String queryStr = mapToQueryString(params);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messages", messages);
            requestBody.put("model", "vivo-BlueLM-TB-Pro");
            requestBody.put("sessionId", sessionId);
            
            // 添加系统人设
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                requestBody.put("systemPrompt", systemPrompt);
            }
            
            // 添加超参数
            Map<String, Object> extra = new HashMap<>();
            extra.put("temperature", 0.9);
            extra.put("top_p", 0.7);
            extra.put("top_k", 50);
            extra.put("max_new_tokens", 2048);
            extra.put("repetition_penalty", 1.02);
            requestBody.put("extra", extra);

            // 生成鉴权头信息
            VivoAuth.Headers authHeaders = VivoAuth.generateAuthHeaders(appId, appKey, METHOD, SYNC_URI, queryStr);

            // 构建URL
            String url = String.format("https://%s%s?%s", DOMAIN, SYNC_URI, queryStr);

            // 创建请求体
            String requestBodyString = gson.toJson(requestBody);
            System.out.println("LanXin: Request body: " + requestBodyString);
            
                        // 对于 OkHttp 3.12.13，使用正确的参数顺序
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    requestBodyString
            );

            // 构建请求头
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json");

            // 添加认证头
            for (Map.Entry<String, String> entry : authHeaders.getHeaders().entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }

            Request request = requestBuilder.build();
            Response response = httpClient.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                System.out.println("LanXin: Multi-chat Response: " + responseBody);
                response.close();
                return responseBody;
            } else {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                System.err.println("LanXin: Multi-chat Error: " + response.code() + ", " + errorBody);
                response.close();
                return "Error: " + response.code();
            }
        } catch (Exception e) {
            System.err.println("LanXin: 多轮对话调用蓝心大模型API异常 - " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 多轮流式对话调用蓝心大模型API（支持系统人设）
     */
    @Log("LanXin")
    public void streamChatCompletionsWithSystem(List<Map<String, String>> messages, String systemPrompt, StreamCallback callback) {
        try {
            // 生成唯一请求ID和会话ID
            String requestId = UUID.randomUUID().toString();
            String sessionId = UUID.randomUUID().toString();
            System.out.println("LanXin: Multi-stream requestId: " + requestId + ", sessionId: " + sessionId);

            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("requestId", requestId);
            String queryStr = mapToQueryString(params);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messages", messages);
            requestBody.put("model", "vivo-BlueLM-TB-Pro");
            requestBody.put("sessionId", sessionId);
            
            // 添加系统人设
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                requestBody.put("systemPrompt", systemPrompt);
            }
            
            // 添加超参数
            Map<String, Object> extra = new HashMap<>();
            extra.put("temperature", 0.9);
            extra.put("top_p", 0.7);
            extra.put("top_k", 50);
            extra.put("max_new_tokens", 2048);
            extra.put("repetition_penalty", 1.02);
            requestBody.put("extra", extra);

            // 生成鉴权头信息
            VivoAuth.Headers authHeaders = VivoAuth.generateAuthHeaders(appId, appKey, METHOD, STREAM_URI, queryStr);

            // 构建URL
            String url = String.format("https://%s%s?%s", DOMAIN, STREAM_URI, queryStr);

            // 创建请求体
            String requestBodyString = gson.toJson(requestBody);
            System.out.println("LanXin: Stream request body: " + requestBodyString);
            
                        // 对于 OkHttp 3.12.13，使用正确的参数顺序
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    requestBodyString
            );

            // 构建请求头
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/json");

            // 添加认证头
            for (Map.Entry<String, String> entry : authHeaders.getHeaders().entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }

            Request request = requestBuilder.build();

            // 异步执行流式请求
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.err.println("LanXin: 多轮流式请求失败 - " + e.getMessage());
                    if (callback != null) {
                        callback.onError("Request failed: " + e.getMessage());
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // ...existing stream response handling code...
                    // 保持原有的流式响应处理逻辑
                    long startTime = System.currentTimeMillis();
                    boolean firstLine = true;
                    StringBuilder resultBuilder = new StringBuilder();

                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseBody = response.body().string();
                            String[] lines = responseBody.split("\n");
                            
                            for (String line : lines) {
                                if (firstLine) {
                                    long firstLineTime = System.currentTimeMillis();
                                    System.out.println("LanXin: 多轮流式首字耗时: " + (firstLineTime - startTime) + "ms");
                                    firstLine = false;
                                }

                                if (line.startsWith("data:")) {
                                    String content = line.substring(5).trim();
                                    System.out.println("LanXin: 多轮流式接收到: " + content);

                                    try {
                                        // 跳过空内容或特殊标记
                                        if (!content.isEmpty() && !content.equals("[DONE]")) {
                                            // 解析JSON
                                            Type type = new TypeToken<Map<String, Object>>(){}.getType();
                                            Map<String, Object> jsonMap = gson.fromJson(content, type);

                                            // 提取message字段
                                            String messageText = extractMessageFromResponse(jsonMap);
                                            if (messageText != null && !messageText.isEmpty()) {
                                                resultBuilder.append(messageText);

                                                // 回调通知新的消息片段
                                                if (callback != null) {
                                                    callback.onMessage(messageText);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        System.err.println("LanXin: 多轮流式解析JSON出错: " + e.getMessage());
                                    }
                                } else if (line.startsWith("event:")) {
                                    System.out.println("LanXin: 多轮流式事件: " + line);
                                }
                            }

                            long endTime = System.currentTimeMillis();
                            System.out.println("LanXin: 多轮流式请求总耗时: " + (endTime - startTime) + "ms");

                            String result = resultBuilder.toString();
                            System.out.println("LanXin: 多轮流式结果长度: " + result.length());

                            if (callback != null) {
                                callback.onComplete(result);
                            }
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                            System.err.println("LanXin: Multi-stream Error: " + response.code() + ", " + errorBody);
                            if (callback != null) {
                                callback.onError("HTTP Error: " + response.code());
                            }
                        }
                    } finally {
                        response.close();
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("LanXin: 多轮流式调用蓝心大模型API异常 - " + e.getMessage());
            e.printStackTrace();
            if (callback != null) {
                callback.onError("Exception: " + e.getMessage());
            }
        }
    }

    /**
     * 从响应中提取消息内容
     */
    private String extractMessageFromResponse(Map<String, Object> jsonMap) {
        try {
            // 根据蓝心API文档，检查data.content字段
            if (jsonMap.containsKey("data")) {
                Object data = jsonMap.get("data");
                if (data instanceof Map) {
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    if (dataMap.containsKey("content")) {
                        return dataMap.get("content").toString();
                    }
                }
            }
            
            // 备用字段检查
            if (jsonMap.containsKey("content")) {
                return jsonMap.get("content").toString();
            }
            if (jsonMap.containsKey("message")) {
                return jsonMap.get("message").toString();
            }
        } catch (Exception e) {
            System.err.println("LanXin: 提取消息内容失败 - " + e.getMessage());
        }
        return null;
    }

    /**
     * 将Map转换为查询字符串
     */
    private String mapToQueryString(Map<String, Object> map) {
        if (map.isEmpty()) {
            return "";
        }
        StringBuilder queryStringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (queryStringBuilder.length() > 0) {
                queryStringBuilder.append("&");
            }
            queryStringBuilder.append(entry.getKey());
            queryStringBuilder.append("=");
            queryStringBuilder.append(entry.getValue());
        }
        return queryStringBuilder.toString();
    }

    /**
     * 流式响应回调接口
     */
    public interface StreamCallback {
        /**
         * 接收到新的消息片段
         * @param message 消息片段
         */
        void onMessage(String message);

        /**
         * 流式响应完成
         * @param fullResponse 完整响应内容
         */
        void onComplete(String fullResponse);

        /**
         * 发生错误
         * @param error 错误信息
         */
        void onError(String error);
    }
}