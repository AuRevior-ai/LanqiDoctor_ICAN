package com.lanqiDoctor.demo.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hjq.base.BaseActivity;
import com.hjq.toast.ToastUtils;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.dao.MedicalHistoryDao;
import com.lanqiDoctor.demo.database.entity.MedicalHistory;
import com.lanqiDoctor.demo.ui.adapter.MedicalHistoryAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 既往病史Activity
 * 
 * 用于管理用户的既往病史信息
 * 
 * @author 蓝旗医生开发团队
 * @version 1.0
 */
public class MedicalHistoryActivity extends BaseActivity {

    private RecyclerView rvMedicalHistoryList; // 既往病史列表的RecyclerView
    private LinearLayout tvEmptyView; // 空数据的提醒视图
    private FloatingActionButton fabAddMedicalHistory; // 添加既往病史的浮动按钮
    private CardView cardHealthReport; // 健康报告生成卡片

    private MedicalHistoryAdapter adapter; // 既往病史列表的适配器
    private MedicalHistoryDao medicalHistoryDao; // 既往病史数据的信息库
    private List<MedicalHistory> medicalHistoryList; // 既往病史数据的列表

    private static final int REQUEST_ADD_MEDICAL_HISTORY = 1003; // 添加既往病史请求码

    @Override
    protected int getLayoutId() {
        return R.layout.activity_medical_history;
    }

    @Override
    protected void initView() {
        rvMedicalHistoryList = findViewById(R.id.rv_medical_history_list);
        tvEmptyView = findViewById(R.id.tv_empty_view);
        fabAddMedicalHistory = findViewById(R.id.fab_add_medical_history);
        cardHealthReport = findViewById(R.id.card_health_report);

        // 设置RecyclerView
        rvMedicalHistoryList.setLayoutManager(new LinearLayoutManager(this));
        medicalHistoryList = new ArrayList<>();
        adapter = new MedicalHistoryAdapter(medicalHistoryList);
        rvMedicalHistoryList.setAdapter(adapter);

        // 设置点击监听
        setOnClickListener(fabAddMedicalHistory, cardHealthReport);
        // 设置标题栏返回按钮点击事件
        findViewById(R.id.tb_title).setOnClickListener(v -> finish());
    }

    @Override
    protected void initData() {
        medicalHistoryDao = new MedicalHistoryDao(this);

        // 设置适配器监听器
        adapter.setOnItemClickListener((history, position) -> {
            Intent intent = new Intent(this, MedicalHistoryDetailActivity.class);
            intent.putExtra("medical_history_id", history.getId());
            startActivity(intent);
        });

        adapter.setOnItemLongClickListener((history, position) -> {
            showMedicalHistoryOperationDialog(history, position);
            return true;
        });

        loadMedicalHistoryData();
    }

    @Override
    public void onClick(View view) {
        if (view == fabAddMedicalHistory) {
            Intent intent = new Intent(this, AddMedicalHistoryActivity.class);
            startActivityForResult(intent, REQUEST_ADD_MEDICAL_HISTORY);
        } else if (view == cardHealthReport) {
            // 点击健康报告卡片，跳转到医疗报告页面
            Intent intent = new Intent(this, MedicalReportActivity.class);
            startActivity(intent);
        } else if (view.getId() == R.id.tb_title) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_MEDICAL_HISTORY && resultCode == RESULT_OK) {
            // 添加既往病史后刷新数据
            loadMedicalHistoryData();
            ToastUtils.show("既往病史记录已保存");
        }
    }

    /**
     * 加载既往病史数据
     */
    private void loadMedicalHistoryData() {
        List<MedicalHistory> allHistories = medicalHistoryDao.findAll();
        medicalHistoryList.clear();
        medicalHistoryList.addAll(allHistories);
        adapter.notifyDataSetChanged();

        // 显示/隐藏空状态
        if (medicalHistoryList.isEmpty()) {
            rvMedicalHistoryList.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.VISIBLE);
        } else {
            rvMedicalHistoryList.setVisibility(View.VISIBLE);
            tvEmptyView.setVisibility(View.GONE);
        }
    }

    /**
     * 显示既往病史操作对话框
     */
    private void showMedicalHistoryOperationDialog(MedicalHistory history, int position) {
        String[] options = {"查看详情", "编辑", "删除"};

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(history.getDiseaseName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // 查看详情
                            viewMedicalHistoryDetail(history);
                            break;
                        case 1: // 编辑
                            editMedicalHistory(history);
                            break;
                        case 2: // 删除
                            showDeleteConfirmDialog(history);
                            break;
                    }
                })
                .show();
    }

    /**
     * 查看既往病史详情
     */
    private void viewMedicalHistoryDetail(MedicalHistory history) {
        Intent intent = new Intent(this, MedicalHistoryDetailActivity.class);
        intent.putExtra("medical_history_id", history.getId());
        startActivity(intent);
    }

    /**
     * 编辑既往病史
     */
    private void editMedicalHistory(MedicalHistory history) {
        Intent editIntent = new Intent(this, AddMedicalHistoryActivity.class);
        editIntent.putExtra("medical_history_id", history.getId());
        editIntent.putExtra("is_edit", true);
        startActivityForResult(editIntent, REQUEST_ADD_MEDICAL_HISTORY);
    }

    /**
     * 显示删除确认对话框
     */
    private void showDeleteConfirmDialog(MedicalHistory history) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定要删除 " + history.getDiseaseName() + " 的病史记录吗？\n\n删除后将无法恢复所有相关数据。")
                .setPositiveButton("删除", (dialog, which) -> {
                    medicalHistoryDao.delete(history.getId());
                    loadMedicalHistoryData(); // 重新加载数据
                    ToastUtils.show("已删除 " + history.getDiseaseName() + " 的病史记录");
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
