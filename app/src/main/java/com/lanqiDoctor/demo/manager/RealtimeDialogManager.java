package com.lanqiDoctor.demo.manager;

import android.content.Context;
import android.util.Log;

import com.lanqiDoctor.demo.realtimeDialog.client.AndroidClientRequestHandler;
import com.lanqiDoctor.demo.realtimeDialog.client.AndroidServerResponseHandler;
import com.lanqiDoctor.demo.realtimeDialog.client.RequestPayloads;
import com.lanqiDoctor.demo.realtimeDialog.protocol.BinaryProtocol;
import com.lanqiDoctor.demo.realtimeDialog.protocol.CompressionBits;
import com.lanqiDoctor.demo.realtimeDialog.protocol.HeaderSizeBits;
import com.lanqiDoctor.demo.realtimeDialog.protocol.SerializationBits;
import com.lanqiDoctor.demo.realtimeDialog.protocol.VersionBits;
import com.lanqiDoctor.demo.realtimeDialog.websocket.RealtimeWebSocketClient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 实时对话管理器
 */
public class RealtimeDialogManager {
    private static final String TAG = "RealtimeDialogManager";
    
    // 配置参数
    private static final String APP_ID = "6341846484";
    private static final String ACCESS_TOKEN = "S-bUnJpTcEjZZJqmsu5_XgDAP-x5pLFk";
    private static final String WS_URL = "wss://openspeech.bytedance.com/api/v3/realtime/dialogue";
    private static final String BOT_NAME = "蓝岐医童";

    private static final String SYSTEM_ROLE = "蓝岐医童，源自东方传统医理智慧与现代AI技术的结合，是一位专为家庭健康管理打造的智能“医童”。" +
            "他既是一位活泼贴心的小伙伴，也是一位细致可靠的健康助手。致力于在日常生活中为用户提供症状欲诊、用药监护、医学知识验证与健康科普等服务，" +
            "也可对患者进行日常生活管理和心灵慰藉。他是一个侍医童子，外形为十岁左右的少年，用户为患者，医童将时时刻刻关心用户的健康。";

    private static final String SPEAKING_STYLE = "语气灵动活泼，口吻亲切可爱，懂事乖巧，兼具专业与耐心；擅长用通俗语言解释医学术语，" +
            "回应用户时态度诚恳温柔，语调清亮柔和，温润而带有稚嫩感与天真感，像说悄悄话，富有陪伴感。面对不适和担忧，他将轻声安慰，陪伴倾听，略带俏皮。" +
            "他说话节奏适中，善于发现句子的语气。偶尔会像小孩子一样开玩笑，大部分时间会正经认真";
    private Context context;
    private RealtimeWebSocketClient webSocketClient;
    private BinaryProtocol protocol;
    private AndroidClientRequestHandler clientHandler;
    private AndroidServerResponseHandler serverHandler;
    private StatusListener statusListener;
    
    public interface StatusListener {
        void onStatusUpdate(String status);
    }
    
    public RealtimeDialogManager(Context context) {
        this.context = context;
        initializeProtocol();
    }
    
    public void setStatusListener(StatusListener listener) {
        this.statusListener = listener;
    }
    
    private void updateStatus(String status) {
        Log.d(TAG, status);
        if (statusListener != null) {
            statusListener.onStatusUpdate(status);
        }
    }
    
    private void initializeProtocol() {
        protocol = new BinaryProtocol();
        protocol.setVersion(VersionBits.VERSION_1);
        protocol.setHeaderSize(HeaderSizeBits.SIZE_4);
        protocol.setSerialization(SerializationBits.JSON);
        protocol.setCompression(CompressionBits.NONE);
        
        Log.d(TAG, "Protocol initialized");
    }
    
    public boolean startDialog(String sessionId) {
        try {
            // 连接到服务器
            if (!connectToServer()) {
                updateStatus("连接服务器失败");
                return false;
            }
            
            // 执行实时对话
            return executeRealtimeDialog(sessionId);
            
        } catch (Exception e) {
            Log.e(TAG, "Start dialog failed", e);
            updateStatus("启动对话失败: " + e.getMessage());
            return false;
        }
    }
    
