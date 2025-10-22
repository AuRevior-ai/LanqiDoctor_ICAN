/**
 * 健康中心组件
 * 
 * 包含健康中心功能相关的UI组件
 * 
 * @author 蓝岐健康提醒团队
 * @version 1.0
 * @since 2025-06-21
 */
package com.example.myapplication2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication2.ui.theme.*

/**
 * 健康中心页面组件
 * 
 * 显示健康管理相关功能，包括症状轨迹可视化、功能按钮和个性目标设置
 * 
 * @param onNavigate 页面导航回调
 * @param onGoalClick 目标点击回调
 */
@Composable
fun HealthCenterPage(
    onNavigate: (NavigationScreen) -> Unit,
    onGoalClick: (String) -> Unit
) {
    // 滚动状态管理
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // 症状轨迹可视化卡片组件
        SymptomTrackingCard(
            onButtonClick = { onNavigate(NavigationScreen.SYMPTOM_TRACKING) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 三个功能按钮：语音对话、文字输入、药品图片识别
        FunctionButtons(
            onVoiceClick = { onNavigate(NavigationScreen.VOICE_CHAT) },
            onTextClick = { onNavigate(NavigationScreen.TEXT_INPUT) },
            onPhotoClick = { onNavigate(NavigationScreen.PHOTO_RECOGNITION) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 个性目标设置卡片：在用药、医嘱识别、拍照功能
        PersonalGoalsCard(
            onGoalClick = onGoalClick
        )
        
        Spacer(modifier = Modifier.height(100.dp)) // 底部导航栏空间
    }
}

/**
 * 症状轨迹可视化卡片
 * 
 * 显示症状追踪和健康日历功能
 * 
 * @param onButtonClick 按钮点击回调
 */
@Composable
fun SymptomTrackingCard(
    onButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "用药轨迹可视化",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "进入个性化健康日历以查看服药历史",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            // 日历图标
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = ButtonBackground,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onButtonClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "日历",
                    tint = IconTint,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * 功能按钮组
 * 
 * 显示三个主要功能按钮
 * 
 * @param onVoiceClick 语音对话点击回调
 * @param onTextClick 文字输入点击回调
 * @param onPhotoClick 图片识别点击回调
 */
@Composable
fun FunctionButtons(
    onVoiceClick: () -> Unit,
    onTextClick: () -> Unit,
    onPhotoClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FunctionButton(
            icon = Icons.Default.Phone,
            title = "语音对话",
            subtitle = "健康交流",
            onClick = onVoiceClick,
            modifier = Modifier.weight(1f)
        )
        FunctionButton(
            icon = Icons.Default.Edit,
            title = "文字输入",
            subtitle = "健康识别",
            onClick = onTextClick,
            modifier = Modifier.weight(1f)
        )
        FunctionButton(
            icon = Icons.Default.Add,
            title = "药品图片",
            subtitle = "健康规划",
            hasNotification = true,
            onClick = onPhotoClick,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 功能按钮组件
 * 
 * 单个功能按钮的实现
 * 
 * @param icon 按钮图标
 * @param title 按钮标题
 * @param subtitle 按钮副标题
 * @param onClick 点击回调
 * @param hasNotification 是否显示通知标识
 * @param modifier 修饰符
 */
@Composable
fun FunctionButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    hasNotification: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = IconTint,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
            
            if (hasNotification) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = Color.Red,
                            shape = CircleShape
                        )
                        .align(Alignment.TopEnd)
                        .offset((-4).dp, 4.dp)
                )
            }
        }
    }
}

/**
 * 个性目标设置卡片
 * 
 * 显示用户可以设置的个性化健康目标选项
 * 
 * @param onGoalClick 目标点击回调
 */
@Composable
fun PersonalGoalsCard(
    onGoalClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 卡片标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "设置个性健康计划",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 三个目标选项：在用药、医嘱识别、拍照功能
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoalItem(
                    icon = Icons.Default.Favorite,
                    title = "在用药",
                    onClick = { onGoalClick("在用药") },
                    modifier = Modifier.weight(1f)
                )
                GoalItem(
                    icon = Icons.Default.Info,
                    title = "医嘱识别",
                    onClick = { onGoalClick("医嘱识别") },
                    modifier = Modifier.weight(1f)
                )
                GoalItem(
                    icon = Icons.Default.CheckCircle,
                    title = "拍照",
                    onClick = { onGoalClick("拍照") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 目标项目组件
 * 
 * 显示单个健康目标选项的图标和标题
 * 
 * @param icon 目标图标
 * @param title 目标标题
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun GoalItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 目标图标背景容器
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    color = ButtonBackground,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            // 目标图标
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = IconTint,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // 目标标题
        Text(
            text = title,
            fontSize = 12.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
    }
} 