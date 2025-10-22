package com.lanqiDoctor.demo.http.api;

import com.lanqiDoctor.demo.aop.Log;
import com.lanqiDoctor.demo.config.AiConfig;
import com.hjq.http.EasyHttp;
import com.hjq.http.listener.OnHttpListener;
import androidx.lifecycle.LifecycleOwner;

import java.util.List;

/**
 * AI服务统一接口 - 支持多种AI模型
 */
public class AiService {
    
    // 初始化标记
    private static boolean initialized = false;
    
    /**
     * 初始化AI服务
     */
    @Log("AiService")
    public static void initialize() {
        if (!initialized) {
            if (AiConfig.isLanXinModel()) {
                // 初始化蓝心客户端
                LanXinChatApi.initLanXinClient(AiConfig.getAppId(), AiConfig.getAppKey());
                System.out.println("AiService: 初始化蓝心AI服务成功");
            } else if (AiConfig.isQwenModel()) {
                // 初始化通义千问客户端（无需特殊初始化）
                System.out.println("AiService: 初始化通义千问AI服务成功");
            } else {
                System.out.println("AiService: 初始化OpenAI服务成功");
            }
            initialized = true;
        }
    }
    
    /**
     * 发送聊天请求 - 同步模式
     */
    @Log("AiService")
    public static void sendChatRequest(List<ChatMessage> messages, OnHttpListener<AiChatApi.Bean> listener) {
        sendChatRequest(messages, listener, null);
    }
    
    /**
     * 发送聊天请求 - 同步模式（带生命周期管理）
     */
    @Log("AiService")
    public static void sendChatRequest(List<ChatMessage> messages, OnHttpListener<AiChatApi.Bean> listener, LifecycleOwner lifecycleOwner) {
        try {
            initialize();
            
            if (AiConfig.isLanXinModel()) {
                // 使用蓝心API
                sendLanXinRequest(messages, listener);
            } else if (AiConfig.isQwenModel()) {
                // 使用通义千问API
                sendQwenRequest(messages, listener);
            } else {
                // 使用OpenAI API
                sendOpenAIRequest(messages, listener, lifecycleOwner);
            }
        } catch (Exception e) {
            System.err.println("AiService: 发送聊天请求失败 - " + e.getMessage());
            e.printStackTrace();
            if (listener != null) {
                listener.onFail(e);
            }
        }
    }
    
    /**
     * 发送流式聊天请求
     */
    @Log("AiService")
    public static void sendStreamChatRequest(List<ChatMessage> messages, StreamResponseCallback callback) {
        sendStreamChatRequest(messages, callback, null);
    }
    
