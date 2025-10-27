package com.lanqiDoctor.demo.ui.fragment;

import android.content.Intent;
import android.widget.LinearLayout;

import com.hjq.base.BaseFragment;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.ui.activity.*;

/**
 * 日常服药Fragment
 * 
 * 包含个性目标设置：在用药、医嘱识别、服药时间
 *
 * @author 蓝岐医童开发团队
 * @version 2.0
 */
public class DailyMedicationFragment extends BaseFragment {

    private LinearLayout llGoalMedicine;
    private LinearLayout llGoalDiagnosis;
    private LinearLayout llGoalPhoto;
    private LinearLayout llHealthTrack;

    @Override
    protected int getLayoutId() {
        return R.layout.page_daily_medication;
    }

    @Override
    protected void initView() {
        // 个性目标按钮
        llGoalMedicine = (LinearLayout) findViewById(R.id.ll_goal_medicine);
        llGoalDiagnosis = (LinearLayout) findViewById(R.id.ll_goal_diagnosis);
        llGoalPhoto = (LinearLayout) findViewById(R.id.ll_goal_photo);
        
        // 健康轨迹卡片
        llHealthTrack = (LinearLayout) findViewById(R.id.ll_health_track);
    }

    @Override
    protected void initData() {
        initListener();
    }

    private void initListener() {
        // 在用药
        if (llGoalMedicine != null) {
            llGoalMedicine.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), MedicationManagementActivity.class);
                startActivity(intent);
            });
        }
        
        // 医嘱识别
        if (llGoalDiagnosis != null) {
            llGoalDiagnosis.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), com.lanqiDoctor.demo.ocr.OCRActivity.class);
                startActivity(intent);
            });
        }
        
        // 服药时间
        if (llGoalPhoto != null) {
            llGoalPhoto.setOnClickListener(v -> {
                startActivity(ClockActivity.class);
            });
        }
        
        // 健康轨迹
        if (llHealthTrack != null) {
            llHealthTrack.setOnClickListener(v -> {
                // 跳转到月历页面，显示每天服药情况
                Intent intent = new Intent(getContext(), MonthCalendarActivity.class);
                startActivity(intent);
            });
        }
    }
}

