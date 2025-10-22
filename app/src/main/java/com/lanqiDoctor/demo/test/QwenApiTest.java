package com.lanqiDoctor.demo.test;

import com.lanqiDoctor.demo.config.AiConfig;
import com.lanqiDoctor.demo.http.api.ChatMessage;
import com.lanqiDoctor.demo.http.api.QwenChatApi;
import java.util.ArrayList;
import java.util.List;

/**
 * 通义千问API测试类
 * 
 * 使用说明：
 * 1. 确保在AiConfig中配置了正确的QWEN_API_KEY
 * 2. 在Android项目中运行此测试
 * 3. 检查日志输出查看API调用结果
 */
public class QwenApiTest {
    
    /**
     * 测试基本聊天功能
     */
    public static void testBasicChat() {
        System.out.println("=== 开始测试通义千问基本聊天功能 ===");
        
        try {
            // 检查API Key配置
            if ("your-qwen-api-key-here".equals(AiConfig.getQwenApiKey())) {
                System.err.println("❌ 错误：请先在AiConfig中配置正确的QWEN_API_KEY");
                return;
            }
            
            // 创建测试消息
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("user", "你好，我想了解一下高血压的症状"));
            
            // 创建API实例
            QwenChatApi api = new QwenChatApi()
                    .setModel("qwen-turbo")
                    .setMessages(messages)
                    .setMaxTokens(1000)
                    .setTemperature(0.7);
            
            // 执行同步请求
            QwenChatApi.Bean result = api.executeSyncChat();
            
            if (result != null && result.getChoices() != null && !result.getChoices().isEmpty()) {
                String response = result.getChoices().get(0).getMessage().getContent();
                System.out.println("✅ API调用成功！");
                System.out.println("📝 AI响应：" + response);
            } else {
                System.err.println("❌ API响应异常：响应为空");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 测试失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 测试结束 ===");
    }
    
    /**
     * 测试多轮对话功能
     */
    public static void testMultiRoundChat() {
        System.out.println("=== 开始测试通义千问多轮对话功能 ===");
        
        try {
            // 检查API Key配置
            if ("your-qwen-api-key-here".equals(AiConfig.getQwenApiKey())) {
                System.err.println("❌ 错误：请先在AiConfig中配置正确的QWEN_API_KEY");
                return;
            }
            
            // 创建多轮对话消息
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("user", "我今年40岁，最近感觉头晕"));
            messages.add(new ChatMessage("assistant", "头晕可能有多种原因，比如血压问题、颈椎问题、内耳平衡问题等。请问您还有其他症状吗？比如恶心、视力模糊、耳鸣等？"));
            messages.add(new ChatMessage("user", "有一些恶心，血压测量是150/95"));
            
            // 创建API实例
            QwenChatApi api = new QwenChatApi()
                    .setModel("qwen-turbo")
                    .setMessages(messages)
                    .setMaxTokens(1000)
                    .setTemperature(0.7);
            
            // 执行同步请求
            QwenChatApi.Bean result = api.executeSyncChat();
            
            if (result != null && result.getChoices() != null && !result.getChoices().isEmpty()) {
                String response = result.getChoices().get(0).getMessage().getContent();
                System.out.println("✅ 多轮对话API调用成功！");
                System.out.println("📝 AI响应：" + response);
            } else {
                System.err.println("❌ 多轮对话API响应异常：响应为空");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 多轮对话测试失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 多轮对话测试结束 ===");
    }
    
    /**
     * 测试流式响应功能
     */
    public static void testStreamChat() {
        System.out.println("=== 开始测试通义千问流式响应功能 ===");
        
        try {
            // 检查API Key配置
            if ("your-qwen-api-key-here".equals(AiConfig.getQwenApiKey())) {
                System.err.println("❌ 错误：请先在AiConfig中配置正确的QWEN_API_KEY");
                return;
            }
            
            // 创建测试消息
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("user", "请介绍一下糖尿病的预防方法"));
            
            // 创建API实例
            QwenChatApi api = new QwenChatApi()
                    .setModel("qwen-turbo")
                    .setMessages(messages)
                    .setMaxTokens(1000)
                    .setTemperature(0.7);
            
            // 执行流式请求
            final StringBuilder fullResponse = new StringBuilder();
            
            api.executeStreamChat(new QwenChatApi.StreamChatCallback() {
                @Override
                public void onStreamMessage(QwenChatApi.Bean deltaBean) {
                    if (deltaBean != null && deltaBean.getChoices() != null && !deltaBean.getChoices().isEmpty()) {
                        String deltaContent = deltaBean.getChoices().get(0).getDelta().getContent();
                        if (deltaContent != null) {
                            fullResponse.append(deltaContent);
                            System.out.print(deltaContent); // 实时输出
                        }
                    }
                }
                
                @Override
                public void onStreamComplete(QwenChatApi.Bean completeBean) {
                    System.out.println("\n✅ 流式响应完成！");
                    System.out.println("📝 完整响应：" + fullResponse.toString());
                }
                
                @Override
                public void onStreamError(QwenChatApi.Bean errorBean) {
                    System.err.println("❌ 流式响应错误");
                }

                @Override
                public void onError(String error) {
                    System.err.println("❌ 流式请求失败: " + error);
                }
            });
            
            // 等待流式响应完成（实际使用中不需要这个等待）
            Thread.sleep(5000);
            
        } catch (Exception e) {
            System.err.println("❌ 流式响应测试失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 流式响应测试结束 ===");
    }
    
    /**
     * 测试系统人设功能
     */
    public static void testSystemPrompt() {
        System.out.println("=== 开始测试通义千问系统人设功能 ===");
        
        try {
            // 检查API Key配置
            if ("your-qwen-api-key-here".equals(AiConfig.getQwenApiKey())) {
                System.err.println("❌ 错误：请先在AiConfig中配置正确的QWEN_API_KEY");
                return;
            }
            
            // 测试默认系统人设
            System.out.println("🤖 当前系统人设：" + AiConfig.getSystemPrompt().substring(0, 100) + "...");
            
            // 创建测试消息
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("user", "感冒了要吃什么药？"));
            
            // 创建API实例
            QwenChatApi api = new QwenChatApi()
                    .setModel("qwen-turbo")
                    .setMessages(messages)
                    .setMaxTokens(1000)
                    .setTemperature(0.7);
            
            // 执行同步请求
            QwenChatApi.Bean result = api.executeSyncChat();
            
            if (result != null && result.getChoices() != null && !result.getChoices().isEmpty()) {
                String response = result.getChoices().get(0).getMessage().getContent();
                System.out.println("✅ 系统人设API调用成功！");
                System.out.println("📝 AI响应（带医疗人设）：" + response);
                
                // 检查是否包含医疗相关的专业回复
                if (response.contains("医童") || response.contains("建议") || response.contains("专业")) {
                    System.out.println("✅ 系统人设生效！AI使用了医疗专业角色");
                } else {
                    System.out.println("⚠️ 系统人设可能未完全生效");
                }
            } else {
                System.err.println("❌ 系统人设API响应异常：响应为空");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 系统人设测试失败：" + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 系统人设测试结束 ===");
    }
    
    /**
     * 运行所有测试
     */
    public static void runAllTests() {
        System.out.println("🚀 开始运行通义千问API全套测试...\n");
        
        testBasicChat();
        System.out.println();
        
        testMultiRoundChat();
        System.out.println();
        
        testSystemPrompt();
        System.out.println();
        
        // 注意：流式测试需要异步处理，在Android环境中单独测试
        // testStreamChat();
        
        System.out.println("🎉 所有测试完成！");
    }
}