package com.lanqiDoctor.demo.ui.fragment;

import com.hjq.base.BaseFragment;
import com.lanqiDoctor.demo.ui.activity.MoodTrackActivity;

/**
 * 心情模块的基础 Fragment
 */
public abstract class BaseMoodFragment extends BaseFragment<MoodTrackActivity> {

    protected MoodTrackActivity getMoodTrackActivity() {
        return getAttachActivity();
    }

    protected void showToast(String message) {
        MoodTrackActivity activity = getMoodTrackActivity();
        if (activity != null) {
            activity.toast(message);
        }
    }

    protected void showToast(int resId) {
        MoodTrackActivity activity = getMoodTrackActivity();
        if (activity != null) {
            activity.toast(resId);
        }
    }
}
