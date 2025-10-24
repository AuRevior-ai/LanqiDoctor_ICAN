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
import androidx.compose.ui.draw.blur
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 四个功能按钮：横排云朵样式
        FunctionButtons(
            onVoiceClick = { onNavigate(NavigationScreen.VOICE_CHAT) },
            onTextClick = { onNavigate(NavigationScreen.TEXT_INPUT) },
            onDailyReportClick = { onNavigate(NavigationScreen.SYMPTOM_TRACKING) },
            onHistoryClick = { onNavigate(NavigationScreen.PHOTO_RECOGNITION) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 个性目标设置卡片：在用药、医嘱识别、拍照功能
        PersonalGoalsCard(
            onGoalClick = onGoalClick
        )
        
        Spacer(modifier = Modifier.height(80.dp)) // 底部导航栏空间
    }
}

/**
 * 症状轨迹可视化卡片 - 美化版本
 * 
 * 显示症状追踪和健康日历功能，带有荧光效果
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
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = ShadowMedium
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "用药轨迹可视化",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "进入个性化健康日历以查看服药历史",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 日历图标按钮 - 带发光效果
            Box(
                modifier = Modifier.size(84.dp)
            ) {
                // 发光层
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(10.dp)
                        .background(
                            color = GlowBlue,
                            shape = RoundedCornerShape(16.dp)
                        )
                )
                
                // 按钮主体
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = GlowBlue
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(NeonBlueSoft, ButtonPrimary.copy(alpha = 0.3f))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onButtonClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "日历",
                        tint = IconTint,
                        modifier = Modifier
                            .size(44.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}

/**
 * 功能按钮组 - 横排云朵样式
 * 
 * 显示四个主要功能按钮，模仿第二张图片的云朵样式
 * 
 * @param onVoiceClick 语音对话点击回调
 * @param onTextClick 文字输入点击回调
 * @param onDailyReportClick 健康日报点击回调
 * @param onHistoryClick 既往病史点击回调
 */
@Composable
fun FunctionButtons(
    onVoiceClick: () -> Unit,
    onTextClick: () -> Unit,
    onDailyReportClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CloudStyleFunctionButton(
            icon = Icons.Default.Phone,
            title = "语音对话",
            onClick = onVoiceClick,
            iconBackgroundColor = NeonBlue,
            glowColor = GlowBlue
        )
        CloudStyleFunctionButton(
            icon = Icons.Default.Edit,
            title = "文字输入",
            onClick = onTextClick,
            iconBackgroundColor = Color(0xFFFF9B7A),  // 橙红色
            glowColor = GlowOrange
        )
        CloudStyleFunctionButton(
            icon = Icons.Default.DateRange,
            title = "健康日报",
            onClick = onDailyReportClick,
            iconBackgroundColor = Color(0xFF7AC8E8),  // 青绿色
            glowColor = GlowTeal
        )
        CloudStyleFunctionButton(
            icon = Icons.Default.Info,
            title = "既往病史",
            onClick = onHistoryClick,
            iconBackgroundColor = Color(0xFF84E8B4),  // 绿色
            glowColor = GlowGreen
        )
    }
}

/**
 * 功能按钮组件 - 美化版本
 * 
 * 单个功能按钮的实现，带有荧光效果和柔和阴影
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
    NeonFunctionCard(
        icon = icon,
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        hasNotification = hasNotification,
        modifier = modifier
    )
}

/**
 * 个性目标设置卡片 - 紧凑版本
 * 
 * 显示用户可以设置的个性化健康目标选项，带有柔和阴影
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
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = ShadowMedium
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 卡片标题
            Text(
                text = "请选择你的个性目标:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
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
 * 目标项目组件 - 紧凑版本
 * 
 * 显示单个健康目标选项的图标和标题，带有柔和的发光效果
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
        // 目标图标背景容器 - 带柔和阴影
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = ShadowMedium
                )
                .background(
                    color = SurfaceGlow,
                    shape = RoundedCornerShape(14.dp)
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
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
    }
} 