package com.lanqiDoctor.demo.dao.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lanqiDoctor.demo.http.api.ChatMessage;

import java.lang.reflect.Type;
import java.util.List;

/**
 * ChatMessage列表的类型转换器
 */
public class ChatMessageListConverter {
    
    private static final Gson gson = new Gson();
    
    @TypeConverter
    public static String fromChatMessageList(List<ChatMessage> messages) {
        if (messages == null) {
            return null;
        }
        return gson.toJson(messages);
    }
    
    @TypeConverter
    public static List<ChatMessage> toChatMessageList(String messagesString) {
        if (messagesString == null) {
            return null;
        }
        Type listType = new TypeToken<List<ChatMessage>>() {}.getType();
        return gson.fromJson(messagesString, listType);
    }
}
