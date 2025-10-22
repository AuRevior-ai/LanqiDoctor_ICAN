package com.lanqiDoctor.demo.realtimeDialog.client;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 请求载荷数据结构
 */
public class RequestPayloads {

    /**
     * 开始会话载荷
     */
    public static class StartSessionPayload {
        @JsonProperty("tts")
        private TTSPayload tts;

        @JsonProperty("dialog")
        private DialogPayload dialog;

        // Constructors
        public StartSessionPayload() {}

        public StartSessionPayload(TTSPayload tts, DialogPayload dialog) {
            this.tts = tts;
            this.dialog = dialog;
        }

        // Getters and Setters
        public TTSPayload getTts() {
            return tts;
        }

        public void setTts(TTSPayload tts) {
            this.tts = tts;
        }

        public DialogPayload getDialog() {
            return dialog;
        }

        public void setDialog(DialogPayload dialog) {
            this.dialog = dialog;
        }
    }

    /**
     * 问候载荷
     */
    public static class SayHelloPayload {
        @JsonProperty("content")
        private String content;

        public SayHelloPayload() {}

        public SayHelloPayload(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    /**
     * 聊天TTS文本载荷
     */
    public static class ChatTTSTextPayload {
        @JsonProperty("start")
        private boolean start;

        @JsonProperty("end")
        private boolean end;

        @JsonProperty("content")
        private String content;

        public ChatTTSTextPayload() {}

        public ChatTTSTextPayload(boolean start, boolean end, String content) {
            this.start = start;
            this.end = end;
            this.content = content;
        }

        public boolean isStart() {
            return start;
        }

        public void setStart(boolean start) {
            this.start = start;
        }

        public boolean isEnd() {
            return end;
        }

        public void setEnd(boolean end) {
            this.end = end;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    /**
     * TTS配置载荷
     */
    public static class TTSPayload {
        @JsonProperty("audio_config")
        private AudioConfig audioConfig;

        public TTSPayload() {}

        public TTSPayload(AudioConfig audioConfig) {
            this.audioConfig = audioConfig;
        }

        public AudioConfig getAudioConfig() {
            return audioConfig;
        }

        public void setAudioConfig(AudioConfig audioConfig) {
            this.audioConfig = audioConfig;
        }
    }

    /**
     * 音频配置
     */
    public static class AudioConfig {
        @JsonProperty("channel")
        private int channel;

        @JsonProperty("format")
        private String format;

        @JsonProperty("sample_rate")
        private int sampleRate;

        public AudioConfig() {}

        public AudioConfig(int channel, String format, int sampleRate) {
            this.channel = channel;
            this.format = format;
            this.sampleRate = sampleRate;
        }

        public int getChannel() {
            return channel;
        }

        public void setChannel(int channel) {
            this.channel = channel;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public int getSampleRate() {
            return sampleRate;
        }

        public void setSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
        }
    }

    /**
     * 对话配置载荷
     */
    public static class DialogPayload {
        @JsonProperty("bot_name")
        private String botName;

        @JsonProperty("dialog_id")
        private String dialogId;

        @JsonProperty("extra")
        private java.util.Map<String, Object> extra;

        @JsonProperty("system_role")
        private String systemRole;

        @JsonProperty("speaking_style")
        private String speakingStyle;

        public DialogPayload() {}

        public DialogPayload(String botName, String dialogId, String systemRole, String speakingStyle, java.util.Map<String, Object> extra) {
            this.botName = botName;
            this.dialogId = dialogId;
            this.systemRole = systemRole;
            this.speakingStyle = speakingStyle;
            this.extra = extra;
        }

        public String getBotName() {
            return botName;
        }

        public void setBotName(String botName) {
            this.botName = botName;
        }

        public String getDialogId() {
            return dialogId;
        }

        public void setDialogId(String dialogId) {
            this.dialogId = dialogId;
        }

        public java.util.Map<String, Object> getExtra() {
            return extra;
        }

        public void setExtra(java.util.Map<String, Object> extra) {
            this.extra = extra;
        }
    }
}
