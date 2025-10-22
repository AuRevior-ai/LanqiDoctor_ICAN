package com.lanqiDoctor.demo.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 健康功能页面适配器
 * 
 * 管理三个健康功能页面的Fragment切换：
 * - 健康中心
 * - 家庭监护模式  
 * - 联动手机健康助手
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class HealthPagerAdapter extends FragmentStateAdapter {
    
    private List<Fragment> fragmentList = new ArrayList<>();
    
    public HealthPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }
    
    @Override
    public int getItemCount() {
        return fragmentList.size();
    }
    
    /**
     * 设置Fragment列表
     * @param fragmentList Fragment列表
     */
    public void setFragmentList(List<Fragment> fragmentList) {
        this.fragmentList = fragmentList;
        notifyDataSetChanged();
    }
} 