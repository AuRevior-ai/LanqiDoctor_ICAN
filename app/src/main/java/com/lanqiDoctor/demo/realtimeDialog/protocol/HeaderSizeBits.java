package com.lanqiDoctor.demo.realtimeDialog.protocol;
/**
 * 头部大小
 */
public enum HeaderSizeBits {
    SIZE_4((byte) 1),
    SIZE_8((byte) 2),
    SIZE_12((byte) 3),
    SIZE_16((byte) 4);

    private final byte value;

    HeaderSizeBits(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
