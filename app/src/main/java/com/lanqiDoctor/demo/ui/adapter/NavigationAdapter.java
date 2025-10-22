package com.lanqiDoctor.demo.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.base.BaseAdapter;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.app.AppAdapter;

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject
 *    time   : 2021/02/28
 *    desc   : 导航栏适配器
 */
public final class NavigationAdapter extends AppAdapter<NavigationAdapter.MenuItem>
        implements BaseAdapter.OnItemClickListener {

    /** 当前选中条目位置 */
    private int mSelectedPosition = 0;

    /** 导航栏点击监听 */
    @Nullable
    private OnNavigationListener mListener;

    public NavigationAdapter(Context context) {
        super(context);
        setOnItemClickListener(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder();
    }

    @Override
    protected RecyclerView.LayoutManager generateDefaultLayoutManager(Context context) {
        return new GridLayoutManager(context, getCount(), RecyclerView.VERTICAL, false);
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public void setSelectedPosition(int position) {
        mSelectedPosition = position;
        notifyDataSetChanged();
    }

    /**
     * 设置导航栏监听
     */
    public void setOnNavigationListener(@Nullable OnNavigationListener listener) {
        mListener = listener;
    }

    /**
     * {@link BaseAdapter.OnItemClickListener}
     */

    @Override
    public void onItemClick(RecyclerView recyclerView, View itemView, int position) {
        /*
         * recyclerView：触发点击的 RecyclerView。
            itemView：被点击的具体条目的视图。
            position：被点击条目的位置索引（在数据集中的位置）。
            当我们点击位置三,也就是"对话"的时候,这个函数将会被触发,到第三个if部分
         *
         * */
        if (mSelectedPosition == position) {
            return;
        }//重复点击部分,不用管,依旧将控制权交给

        if (mListener == null) {
            mSelectedPosition = position;//这几个其实是用1234的代号来的
            notifyDataSetChanged();
            return;
        }//如果没有设置监听器，更新选中状态后返回，不交给监听器处理

        if (mListener.onNavigationItemSelected(position)) {//调用方法给外部activity或者fragment,要是返回值是true就可以更新
            //选中位置并且刷新UI,注意这里是回调到了home_activity来处理地,所以我们要回到那里去看
            mSelectedPosition = position;//更新选中位置
            notifyDataSetChanged();//刷新UI
        }
    }

    private final class ViewHolder extends AppAdapter<?>.ViewHolder {

        private final ImageView mIconView;
        private final TextView mTitleView;

        private ViewHolder() {
            super(R.layout.home_navigation_item);
            mIconView = findViewById(R.id.iv_home_navigation_icon);
            mTitleView = findViewById(R.id.tv_home_navigation_title);
        }

        @Override
        public void onBindView(int position) {
            MenuItem item = getItem(position);
            mIconView.setImageDrawable(item.getDrawable());
            mTitleView.setText(item.getText());
            mIconView.setSelected(mSelectedPosition == position);
            mTitleView.setSelected(mSelectedPosition == position);
        }
    }

    public static class MenuItem {

        private final String mText;
        private final Drawable mDrawable;

        public MenuItem(String text, Drawable drawable) {
            mText = text;
            mDrawable = drawable;
        }

        public String getText() {
            return mText;
        }

        public Drawable getDrawable() {
            return mDrawable;
        }
    }

    public interface OnNavigationListener {

        boolean onNavigationItemSelected(int position);
    }
}