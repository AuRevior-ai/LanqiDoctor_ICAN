package com.lanqiDoctor.demo.realtimeDialog.protocol;

/**
 * 消息类型枚举
 */
public enum MsgType {
    INVALID(0),
    FULL_CLIENT(1),
    AUDIO_ONLY_CLIENT(2),
    FULL_SERVER(9),
    AUDIO_ONLY_SERVER(11),
    FRONT_END_RESULT_SERVER(12),
    ERROR(15);

    private final int value;

    MsgType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MsgType fromValue(int value) {
        for (MsgType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return INVALID;
    }

    /**
     * 转换为二进制位，与Go版本保持一致
     * Go版本映射:
     * msgTypeToBits = map[MsgType]uint8{
     *   MsgTypeFullClient:           0b1 << 4,
     *   MsgTypeAudioOnlyClient:      0b10 << 4,
     *   MsgTypeFullServer:           0b1001 << 4,
     *   MsgTypeAudioOnlyServer:      0b1011 << 4,
     *   MsgTypeFrontEndResultServer: 0b1100 << 4,
     *   MsgTypeError:                0b1111 << 4,
     * }
     */
    public byte toBits() {
        switch (this) {
            case FULL_CLIENT:
                return (byte) (0b0001 << 4);
            case AUDIO_ONLY_CLIENT:
                return (byte) (0b0010 << 4);
            case FULL_SERVER:
                return (byte) (0b1001 << 4);
            case AUDIO_ONLY_SERVER:
                return (byte) (0b1011 << 4);
            case FRONT_END_RESULT_SERVER:
                return (byte) (0b1100 << 4);
            case ERROR:
                return (byte) (0b1111 << 4);
            default:
                return 0;
        }
    }

    /**
     * 从二进制位转换为消息类型，与Go版本保持一致
     */
    public static MsgType fromBits(byte bits) {
        byte typeBits = (byte) (bits & 0b11110000);
        switch (typeBits) {
            case (byte) (0b0001 << 4):
                return FULL_CLIENT;
            case (byte) (0b0010 << 4):
                return AUDIO_ONLY_CLIENT;
            case (byte) (0b1001 << 4):
                return FULL_SERVER;
            case (byte) (0b1011 << 4):
                return AUDIO_ONLY_SERVER;
            case (byte) (0b1100 << 4):
                return FRONT_END_RESULT_SERVER;
            case (byte) (0b1111 << 4):
                return ERROR;
            default:
                return INVALID;
        }
    }
}