    private boolean connectToServer() {
        try {
            URI serverUri = new URI(WS_URL);
            
            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Api-Resource-Id", "volc.speech.dialog");
            headers.put("X-Api-Access-Key", ACCESS_TOKEN);
            headers.put("X-Api-App-Key", "PlgvMymc7f3tQnJ6");
            headers.put("X-Api-App-ID", APP_ID);
            headers.put("X-Api-Connect-Id", UUID.randomUUID().toString());
            
            Log.d(TAG, "Connecting to: " + serverUri);
            
            // 创建WebSocket客户端
            webSocketClient = new RealtimeWebSocketClient(serverUri, headers, protocol);
            
            // 创建处理器
            serverHandler = new AndroidServerResponseHandler(context);
            clientHandler = new AndroidClientRequestHandler(webSocketClient, protocol, context);

            // 设置消息监听器
            webSocketClient.setMessageListener(serverHandler);
            
            // 连接录音控制器 - 关键：让服务端响应处理器能够控制客户端录音
            serverHandler.setAudioRecordingController(clientHandler);
            
            // 连接到服务器
            webSocketClient.connect();
            
            // 等待连接建立
            if (!webSocketClient.waitForConnection(10, TimeUnit.SECONDS)) {
                Log.e(TAG, "Connection timeout");
                return false;
            }
            
            Log.d(TAG, "WebSocket connected successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Connect to server failed", e);
            return false;
        }
    }
    
    private boolean executeRealtimeDialog(String sessionId) {
        try {
            updateStatus("建立连接中...");
            
            // 开始连接
            clientHandler.startConnection().get(5, TimeUnit.SECONDS);
            
            // 等待服务器确认
            int maxWaitTime = 5000;
            int waitTime = 0;
            while (!serverHandler.isConnectionActive() && waitTime < maxWaitTime) {
                Thread.sleep(100);
                waitTime += 100;
            }
            
            if (!serverHandler.isConnectionActive()) {
                updateStatus("服务器连接确认失败");
                return false;
            }
            
            updateStatus("连接已确认，开始会话...");
            
            // 开始会话
            startSession(sessionId);
            
            // 等待会话确认
            Thread.sleep(1500);
            
            // 启动音频输出
            serverHandler.startAudioOutput();
            
            // 开始发送音频
            clientHandler.startSendingAudio(sessionId);
            
            updateStatus("实时对话已启动");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Execute dialog failed", e);
            updateStatus("执行对话失败: " + e.getMessage());
            return false;
        }
    }
    
    private void startSession(String sessionId) throws Exception {
        // 创建会话配置
        RequestPayloads.AudioConfig audioConfig = new RequestPayloads.AudioConfig(1, "pcm", 24000);
        RequestPayloads.TTSPayload ttsPayload = new RequestPayloads.TTSPayload(audioConfig);
        
        Map<String, Object> extra = new HashMap<>();
        extra.put("strict_audit", false);
        RequestPayloads.DialogPayload dialogPayload = new RequestPayloads.DialogPayload(BOT_NAME, null, SYSTEM_ROLE, SPEAKING_STYLE, extra);
        
        RequestPayloads.StartSessionPayload startSessionPayload = 
            new RequestPayloads.StartSessionPayload(ttsPayload, dialogPayload);
        
        // 发送开始会话请求
        clientHandler.startSession(sessionId, startSessionPayload).get(5, TimeUnit.SECONDS);
        
        Log.d(TAG, "Session started successfully");
    }
    
    public void stopDialog() {
        try {
            updateStatus("正在停止对话...");
            
            // 停止音频录制
            if (clientHandler != null) {
                clientHandler.stopSendingAudio();
                clientHandler.release();
            }
            
            // 停止音频播放
            if (serverHandler != null) {
                serverHandler.stopAudioOutput();
                serverHandler.release();
            }
            
            // 关闭WebSocket连接
            if (webSocketClient != null && webSocketClient.isConnected()) {
                webSocketClient.close();
            }
            
            updateStatus("对话已停止");
            
        } catch (Exception e) {
            Log.e(TAG, "Stop dialog failed", e);
            updateStatus("停止对话失败: " + e.getMessage());
        }
    }
}