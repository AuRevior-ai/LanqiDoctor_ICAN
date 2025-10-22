package com.lanqiDoctor.demo.ocr;

import android.content.Context;
import android.util.Log;

import com.lanqiDoctor.demo.common.SDKExceptions;
import com.lanqiDoctor.demo.common.Utils;

public class Native {
    static {
        System.loadLibrary("Native");
    }

    private long ctx = 0;
    private boolean run_status = false;

    public boolean init(Context mContext,
                        String detModelPath,
                        String clsModelPath,
                        String recModelPath,
                        String configPath,
                        String labelPath,
                        int cputThreadNum,
                        String cpuPowerMode) {
        ctx = nativeInit(
                detModelPath,
                clsModelPath,
                recModelPath,
                configPath,
                labelPath,
                cputThreadNum,
                cpuPowerMode);
        return ctx != 0; // 修改：返回 true 表示成功，false 表示失败
    }

    public boolean release() {
        if (ctx == 0) {
            return false;
        }
        return nativeRelease(ctx);
    }

    public boolean process(int inTextureId, int outTextureId, int textureWidth, int textureHeight, String savedImagePath) {
        if (ctx == 0) {
            return false;
        }
        run_status = nativeProcess(ctx, inTextureId, outTextureId, textureWidth, textureHeight, savedImagePath);
        return run_status;
    }

    public String[] processImageFile(String imagePath) {
        if (ctx == 0) {
            return new String[0];
        }
        return nativeProcessImageFile(ctx, imagePath);
    }

    public String[] getRecognitionResults() {
        if (ctx == 0) {
            return new String[0];
        }
        return nativeGetRecognitionResults(ctx);
    }

    public static native long nativeInit(String detModelPath,
                                         String clsModelPath,
                                         String recModelPath,
                                         String configPath,
                                         String labelPath,
                                         int cputThreadNum,
                                         String cpuPowerMode);

    public static native boolean nativeRelease(long ctx);

    public static native boolean nativeProcess(long ctx, int inTextureId, int outTextureId, int textureWidth, int textureHeight, String savedImagePath);

    public static native String[] nativeProcessImageFile(long ctx, String imagePath);

    public static native String[] nativeGetRecognitionResults(long ctx);
}
