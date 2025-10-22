package com.lanqiDoctor.demo.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.gyf.immersionbar.ImmersionBar;
import com.hjq.base.FragmentPagerAdapter;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.app.AppActivity;
import com.lanqiDoctor.demo.app.AppFragment;
import com.lanqiDoctor.demo.manager.ActivityManager;
import com.lanqiDoctor.demo.other.DoubleClickHelper;
import com.lanqiDoctor.demo.ui.adapter.NavigationAdapter;
import com.lanqiDoctor.demo.ui.fragment.FindFragment;
import com.lanqiDoctor.demo.ui.fragment.HomeFragment;
import com.lanqiDoctor.demo.ui.fragment.MessageFragment;
import com.lanqiDoctor.demo.ui.fragment.MineFragment;
import com.lanqiDoctor.demo.ui.fragment.ChatFragment;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2018/10/18
 *    desc   : 首页界面
 *    edit   : rrrrrzy
 *    time   : 2025/06/20
 */
public final class HomeActivity extends AppActivity
        implements NavigationAdapter.OnNavigationListener {

    private static final String INTENT_KEY_IN_FRAGMENT_INDEX = "fragmentIndex";
    private static final String INTENT_KEY_IN_FRAGMENT_CLASS = "fragmentClass";

    private ViewPager mViewPager;
    private RecyclerView mNavigationView;

    private NavigationAdapter mNavigationAdapter;
    private FragmentPagerAdapter<AppFragment<?>> mPagerAdapter;

    public static void start(Context context) {
        start(context, HomeFragment.class);
    }

    public static void start(Context context, Class<? extends AppFragment<?>> fragmentClass) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(INTENT_KEY_IN_FRAGMENT_CLASS, fragmentClass);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.home_activity;
    }

    @Override
    protected void initView() {
        mViewPager = findViewById(R.id.vp_home_pager);
        mNavigationView = findViewById(R.id.rv_home_navigation);

        mNavigationAdapter = new NavigationAdapter(this);
        mNavigationAdapter.addItem(new NavigationAdapter.MenuItem("首页",
                ContextCompat.getDrawable(this, R.drawable.home_home_selector)));
        mNavigationAdapter.addItem(new NavigationAdapter.MenuItem("更多",
                ContextCompat.getDrawable(this, R.drawable.home_found_selector)));
        mNavigationAdapter.addItem(new NavigationAdapter.MenuItem("我的",
                ContextCompat.getDrawable(this, R.drawable.home_message_selector)));
        //这里就没有对话导航项了，我们只需要四个东西：更多，首页，我的，关于我们
        mNavigationAdapter.addItem(new NavigationAdapter.MenuItem("对话",
                ContextCompat.getDrawable(this, R.drawable.home_chat_selector)));
        mNavigationAdapter.addItem(new NavigationAdapter.MenuItem("关于我们",
                ContextCompat.getDrawable(this, R.drawable.home_me_selector)));
        //这只是 向适配器添加了一个数据项（MenuItem），包含文字和图标，但此时还未绑定跳转逻辑。
        /*
        * 关键的绑定位置是在NavigationAdapter部分,我们可以转向那里去看看
        *   用户->>Adapter: 点击某个导航项
            Adapter->>Adapter: 检查是否重复点击(mSelectedPosition)
            Adapter->>Activity/Fragment: 回调mListener.onNavigationItemSelected(position)
            Activity/Fragment->>FragmentManager: 执行Fragment切换
            FragmentManager-->>用户: 显示新界面
        * */
        mNavigationAdapter.setOnNavigationListener(this);
        mNavigationView.setAdapter(mNavigationAdapter);
    }

    @Override
    protected void initData() {
        mPagerAdapter = new FragmentPagerAdapter<>(this);
        mPagerAdapter.addFragment(HomeFragment.newInstance());
        mPagerAdapter.addFragment(FindFragment.newInstance());
        mPagerAdapter.addFragment(MessageFragment.newInstance());
        // 添加Chat Fragment
        mPagerAdapter.addFragment(ChatFragment.newInstance());
        mPagerAdapter.addFragment(MineFragment.newInstance());//这里是绑定了所有的导航
        /*
        *    位置索引 | 导航文本  | Fragment
            0       | 首页     | HomeFragment
            1       | 更多     | FindFragment
            2       | 我的     | MessageFragment
            3       | 对话     | ChatFragment
            4       | 关于我们 | MineFragment*/
        mViewPager.setAdapter(mPagerAdapter);

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        switchFragment(mPagerAdapter.getFragmentIndex(getSerializable(INTENT_KEY_IN_FRAGMENT_CLASS)));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 保存当前 Fragment 索引位置
        outState.putInt(INTENT_KEY_IN_FRAGMENT_INDEX, mViewPager.getCurrentItem());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // 恢复当前 Fragment 索引位置
        switchFragment(savedInstanceState.getInt(INTENT_KEY_IN_FRAGMENT_INDEX));
    }

    private void switchFragment(int fragmentIndex) {
        if (fragmentIndex == -1) {
            return;
        }

        switch (fragmentIndex) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                mViewPager.setCurrentItem(fragmentIndex);
                mNavigationAdapter.setSelectedPosition(fragmentIndex);
                break;
            default:
                break;
        }
    }

    /**
     * {@link NavigationAdapter.OnNavigationListener}
     */

    @Override
    public boolean onNavigationItemSelected(int position) {//这就是在adapter处交给外部处理地部分了
        switch (position) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                mViewPager.setCurrentItem(position);//调用函数切换页面
                /*
                用户->>NavigationAdapter: 点击"对话"按钮(position=3)
                NavigationAdapter->>HomeActivity: onNavigationItemSelected(3)
                HomeActivity->>ViewPager: setCurrentItem(3)
                ViewPager->>FragmentPagerAdapter: 请求第3个Fragment
                FragmentPagerAdapter->>ChatFragment: 实例化/获取缓存
                FragmentPagerAdapter->>ViewPager: 显示ChatFragment
                * */
                return true;//切换到当前想要去的界面,然后返回true
            default:
                return false;
        }
    }

    @NonNull
    @Override
    protected ImmersionBar createStatusBarConfig() {
        return super.createStatusBarConfig()
                // 指定导航栏背景颜色
                .navigationBarColor(R.color.white);
    }

    @Override
    public void onBackPressed() {
        if (!DoubleClickHelper.isOnDoubleClick()) {
            toast(R.string.home_exit_hint);
            return;
        }

        // 移动到上一个任务栈，避免侧滑引起的不良反应
        moveTaskToBack(false);
        postDelayed(() -> {
            // 进行内存优化，销毁掉所有的界面
            ActivityManager.getInstance().finishAllActivities();
            // 销毁进程（注意：调用此 API 可能导致当前 Activity onDestroy 方法无法正常回调）
            // System.exit(0);
        }, 300);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.setAdapter(null);
        mNavigationView.setAdapter(null);
        mNavigationAdapter.setOnNavigationListener(null);
    }
}