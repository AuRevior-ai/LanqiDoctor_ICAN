package com.lanqiDoctor.demo.realtimeDialog.client;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanqiDoctor.demo.realtimeDialog.audio.AndroidAudioRecorder;
import com.lanqiDoctor.demo.realtimeDialog.protocol.Message;
import com.lanqiDoctor.demo.realtimeDialog.protocol.MsgType;
import com.lanqiDoctor.demo.realtimeDialog.protocol.MsgTypeFlagBits;
import com.lanqiDoctor.demo.realtimeDialog.protocol.BinaryProtocol;
import com.lanqiDoctor.demo.realtimeDialog.protocol.SerializationBits;
import com.lanqiDoctor.demo.realtimeDialog.websocket.RealtimeWebSocketClient;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Android版本的客户端请求处理类
 */
public class AndroidClientRequestHandler implements AndroidServerResponseHandler.AudioRecordingController {
    private static final String TAG = "AndroidClientRequestHandler";

    private final Context context;
    private final RealtimeWebSocketClient webSocketClient;
    private final BinaryProtocol protocol;
    private final ObjectMapper objectMapper;
    private final AndroidAudioRecorder audioRecorder;

    public AndroidClientRequestHandler(RealtimeWebSocketClient webSocketClient, BinaryProtocol protocol, Context context) {
        this.context = context;
        this.webSocketClient = webSocketClient;
        this.protocol = protocol;
        this.objectMapper = new ObjectMapper();
        this.audioRecorder = new AndroidAudioRecorder();
    }

    /**
     * 开始连接（与Go版本startConnection保持一致）
     */
    public CompletableFuture<Void> startConnection() {
        return CompletableFuture.runAsync(() -> {
            try {
                // 发送StartConnection请求
                Message message = Message.newMessage(MsgType.FULL_CLIENT, MsgTypeFlagBits.WITH_EVENT);
                message.setEvent(1);
                message.setPayload("{}".getBytes(StandardCharsets.UTF_8));

                webSocketClient.sendMessage(message);
                Log.i(TAG, "StartConnection request sent, waiting for response...");

                // 注意：与Go版本不同，我们不在这里同步等待响应
                // 响应将由ServerResponseHandler异步处理
                // Go版本会在这里同步等待Event=50的响应

            } catch (Exception e) {
                Log.e(TAG, "Failed to start connection", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 开始会话
     */
    public CompletableFuture<Void> startSession(String sessionId, RequestPayloads.StartSessionPayload payload) {
        return CompletableFuture.runAsync(() -> {
            try {
                String payloadJson = objectMapper.writeValueAsString(payload);
                Log.i(TAG, "StartSession request payload: " + payloadJson);

                Message message = Message.newMessage(MsgType.FULL_CLIENT, MsgTypeFlagBits.WITH_EVENT);
                message.setEvent(100);
                message.setSessionId(sessionId);
                message.setPayload(payloadJson.getBytes(StandardCharsets.UTF_8));

                webSocketClient.sendMessage(message);
                Log.i(TAG, "StartSession request sent");

            } catch (Exception e) {
                Log.e(TAG, "Failed to start session", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 发送问候
     */
    public CompletableFuture<Void> sayHello(String sessionId, RequestPayloads.SayHelloPayload payload) {
        return CompletableFuture.runAsync(() -> {
            try {
                String payloadJson = objectMapper.writeValueAsString(payload);
                Log.i(TAG, "SayHello request payload: " + payloadJson);

                Message message = Message.newMessage(MsgType.FULL_CLIENT, MsgTypeFlagBits.WITH_EVENT);
                message.setEvent(300);
                message.setSessionId(sessionId);
                message.setPayload(payloadJson.getBytes(StandardCharsets.UTF_8));

                webSocketClient.sendMessage(message);
                Log.i(TAG, "SayHello request sent");

            } catch (Exception e) {
                Log.e(TAG, "Failed to send hello", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 发送聊天TTS文本
     */
    public CompletableFuture<Void> chatTTSText(String sessionId, RequestPayloads.ChatTTSTextPayload payload) {
        return CompletableFuture.runAsync(() -> {
            try {
                String payloadJson = objectMapper.writeValueAsString(payload);
                Log.i(TAG, "ChatTTSText request payload: " + payloadJson);

                Message message = Message.newMessage(MsgType.FULL_CLIENT, MsgTypeFlagBits.WITH_EVENT);
                message.setEvent(500);
                message.setSessionId(sessionId);
                message.setPayload(payloadJson.getBytes(StandardCharsets.UTF_8));

                webSocketClient.sendMessage(message);
                Log.i(TAG, "ChatTTSText request sent");

            } catch (Exception e) {
                Log.e(TAG, "Failed to send chat TTS text", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 开始发送音频
     */
    public void startSendingAudio(String sessionId) {
        Log.i(TAG, "Starting audio recording and transmission...");

        audioRecorder.setAudioDataListener(audioData -> {
            try {
                // 在每次发送音频数据前，先设置序列化方式为原始数据（与Go版本行为一致）
                protocol.setSerialization(SerializationBits.RAW);
                
                Message message = Message.newMessage(MsgType.AUDIO_ONLY_CLIENT, MsgTypeFlagBits.WITH_EVENT);
                message.setEvent(200);
                message.setSessionId(sessionId);
                message.setPayload(audioData);

                webSocketClient.sendMessage(message);
                Log.d(TAG, "Sent " + audioData.length + " bytes of audio data");

            } catch (Exception e) {
                Log.e(TAG, "Error sending audio message", e);
            }
        });

        audioRecorder.startRecording();
    }

    /**
     * 停止发送音频
     */
    public void stopSendingAudio() {
        audioRecorder.stopRecording();
    }

    /**
     * 结束会话
     */
    public CompletableFuture<Void> finishSession(String sessionId) {
        return CompletableFuture.runAsync(() -> {
            try {
                Message message = Message.newMessage(MsgType.FULL_CLIENT, MsgTypeFlagBits.WITH_EVENT);
                message.setEvent(102);
                message.setSessionId(sessionId);
                message.setPayload("{}".getBytes(StandardCharsets.UTF_8));

                webSocketClient.sendMessage(message);
                Log.i(TAG, "FinishSession request sent");

            } catch (Exception e) {
                Log.e(TAG, "Failed to finish session", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 结束连接
     */
    public CompletableFuture<Void> finishConnection() {
        return CompletableFuture.runAsync(() -> {
            try {
                Message message = Message.newMessage(MsgType.FULL_CLIENT, MsgTypeFlagBits.WITH_EVENT);
                message.setEvent(2);
                message.setPayload("{}".getBytes(StandardCharsets.UTF_8));

                webSocketClient.sendMessage(message);
                Log.i(TAG, "FinishConnection request sent");

                // 等待连接关闭响应
                Thread.sleep(1000);

            } catch (Exception e) {
                Log.e(TAG, "Failed to finish connection", e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 释放资源
     */
    public void release() {
        audioRecorder.release();
    }
    
    // 实现 AudioRecordingController 接口
    @Override
    public void pauseRecording() {
        audioRecorder.pauseRecording();
    }
    
    @Override
    public void resumeRecording() {
        audioRecorder.resumeRecording();
    }
}