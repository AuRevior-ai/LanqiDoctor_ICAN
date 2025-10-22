package com.lanqiDoctor.demo.realtimeDialog.protocol;

/**
 * 消息类型标志位
 */
public class MsgTypeFlagBits {
    public static final byte NO_SEQ = 0;           // 非终端包，无序列号
    public static final byte POSITIVE_SEQ = 0b1;   // 非终端包，有正序列号
    public static final byte LAST_NO_SEQ = 0b10;   // 最后一个包，无序列号
    public static final byte NEGATIVE_SEQ = 0b11;  // 最后一个包，有负序列号
    public static final byte WITH_EVENT = 0b100;   // 包含事件编号

    /**
     * 检查是否包含序列号（与Go版本ContainsSequence函数保持一致）
     * Go版本：return bits&MsgTypeFlagPositiveSeq == MsgTypeFlagPositiveSeq || bits&MsgTypeFlagNegativeSeq == MsgTypeFlagNegativeSeq
     */
    public static boolean containsSequence(byte flagBits) {
        return (flagBits & POSITIVE_SEQ) == POSITIVE_SEQ || (flagBits & NEGATIVE_SEQ) == NEGATIVE_SEQ;
    }

    /**
     * 检查是否包含事件
     */
    public static boolean containsEvent(byte flagBits) {
        return (flagBits & WITH_EVENT) != 0;
    }

    /**
     * 检查是否为最后一个包
     */
    public static boolean isLastPacket(byte flagBits) {
        return (flagBits & 0b10) != 0;
    }
}
