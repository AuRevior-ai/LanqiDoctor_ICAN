package com.lanqiDoctor.demo.ui.fragment;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.base.BaseAdapter;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.app.AppActivity;
import com.lanqiDoctor.demo.app.TitleBarFragment;
import com.lanqiDoctor.demo.ui.adapter.StatusAdapter;
import com.lanqiDoctor.demo.manager.DatabaseManager;
import com.lanqiDoctor.demo.database.entity.HealthInfo;
import com.hjq.widget.layout.WrapRecyclerView;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2020/07/10
 *    desc   : 加载案例 Fragment
 */
public final class StatusFragment extends TitleBarFragment<AppActivity>
        implements OnRefreshLoadMoreListener,
        BaseAdapter.OnItemClickListener {

    public static StatusFragment newInstance() {
        return new StatusFragment();
    }

    private SmartRefreshLayout mRefreshLayout;
    private WrapRecyclerView mRecyclerView;

    private StatusAdapter mAdapter;

    /*
    DataBase 测试部分：======开始========
     */
    private HealthInfo mHealthInfo;

    private void initHealth() {
        mHealthInfo = new HealthInfo();
        mHealthInfo.setAge(25); // 改为合理的年龄值
        mHealthInfo.setBloodSugar(12.4);
        mHealthInfo.setHeight(156.0); // 改为厘米单位
        mHealthInfo.setHeartRate(90);
        // 移除手动设置ID，让数据库自动生成
    }

    private DatabaseManager mDbManager; // 改为非final，在initView中初始化

    private void useDB() {
        if (mDbManager != null && mHealthInfo != null) {
            // 在后台线程执行数据库操作
            new Thread(() -> {
                long result = mDbManager.saveHealthInfo(mHealthInfo);
                // 在主线程显示结果
                post(() -> {
                    if (result > 0) {
                        toast("数据保存成功，ID: " + result);
                    } else {
                        toast("数据保存失败");
                    }
                });
            }).start();
        }
    }

    private void showDB() {
        if (mDbManager != null) {
            // 在后台线程查询数据库
            new Thread(() -> {
                List<HealthInfo> dbList = mDbManager.getAllHealthInfo();
                // 在主线程显示结果
                post(() -> {
                    if (dbList != null && !dbList.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("共有 ").append(dbList.size()).append(" 条记录:\n");
                        for (int i = 0; i < Math.min(dbList.size(), 3); i++) {
                            HealthInfo info = dbList.get(i);
                            sb.append("记录").append(i + 1).append(": ");
                            if (info.getAge() != null) sb.append("年龄:").append(info.getAge()).append(" ");
                            if (info.getHeight() != null) sb.append("身高:").append(info.getHeight()).append("cm ");
                            if (info.getWeight() != null) sb.append("体重:").append(info.getWeight()).append("kg ");
                            sb.append("\n");
                        }
                        if (dbList.size() > 3) {
                            sb.append("...(还有").append(dbList.size() - 3).append("条记录)");
                        }
                        toast(sb.toString());
                    } else {
                        toast("暂无数据");
                    }
                });
            }).start();
        }
    }
    @Override
    protected int getLayoutId() {
        return R.layout.status_fragment;
    }

    @Override
    protected void initView() {
        mRefreshLayout = findViewById(R.id.rl_status_refresh);
        mRecyclerView = findViewById(R.id.rv_status_list);
        
        // 在这里初始化DatabaseManager，使用正确的Context
        mDbManager = DatabaseManager.getInstance(getAttachActivity());
        initHealth();
        useDB();
        
        mAdapter = new StatusAdapter(getAttachActivity());
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        TextView headerView = mRecyclerView.addHeaderView(R.layout.picker_item);
        headerView.setText("我是头部");
//        headerView.setOnClickListener(v -> toast("点击了头部"));
        headerView.setOnClickListener(v -> showDB());

        TextView footerView = mRecyclerView.addFooterView(R.layout.picker_item);
        footerView.setText("我是尾部");
        footerView.setOnClickListener(v -> toast("点击了尾部"));

        mRefreshLayout.setOnRefreshLoadMoreListener(this);
    }

    @Override
    protected void initData() {
        mAdapter.setData(analogData());
    }

    /**
     * 模拟数据
     */
    private List<String> analogData() {
        List<String> data = new ArrayList<>();
        for (int i = mAdapter.getCount(); i < mAdapter.getCount() + 20; i++) {
            data.add("我是第" + i + "条目");
        }
        return data;
    }

    /**
     * {@link BaseAdapter.OnItemClickListener}
     *
     * @param recyclerView      RecyclerView对象
     * @param itemView          被点击的条目对象
     * @param position          被点击的条目位置
     */
    @Override
    public void onItemClick(RecyclerView recyclerView, View itemView, int position) {
        toast(mAdapter.getItem(position));
    }

    /**
     * {@link OnRefreshLoadMoreListener}
     */

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        postDelayed(() -> {
            mAdapter.clearData();
            mAdapter.setData(analogData());
            mRefreshLayout.finishRefresh();
        }, 1000);
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        postDelayed(() -> {
            mAdapter.addData(analogData());
            mRefreshLayout.finishLoadMore();

            mAdapter.setLastPage(mAdapter.getCount() >= 100);
            mRefreshLayout.setNoMoreData(mAdapter.isLastPage());
        }, 1000);
    }
}