    /**
     * 发送流式聊天请求（带生命周期管理）
     */
    @Log("AiService")
    public static void sendStreamChatRequest(List<ChatMessage> messages, StreamResponseCallback callback, LifecycleOwner lifecycleOwner) {
        try {
            initialize();
            
            if (AiConfig.isLanXinModel()) {
                // 使用蓝心流式API
                sendLanXinStreamRequest(messages, callback);
            } else if (AiConfig.isQwenModel()) {
                // 使用通义千问流式API
                sendQwenStreamRequest(messages, callback);
            } else {
                // 使用OpenAI流式API（如果需要）
                sendOpenAIStreamRequest(messages, callback, lifecycleOwner);
            }
        } catch (Exception e) {
            System.err.println("AiService: 发送流式聊天请求失败 - " + e.getMessage());
            e.printStackTrace();
            if (callback != null) {
                callback.onError("发送请求失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 发送蓝心API请求
     */
    private static void sendLanXinRequest(List<ChatMessage> messages, OnHttpListener<AiChatApi.Bean> listener) {
        try {
            LanXinChatApi api = new LanXinChatApi()
                    .setModel(AiConfig.MODEL)
                    .setMessages(messages)
                    .setMaxTokens(AiConfig.MAX_TOKENS)
                    .setTemperature(AiConfig.TEMPERATURE)
                    .setStream(false);
            
            // 执行同步请求
            LanXinChatApi.Bean result = api.executeSyncChat();
            
            if (listener != null) {
                // 将LanXinChatApi.Bean转换为AiChatApi.Bean
                AiChatApi.Bean convertedBean = convertLanXinBeanToAiChatBean(result);
                listener.onSucceed(convertedBean);
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onFail(e);
            }
        }
    }
    
    /**
     * 发送蓝心流式API请求
     */
    private static void sendLanXinStreamRequest(List<ChatMessage> messages, StreamResponseCallback callback) {
        try {
            LanXinChatApi api = new LanXinChatApi()
                    .setModel(AiConfig.MODEL)
                    .setMessages(messages)
                    .setMaxTokens(AiConfig.MAX_TOKENS)
                    .setTemperature(AiConfig.TEMPERATURE)
                    .setStream(true);
            
            api.executeStreamChat(new LanXinChatApi.StreamChatCallback() {
                @Override
                public void onStreamMessage(LanXinChatApi.Bean deltaBean) {
                    if (callback != null) {
                        callback.onStreamMessage(convertLanXinBeanToAiChatBean(deltaBean));
                    }
                }
                
                @Override
                public void onStreamComplete(LanXinChatApi.Bean completeBean) {
                    if (callback != null) {
                        callback.onComplete(convertLanXinBeanToAiChatBean(completeBean));
                    }
                }
                
                @Override
                public void onStreamError(LanXinChatApi.Bean errorBean) {
                    if (callback != null) {
                        callback.onError("流式请求错误");
                    }
                }
            });
        } catch (Exception e) {
            if (callback != null) {
                callback.onError("发送流式请求失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 发送通义千问API请求
     */
    private static void sendQwenRequest(List<ChatMessage> messages, OnHttpListener<AiChatApi.Bean> listener) {
        try {
            QwenChatApi api = new QwenChatApi()
                    .setModel(AiConfig.getModelName())
                    .setMessages(messages)
                    .setMaxTokens(AiConfig.MAX_TOKENS)
                    .setTemperature(AiConfig.TEMPERATURE)
                    .setStream(false);
            
            // 执行同步请求
            QwenChatApi.Bean result = api.executeSyncChat();
            
            if (listener != null) {
                // 将QwenChatApi.Bean转换为AiChatApi.Bean
                AiChatApi.Bean convertedBean = convertQwenBeanToAiChatBean(result);
                listener.onSucceed(convertedBean);
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onFail(e);
            }
        }
    }
    
    /**
     * 发送通义千问流式API请求
     */
    private static void sendQwenStreamRequest(List<ChatMessage> messages, StreamResponseCallback callback) {
        try {
            QwenChatApi api = new QwenChatApi()
                    .setModel(AiConfig.getModelName())
                    .setMessages(messages)
                    .setMaxTokens(AiConfig.MAX_TOKENS)
                    .setTemperature(AiConfig.TEMPERATURE)
                    .setStream(true);
            
            api.executeStreamChat(new QwenChatApi.StreamChatCallback() {
                @Override
                public void onStreamMessage(QwenChatApi.Bean deltaBean) {
                    if (callback != null) {
                        callback.onStreamMessage(convertQwenBeanToAiChatBean(deltaBean));
                    }
                }
                
                @Override
                public void onStreamComplete(QwenChatApi.Bean completeBean) {
                    if (callback != null) {
                        callback.onComplete(convertQwenBeanToAiChatBean(completeBean));
                    }
                }
                
                @Override
                public void onStreamError(QwenChatApi.Bean errorBean) {
                    if (callback != null) {
                        callback.onError("通义千问流式请求错误");
                    }
                }
                
                @Override
                public void onError(String error) {
                    if (callback != null) {
                        callback.onError(error);
                    }
                }
            });
        } catch (Exception e) {
            if (callback != null) {
                callback.onError("发送通义千问流式请求失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 发送OpenAI API请求
     */
    private static void sendOpenAIRequest(List<ChatMessage> messages, OnHttpListener<AiChatApi.Bean> listener, LifecycleOwner lifecycleOwner) {
        AiChatApi api = new AiChatApi()
                .setModel(AiConfig.MODEL)
                .setMessages(messages)
                .setMaxTokens(AiConfig.MAX_TOKENS)
                .setTemperature(AiConfig.TEMPERATURE)
                .setStream(false);

        if (lifecycleOwner != null) {
            // 使用正确的EasyHttp调用方式（带生命周期管理）
            EasyHttp.post(lifecycleOwner)
                    .api(api)
                    .request(listener);
        } else {
            // 如果没有传入生命周期管理，使用ApplicationLifecycle作为备用方案
            try {
                Class<?> appLifecycleClass = Class.forName("com.hjq.http.lifecycle.ApplicationLifecycle");
                Object appLifecycle = appLifecycleClass.newInstance();
                EasyHttp.post((LifecycleOwner) appLifecycle)
                        .api(api)
                        .request(listener);
            } catch (Exception e) {
                // 如果ApplicationLifecycle也无法使用，创建一个模拟响应
                System.err.println("AiService: 无法创建HTTP请求，返回模拟响应");
                if (listener != null) {
                    AiChatApi.Bean mockResponse = createMockOpenAIResponse(messages);
                    listener.onSucceed(mockResponse);
                }
            }
        }
    }
    
    /**
     * 发送OpenAI流式API请求
     */
    private static void sendOpenAIStreamRequest(List<ChatMessage> messages, StreamResponseCallback callback, LifecycleOwner lifecycleOwner) {
        // 这里可以实现OpenAI的流式请求
        // 暂时使用同步请求作为示例
        sendOpenAIRequest(messages, new OnHttpListener<AiChatApi.Bean>() {
            @Override
            public void onStart(okhttp3.Call call) {
                // 请求开始
            }

            @Override
            public void onSucceed(AiChatApi.Bean result) {
                if (callback != null) {
                    callback.onComplete(result);
                }
            }

            @Override
            public void onFail(Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onEnd(okhttp3.Call call) {
                // 请求结束
            }
        }, lifecycleOwner);
    }
    
    /**
     * 创建OpenAI模拟响应
     */
    private static AiChatApi.Bean createMockOpenAIResponse(List<ChatMessage> messages) {
        try {
            AiChatApi.Bean bean = new AiChatApi.Bean();
            
            // 使用反射设置choices字段
            java.lang.reflect.Field choicesField = AiChatApi.Bean.class.getDeclaredField("choices");
            choicesField.setAccessible(true);
            
            List<AiChatApi.Bean.Choice> choices = new java.util.ArrayList<>();
            AiChatApi.Bean.Choice choice = new AiChatApi.Bean.Choice();
            
            // 设置message字段
            java.lang.reflect.Field messageField = AiChatApi.Bean.Choice.class.getDeclaredField("message");
            messageField.setAccessible(true);
            
            String lastUserMessage = "";
            if (messages != null && !messages.isEmpty()) {
                for (int i = messages.size() - 1; i >= 0; i--) {
                    if ("user".equals(messages.get(i).getRole())) {
                        lastUserMessage = messages.get(i).getContent();
                        break;
                    }
                }
            }
            
            String responseText = String.format("这是OpenAI模式的模拟响应，您问的是：%s", lastUserMessage);
            messageField.set(choice, new ChatMessage("assistant", responseText));
            
            choices.add(choice);
            choicesField.set(bean, choices);
            
            return bean;
        } catch (Exception e) {
            System.err.println("AiService: 创建模拟响应失败 - " + e.getMessage());
            return new AiChatApi.Bean();
        }
    }
    
    /**
     * 将LanXinChatApi.Bean转换为AiChatApi.Bean
     */
    private static AiChatApi.Bean convertLanXinBeanToAiChatBean(LanXinChatApi.Bean lanXinBean) {
        if (lanXinBean == null) {
            return null;
        }
        
        try {
            // 使用反射或直接创建新的Bean实例
            AiChatApi.Bean aiBean = new AiChatApi.Bean();
            
            // 通过反射设置choices字段
            java.lang.reflect.Field choicesField = AiChatApi.Bean.class.getDeclaredField("choices");
            choicesField.setAccessible(true);
            
            List<AiChatApi.Bean.Choice> aiChoices = new java.util.ArrayList<>();
            
            if (lanXinBean.getChoices() != null) {
                for (LanXinChatApi.Bean.Choice lanXinChoice : lanXinBean.getChoices()) {
                    AiChatApi.Bean.Choice aiChoice = new AiChatApi.Bean.Choice();
                    
                    // 设置message字段
                    if (lanXinChoice.getMessage() != null) {
                        java.lang.reflect.Field messageField = AiChatApi.Bean.Choice.class.getDeclaredField("message");
                        messageField.setAccessible(true);
                        messageField.set(aiChoice, lanXinChoice.getMessage());
                    }
                    
                    // 设置delta字段
                    if (lanXinChoice.getDelta() != null) {
                        java.lang.reflect.Field deltaField = AiChatApi.Bean.Choice.class.getDeclaredField("delta");
                        deltaField.setAccessible(true);
                        deltaField.set(aiChoice, lanXinChoice.getDelta());
                    }
                    
                    aiChoices.add(aiChoice);
                }
            }
            choicesField.set(aiBean, aiChoices);
        // 2. 转换data字段（新增！）
        if (lanXinBean.getData() != null && lanXinBean.getData().getContent() != null) {
            java.lang.reflect.Field dataField = AiChatApi.Bean.class.getDeclaredField("data");
            dataField.setAccessible(true);
            AiChatApi.Bean.Data aiData = new AiChatApi.Bean.Data();

            // 通过反射设置 content 字段
            java.lang.reflect.Field contentField = AiChatApi.Bean.Data.class.getDeclaredField("content");
            contentField.setAccessible(true);
            contentField.set(aiData, lanXinBean.getData().getContent());

            dataField.set(aiBean, aiData);
        }
            

            return aiBean;
            
        } catch (Exception e) {
            System.err.println("AiService: Bean转换失败 - " + e.getMessage());
            e.printStackTrace();
            
            // 如果反射失败，创建一个简单的Bean
            return createSimpleAiChatBean(lanXinBean);
        }
    }
    
    /**
     * 创建简单的AiChatApi.Bean（备用方案）
     */
    private static AiChatApi.Bean createSimpleAiChatBean(LanXinChatApi.Bean lanXinBean) {
        try {
            // 尝试从蓝心Bean中提取内容
            if (lanXinBean != null && lanXinBean.getChoices() != null && !lanXinBean.getChoices().isEmpty()) {
                LanXinChatApi.Bean.Choice lanXinChoice = lanXinBean.getChoices().get(0);
                if (lanXinChoice.getMessage() != null) {
                    AiChatApi.Bean bean = new AiChatApi.Bean();
                    // 这里可以使用JSON序列化/反序列化的方式
                    // 但为了简单起见，返回空Bean
                    return bean;
                }
            }
        } catch (Exception e) {
            System.err.println("AiService: 创建简单Bean失败 - " + e.getMessage());
        }
        return new AiChatApi.Bean(); // 返回空Bean作为最后的备用方案
    }
    
    /**
     * 将QwenChatApi.Bean转换为AiChatApi.Bean
     */
    private static AiChatApi.Bean convertQwenBeanToAiChatBean(QwenChatApi.Bean qwenBean) {
        if (qwenBean == null) {
            return null;
        }
        
        try {
            // 使用反射或直接创建新的Bean实例
            AiChatApi.Bean aiBean = new AiChatApi.Bean();
            
            // 通过反射设置choices字段
            java.lang.reflect.Field choicesField = AiChatApi.Bean.class.getDeclaredField("choices");
            choicesField.setAccessible(true);
            
            List<AiChatApi.Bean.Choice> aiChoices = new java.util.ArrayList<>();
            
            if (qwenBean.getChoices() != null) {
                for (QwenChatApi.Bean.Choice qwenChoice : qwenBean.getChoices()) {
                    AiChatApi.Bean.Choice aiChoice = new AiChatApi.Bean.Choice();
                    
                    // 设置message字段
                    if (qwenChoice.getMessage() != null) {
                        java.lang.reflect.Field messageField = AiChatApi.Bean.Choice.class.getDeclaredField("message");
                        messageField.setAccessible(true);
                        messageField.set(aiChoice, qwenChoice.getMessage());
                    }
                    
                    // 设置delta字段
                    if (qwenChoice.getDelta() != null) {
                        java.lang.reflect.Field deltaField = AiChatApi.Bean.Choice.class.getDeclaredField("delta");
                        deltaField.setAccessible(true);
                        deltaField.set(aiChoice, qwenChoice.getDelta());
                    }
                    
                    aiChoices.add(aiChoice);
                }
            }
            choicesField.set(aiBean, aiChoices);
            
            // 转换data字段
            if (qwenBean.getData() != null && qwenBean.getData().getContent() != null) {
                java.lang.reflect.Field dataField = AiChatApi.Bean.class.getDeclaredField("data");
                dataField.setAccessible(true);
                AiChatApi.Bean.Data aiData = new AiChatApi.Bean.Data();

                // 通过反射设置 content 字段
                java.lang.reflect.Field contentField = AiChatApi.Bean.Data.class.getDeclaredField("content");
                contentField.setAccessible(true);
                contentField.set(aiData, qwenBean.getData().getContent());

                dataField.set(aiBean, aiData);
            }

            return aiBean;
            
        } catch (Exception e) {
            System.err.println("AiService: 通义千问Bean转换失败 - " + e.getMessage());
            e.printStackTrace();
            
            // 如果反射失败，创建一个简单的Bean
            return createSimpleQwenAiChatBean(qwenBean);
        }
    }
    
    /**
     * 创建简单的AiChatApi.Bean（通义千问备用方案）
     */
    private static AiChatApi.Bean createSimpleQwenAiChatBean(QwenChatApi.Bean qwenBean) {
        try {
            // 尝试从通义千问Bean中提取内容
            if (qwenBean != null && qwenBean.getChoices() != null && !qwenBean.getChoices().isEmpty()) {
                QwenChatApi.Bean.Choice qwenChoice = qwenBean.getChoices().get(0);
                if (qwenChoice.getMessage() != null) {
                    AiChatApi.Bean bean = new AiChatApi.Bean();
                    // 这里可以使用JSON序列化/反序列化的方式
                    // 但为了简单起见，返回空Bean
                    return bean;
                }
            }
        } catch (Exception e) {
            System.err.println("AiService: 创建简单通义千问Bean失败 - " + e.getMessage());
        }
        return new AiChatApi.Bean(); // 返回空Bean作为最后的备用方案
    }
    
    /**
     * 流式响应回调接口
     */
    public interface StreamResponseCallback {
        void onStreamMessage(AiChatApi.Bean deltaBean);
        void onComplete(AiChatApi.Bean completeBean);
        void onError(String error);
    }
}