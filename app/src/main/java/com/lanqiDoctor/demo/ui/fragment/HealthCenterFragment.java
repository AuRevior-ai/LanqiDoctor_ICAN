package com.lanqiDoctor.demo.ui.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.cardview.widget.CardView;

import com.hjq.base.BaseFragment;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.ui.activity.ChatActivity;
import com.lanqiDoctor.demo.ui.activity.ChatLlmActivity;
import com.lanqiDoctor.demo.ui.activity.HealthFunctionActivity;
import com.lanqiDoctor.demo.ui.activity.MedicationManagementActivity;
import com.lanqiDoctor.demo.ui.activity.ClockActivity;
import com.lanqiDoctor.demo.ui.activity.MonthCalendarActivity;
import com.lanqiDoctor.demo.aop.Permissions;
import com.lanqiDoctor.demo.ui.activity.*;
import com.hjq.permissions.Permission;

/**
 * 健康中心Fragment
 *
 * 迁移自myapplication2项目的健康中心功能，包含：
 * - 症状轨迹可视化
 * - 语音对话功能
 * - 文字输入功能
 * - 药品图片识别功能
 * - 个性目标设置（在用药、医嘱识别、拍照）
 *
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class HealthCenterFragment extends BaseFragment {
    //public class HealthCenterFragment extends AppActivity {
    // 症状轨迹可视化卡片
    private CardView cardSymptomTracking;
    private ImageView ivCalendar;

    // 功能按钮
    private LinearLayout llVoiceChat;
    private LinearLayout llTextInput;
    private LinearLayout llPhotoRecognition;
    private LinearLayout llMedicalHistory;

    // 个性目标卡片
    private LinearLayout llGoalMedicine;
    private LinearLayout llGoalDiagnosis;
    private LinearLayout llGoalPhoto;

    private View mainContentView,fragmentContainerView;
    @Override
    protected int getLayoutId() {
        return R.layout.health_center_fragment2;
    }

    @Override
    protected void initView() {
        // 症状轨迹可视化
        // 必须先初始化这两个关键View
        mainContentView = findViewById(R.id.main_content); // 主内容容器
        fragmentContainerView = findViewById(R.id.fragment_container); // Frag

        cardSymptomTracking = (CardView) findViewById(R.id.card_symptom_tracking);
        ivCalendar = (ImageView) findViewById(R.id.iv_calendar);

        // 功能按钮
        llVoiceChat = (LinearLayout) findViewById(R.id.ll_voice_chat);
        llTextInput = (LinearLayout) findViewById(R.id.ll_text_input);
        llPhotoRecognition = (LinearLayout) findViewById(R.id.ll_photo_recognition);
        llMedicalHistory = (LinearLayout) findViewById(R.id.ll_medical_history);

        // 个性目标
        llGoalMedicine = (LinearLayout) findViewById(R.id.ll_goal_medicine);
        llGoalDiagnosis = (LinearLayout) findViewById(R.id.ll_goal_diagnosis);
        llGoalPhoto = (LinearLayout) findViewById(R.id.ll_goal_photo);
    }

    @Override
    protected void initData() {
        // 初始化数据
        initListener();
    }

    @Permissions(Permission.RECORD_AUDIO)
    private void initListener() {
        // 症状轨迹可视化点击事件
        cardSymptomTracking.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MonthCalendarActivity.class);
            startActivity(intent);//点击日历模块之后打开页面
        });

        // 语音对话功能
        llVoiceChat.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), RealtimeDialogActivity.class);
            startActivity(intent);
        });
        //文字输入功能
        llTextInput.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChatLlmActivity.class);
            startActivity(intent);

        });

        //家庭健康日报功能
        if (llPhotoRecognition != null) {
            llPhotoRecognition.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), MobileHealthDaily.class);
                startActivity(intent);
            });
        }

        //既往病史功能
        if (llMedicalHistory != null) {
            llMedicalHistory.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), MedicalHistoryActivity.class);
                startActivity(intent);
            });
        }

        // 个性目标：在用药
        if (llGoalMedicine != null) {
            llGoalMedicine.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), MedicationManagementActivity.class);
                startActivity(intent);
            });
        }

        // 个性目标：医嘱识别
        if (llGoalDiagnosis != null) {
            llGoalDiagnosis.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), com.lanqiDoctor.demo.ocr.OCRActivity.class);
                startActivity(intent);
            });
        }

        // 个性目标：每日服药时间设置
        llGoalPhoto.setOnClickListener(v -> {
            startActivity(ClockActivity.class);
        });
    }
    public boolean onBackPressed() {
        if (fragmentContainerView != null && fragmentContainerView.getVisibility() == View.VISIBLE) {
            // 如果当前显示的是全屏 ChatFragment，则返回主界面
            fragmentContainerView.setVisibility(View.GONE);
            mainContentView.setVisibility(View.VISIBLE);
            return true; // 表示已处理
        }
        return false; // 表示未处理
    }
}