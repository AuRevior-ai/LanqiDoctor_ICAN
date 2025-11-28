package com.lanqiDoctor.demo.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 心情轨迹页面适配器
 */
public class MoodTrackPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragments = new ArrayList<>();

    public MoodTrackPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    public void setFragments(List<Fragment> fragmentList) {
        fragments.clear();
        if (fragmentList != null) {
            fragments.addAll(fragmentList);
        }
        notifyDataSetChanged();
    }

    public Fragment getFragmentAt(int position) {
        return fragments.get(position);
    }
}
