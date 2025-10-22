package com.lanqiDoctor.demo.ui.activity;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class LocalLLMActivity extends AppCompatActivity {
    private static final String TAG = "LocalLLMActivity";
    private long llmHandle = 0;

    static {
        System.loadLibrary("aigc");
    }

    // Native methods
    public native long init();
    public native void reset(long handle);
    public native void release(long handle);
    public native void forward(long handle, String prompt);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化 LLM
        initLLM();
    }

    private void initLLM() {
        llmHandle = init();
        if (llmHandle != 0) {
            Log.i(TAG, "LLM initialized successfully");
        } else {
            Log.e(TAG, "LLM initialization failed");
        }
    }

    // 这个方法被 native 代码调用来更新 UI
    public void updateUI(String result) {
        runOnUiThread(() -> {
            Log.d(TAG, "LLM output: " + result);
            // 在这里处理 LLM 的输出，目前只需要记录日志
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (llmHandle != 0) {
            release(llmHandle);
            llmHandle = 0;
        }
    }
}