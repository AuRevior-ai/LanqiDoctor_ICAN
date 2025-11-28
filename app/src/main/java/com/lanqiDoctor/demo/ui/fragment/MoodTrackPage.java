package com.lanqiDoctor.demo.ui.fragment;

/**
 * 心情轨迹页面通用行为
 */
public interface MoodTrackPage {

    /**
     * 当页面被选中时回调，可用于刷新数据。
     */
    default void onPageSelected() {
        // default no-op
    }

    /**
     * 当用户点击添加按钮时回调。
     */
    void onAddRecordRequested();
}
