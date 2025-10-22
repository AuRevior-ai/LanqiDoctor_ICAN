package com.lanqiDoctor.demo.http.api;

import com.hjq.http.config.IRequestApi;
import java.util.List;

/**
 * AI聊天API
 */
public final class AiChatApi implements IRequestApi {

    @Override
    public String getApi() {
        return "chat/completions";
    }

    private String model;
    private List<ChatMessage> messages;
    private int max_tokens;
    private double temperature;
    private boolean stream;
    

    public AiChatApi setModel(String model) {
        this.model = model;
        return this;
    }

    public AiChatApi setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        return this;
    }

    public AiChatApi setMaxTokens(int maxTokens) {
        this.max_tokens = maxTokens;
        return this;
    }

    public AiChatApi setTemperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public AiChatApi setStream(boolean stream) {
        this.stream = stream;
        return this;
    }

    public static final class Bean {
        private List<Choice> choices;
        private Data data;

        public Data getData() {
            return data;
        }
        public List<Choice> getChoices() {
            return choices;
        }

        public static class Data {
            private String content;
            public String getContent() {
                return content;
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