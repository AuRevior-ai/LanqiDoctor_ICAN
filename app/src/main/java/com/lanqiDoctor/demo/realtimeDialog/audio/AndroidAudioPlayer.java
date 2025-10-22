package com.lanqiDoctor.demo.realtimeDialog.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Android 音频播放器
 */
public class AndroidAudioPlayer {
    private static final String TAG = "AndroidAudioPlayer";
    
    // 音频格式配置
    private static final int SAMPLE_RATE = 24000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int FRAMES_PER_BUFFER = 512;
    
    private AudioTrack audioTrack;
    private Thread playThread;
    private final AtomicBoolean isPlaying = new AtomicBoolean(false);
    private final AtomicBoolean isActivelyPlaying = new AtomicBoolean(false);
    private final BlockingQueue<byte[]> audioBuffer = new LinkedBlockingQueue<>();
    
    public interface PlaybackStateListener {
        void onPlaybackStart();
        void onPlaybackEnd();
    }
    
    private PlaybackStateListener playbackStateListener;
    
    public AndroidAudioPlayer() {
        setupAudioTrack();
    }
    
    public void setPlaybackStateListener(PlaybackStateListener listener) {
        this.playbackStateListener = listener;
    }
    
    private void setupAudioTrack() {
        try {
            int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
            
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
            
            AudioFormat audioFormat = new AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(CHANNEL_CONFIG)
                .setEncoding(AUDIO_FORMAT)
                .build();
            
            audioTrack = new AudioTrack(
                audioAttributes,
                audioFormat,
                bufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            );
            
            if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                Log.e(TAG, "AudioTrack initialization failed");
                return;
            }
            
            Log.d(TAG, "AudioTrack initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize AudioTrack", e);
        }
    }
    
    public void startPlayback() {
        if (audioTrack == null) {
            Log.e(TAG, "AudioTrack not initialized");
            return;
        }
        
        if (isPlaying.get()) {
            Log.w(TAG, "Playback already started");
            return;
        }
        
        isPlaying.set(true);
        audioTrack.play();
        
        playThread = new Thread(this::playAudio);
        playThread.setDaemon(true);
        playThread.start();
        
        Log.d(TAG, "Audio playback started");
    }
    
    public void stopPlayback() {
        if (!isPlaying.get()) {
            return;
        }
        
        isPlaying.set(false);
        
        // 确保通知播放结束
        if (isActivelyPlaying.get() && playbackStateListener != null) {
            isActivelyPlaying.set(false);
            playbackStateListener.onPlaybackEnd();
            Log.d(TAG, "Audio playback stopped - microphone should be resumed");
        }
        
        if (audioTrack != null) {
            try {
                audioTrack.stop();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping AudioTrack", e);
            }
        }
        
        if (playThread != null) {
            try {
                playThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        Log.d(TAG, "Audio playback stopped");
    }
    
    public void addAudioData(byte[] audioData) {
        try {
            // 转换32位float数据为16位PCM
            byte[] pcmData = convertFloat32ToPCM16(audioData);
            audioBuffer.offer(pcmData);
            
            // 第一次收到音频数据时通知开始播放
            if (!isActivelyPlaying.get() && playbackStateListener != null) {
                isActivelyPlaying.set(true);
                playbackStateListener.onPlaybackStart();
                Log.d(TAG, "AI audio playback started - microphone should be paused");
            }
            
            // 限制缓冲区大小
            while (audioBuffer.size() > 100) {
                audioBuffer.poll();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding audio data", e);
        }
    }
    
    private void playAudio() {
        byte[] buffer = new byte[FRAMES_PER_BUFFER * 2];
        long lastAudioTime = System.currentTimeMillis();
        final long SILENCE_THRESHOLD = 1000; // 1秒无音频数据视为播放结束
        
        while (isPlaying.get()) {
            try {
                byte[] audioData = audioBuffer.poll();
                if (audioData != null) {
                    audioTrack.write(audioData, 0, audioData.length);
                    lastAudioTime = System.currentTimeMillis();
                } else {
                    // 播放静音
                    java.util.Arrays.fill(buffer, (byte) 0);
                    audioTrack.write(buffer, 0, buffer.length);
                    Thread.sleep(10);
                    
                    // 检查是否长时间无音频数据，如果是则通知播放结束
                    if (isActivelyPlaying.get() && 
                        System.currentTimeMillis() - lastAudioTime > SILENCE_THRESHOLD) {
                        isActivelyPlaying.set(false);
                        if (playbackStateListener != null) {
                            playbackStateListener.onPlaybackEnd();
                            Log.d(TAG, "AI audio playback ended - microphone should be resumed");
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error playing audio", e);
                break;
            }
        }
        
        // 播放线程结束时确保通知播放结束
        if (isActivelyPlaying.get() && playbackStateListener != null) {
            isActivelyPlaying.set(false);
            playbackStateListener.onPlaybackEnd();
            Log.d(TAG, "Audio playback thread ended - microphone should be resumed");
        }
    }
    
    private byte[] convertFloat32ToPCM16(byte[] float32Data) {
        if (float32Data.length % 4 != 0) {
            Log.w(TAG, "Invalid float32 data length: " + float32Data.length);
            return new byte[0];
        }
        
        int sampleCount = float32Data.length / 4;
        byte[] pcm16Data = new byte[sampleCount * 2];
        
        ByteBuffer floatBuffer = ByteBuffer.wrap(float32Data);
        floatBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        ByteBuffer pcmBuffer = ByteBuffer.wrap(pcm16Data);
        pcmBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        for (int i = 0; i < sampleCount; i++) {
            float sample = floatBuffer.getFloat();
            short pcmSample = (short) (sample * Short.MAX_VALUE);
            pcmBuffer.putShort(pcmSample);
        }
        
        return pcm16Data;
    }
    
    public void clearBuffer() {
        audioBuffer.clear();
    }
    
    public void release() {
        stopPlayback();
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }
    }
}