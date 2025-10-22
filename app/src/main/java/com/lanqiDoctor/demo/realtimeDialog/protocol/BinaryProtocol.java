package com.lanqiDoctor.demo.realtimeDialog.protocol;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * 二进制协议处理类
 */
public class BinaryProtocol {
    private final String TAG = "BinaryProtocol";

    private byte versionAndHeaderSize;
    private byte serializationAndCompression;

    public BinaryProtocol() {
        // 默认配置：版本1，头部大小4，JSON序列化，无压缩
        setVersion(VersionBits.VERSION_1);
        setHeaderSize(HeaderSizeBits.SIZE_4);
        setSerialization(SerializationBits.JSON);
        setCompression(CompressionBits.NONE);
    }

    /**
     * 设置协议版本
     */
    public void setVersion(VersionBits version) {
        versionAndHeaderSize = (byte) ((versionAndHeaderSize & 0b00001111) | version.getValue());
    }

    /**
     * 设置头部大小
     */
    public void setHeaderSize(HeaderSizeBits headerSize) {
        versionAndHeaderSize = (byte) ((versionAndHeaderSize & 0b11110000) | headerSize.getValue());
    }

    /**
     * 设置序列化方式
     */
    public void setSerialization(SerializationBits serialization) {
        serializationAndCompression = (byte) ((serializationAndCompression & 0b00001111) | serialization.getValue());
    }

    /**
     * 设置压缩方式
     */
    public void setCompression(CompressionBits compression) {
        serializationAndCompression = (byte) ((serializationAndCompression & 0b11110000) | compression.getValue());
    }

    /**
     * 序列化消息为字节数组
     */
    public byte[] marshal(Message message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 记录当前消息类型和序列化方式
        Log.d(TAG, "Marshalling message - Type: " + message.getType() +
              ", TypeFlag: " + message.getTypeFlag() +
              ", Serialization: " + ((serializationAndCompression >> 4) & 0xF) +
              ", Compression: " + (serializationAndCompression & 0xF));
        
        // 写入头部
        writeHeader(baos, message);

        // 按照Go版本的写入顺序：Sequence -> Event/SessionID -> Payload
        
        // 写入序列号（如果需要）
        if (MsgTypeFlagBits.containsSequence(message.getTypeFlag())) {
            writeInt(baos, message.getSequence());
        //            logger.debug("Writing Sequence: {}", message.getSequence());
            Log.d(TAG, "Writing Sequence: "+message.getSequence());
        }

        // 写入事件相关字段
        if (MsgTypeFlagBits.containsEvent(message.getTypeFlag())) {
            writeInt(baos, message.getEvent());
            Log.d(TAG, "Writing Event: "+message.getEvent());
            
            // 写入会话ID（特定事件跳过）
            if (shouldSkipSessionId(message.getEvent())) {
                Log.d(TAG, "Skip writing session ID for event: "+message.getEvent());
            } else {
                if (message.getSessionId() != null && !message.getSessionId().isEmpty()) {
                    byte[] sessionIdBytes = message.getSessionId().getBytes(StandardCharsets.UTF_8);
                    writeInt(baos, sessionIdBytes.length);
                    baos.write(sessionIdBytes);
                    Log.d(TAG, "Writing SessionID: "+message.getSessionId()+" (length: "+sessionIdBytes.length+")");
                } else {
                    writeInt(baos, 0);
                    Log.d(TAG, "Writing empty SessionID");
                }
            }
            
            // 写入连接ID（特定事件需要）
            if (shouldWriteConnectId(message.getEvent())) {
                if (message.getConnectId() != null && !message.getConnectId().isEmpty()) {
                    byte[] connectIdBytes = message.getConnectId().getBytes(StandardCharsets.UTF_8);
                    writeInt(baos, connectIdBytes.length);
                    baos.write(connectIdBytes);
                    Log.d(TAG, "Writing ConnectID: "+message.getConnectId()+" (lenth: "+connectIdBytes.length+")");
                } else {
                    writeInt(baos, 0);
                    Log.d(TAG, "Writing empty ConnectID");
                }
            }
        }

        // 写入错误码
        if (message.getType() == MsgType.ERROR) {
            writeInt(baos, message.getErrorCode());
            Log.d(TAG, "Writing ErrorCode: "+message.getErrorCode());
        }

        // 写入载荷
        if (message.getPayload() != null) {
            writeInt(baos, message.getPayload().length);
            baos.write(message.getPayload());
            
            // 如果载荷是文本类型，记录日志
            if (this.serializationAndCompression >> 4 == SerializationBits.JSON.getValue() >> 4) {
                try {
                    String payloadText = new String(message.getPayload(), StandardCharsets.UTF_8);
                    Log.d(TAG, "Writing JSON payload: "+payloadText);
                } catch (Exception e) {
                    Log.d(TAG, "Writing binary payload, length: "+message.getPayload().length);
                }
            } else {
                Log.d(TAG, "Writing binary payload, length: "+message.getPayload().length);
            }
        } else {
            writeInt(baos, 0);
            Log.d(TAG, "Writing empty payload");
        }

        byte[] result = baos.toByteArray();
        Log.d(TAG, "Marshal result length: "+result.length);
        return result;
    }

