package com.lanqiDoctor.demo.realtimeDialog.websocket;

import android.util.Log;

import com.lanqiDoctor.demo.realtimeDialog.protocol.BinaryProtocol;
import com.lanqiDoctor.demo.realtimeDialog.protocol.Message;
import com.lanqiDoctor.demo.realtimeDialog.protocol.MsgType;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebSocket客户端封装
 */
public class RealtimeWebSocketClient extends WebSocketClient {
    private static final String TAG = "RealtimeWebSocketClient";

    private final BinaryProtocol protocol;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final CountDownLatch connectionLatch = new CountDownLatch(1);

    /**
     * 消息监听器接口
     */
    public interface MessageListener {
        void onMessage(Message message);
        void onError(String error);
    }

    private MessageListener messageListener;

    public RealtimeWebSocketClient(URI serverUri, Map<String, String> headers, BinaryProtocol protocol) {
        super(serverUri, headers);
        this.protocol = protocol;
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        Log.i(TAG, "WebSocket connection opened successfully");
        Log.i(TAG, "Server handshake status: " + handshake.getHttpStatus());
        Log.i(TAG, "Server handshake status message: " + handshake.getHttpStatusMessage());
        
        // 记录服务器返回的所有响应头
        StringBuilder headers = new StringBuilder();
        // 使用Android兼容的迭代方式
        Iterator<String> headerIterator = handshake.iterateHttpFields();
        while (headerIterator.hasNext()) {
            String key = headerIterator.next();
            headers.append("\n  ").append(key).append(": ").append(handshake.getFieldValue(key));
        }
        Log.i(TAG, "Server response headers: " + headers.toString());
        
        isConnected.set(true);
        connectionLatch.countDown();
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "Received text message: " + message);
        // 处理文本消息（通常不会使用）
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        try {
            byte[] data = new byte[bytes.remaining()];
            bytes.get(data);

            // 只记录前几个字节，避免日志爆炸
            if (data.length > 0) {
                int prefixLength = Math.min(data.length, 20);
                byte[] prefix = new byte[prefixLength];
                System.arraycopy(data, 0, prefix, 0, prefixLength);
                Log.d(TAG, "Received frame: length=" + data.length + ", prefix=" + java.util.Arrays.toString(prefix));
            }

            // 尝试使用协议解析消息
            try {
                Message message = protocol.unmarshal(data);
                
                if (messageListener != null) {
                    messageListener.onMessage(message);
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to parse message with protocol: " + e.getMessage());
                
                // 如果是小数据，尝试作为文本处理；大数据直接跳过避免日志爆炸
                if (data.length < 1024) {
                    try {
                        String textContent = new String(data, StandardCharsets.UTF_8);
                        if (textContent.trim().startsWith("{")) {
                            Log.i(TAG, "Received JSON message: " + textContent);
                        }
                    } catch (Exception ex) {
                        Log.d(TAG, "Not a text message, size: " + data.length + " bytes");
                    }
                } else {
                    Log.d(TAG, "Large binary message received: " + data.length + " bytes");
                }
                
                if (messageListener != null) {
                    messageListener.onError("Failed to process message: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing binary message", e);
            if (messageListener != null) {
                messageListener.onError("Failed to process message: " + e.getMessage());
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i(TAG, "WebSocket connection closed: code=" + code + ", reason=" + reason + ", remote=" + remote);
        
        // 记录更详细的连接关闭信息，便于排查问题
        if (code == 1000) {
            Log.i(TAG, "Normal closure (1000): Connection closed normally");
        } else if (code == 1002) {
            Log.w(TAG, "Protocol error (1002): Server rejected the connection due to protocol error");
            Log.w(TAG, "This may indicate that request headers or protocol formats are incorrect");
        } else if (code == 1008) {
            Log.w(TAG, "Policy violation (1008): Server rejected the connection due to policy violation");
            Log.w(TAG, "This may indicate authentication failure or invalid parameters");
        }
        
        isConnected.set(false);
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG, "WebSocket error", ex);
        isConnected.set(false);
        
        // 记录更详细的连接信息，便于排查问题
        Log.e(TAG, "WebSocket connection details:");
        Log.e(TAG, "  URI: " + getURI());
        Log.e(TAG, "  Ready state: " + getReadyState());
        Log.e(TAG, "  Connection established: " + (getConnection() != null));
        
        if (messageListener != null) {
            messageListener.onError("WebSocket error: " + ex.getMessage());
        }
        
        // 尝试重新连接
        if (!isClosed() && !isClosing()) {
            Log.i(TAG, "Attempting to reconnect...");
            try {
                reconnect();
            } catch (Exception e) {
                Log.e(TAG, "Failed to reconnect", e);
            }
        }
    }

    /**
     * 发送消息
     */
    public void sendMessage(Message message) {
        if (!isConnected.get()) {
            Log.e(TAG, "WebSocket not connected");
            return;
        }

        try {
            byte[] data = protocol.marshal(message);
            
            // 对于音频数据，只打印长度和前几个字节，避免日志过大
            if (message.getType() == MsgType.AUDIO_ONLY_CLIENT && message.getEvent() == 200) {
                byte[] prefix = new byte[Math.min(10, data.length)];
                System.arraycopy(data, 0, prefix, 0, prefix.length);
                Log.d(TAG, "Sending audio frame: length=" + data.length + ", prefix=" + java.util.Arrays.toString(prefix));
            } else {
                // 对于其他消息类型，打印更详细信息
                Log.d(TAG, "Sending message: type=" + message.getType() + ", event=" + message.getEvent() + 
                      ", length=" + data.length + ", data=" + 
                      (data.length < 200 ? java.util.Arrays.toString(data) : "...(too long)"));
            }
            
            send(data);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send message", e);
            if (messageListener != null) {
                messageListener.onError("Failed to send message: " + e.getMessage());
            }
        }
    }

    /**
     * 等待连接建立
     */
    public boolean waitForConnection(long timeout, TimeUnit unit) {
        try {
            return connectionLatch.await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return isConnected.get() && !isClosed();
    }
}