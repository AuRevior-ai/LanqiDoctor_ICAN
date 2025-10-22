package com.lanqiDoctor.demo.realtimeDialog.client;

import android.content.Context;
import android.util.Log;

import com.lanqiDoctor.demo.realtimeDialog.audio.AndroidAudioPlayer;
import com.lanqiDoctor.demo.realtimeDialog.audio.AudioFileManager;
import com.lanqiDoctor.demo.realtimeDialog.protocol.Message;
import com.lanqiDoctor.demo.realtimeDialog.protocol.MsgType;
import com.lanqiDoctor.demo.realtimeDialog.websocket.RealtimeWebSocketClient;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Android版本的服务器响应处理类
 */
public class AndroidServerResponseHandler implements RealtimeWebSocketClient.MessageListener {
    private static final String TAG = "AndroidServerResponseHandler";

    private final Context context;
    private final AndroidAudioPlayer audioPlayer;
    private final AudioFileManager audioFileManager;
    private final AtomicBoolean isSessionActive = new AtomicBoolean(false);
    private final AtomicBoolean isConnectionActive = new AtomicBoolean(false);
    
    public interface AudioRecordingController {
        void pauseRecording();
        void resumeRecording();
    }
    
    private AudioRecordingController recordingController;

    public AndroidServerResponseHandler(Context context) {
        this.context = context;
        this.audioPlayer = new AndroidAudioPlayer();
        this.audioFileManager = new AudioFileManager();
        
        // 设置音频播放状态监听器
        this.audioPlayer.setPlaybackStateListener(new AndroidAudioPlayer.PlaybackStateListener() {
            @Override
            public void onPlaybackStart() {
                if (recordingController != null) {
                    recordingController.pauseRecording();
                }
            }
            
            @Override
            public void onPlaybackEnd() {
                if (recordingController != null) {
                    recordingController.resumeRecording();
                }
            }
        });
    }
    
    public void setAudioRecordingController(AudioRecordingController controller) {
        this.recordingController = controller;
    }

    /**
     * 启动音频播放
     */
    public void startAudioOutput() {
        audioPlayer.startPlayback();
    }

    /**
     * 停止音频播放并保存文件
     */
    public void stopAudioOutput() {
        audioPlayer.stopPlayback();
        audioFileManager.saveAudioToPCMFile("output.pcm");
    }

    @Override
    public void onMessage(Message message) {
        try {
            switch (message.getType()) {
                case FULL_SERVER:
                    handleFullServerMessage(message);
                    break;
                case AUDIO_ONLY_SERVER:
                    handleAudioOnlyServerMessage(message);
                    break;
                case ERROR:
                    handleErrorMessage(message);
                    break;
                default:
                    Log.w(TAG, "Received unexpected message type: " + message.getType());
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling message", e);
        }
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "WebSocket error: " + error);
    }

    private void handleFullServerMessage(Message message) {
        String payloadText = "";
        if (message.getPayload() != null) {
            // 防止日志爆炸，限制显示的文本长度
            if (message.getPayload().length > 2048) {
                payloadText = new String(message.getPayload(), 0, 100, StandardCharsets.UTF_8) + 
                             "...(truncated, total length: " + message.getPayload().length + ")";
            } else {
                payloadText = new String(message.getPayload(), StandardCharsets.UTF_8);
            }
        }

        Log.i(TAG, "Received text message (event=" + message.getEvent() + 
              ", session_id=" + message.getSessionId() + "): " + payloadText);

        // 处理会话结束事件
        if (message.getEvent() == 152 || message.getEvent() == 153) {
            Log.i(TAG, "Session finished, event: " + message.getEvent());
            isSessionActive.set(false);
            return;
        }

        // 处理ASR信息事件，清空音频缓冲区
        if (message.getEvent() == 450) {
            Log.i(TAG, "ASR info received, clearing audio buffer");
            audioFileManager.clearAudioData();
            audioPlayer.clearBuffer();
        }

        // 处理连接开始事件（与Go版本对应，期望Event=50）
        if (message.getEvent() == 50) {
            Log.i(TAG, "Connection started (event=" + message.getEvent() + 
                  "), connectID: " + message.getConnectId());
            isConnectionActive.set(true);
            return;
        }

        // 处理会话开始事件
        if (message.getEvent() == 150) {
            Log.i(TAG, "Session started (event=" + message.getEvent() + ")");
            isSessionActive.set(true);
        }

        // 处理连接结束事件
        if (message.getEvent() == 52) {
            Log.i(TAG, "Connection finished");
        }
    }

    private void handleAudioOnlyServerMessage(Message message) {
        Log.i(TAG, "Received audio message (event=" + message.getEvent() + 
              "): session_id=" + message.getSessionId());

        if (message.getPayload() != null && message.getPayload().length > 0) {
            handleIncomingAudio(message.getPayload());
            audioFileManager.addAudioData(message.getPayload());
        }
    }

    private void handleErrorMessage(Message message) {
        String errorText = message.getPayload() != null 
            ? new String(message.getPayload(), StandardCharsets.UTF_8) 
            : "Unknown error";

        Log.e(TAG, "Received Error message (code=" + message.getErrorCode() + "): " + errorText);
        
        // 打印更多错误细节以便调试
        Log.e(TAG, "Error details - Event: " + message.getEvent() + 
              ", SessionID: " + message.getSessionId() + 
              ", ConnectID: " + message.getConnectId());
                   
        // 如果是认证错误(常见错误码1000-1999)
        if (message.getErrorCode() >= 1000 && message.getErrorCode() < 2000) {
            Log.e(TAG, "Authentication error - Please check your APP_ID and ACCESS_TOKEN");
        }
        
        isSessionActive.set(false);
    }

    private void handleIncomingAudio(byte[] audioData) {
        Log.d(TAG, "Received audio byte len: " + audioData.length + 
              ", float32 len: " + audioData.length / 4);

        // 将音频数据添加到播放缓冲区
        audioPlayer.addAudioData(audioData);
    }

    /**
     * 检查会话是否激活
     */
    public boolean isSessionActive() {
        return isSessionActive.get();
    }

    /**
     * 检查连接是否已建立
     */
    public boolean isConnectionActive() {
        return isConnectionActive.get();
    }

    /**
     * 获取音频文件管理器
     */
    public AudioFileManager getAudioFileManager() {
        return audioFileManager;
    }

    /**
     * 释放资源
     */
    public void release() {
        audioPlayer.release();
    }
}