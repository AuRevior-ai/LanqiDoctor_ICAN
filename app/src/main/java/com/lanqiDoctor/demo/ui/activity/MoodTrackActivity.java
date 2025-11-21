package com.lanqiDoctor.demo.ui.activity;

import android.view.View;

import com.hjq.base.BaseActivity;
import com.lanqiDoctor.demo.R;

/**
 * 心情轨迹介绍页面（功能说明）
 */
public class MoodTrackActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_mood_track;
    }

    @Override
    protected void initView() {
        // 如果布局中存在标题返回按钮则设置返回逻辑
        if (findViewById(R.id.tb_title) != null) {
            findViewById(R.id.tb_title).setOnClickListener(v -> finish());
        }
    }

    @Override
    protected void initData() {
        // No-op: static informational page
    }

    @Override
    public void onClick(View v) {
        // no-op
    }
}
