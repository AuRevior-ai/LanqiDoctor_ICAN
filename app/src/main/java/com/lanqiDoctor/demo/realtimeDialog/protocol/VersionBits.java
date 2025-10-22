package com.lanqiDoctor.demo.realtimeDialog.protocol;

/**
 * 协议版本
 */
public enum VersionBits {
    VERSION_1((byte) (1 << 4)),
    VERSION_2((byte) (2 << 4)),
    VERSION_3((byte) (3 << 4)),
    VERSION_4((byte) (4 << 4));

    private final byte value;

    VersionBits(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
