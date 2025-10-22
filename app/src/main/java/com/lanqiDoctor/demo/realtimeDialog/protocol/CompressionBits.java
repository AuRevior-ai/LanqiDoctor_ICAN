package com.lanqiDoctor.demo.realtimeDialog.protocol;

/**
 * 压缩方式
 */
public enum CompressionBits {
    NONE((byte) 0),
    GZIP((byte) 0b1),
    CUSTOM((byte) 0b1111);

    private final byte value;

    CompressionBits(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
