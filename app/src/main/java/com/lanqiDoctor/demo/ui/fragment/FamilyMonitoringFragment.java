package com.lanqiDoctor.demo.ui.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hjq.base.BaseFragment;
import com.hjq.toast.ToastUtils;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.manager.FamilyMemberManager;
import com.lanqiDoctor.demo.model.FamilyMember;
import com.lanqiDoctor.demo.ui.activity.AddFamilyMemberActivity;
import com.lanqiDoctor.demo.ui.activity.FamilyHealthDaily;
import com.lanqiDoctor.demo.ui.activity.HealthFunctionActivity;
import com.lanqiDoctor.demo.ui.activity.FriendTodayMedicationActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.umeng.analytics.pro.n.a.A;

/**
 * 家庭监护模式Fragment
 *
 * 迁移自myapplication2项目的家庭监护功能，包含：
 * - 家庭成员健康状态监控
 * - 健康数据共享
 * - 紧急情况通知
 * - 家庭健康报告
 *
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class FamilyMonitoringFragment extends BaseFragment {

    private static final String TAG = "FamilyMonitoringFragment";
    private static final int REQUEST_CODE_ADD_MEMBER = 1001;

    // 动态家庭成员容器
    private LinearLayout llFamilyMembersContainer;
    private LinearLayout llEmptyState;

    // 功能按钮
    private LinearLayout llHealthSharing;
    private LinearLayout llEmergencyAlert;
    private LinearLayout llFamilyReport;

    private FamilyMemberManager familyMemberManager;

    // 存储动态创建的成员卡片视图
    private Map<String, View> memberCardViews = new HashMap<>();

    @Override
    protected int getLayoutId() {
        return R.layout.family_monitoring_fragment;
    }

    @Override
    protected void initView() {
        // 动态家庭成员容器
        llFamilyMembersContainer = (LinearLayout) findViewById(R.id.ll_family_members_container);
        llEmptyState = (LinearLayout) findViewById(R.id.ll_empty_state);

        // 功能按钮
        llHealthSharing = (LinearLayout) findViewById(R.id.ll_health_sharing);
        llFamilyReport = (LinearLayout) findViewById(R.id.ll_family_report);
    }

    @Override
    protected void initData() {
        try {
            familyMemberManager = FamilyMemberManager.getInstance(getContext());

            // 初始化监听器
            initListener();

            // 加载家庭成员
            loadFamilyMembers();

            // 从服务器同步家庭成员
            syncFamilyMembersFromServer();
        } catch (Exception e) {
            Log.e(TAG, "初始化数据失败", e);
            ToastUtils.show("初始化失败，请重试");
        }
    }

    //但是怎么样获取其中的服药数据,还是个问题
    private void initListener() {
        // 添加家庭成员功能
        llHealthSharing.setOnClickListener(v -> {
            try {
                AddFamilyMemberActivity.startForResult(getAttachActivity(), REQUEST_CODE_ADD_MEMBER);
            } catch (Exception e) {
                Log.e(TAG, "启动添加家庭成员页面失败", e);
                ToastUtils.show("页面加载失败，请重试");
            }
        });

        // 家庭健康报告功能
        llFamilyReport.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), FamilyHealthDaily.class);
            startActivity(intent);
        });
    }

    /**
     * 加载家庭成员列表
     */
    private void loadFamilyMembers() {
        Log.d(TAG, "开始加载家庭成员列表");

        try {
            List<FamilyMember> members = familyMemberManager.getFamilyMembers();
            Log.d(TAG, "本地家庭成员数量: " + members.size());

            // 清空现有的成员卡片
            clearMemberCards();

            // 检查是否有家庭成员
            if (members.isEmpty()) {
                // 显示空状态
                showEmptyState();
            } else {
                // 隐藏空状态
                hideEmptyState();

                // 创建家庭成员卡片
                for (FamilyMember member : members) {
                    createMemberCard(member);
                }
            }

            Log.d(TAG, "家庭成员卡片创建完成");
        } catch (Exception e) {
            Log.e(TAG, "加载家庭成员列表失败", e);
            ToastUtils.show("加载家庭成员失败");
        }
    }

    /**
     * 显示空状态
     */
    private void showEmptyState() {
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏空状态
     */
    private void hideEmptyState() {
        if (llEmptyState != null) {
            llEmptyState.setVisibility(View.GONE);
        }
    }

    /**
     * 从服务器同步家庭成员
     */
    private void syncFamilyMembersFromServer() {
        Log.d(TAG, "开始从服务器同步家庭成员");

        try {
            familyMemberManager.syncFamilyMembersFromServer(new FamilyMemberManager.SyncCallback() {
                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "家庭成员同步成功: " + message);

                    // 在主线程中刷新UI
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            loadFamilyMembers();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "家庭成员同步失败: " + error);
                    // 同步失败不影响显示本地数据
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "同步家庭成员时发生异常", e);
        }
    }

    /**
     * 创建家庭成员卡片
     */
    private void createMemberCard(FamilyMember member) {
        try {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View cardView = inflater.inflate(R.layout.family_member_card, llFamilyMembersContainer, false);

            // 设置成员信息
            TextView tvMemberName = cardView.findViewById(R.id.tv_member_name);
            TextView tvMemberInfo = cardView.findViewById(R.id.tv_member_info);
            ImageView ivMemberAvatar = cardView.findViewById(R.id.iv_member_avatar);

            tvMemberName.setText(member.getDisplayName());
            tvMemberInfo.setText(member.getEmail());


            // 设置头像（可以后续扩展）
            ivMemberAvatar.setImageResource(R.drawable.ic_family_member_default);

            // 修改点击事件：跳转到亲友今日服药页面
            cardView.setOnClickListener(v -> {
                try {
                    FriendTodayMedicationActivity.start(
                            getContext(),
                            member.getUserId(),
                            member.getNickname(),
                            member.getEmail()
                    );
                } catch (Exception e) {
                    Log.e(TAG, "启动亲友今日服药页面失败", e);
                    ToastUtils.show("页面加载失败，请重试");
                }
            });

            // 设置长按删除事件
            cardView.setOnLongClickListener(v -> {
                showRemoveMemberDialog(member);
                return true;
            });

            // 添加到容器
            llFamilyMembersContainer.addView(cardView);

            // 保存到映射中，便于后续操作
            memberCardViews.put(member.getUserId(), cardView);

            Log.d(TAG, "创建家庭成员卡片: " + member.getDisplayName());

        } catch (Exception e) {
            Log.e(TAG, "创建家庭成员卡片失败: " + member.getDisplayName(), e);
        }
    }

    /**
     * 清空所有成员卡片
     */
    private void clearMemberCards() {
        if (llFamilyMembersContainer != null) {
            // 只移除家庭成员卡片，保留空状态视图
            for (int i = llFamilyMembersContainer.getChildCount() - 1; i >= 0; i--) {
                View child = llFamilyMembersContainer.getChildAt(i);
                if (child != llEmptyState) {
                    llFamilyMembersContainer.removeView(child);
                }
            }
        }
        memberCardViews.clear();
        Log.d(TAG, "清空所有家庭成员卡片");
    }

    /**
     * 显示删除成员确认对话框
     */
    private void showRemoveMemberDialog(FamilyMember member) {
        try {
            new AlertDialog.Builder(getContext())
                    .setTitle("删除家庭成员")
                    .setMessage("确定要删除家庭成员 \"" + member.getDisplayName() + "\" 吗？\n\n删除后将无法查看该成员的健康数据。")
                    .setPositiveButton("删除", (dialog, which) -> {
                        removeFamilyMember(member);
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "显示删除确认对话框失败", e);
            ToastUtils.show("操作失败，请重试");
        }
    }

    /**
     * 删除家庭成员
     */
    private void removeFamilyMember(FamilyMember member) {
        Log.d(TAG, "开始删除家庭成员: " + member.getDisplayName());

        try {
            // 显示进度提示
            ToastUtils.show("正在删除家庭成员...");

            familyMemberManager.removeFamilyMemberFromServer(member.getUserId(),
                    new FamilyMemberManager.RemoveCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Log.d(TAG, "家庭成员删除成功: " + message);

                            // 在主线程中更新UI
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    // 移除对应的卡片视图
                                    View cardView = memberCardViews.get(member.getUserId());
                                    if (cardView != null) {
                                        llFamilyMembersContainer.removeView(cardView);
                                        memberCardViews.remove(member.getUserId());
                                    }

                                    // 检查是否需要显示空状态
                                    if (memberCardViews.isEmpty()) {
                                        showEmptyState();
                                    }

                                    ToastUtils.show("家庭成员 \"" + member.getDisplayName() + "\" 已删除");
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "家庭成员删除失败: " + error);

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    ToastUtils.show("删除失败: " + error);
                                });
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "删除家庭成员时发生异常", e);
            ToastUtils.show("删除失败，请重试");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD_MEMBER && resultCode == RESULT_OK && data != null) {
            boolean memberAdded = data.getBooleanExtra("member_added", false);

            if (memberAdded) {
                String memberEmail = data.getStringExtra("member_email");
                String memberNickname = data.getStringExtra("member_nickname");
                Long memberId = data.getLongExtra("member_id", -1);

                if (memberId != -1) {
                    Log.d(TAG, "添加家庭成员成功: " + memberNickname);

                    try {
                        // 创建新的家庭成员对象，userId用String
                        FamilyMember newMember = new FamilyMember(String.valueOf(memberId), memberEmail, memberNickname);
                        newMember.setAddedTime(String.valueOf(System.currentTimeMillis()));
                        newMember.setOnline(false); // 默认离线状态

                        // 添加到管理器
                        familyMemberManager.addFamilyMember(newMember);

                        // 隐藏空状态
                        hideEmptyState();

                        // 创建卡片视图
                        createMemberCard(newMember);

                        ToastUtils.show("家庭成员 \"" + memberNickname + "\" 添加成功");
                    } catch (Exception e) {
                        Log.e(TAG, "处理添加家庭成员结果时发生异常", e);
                        ToastUtils.show("添加家庭成员成功，但显示可能有延迟");
                    }
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 页面恢复时刷新数据
        try {
            syncFamilyMembersFromServer();
        } catch (Exception e) {
            Log.e(TAG, "页面恢复时同步数据失败", e);
        }
    }
}