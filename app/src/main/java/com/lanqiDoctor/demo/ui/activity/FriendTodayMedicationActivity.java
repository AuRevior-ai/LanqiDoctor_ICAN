package com.lanqiDoctor.demo.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.base.BaseActivity;
import com.hjq.toast.ToastUtils;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.dao.MedicationIntakeRecordDao;
import com.lanqiDoctor.demo.database.entity.MedicationIntakeRecord;
import com.lanqiDoctor.demo.http.api.FriendTodayIntakeApi;
import com.lanqiDoctor.demo.http.model.HttpData;
import com.lanqiDoctor.demo.manager.UserStateManager;
import com.lanqiDoctor.demo.model.TodayMedicationItem;
import com.lanqiDoctor.demo.ui.adapter.FriendTodayMedicationAdapter;
import com.lanqiDoctor.demo.util.NetworkErrorHandler;
import com.hjq.http.EasyHttp;
import com.hjq.http.listener.OnHttpListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;

/**
 * 亲友今日服药Activity
 *
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class FriendTodayMedicationActivity extends BaseActivity {

    private static final String TAG = "FriendTodayMedication";

    // Intent参数键
    private static final String EXTRA_FRIEND_USER_ID = "friend_user_id";
    private static final String EXTRA_FRIEND_NICKNAME = "friend_nickname";
    private static final String EXTRA_FRIEND_EMAIL = "friend_email";

    // UI组件
    private TextView tvFriendName;
    private TextView tvTodayDate;
    private TextView tvMedicationSummary;
    private RecyclerView rvTodayMedications;
    private LinearLayout llLoadingState;
    private LinearLayout llEmptyState;
    private LinearLayout llErrorState;
    private TextView tvErrorMessage;

    // 数据
    private String friendUserId;
    private String friendNickname;
    private String friendEmail;
    private FriendTodayMedicationAdapter adapter;
    private List<TodayMedicationItem> medicationItems;
    private UserStateManager userStateManager;

    /**
     * 启动亲友今日服药Activity
     */
    public static void start(Context context, String friendUserId, String friendNickname, String friendEmail) {
        Intent intent = new Intent(context, FriendTodayMedicationActivity.class);
        intent.putExtra(EXTRA_FRIEND_USER_ID, friendUserId);
        intent.putExtra(EXTRA_FRIEND_NICKNAME, friendNickname);
        intent.putExtra(EXTRA_FRIEND_EMAIL, friendEmail);

        if (!(context instanceof BaseActivity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.friend_today_medication_activity;
    }

    @Override
    protected void initView() {
        tvFriendName = findViewById(R.id.tv_friend_name);
        tvTodayDate = findViewById(R.id.tv_today_date);
        tvMedicationSummary = findViewById(R.id.tv_medication_summary);
        rvTodayMedications = findViewById(R.id.rv_today_medications);
        llLoadingState = findViewById(R.id.ll_loading_state);
        llEmptyState = findViewById(R.id.ll_empty_state);
        llErrorState = findViewById(R.id.ll_error_state);
        tvErrorMessage = findViewById(R.id.tv_error_message);

        // 设置RecyclerView
        rvTodayMedications.setLayoutManager(new LinearLayoutManager(this));
        medicationItems = new ArrayList<>();
        adapter = new FriendTodayMedicationAdapter(medicationItems);
        rvTodayMedications.setAdapter(adapter);

        // 显示今日日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.getDefault());
        tvTodayDate.setText(dateFormat.format(Calendar.getInstance().getTime()));

        // 绑定重试按钮点击事件
        findViewById(R.id.btn_retry).setOnClickListener(this);
    }

    @Override
    protected void initData() {
        userStateManager = UserStateManager.getInstance(this);

        // 获取Intent参数
        friendUserId = getIntent().getStringExtra(EXTRA_FRIEND_USER_ID);
        friendNickname = getIntent().getStringExtra(EXTRA_FRIEND_NICKNAME);
        friendEmail = getIntent().getStringExtra(EXTRA_FRIEND_EMAIL);

        if (friendUserId == null || friendUserId.isEmpty()) {
            ToastUtils.show("参数错误");
            finish();
            return;
        }

        // 设置亲友信息
        String displayName = friendNickname != null && !friendNickname.trim().isEmpty()
                ? friendNickname : friendEmail;
        tvFriendName.setText(displayName + "的今日服药");

        // 检查用户登录状态
        if (!userStateManager.isUserLoggedIn()) {
            ToastUtils.show("请先登录");
            finish();
            return;
        }

        // 加载今日服药数据
        loadFriendTodayMedications();
    }



    /*
    作用是将远程API的服药记录转换为本地数据库的服药记录
    */
    private MedicationIntakeRecord convertToLocalRecord(FriendTodayIntakeApi.Bean.IntakeRecord record) {
        
        MedicationIntakeRecord local = new MedicationIntakeRecord();

        local.setUserId(friendUserId); // 使用亲友的ID，而不是当前用户的ID

        local.setMedicationId(record.getMedicationId());
        local.setMedicationName(record.getMedicationName());
        local.setPlannedTime(record.getPlannedTime());
        local.setActualTime(record.getActualTime());
        local.setActualDosage(record.getActualDosage() != null ? record.getActualDosage() : record.getDosage());
        local.setStatus(record.isTaken() ? 1 : 0); // 1=已服用，0=未服用
        local.setNotes(record.getNotes());
        local.setCreateTime(System.currentTimeMillis());
        local.setUpdateTime(System.currentTimeMillis());

        // 添加日志确认userId设置正确
        Log.d(TAG, "转换亲友服药记录: 药物=" + record.getMedicationName() + 
            ", 亲友userId=" + friendUserId + ", 当前用户userId=" + userStateManager.getUserId());
        
        return local;
    }
    /**
     * 加载亲友今日服药数据
     */
    private void loadFriendTodayMedications() {
        Log.d(TAG, "开始加载亲友今日服药数据: " + friendUserId);

        showLoadingState();

        EasyHttp.get(this)
                .api(new FriendTodayIntakeApi(friendUserId))
                .request(new OnHttpListener<HttpData<FriendTodayIntakeApi.Bean>>() {

                    @Override
                    public void onStart(Call call) {
                        Log.d(TAG, "开始请求亲友今日服药数据");
                    }

                    @Override
                    public void onEnd(Call call) {
                        Log.d(TAG, "亲友今日服药数据请求结束");
                    }

                    @Override
                    public void onSucceed(HttpData<FriendTodayIntakeApi.Bean> data) {
                        Log.d(TAG, "亲友今日服药数据请求成功");
                        try {
                            FriendTodayIntakeApi.Bean result = data.getData();
                            if (result != null && result.isSuccess()) {
                                handleSuccessResponse(result);
                            } else {
                                String errorMsg = result != null ? result.getMessage() : "获取数据失败";
                                showErrorState(errorMsg);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "处理响应数据时发生异常", e);
                            showErrorState("数据处理失败");
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        Log.e(TAG, "请求亲友今日服药数据失败", e);
                        String errorMessage = NetworkErrorHandler.getAddFriendErrorMessage(e);
                        showErrorState(errorMessage);
                    }
                });
    }

    /**
     * 处理成功响应
     */
    private void handleSuccessResponse(FriendTodayIntakeApi.Bean result) {
        try {
            Log.d(TAG, "开始处理响应数据");

            // 更新亲友信息
            if (result.getFriendInfo() != null) {
                String displayName = result.getFriendInfo().getNickname() != null
                        ? result.getFriendInfo().getNickname()
                        : result.getFriendInfo().getEmail();
                tvFriendName.setText(displayName + "的今日服药");
                Log.d(TAG, "更新亲友信息: " + displayName);
            }

            // 更新统计信息
            int totalCount = result.getTotalCount();
            // 由于服务端没有直接返回takenCount和missedCount，需要我们计算
            List<FriendTodayIntakeApi.Bean.IntakeRecord> intakeRecords = result.getIntakeRecords();
            int takenCount = 0;
            int missedCount = 0;

            if (intakeRecords != null) {
                for (FriendTodayIntakeApi.Bean.IntakeRecord record : intakeRecords) {
                    if (record.isTaken()) {
                        takenCount++;
                    } else {
                        missedCount++;
                    }
                }
            }

            // 🔥 关键修复：使用支持多用户的数据库操作
            MedicationIntakeRecordDao intakeDao = new MedicationIntakeRecordDao(this);
            if (intakeRecords != null) {
                for (FriendTodayIntakeApi.Bean.IntakeRecord record : intakeRecords) {
                    try {
                        Log.d(TAG, "处理亲友服药记录: 药物=" + record.getMedicationName() + 
                            ", 亲友userId=" + friendUserId + ", 当前用户userId=" + userStateManager.getUserId());
                        
                        MedicationIntakeRecord localRecord = convertToLocalRecord(record);
                        
                        // 🔥 使用支持多用户的查找和插入方法
                        MedicationIntakeRecord existing = intakeDao.findByUserMedicationAndTime(
                            localRecord.getUserId(), 
                            localRecord.getMedicationName(), 
                            localRecord.getPlannedTime()
                        );
                        
                        if (existing == null) {
                            // 直接插入新记录
                            long newId = intakeDao.insert(localRecord);
                            Log.d(TAG, "插入新的亲友服药记录: " + localRecord.getMedicationName() + 
                                " ID: " + newId + " userId: " + localRecord.getUserId());
                        } else {
                            // 更新现有记录
                            localRecord.setId(existing.getId());
                            intakeDao.update(localRecord);
                            Log.d(TAG, "更新现有亲友服药记录: " + localRecord.getMedicationName() + 
                                " ID: " + existing.getId() + " userId: " + localRecord.getUserId());
                        }
                        
                    } catch (Exception e) {
                        Log.e(TAG, "处理亲友服药记录失败: " + record.getMedicationName(), e);
                        // 单条记录失败不影响其他记录的处理
                    }
                }
                Log.d(TAG, "已同步服务器服药数据到本地数据库");
            } else {
                Log.d(TAG, "没有服药记录，跳过同步");
            }

            updateSummaryInfo(totalCount, takenCount, missedCount);
            Log.d(TAG, "统计信息 - 总计:" + totalCount + ", 已服用:" + takenCount + ", 未服用:" + missedCount);

            // 转换并显示服药记录
            if (intakeRecords != null && !intakeRecords.isEmpty()) {
                Log.d(TAG, "开始转换服药记录，共 " + intakeRecords.size() + " 条");
                convertAndDisplayRecords(intakeRecords);
                showContentState();
            } else {
                Log.d(TAG, "没有服药记录，显示空状态");
                showEmptyState();
            }

            Log.d(TAG, "成功处理亲友今日服药数据");

        } catch (Exception e) {
            Log.e(TAG, "处理成功响应时发生异常", e);
            showErrorState("数据处理失败: " + e.getMessage());
        }
    }


    /**
     * 转换并显示服药记录
     */
    private void convertAndDisplayRecords(List<FriendTodayIntakeApi.Bean.IntakeRecord> intakeRecords) {
        medicationItems.clear();

        // 按时间分组处理
        String currentTimeGroup = null;
        for (int i = intakeRecords.size() - 1; i >= 0; i--) { // 保证该页面的时间是从早上到晚上排序的
//        for (FriendTodayIntakeApi.Bean.IntakeRecord record : intakeRecords) {
            FriendTodayIntakeApi.Bean.IntakeRecord record = intakeRecords.get(i);
            Log.d(TAG, "处理服药记录: " + record.getMedicationName() + ", 状态: " + record.getStatus());

            // 根据计划时间确定时间分组
            String timeGroup = getTimeGroupFromPlannedTime(record.getPlannedTime());

            // 添加时间段标题
            if (!timeGroup.equals(currentTimeGroup)) {
                currentTimeGroup = timeGroup;
                TodayMedicationItem headerItem = new TodayMedicationItem();
                headerItem.setItemType(TodayMedicationItem.TYPE_HEADER);
                headerItem.setTimeGroup(currentTimeGroup);
                medicationItems.add(headerItem);
                Log.d(TAG, "添加时间组标题: " + currentTimeGroup);
            }

            // 添加服药项目
            TodayMedicationItem medicationItem = new TodayMedicationItem();
            medicationItem.setItemType(TodayMedicationItem.TYPE_MEDICATION);
            medicationItem.setMedicationId(record.getMedicationId());
            medicationItem.setMedicationName(record.getMedicationName());
            medicationItem.setDosage(record.getActualDosage() != null ? record.getActualDosage() : record.getDosage());
            medicationItem.setUnit(record.getUnit());
            medicationItem.setPlannedTime(record.getPlannedTime());
            medicationItem.setTimeGroup(timeGroup);

            // 设置时间字符串
            if (record.getPlannedTime() != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                medicationItem.setTimeString(timeFormat.format(record.getPlannedTime()));
            }

            // 设置服用状态
            if (record.isTaken()) {
                // 创建一个简单的服药记录对象表示已服用
                medicationItem.setHasTakenToday(true);
            }

            medicationItems.add(medicationItem);
            Log.d(TAG, "添加服药项目: " + record.getMedicationName());
        }

        Log.d(TAG, "服药记录转换完成，共生成 " + medicationItems.size() + " 个项目");
        adapter.notifyDataSetChanged();
    }

    /**
     * 根据计划时间获取时间分组
     */
    private String getTimeGroupFromPlannedTime(Long plannedTime) {
        if (plannedTime == null) {
            return "其他";
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(plannedTime);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 6 && hour < 12) {
            return "早上";
        } else if (hour >= 12 && hour < 18) {
            return "下午";
        } else if (hour >= 18 && hour < 24) {
            return "晚上";
        } else {
            return "深夜";
        }
    }

    /**
     * 更新统计信息
     */
    private void updateSummaryInfo(int totalCount, int takenCount, int missedCount) {
        String summaryText = String.format(Locale.getDefault(),
                "今日共 %d 次服药，已完成 %d 次，未完成 %d 次",
                totalCount, takenCount, missedCount);
        tvMedicationSummary.setText(summaryText);
    }

    /**
     * 显示加载状态
     */
    private void showLoadingState() {
        llLoadingState.setVisibility(View.VISIBLE);
        rvTodayMedications.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);
        llErrorState.setVisibility(View.GONE);
    }

    /**
     * 显示内容状态
     */
    private void showContentState() {
        llLoadingState.setVisibility(View.GONE);
        rvTodayMedications.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
        llErrorState.setVisibility(View.GONE);
    }

    /**
     * 显示空状态
     */
    private void showEmptyState() {
        llLoadingState.setVisibility(View.GONE);
        rvTodayMedications.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
        llErrorState.setVisibility(View.GONE);
    }

    /**
     * 显示错误状态
     */
    private void showErrorState(String errorMessage) {
        llLoadingState.setVisibility(View.GONE);
        rvTodayMedications.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);
        llErrorState.setVisibility(View.VISIBLE);

        if (tvErrorMessage != null) {
            tvErrorMessage.setText(errorMessage);
        }
    }

    @Override
    public void onClick(View view) {
        // 可以添加刷新等操作
        if (view.getId() == R.id.btn_retry) {
            Log.d(TAG, "用户点击重试按钮");
            loadFriendTodayMedications();
        }
    }
}