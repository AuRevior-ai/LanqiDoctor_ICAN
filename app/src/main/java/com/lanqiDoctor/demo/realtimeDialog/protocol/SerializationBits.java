package com.lanqiDoctor.demo.realtimeDialog.protocol;

/**
 * 序列化方式
 */
public enum SerializationBits {
    RAW((byte) 0),
    JSON((byte) (0b1 << 4)),
    THRIFT((byte) (0b11 << 4)),
    CUSTOM((byte) (0b1111 << 4));

    private final byte value;

    SerializationBits(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
