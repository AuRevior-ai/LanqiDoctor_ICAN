package com.lanqiDoctor.demo.realtimeDialog.protocol;

/**
 * 协议消息实体类
 */
public class Message {
    private MsgType type;
    private byte typeAndFlagBits;
    private int event;
    private String sessionId;
    private String connectId;
    private int sequence;
    private int errorCode;
    private byte[] payload;

    public Message() {
    }

    public Message(MsgType type, byte typeFlag) {
        this.type = type;
        // 与Go版本保持一致：bits + uint8(typeFlag)
        this.typeAndFlagBits = (byte) (type.toBits() | (typeFlag & 0x0F));
    }

    // Getters and Setters
    public MsgType getType() {
        return type;
    }

    public void setType(MsgType type) {
        this.type = type;
    }

    public byte getTypeAndFlagBits() {
        return typeAndFlagBits;
    }

    public void setTypeAndFlagBits(byte typeAndFlagBits) {
        this.typeAndFlagBits = typeAndFlagBits;
    }

    public byte getTypeFlag() {
        return (byte) (typeAndFlagBits & 0b00001111);
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getConnectId() {
        return connectId;
    }

    public void setConnectId(String connectId) {
        this.connectId = connectId;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    /**
     * 创建新的消息实例
     */
    public static Message newMessage(MsgType msgType, byte typeFlag) {
        return new Message(msgType, typeFlag);
    }

    /**
     * 从字节创建消息实例（与Go版本NewMessageFromByte保持一致）
     */
    public static Message newMessageFromByte(byte typeAndFlag) {
        // 与Go版本一致：bits := typeAndFlag &^ 0b00001111
        byte bits = (byte) (typeAndFlag & 0b11110000);
        MsgType msgType = MsgType.fromBits(bits);
        Message message = new Message();
        message.type = msgType;
        message.typeAndFlagBits = typeAndFlag;
        return message;
    }
}