    /**
     * 反序列化字节数组为消息
     */
    public Message unmarshal(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);

        // 读取版本和头部大小
        byte versionSize = (byte) bais.read();
        Log.d(TAG, "Read version: "+Integer.toBinaryString((versionSize >> 4) & 0xF)+", size: "+Integer.toBinaryString(versionSize & 0xF));
        
        // 读取消息类型和标志
        byte typeAndFlag = (byte) bais.read();
        Log.d(TAG, "Read message type: "+Integer.toBinaryString((typeAndFlag >> 4) & 0xF)+", flag: "+Integer.toBinaryString(typeAndFlag & 0xF));

        Message message = Message.newMessageFromByte(typeAndFlag);

        // 读取序列化和压缩方式
        byte serializationCompression = (byte) bais.read();
        Log.d(TAG, "Read serialization: "+Integer.toBinaryString((serializationCompression >> 4) & 0xF)+", compression: "+Integer.toBinaryString(serializationCompression & 0xF));

        // 跳过保留字节
        bais.read();

        // 按照Go版本的读取顺序处理不同消息类型
        switch (message.getType()) {
            case FULL_CLIENT:
            case FULL_SERVER:
            case FRONT_END_RESULT_SERVER:
                // 这些类型不需要读取额外字段
                break;
                
            case AUDIO_ONLY_CLIENT:
                // 客户端音频消息：读取序列号
                if (MsgTypeFlagBits.containsSequence(message.getTypeFlag())) {
                    message.setSequence(readInt(bais));
                    Log.d(TAG, "AudioOnlyClient: Read Sequence: "+message.getSequence());
                }
                break;
                
            case AUDIO_ONLY_SERVER:
                // 服务端音频消息：读取序列号
                if (MsgTypeFlagBits.containsSequence(message.getTypeFlag())) {
                    message.setSequence(readInt(bais));
                    Log.d(TAG, "AudioOnlyServer: Read Sequence: "+message.getSequence());
                }
                break;
                
            case ERROR:
                // 错误消息：读取错误码
                message.setErrorCode(readInt(bais));
                Log.d(TAG, "Error message: Read ErrorCode: "+message.getErrorCode());
                break;
                
            default:
                throw new IOException("Cannot deserialize message with invalid type: " + message.getType());
        }

        // 读取事件相关字段（如果包含事件标志）
        if (MsgTypeFlagBits.containsEvent(message.getTypeFlag())) {
            message.setEvent(readInt(bais));
            Log.d(TAG, "Read Event: "+message.getEvent());
            
            // 读取会话ID（特定事件跳过）
            if (shouldSkipSessionId(message.getEvent())) {
                Log.d(TAG, "Skip reading session ID for event: "+message.getEvent());
            } else {
                int sessionIdLength = readInt(bais);
                Log.d(TAG, "Read SessionID length: "+sessionIdLength);
                
                // 添加安全检查，防止异常大的长度值
                if (sessionIdLength < 0 || sessionIdLength > 1024) {
                    throw new IOException("Invalid SessionID length: " + sessionIdLength);
                }
                
                if (sessionIdLength > 0) {
                    byte[] sessionIdBytes = new byte[sessionIdLength];
                    int bytesRead = bais.read(sessionIdBytes);
                    if (bytesRead != sessionIdLength) {
                        throw new IOException("Failed to read complete SessionID, expected: " + sessionIdLength + ", got: " + bytesRead);
                    }
                    message.setSessionId(new String(sessionIdBytes, StandardCharsets.UTF_8));
                    Log.d(TAG, "Read SessionID: "+message.getSessionId());
                }
            }
            
            // 读取连接ID（特定事件需要）
            if (shouldReadConnectId(message.getEvent())) {
                int connectIdLength = readInt(bais);
                Log.d(TAG, "Read ConnectID length: "+connectIdLength);
                
                // 添加安全检查
                if (connectIdLength < 0 || connectIdLength > 1024) {
                    throw new IOException("Invalid ConnectID length: " + connectIdLength);
                }
                
                if (connectIdLength > 0) {
                    byte[] connectIdBytes = new byte[connectIdLength];
                    int bytesRead = bais.read(connectIdBytes);
                    if (bytesRead != connectIdLength) {
                        throw new IOException("Failed to read complete ConnectID, expected: " + connectIdLength + ", got: " + bytesRead);
                    }
                    message.setConnectId(new String(connectIdBytes, StandardCharsets.UTF_8));
                    Log.d(TAG, "Read ConnectID: "+message.getConnectId());
                }
            } else {
                Log.d(TAG, "Skip reading ConnectID for event: "+message.getEvent());
            }
        }

