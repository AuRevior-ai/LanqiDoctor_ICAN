package com.lanqiDoctor.demo.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hjq.base.BaseActivity;
import com.hjq.toast.ToastUtils;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.dao.MedicationRecordDao;
import com.lanqiDoctor.demo.database.entity.MedicationRecord;
import com.lanqiDoctor.demo.manager.MedicationAlarmManager;
import com.lanqiDoctor.demo.manager.CloudSyncManager;
import com.lanqiDoctor.demo.ui.adapter.MedicationRecordAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 在用药管理Activity
 *
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class MedicationManagementActivity extends BaseActivity {//又是从这个baseActivity里面拿过来的神人构造函数
//别忘了三个继承函数的调用时机都是在创建页面的时候

    private RecyclerView rvMedicationList;//用药列表的RecyclerView
    private LinearLayout tvEmptyView; //空数据的提醒视图
    private FloatingActionButton fabAddMedication;//添加药品的浮动按钮

    private MedicationRecordAdapter adapter;//用药列表的适配器(时刻记得适配器是用来打包数据的)
    private MedicationRecordDao medicationDao;//药品数据的信息库
    private List<MedicationRecord> medicationList;//药品数据的列表

    // 闹钟管理器
    private MedicationAlarmManager alarmManager;//用药提醒闹钟管理器
    private CloudSyncManager cloudSyncManager;//云端同步管理器

    private static final int REQUEST_TIME_SETTINGS = 1002;//时间设置请求码
    private static final int REQUEST_ADD_MEDICATION = 1001;//添加药品请求码

    @Override
    protected int getLayoutId() {//返回布局文件ID
        return R.layout.medication_management_activity;
    }

    @Override
    protected void initView() {//初始化视图组件
        rvMedicationList = findViewById(R.id.rv_medication_list);//用药列表的视图
        tvEmptyView = findViewById(R.id.tv_empty_view);//空状态的视图
        fabAddMedication = findViewById(R.id.fab_add_medication);//添加药品的浮动按钮

        // 设置RecyclerView
        rvMedicationList.setLayoutManager(new LinearLayoutManager(this));//设置布局管理器
        medicationList = new ArrayList<>();//初始化数据列表,这个是药品的数据列表
        adapter = new MedicationRecordAdapter(medicationList);//初始化适配器,绑定数据为药品列表
        rvMedicationList.setAdapter(adapter);//为RecycleView设置适配器

        // 设置点击监听
        setOnClickListener(fabAddMedication);
        // 设置标题栏返回按钮点击事件
        findViewById(R.id.tb_title).setOnClickListener(v -> finish());
    }

    @Override
    protected void initData() {
        medicationDao = new MedicationRecordDao(this);//初始化数据库
        alarmManager = new MedicationAlarmManager(this);//初始化闹钟管理
        cloudSyncManager = CloudSyncManager.getInstance(this);//云同步(这里不用管)

        // 设置适配器监听器
        adapter.setOnItemClickListener((medication, position) -> {//设置适配器列表的点击事件
            Intent intent = new Intent(this, MedicationDetailActivity.class);
            //跳转意图,点击栏后跳转到用药详情页面
            intent.putExtra("medication_id", medication.getId());//然后传递药品参数
            //页面中会根据药品参数去进行显示
            startActivity(intent);//启动intent
        });

        adapter.setOnItemLongClickListener((medication, position) -> {//列表长按事件
            showMedicationOperationDialog(medication, position);//进行处理,显示操作框
            return true;//不再进行其它处理
        });

        loadMedicationData();//加载药品数据
    }

    @Override
    public void onClick(View view) {
        if (view == fabAddMedication) {//要是点击了添加药品浮动按钮
            // 检查时间设置是否完整
            if (!MedicationAlarmManager.isTimeSettingsComplete(this)) {//检查事件设置
                showTimeSettingsRequiredDialog();//没设置时间就进入设置对话框
                return;
            }

            Intent intent = new Intent(this, AddMedicationActivity.class);
            startActivityForResult(intent, REQUEST_ADD_MEDICATION);
        } else if (view.getId() == R.id.tb_title) {
            // 处理标题栏点击事件
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        // 确保系统返回键正常工作
        super.onBackPressed();
    }

    /**
     * 显示需要设置时间的对话框
     */
    private void showTimeSettingsRequiredDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("需要设置用药时间")
                .setMessage("在添加药品之前，请先设置用药提醒时间。是否现在去设置？")
                .setPositiveButton("去设置", (dialog, which) -> {
                    Intent intent = new Intent(this, ClockActivity.class);
                    startActivityForResult(intent, REQUEST_TIME_SETTINGS);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_MEDICATION && resultCode == RESULT_OK) {
            // 添加药品后刷新数据并更新闹钟
            loadMedicationData();

            // 添加短暂延迟确保数据库操作完成
            new android.os.Handler().postDelayed(() -> {
                updateMedicationAlarms(); // 这里会根据用户偏好自动设置系统闹钟
                // 触发云端同步
                triggerCloudSyncIfEnabled();
            }, 100);

        } else if (requestCode == REQUEST_TIME_SETTINGS && resultCode == RESULT_OK) {
            // 时间设置完成后，可以继续添加药品
            ToastUtils.show("时间设置完成，现在可以添加药品了");
        }
    }

    /**
     * 加载用药数据 - 显示所有药物（包括已停用的）
     */
    private void loadMedicationData() {
        // 获取所有药物记录，而不仅仅是活跃的
        List<MedicationRecord> allMedications = medicationDao.findAll();//从数据库对象中获取全部的药品数据,然后返回一个列表
        medicationList.clear();//清空现有的药品列表数据
        medicationList.addAll(allMedications);//添加新数据
        adapter.notifyDataSetChanged();//通知适配器的打包数据变化

        // 显示/隐藏空状态
        if (medicationList.isEmpty()) {//没有数据的时候显示空视图并且隐藏列表
            rvMedicationList.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.VISIBLE);
        } else {//有数据的时候显示列表并且隐藏空视图
            rvMedicationList.setVisibility(View.VISIBLE);
            tvEmptyView.setVisibility(View.GONE);
        }
    }

    /**
     * 更新用药闹钟
     */
    private void updateMedicationAlarms() {
        if (alarmManager != null) {
            // 检查用户是否开启了自动系统闹钟
            boolean autoSystemAlarm = alarmManager.shouldAutoSetSystemAlarm();
            alarmManager.updateAllMedicationAlarms(autoSystemAlarm);

            if (autoSystemAlarm) {
                android.util.Log.i("MedicationManagement", "已自动更新应用内闹钟和系统闹钟");
            } else {
                android.util.Log.i("MedicationManagement", "已更新应用内闹钟");
            }
        }
    }

    /**
     * 显示用药操作对话框 - 根据药物状态显示不同选项
     */
    private void showMedicationOperationDialog(MedicationRecord medication, int position) {
        String[] options;//准备选项操作按钮

        // 根据药物状态显示不同的操作选项
        if (medication.getStatus() == null || medication.getStatus() == 1) {
            // 正在服用的药物
            options = new String[]{"查看详情", "编辑", "停用", "删除"};
        } else {
            // 已停用的药物
            options = new String[]{"查看详情", "重新启用", "删除"};
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)//创建并且显示对话框
                .setTitle(medication.getMedicationName())//设置标题为药品的名称
                .setItems(options, (dialog, which) -> {//根据药品的状态设置操作列表
                    if (medication.getStatus() == null || medication.getStatus() == 1) {
                        // 正在服用的药物的操作
                        handleActiveMedicationOperation(medication, which);
                    } else {
                        // 已停用药物的操作
                        handleInactiveMedicationOperation(medication, which);
                    }
                })
                .show();
    }



    /**
     * 处理正在服用药物的操作
     */
    private void handleActiveMedicationOperation(MedicationRecord medication, int which) {
        switch (which) {
            case 0: // 查看详情
                viewMedicationDetail(medication);//转向不同的操作
                break;
            case 1: // 编辑
                editMedication(medication);
                break;
            case 2: // 停用
                deactivateMedication(medication);
                break;
            case 3: // 删除
                showDeleteConfirmDialog(medication);
                break;
        }
    }

    /**
     * 处理已停用药物的操作
     */
    private void handleInactiveMedicationOperation(MedicationRecord medication, int which) {
        switch (which) {
            case 0: // 查看详情
                viewMedicationDetail(medication);
                break;
            case 1: // 重新启用
                reactivateMedication(medication);
                break;
            case 2: // 删除
                showDeleteConfirmDialog(medication);
                break;
        }
    }

    /**
     * 查看药物详情
     */
    private void viewMedicationDetail(MedicationRecord medication) {
        Intent intent = new Intent(this, MedicationDetailActivity.class);
        intent.putExtra("medication_id", medication.getId());
        startActivity(intent);
    }

    /**
     * 编辑药物
     */
    private void editMedication(MedicationRecord medication) {
        Intent editIntent = new Intent(this, AddMedicationActivity.class);
        editIntent.putExtra("medication_id", medication.getId());
        editIntent.putExtra("is_edit", true);
        startActivityForResult(editIntent, REQUEST_ADD_MEDICATION);
    }

    /**
     * 停用药物
     */
    private void deactivateMedication(MedicationRecord medication) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("停用药物")
                .setMessage("确定要停用 " + medication.getMedicationName() + " 吗？\n\n停用后药物将保留在列表中，您可以随时重新启用或删除。")
                .setPositiveButton("停用", (dialog, which) -> {
                    medication.setStatus(0); // 设置为停用状态
                    medicationDao.update(medication);
                    adapter.notifyDataSetChanged(); // 刷新列表显示状态
                    updateMedicationAlarms(); // 更新闹钟
                    ToastUtils.show("已停用 " + medication.getMedicationName());

                    // 触发云端同步
                    triggerCloudSyncIfEnabled();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 重新启用药物
     */
    private void reactivateMedication(MedicationRecord medication) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("重新启用")
                .setMessage("确定要重新启用 " + medication.getMedicationName() + " 吗？")
                .setPositiveButton("启用", (dialog, which) -> {
                    medication.setStatus(1); // 设置为活跃状态
                    medicationDao.update(medication);
                    adapter.notifyDataSetChanged(); // 刷新列表显示状态
                    updateMedicationAlarms(); // 更新闹钟
                    ToastUtils.show("已启用 " + medication.getMedicationName());

                    // 触发云端同步
                    triggerCloudSyncIfEnabled();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 如果启用了自动同步，则触发云端同步
     */
    private void triggerCloudSyncIfEnabled() {
        if (cloudSyncManager.canSyncToCloud()) {
            // 在后台执行同步，不打扰用户
            new Thread(() -> {
                cloudSyncManager.autoSyncInBackground();
            }).start();
        }
    }

    /**
     * 显示删除确认对话框
     */
    private void showDeleteConfirmDialog(MedicationRecord medication) {
        String statusText = (medication.getStatus() != null && medication.getStatus() == 1)
                ? "正在服用" : "已停用";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除 " + medication.getMedicationName() + " 吗？\n\n状态：" + statusText + "\n\n删除后将无法恢复所有相关数据。")
                .setPositiveButton("删除", (dialog, which) -> {
                    medicationDao.delete(medication.getId());
                    loadMedicationData(); // 重新加载数据
                    updateMedicationAlarms(); // 更新闹钟
                    ToastUtils.show("已删除 " + medication.getMedicationName());
                })
                .setNegativeButton("取消", null)
                .show();
    }
}