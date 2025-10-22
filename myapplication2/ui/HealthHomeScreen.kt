/**
 * 医疗健康应用主页面
 * 
 * 这个文件包含了医疗健康应用的主要导航逻辑和页面结构
 * 支持多个功能标签页面切换：健康中心、家庭监护模式、联动手机健康助手
 * 
 * 主要功能：
 * - 应用主屏幕导航管理
 * - 主页面布局和功能标签切换
 * - 与各个子组件的集成
 * 
 * @author 蓝岐健康提醒团队
 * @version 1.0
 * @since 2025-06-21
 */
package com.example.myapplication2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication2.ui.theme.*

/**
 * 医疗健康应用主屏幕组件
 * 
 * 这是应用的根组件，负责管理整个应用的导航和页面切换逻辑
 * 根据当前选中的导航状态显示不同的页面内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthHomeScreen() {
    // 当前页面导航状态管理
    var currentScreen by remember { mutableStateOf(NavigationScreen.HOME) }
    // 用户选择的个性化目标
    var selectedGoal by remember { mutableStateOf("") }
    
    // 根据当前页面状态显示对应的页面内容
    when (currentScreen) {
        NavigationScreen.HOME -> {
            HomeScreen(
                onNavigate = { screen -> currentScreen = screen },
                onGoalClick = { goal ->
                    selectedGoal = goal
                    currentScreen = NavigationScreen.GOAL_SETTING
                }
            )
        }
        NavigationScreen.VOICE_CHAT -> {
            FunctionScreen(
                title = "语音对话功能",
                description = "这是语音对话功能页面\n\n• 智能语音识别\n• 健康问题咨询\n• 实时语音交流\n• 专业医疗建议",
                onBack = { currentScreen = NavigationScreen.HOME }
            )
        }
        NavigationScreen.TEXT_INPUT -> {
            FunctionScreen(
                title = "文字输入功能",
                description = "这是文字输入功能页面\n\n• 症状描述输入\n• 健康数据记录\n• 智能文字识别\n• 医疗建议获取",
                onBack = { currentScreen = NavigationScreen.HOME }
            )
        }
        NavigationScreen.PHOTO_RECOGNITION -> {
            FunctionScreen(
                title = "药品图片识别功能",
                description = "这是药品图片识别功能页面\n\n• 药品拍照识别\n• 药物信息查询\n• 用药指导建议\n• 相互作用提醒",
                onBack = { currentScreen = NavigationScreen.HOME }
            )
        }
        NavigationScreen.SYMPTOM_TRACKING -> {
            FunctionScreen(
                title = "症状轨迹可视化功能",
                description = "这是症状轨迹可视化功能页面\n\n• 症状记录追踪\n• 数据可视化分析\n• 健康趋势预测\n• 个性化建议",
                onBack = { currentScreen = NavigationScreen.HOME }
            )
        }
        NavigationScreen.GOAL_SETTING -> {
            GoalDetailScreen(
                goal = selectedGoal,
                onBack = { currentScreen = NavigationScreen.HOME }
            )
        }
        NavigationScreen.USER_PROFILE -> {
            FunctionScreen(
                title = "用户信息",
                description = "这是用户信息功能页面\n\n用户名: 张三\n年龄: 35岁\n注册时间: 2024年1月\n健康积分: 1,280分\n\n• 个人资料管理\n• 健康档案查看\n• 设置个性化偏好",
                onBack = { currentScreen = NavigationScreen.HOME }
            )
        }
        NavigationScreen.ADD_RECORD -> {
            FunctionScreen(
                title = "添加健康记录功能",
                description = "这是添加健康记录功能页面\n\n• 健康数据录入\n• 体征指标记录\n• 用药记录管理\n• 就诊信息保存",
                onBack = { currentScreen = NavigationScreen.HOME }
            )
        }
    }
}

/**
 * 应用主页面组件
 * 
 * 包含顶部导航栏、功能标签和根据标签显示的不同页面内容
 * 
 * @param onNavigate 页面导航回调函数
 * @param onGoalClick 目标点击回调函数
 */
@Composable
fun HomeScreen(
    onNavigate: (NavigationScreen) -> Unit,
    onGoalClick: (String) -> Unit
) {
    // 创建蓝色渐变背景画刷
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(BlueGradientStart, BlueGradientEnd)
    )
    
    // 当前选中的功能标签状态管理
    var selectedTag by remember { mutableStateOf("健康中心") }

    // 主容器：使用Box布局，背景为蓝色渐变
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        // 页面主要内容：垂直排列
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部导航栏：包含标题和用户头像
            TopNavigationBar(
                onUserClick = { onNavigate(NavigationScreen.USER_PROFILE) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 功能标签选择器：三个标签页切换
            FunctionTags(
                selectedTag = selectedTag,
                onTagSelected = { selectedTag = it }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 根据选中的标签显示不同的页面内容
            when (selectedTag) {
                "健康中心" -> HealthCenterPage(
                    onNavigate = onNavigate,
                    onGoalClick = onGoalClick
                )
                "家庭监护" -> FamilyMonitoringPage(
                    onNavigate = onNavigate
                )
                "健康助手" -> MobileHealthAssistantPage(
                    onNavigate = onNavigate
                )
            }
        }
        
        // 底部导航栏：固定在屏幕底部
        BottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = { /* 当前已在首页，无需操作 */ },
            onAddClick = { onNavigate(NavigationScreen.ADD_RECORD) },
            onProfileClick = { onNavigate(NavigationScreen.USER_PROFILE) }
        )
    }
}

/**
 * 健康应用主页面预览
 * 
 * 用于Android Studio设计器中预览应用界面
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HealthHomeScreenPreview() {
    MyApplication2Theme {
        HealthHomeScreen()
    }
} 