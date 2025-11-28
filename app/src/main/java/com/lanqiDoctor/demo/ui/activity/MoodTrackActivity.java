package com.lanqiDoctor.demo.ui.activity;

import android.content.Intent;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hjq.bar.TitleBar;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.app.AppActivity;
import com.lanqiDoctor.demo.entity.MoodRecord;
import com.lanqiDoctor.demo.ui.adapter.MoodTrackPagerAdapter;
import com.lanqiDoctor.demo.ui.fragment.MoodCalendarFragment;
import com.lanqiDoctor.demo.ui.fragment.MoodMemoriesFragment;
import com.lanqiDoctor.demo.ui.fragment.MoodStatisticsFragment;
import com.lanqiDoctor.demo.ui.fragment.MoodTrackPage;

import java.util.ArrayList;
import java.util.List;

/**
 * 心情轨迹功能入口
 */
public class MoodTrackActivity extends AppActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private MoodTrackPagerAdapter pagerAdapter;

    private MoodCalendarFragment calendarFragment;
    private MoodMemoriesFragment memoriesFragment;
    private MoodStatisticsFragment statisticsFragment;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_mood_track;
    }

    @Override
    protected void initView() {
        TitleBar titleBar = findViewById(R.id.tb_title);
        if (titleBar != null) {
            titleBar.setTitle(R.string.mood_track_title);
        }

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        setupPagerAndTabs();
    }

    @Override
    protected void initData() {
        // 首次进入时刷新首页数据
        if (calendarFragment != null) {
            calendarFragment.onPageSelected();
        }
    }

    private void setupPagerAndTabs() {
        pagerAdapter = new MoodTrackPagerAdapter(this);
        calendarFragment = MoodCalendarFragment.newInstance();
        memoriesFragment = MoodMemoriesFragment.newInstance();
        statisticsFragment = MoodStatisticsFragment.newInstance();

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(calendarFragment);
        fragments.add(memoriesFragment);
        fragments.add(statisticsFragment);
        pagerAdapter.setFragments(fragments);

        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(fragments.size());

        List<String> titles = new ArrayList<>();
        titles.add(getString(R.string.mood_tab_calendar));
        titles.add(getString(R.string.mood_tab_memories));
        titles.add(getString(R.string.mood_tab_statistics));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(titles.get(position))).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Fragment fragment = pagerAdapter.getFragmentAt(position);
                if (fragment instanceof MoodTrackPage) {
                    ((MoodTrackPage) fragment).onPageSelected();
                }
            }
        });
    }

    public void openCreateRecordEditor(long dayMillis) {
        Intent intent = new Intent(this, MoodRecordEditorActivity.class);
        intent.putExtra(MoodRecordEditorActivity.EXTRA_DAY, dayMillis);
        startActivity(intent);
    }

    public void openEditRecordEditor(MoodRecord record) {
        Intent intent = new Intent(this, MoodRecordEditorActivity.class);
        intent.putExtra(MoodRecordEditorActivity.EXTRA_DAY, record.getRecordDate());
        intent.putExtra(MoodRecordEditorActivity.EXTRA_RECORD, record);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        // no-op
    }
}