        // 读取载荷
        int payloadLength = readInt(bais);
        Log.d(TAG, "Read payload length: "+payloadLength);
        
        // 添加安全检查，防止异常大的payload长度
        if (payloadLength < 0 || payloadLength > 10 * 1024 * 1024) { // 限制为10MB
            throw new IOException("Invalid payload length: " + payloadLength);
        }
        
        if (payloadLength > 0) {
            byte[] payload = new byte[payloadLength];
            int bytesRead = bais.read(payload);
            if (bytesRead != payloadLength) {
                throw new IOException("Failed to read complete payload, expected: " + payloadLength + ", got: " + bytesRead);
            }
            message.setPayload(payload);
            
            // 只对小于1KB的payload记录内容，避免日志爆炸
            if (payloadLength < 1024) {
                Log.d(TAG, "Read payload: "+new String(payload, StandardCharsets.UTF_8));
            } else {
                Log.d(TAG, "Read large payload: "+payloadLength+" bytes");
            }
        }

        return message;
    }

    private void writeHeader(ByteArrayOutputStream baos, Message message) {
        baos.write(versionAndHeaderSize);
        baos.write(message.getTypeAndFlagBits());
        baos.write(serializationAndCompression);
        baos.write(0); // 保留字节
    }

    private void writeInt(ByteBuffer buffer, int value) {
        buffer.order(ByteOrder.BIG_ENDIAN); // 与Go版本保持一致：binary.BigEndian
        buffer.putInt(value);
    }

    private void writeInt(ByteArrayOutputStream baos, int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN); // 与Go版本保持一致：binary.BigEndian
        buffer.putInt(value);
        baos.write(buffer.array(), 0, 4);
        Log.d(TAG, "Writing int (big-endian): "+value+", bytes: "+java.util.Arrays.toString(buffer.array()));
    }

    /**
     * 读取整数（与Go版本保持一致，使用大端序）
     */
    private int readInt(ByteArrayInputStream bais) throws IOException {
        byte[] bytes = new byte[4];
        int bytesRead = bais.read(bytes);
        if (bytesRead != 4) {
            throw new IOException("Expected 4 bytes for int, got: " + bytesRead);
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN); // 与Go版本保持一致：binary.BigEndian
        int value = buffer.getInt();
        Log.d(TAG, "Read int (big-endian): "+value+", bytes: "+java.util.Arrays.toString(bytes));
        return value;
    }
    
    /**
     * 判断是否应该跳过SessionID（与Go版本一致）
     * 事件 1,2,50,51,52 时跳过SessionID
     */
    private boolean shouldSkipSessionId(int event) {
        return event == 1 || event == 2 || event == 50 || event == 51 || event == 52;
    }
    
    /**
     * 判断是否应该写入ConnectID（与Go版本一致）
     * 仅事件 50,51,52 时写入ConnectID
     */
    private boolean shouldWriteConnectId(int event) {
        return event == 50 || event == 51 || event == 52;
    }
    
    /**
     * 判断是否应该读取ConnectID（与Go版本一致）
     * 仅事件 50,51,52 时读取ConnectID
     */
    private boolean shouldReadConnectId(int event) {
        return event == 50 || event == 51 || event == 52;
    }
}
