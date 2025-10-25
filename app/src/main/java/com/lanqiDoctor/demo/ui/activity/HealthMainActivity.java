package com.lanqiDoctor.demo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hjq.base.BaseActivity;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.app.AppActivity;
import com.lanqiDoctor.demo.ui.adapter.HealthPagerAdapter;
import com.lanqiDoctor.demo.ui.fragment.DailyMedicationFragment;
import com.lanqiDoctor.demo.ui.fragment.FamilyMonitoringFragment;
import com.lanqiDoctor.demo.ui.fragment.HealthCenterFragmentNew;
import com.lanqiDoctor.demo.ui.fragment.MobileHealthAssistantFragment;
import com.lanqiDoctor.demo.ui.activity.TodayMedicationActivity;
import com.lanqiDoctor.demo.manager.CloudSyncManager;
import com.umeng.commonsdk.debug.I;

import java.util.ArrayList;
import java.util.List;

/**
 * 健康管理主页面
 *
 * 迁移自myapplication2项目的主要功能，包含三个功能标签页面：
 * - 健康中心：症状轨迹可视化、语音对话、文字输入、药品图片识别
 * - 家庭监护：家庭成员健康监控
 * - 健康助手：与手机健康数据联动
 *
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class HealthMainActivity extends AppActivity {
    //    public class HealthMainActivity extends BaseActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private HealthPagerAdapter pagerAdapter;
    private CloudSyncManager cloudSyncManager;

    // 底部导航按钮
    private LinearLayout llHome;
    private LinearLayout llAdd;
    private LinearLayout llProfile;

    private List<Fragment> fragmentList;
    private List<String> titleList;

    @Override
    protected int getLayoutId() {
        return R.layout.health_main_activity;
    }

    @Override
    protected void initView() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        // 初始化底部导航按钮
        llHome = findViewById(R.id.ll_home);
        llAdd = findViewById(R.id.ll_add);
        llProfile = findViewById(R.id.ll_profile);

        setupViewPager();
        setupTabLayout();
    }

    @Override
    protected void initData() {

        // 初始化Fragment列表
        fragmentList = new ArrayList<>();
        fragmentList.add(new HealthCenterFragmentNew());  // AI助手
        fragmentList.add(new DailyMedicationFragment());  // 日常服药
        fragmentList.add(new FamilyMonitoringFragment());  // 家庭监护
        fragmentList.add(new MobileHealthAssistantFragment());  // 健康助手

        // 初始化标题列表
        titleList = new ArrayList<>();
        titleList.add("AI助手");
        titleList.add("日常服药");
        titleList.add("家庭监护");
        titleList.add("健康助手");

        pagerAdapter.setFragmentList(fragmentList);

        cloudSyncManager = CloudSyncManager.getInstance(this);

        // 在应用启动时尝试自动同步
        postDelayed(() -> {
            if (cloudSyncManager.canSyncToCloud()) {
                new Thread(() -> {
                    cloudSyncManager.autoSyncInBackground();
                }).start();
            }
        }, 2000); // 延迟2秒执行，避免影响启动速度

        initListener();
    }

    private void initListener() {
        // 底部导航点击事件
        llHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回到主页面 - 可以关闭当前Activity或跳转到HomeActivity
//                finish();这会直接关闭掉应用，不能这么做
                Intent intent = new Intent(HealthMainActivity.this, HealthMainActivity.class);
                startActivity(intent);
                Toast.makeText(HealthMainActivity.this, "返回首页", Toast.LENGTH_SHORT).show();
            }
        });

        llAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到今日服药情况页面
                Intent intent = new Intent(HealthMainActivity.this, TodayMedicationActivity.class);
                startActivity(intent);
            }
        });

        llProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 个人中心 - 跳转到用户信息页面
                startActivity(new Intent(HealthMainActivity.this, UserProfileActivity.class));
            }
        });
    }

    /**
     * 设置ViewPager
     */
    private void setupViewPager() {
        pagerAdapter = new HealthPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);
    }

    /**
     * 设置TabLayout
     */
    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(titleList.get(position));
        }).attach();
    }
    @Override
    public void onBackPressed() {
        // 1. 获取当前显示的 Fragment
        Fragment currentFragment = fragmentList.get(viewPager.getCurrentItem());

        // 2. 优先让 Fragment 处理返回键
        if (currentFragment instanceof HealthCenterFragmentNew) {
            boolean isHandled = ((HealthCenterFragmentNew) currentFragment).onBackPressed();
            if (isHandled) return; // 如果 Fragment 已处理，则不再执行默认逻辑
        }
        // 3. 默认逻辑（如退出 Activity）
        super.onBackPressed();
    }
} 