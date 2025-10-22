package com.lanqiDoctor.demo.realtimeDialog.audio;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 音频文件管理器
 */
public class AudioFileManager {
    private final String TAG = "AudioFileManager";

    private final List<byte[]> audioDataList = new ArrayList<>();

    /**
     * 添加音频数据
     */
    public void addAudioData(byte[] audioData) {
        audioDataList.add(audioData.clone());
    }

    /**
     * 保存音频数据到PCM文件
     */
    public void saveAudioToPCMFile(String filename) {
        if (audioDataList.isEmpty()) {
//            logger.info("No audio data to save");
            Log.i(TAG, "No audio to save");
            return;
        }
        Log.i(TAG, "In android, we do not save file to "+filename+" or any other place");

//        try {
//            Path filePath = Paths.get("./", filename);
//
//            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
//                for (byte[] audioData : audioDataList) {
//                    fos.write(audioData);
//                }
//                fos.flush();
//            }
//
//            // 计算总的音频数据大小
//            int totalBytes = audioDataList.stream().mapToInt(data -> data.length).sum();
//            logger.info("Saved {} bytes of audio data to {}", totalBytes, filePath.toAbsolutePath());
//
//        } catch (IOException e) {
//            logger.error("Failed to save PCM file: {}", filename, e);
//        }
    }

    /**
     * 清空音频数据
     */
    public void clearAudioData() {
        audioDataList.clear();
//        logger.debug("Audio data cleared");
        Log.d(TAG, "Audio data cleared");
    }

    /**
     * 获取音频数据总大小
     */
    public int getTotalAudioSize() {
        return audioDataList.stream().mapToInt(data -> data.length).sum();
    }

    /**
     * 获取音频数据片段数量
     */
    public int getAudioSegmentCount() {
        return audioDataList.size();
    }
}
