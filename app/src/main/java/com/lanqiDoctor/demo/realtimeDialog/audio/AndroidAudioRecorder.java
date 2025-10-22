package com.lanqiDoctor.demo.realtimeDialog.audio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import com.lanqiDoctor.demo.aop.Permissions;

import com.hjq.permissions.Permission;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Android 音频录制器
 */
public class AndroidAudioRecorder {
    private static final String TAG = "AndroidAudioRecorder";
    
    // 音频格式配置
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int FRAMES_PER_BUFFER = 160;
    
    private AudioRecord audioRecord;
    private Thread recordThread;
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    
    public interface AudioDataListener {
        void onAudioData(byte[] audioData);
    }
    
    private AudioDataListener audioDataListener;
    
    public AndroidAudioRecorder() {
        setupMicrophone();
    }
    
    public void setAudioDataListener(AudioDataListener listener) {
        this.audioDataListener = listener;
    }

    @Permissions(Permission.RECORD_AUDIO)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private void setupMicrophone() {
        try {
            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

            audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            );
            
            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed");
                return;
            }
            
            Log.d(TAG, "AudioRecord initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize AudioRecord", e);
        }
    }
    
    public void startRecording() {
        if (audioRecord == null) {
            Log.e(TAG, "AudioRecord not initialized");
            return;
        }
        
        if (isRecording.get()) {
            Log.w(TAG, "Recording already started");
            return;
        }
        
        isRecording.set(true);
        audioRecord.startRecording();
        
        recordThread = new Thread(this::recordAudio);
        recordThread.setDaemon(true);
        recordThread.start();
        
        Log.d(TAG, "Audio recording started");
    }
    
    
    /**
     * 暂停录音（但不停止录音线程）
     */
    public void pauseRecording() {
        isPaused.set(true);
        Log.d(TAG, "Audio recording paused");
    }
    
    /**
     * 恢复录音
     */
    public void resumeRecording() {
        isPaused.set(false);
        Log.d(TAG, "Audio recording resumed");
    }
    
    /**
     * 检查是否处于暂停状态
     */
    public boolean isPaused() {
        return isPaused.get();
    }
    
    public void stopRecording() {
        if (!isRecording.get()) {
            return;
        }
        
        isRecording.set(false);
        
        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping AudioRecord", e);
            }
        }
        
        if (recordThread != null) {
            try {
                recordThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        Log.d(TAG, "Audio recording stopped");
    }
    
    private void recordAudio() {
        byte[] buffer = new byte[FRAMES_PER_BUFFER * 2]; // 16-bit samples
        
        while (isRecording.get()) {
            try {
                // 如果暂停状态，跳过录音但保持线程运行
                if (isPaused.get()) {
                    Thread.sleep(50); // 短暂休眠，避免CPU空转
                    continue;
                }
                
                int bytesRead = audioRecord.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    // 确保数据长度是完整的16位样本
                    if (bytesRead % 2 != 0) {
                        bytesRead--;
                    }
                    
                    byte[] audioData = new byte[bytesRead];
                    System.arraycopy(buffer, 0, audioData, 0, bytesRead);
                    
                    if (audioDataListener != null) {
                        audioDataListener.onAudioData(audioData);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading audio data", e);
                break;
            }
        }
    }
    
    public void release() {
        stopRecording();
        isPaused.set(false); // 重置暂停状态
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
    }
}