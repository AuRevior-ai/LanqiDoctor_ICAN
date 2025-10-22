package com.lanqiDoctor.demo.ui.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.hjq.base.BaseActivity;
import com.hjq.toast.ToastUtils;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.database.dao.MedicationRecordDao;
import com.lanqiDoctor.demo.database.entity.MedicationRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 用药详情Activity
 * 这个activity
 * 这个文件干的事情是:在初始化自己的layout的时候,从数据库中读取全部药品信息并且展现出来
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class MedicationDetailActivity extends BaseActivity {//用药详情页面

    private TextView tvMedicationName;//药品名称
    private TextView tvDosageInfo;//药品计量
    private TextView tvFrequencyInfo;//用药频率
    private TextView tvStartDate;//开始日期
    private TextView tvEndDate;//截至日期
    private TextView tvStatus;//用药形式
    private TextView tvNotes;//备注
    private TextView tvCreateTime;//创建时间

    private MedicationRecordDao medicationDao;//数据库访问对象,用于查询用药记录
    private MedicationRecord medication;//当前加载的用药记录对象
    private long medicationId;//从intent中获取的用药记录ID
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    //格式化使用时间
    /*
     * 下面三个继承函数在父类是这样写的    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity();
    }

    protected void initActivity() {
        initLayout();
        initView();
        initData();
    }
        所以,它们都是在加载布局的时候被触发
     */
    @Override//这个方法是继承自自定义类型BaseActivity的接口方法
    protected int getLayoutId() {//返回这个Activity对应的布局资源ID
        return R.layout.medication_detail_activity;
    }

    @Override//同样,这个也是
    protected void initView() {//初始化所有的UI组件
        tvMedicationName = findViewById(R.id.tv_medication_name);//药品名称textview控件
        tvDosageInfo = findViewById(R.id.tv_dosage_info);//剂量控件
        tvFrequencyInfo = findViewById(R.id.tv_frequency_info);//频率控件
        tvStartDate = findViewById(R.id.tv_start_date);//开始时间控件
        tvEndDate = findViewById(R.id.tv_end_date);//结束时间控件
        tvStatus = findViewById(R.id.tv_status);//用药状态控件
        tvNotes = findViewById(R.id.tv_notes);//备注控件
        tvCreateTime = findViewById(R.id.tv_create_time);//创建时间文本控件
    }

    @Override
    protected void initData() {
        medicationDao = new MedicationRecordDao(this);//创建数据库实例

        // 获取传入的用药记录ID
        medicationId = getIntent().getLongExtra("medication_id", -1);//获取从intent传来的用药ID
        if (medicationId == -1) {
            ToastUtils.show("用药记录不存在");
            finish();
            return;
        }

        loadMedicationDetail();//加载具体的用药数据
    }

    /**
     * 加载用药详情
     */
    private void loadMedicationDetail() {
        medication = medicationDao.findById(medicationId);//从数据库中通过ID来获取用药记录对象
        if (medication == null) {
            ToastUtils.show("用药记录不存在");
            finish();
            return;
        }

        // 显示用药信息
        displayMedicationInfo();//展示这个用药信息
    }

    /**
     * 显示用药信息
     */
    private void displayMedicationInfo() {
        // 药品名称
        tvMedicationName.setText(medication.getMedicationName());//从数据库中使劲找,数据库是什么样的你先别管
        //找到之后对控件进行文本覆盖

        // 剂量信息
        String dosageInfo = medication.getDosage() + " " + medication.getUnit();//从数据库中找到计量信息
        tvDosageInfo.setText(dosageInfo);//覆盖文本

        // 频率信息
        tvFrequencyInfo.setText(medication.getFrequency());

        // 开始时间
        if (medication.getStartDate() != null) {
            String startDate = dateFormat.format(new Date(medication.getStartDate()));
            tvStartDate.setText(startDate);
        } else {
            tvStartDate.setText("未设置");
        }

        // 结束时间
        if (medication.getEndDate() != null) {
            String endDate = dateFormat.format(new Date(medication.getEndDate()));
            tvEndDate.setText(endDate);
        } else {
            tvEndDate.setText("未设置");
        }

        // 状态
        String statusText = getStatusText(medication.getStatus());
        tvStatus.setText(statusText);
        tvStatus.setTextColor(getStatusColor(medication.getStatus()));//通过键值对寻找当前状态和颜色备注

        // 备注
        if (medication.getNotes() != null && !medication.getNotes().trim().isEmpty()) {
            tvNotes.setText(medication.getNotes());
        } else {
            tvNotes.setText("无备注");
        }//备注

        // 创建时间
        if (medication.getCreateTime() != null) {
            String createTime = dateTimeFormat.format(new Date(medication.getCreateTime()));
            tvCreateTime.setText("创建时间：" + createTime);
        }//创建时间
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "已停用";
            case 1: return "正在服用";
            case 2: return "已完成";
            default: return "未知";
        }
    }//找文本

    /**
     * 获取状态颜色
     */
    private int getStatusColor(Integer status) {
        if (status == null) return getColor(android.R.color.darker_gray);
        switch (status) {
            case 0: return getColor(android.R.color.darker_gray);  // 已停用 - 灰色
            case 1: return getColor(android.R.color.holo_green_dark);  // 正在服用 - 绿色
            case 2: return getColor(android.R.color.holo_blue_dark);   // 已完成 - 蓝色
            default: return getColor(android.R.color.darker_gray);
        }
    }//找颜色

    @Override
    public void onClick(View view) {
        // 可以添加编辑、删除等操作按钮的点击事件
    }//点击事件,遗传来的,但是没有重写
}