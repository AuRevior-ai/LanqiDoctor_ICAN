/**
 * 移动健康助手组件
 * 
 * 包含移动健康助手功能相关的UI组件
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication2.ui.theme.*

/**
 * 联动手机健康助手页面组件
 * 
 * 显示健康设备连接和AI助手相关功能，包括设备状态、数据同步、AI助手和智能提醒
 * 
 * @param onNavigate 页面导航回调
 */
@Composable
fun MobileHealthAssistantPage(
    onNavigate: (NavigationScreen) -> Unit
) {
    // 滚动状态管理
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // 健康设备连接状态卡片
        DeviceConnectionCard()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 健康数据同步功能按钮
        HealthDataSyncButtons(onNavigate)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // AI智能健康助手卡片
        AIHealthAssistantCard()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 智能提醒设置卡片
        SmartReminderCard()
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

/**
 * 设备连接状态卡片
 * 
 * 显示各种健康设备的连接状态
 */
@Composable
fun DeviceConnectionCard() {
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
            Text(
                text = "设备连接状态",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // 设备连接状态列表
            DeviceStatusItem("智能手环", true)
            Spacer(modifier = Modifier.height(8.dp))
            DeviceStatusItem("血压计", true)
            Spacer(modifier = Modifier.height(8.dp))
            DeviceStatusItem("体重秤", false)
            Spacer(modifier = Modifier.height(8.dp))
            DeviceStatusItem("血糖仪", false)
        }
    }
}

/**
 * 设备状态项目组件
 * 
 * 显示单个设备的连接状态
 * 
 * @param deviceName 设备名称
 * @param isConnected 是否已连接
 */
@Composable
fun DeviceStatusItem(deviceName: String, isConnected: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Close,
            contentDescription = null,
            tint = if (isConnected) Color.Green else Color.Red,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = deviceName,
            fontSize = 14.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = if (isConnected) "已连接" else "未连接",
            fontSize = 12.sp,
            color = if (isConnected) Color.Green else Color.Red
        )
    }
}

/**
 * 健康数据同步按钮组
 * 
 * 显示数据同步相关的功能按钮
 * 
 * @param onNavigate 页面导航回调
 */
@Composable
fun HealthDataSyncButtons(onNavigate: (NavigationScreen) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FunctionButton(
            icon = Icons.Default.Refresh,
            title = "数据同步",
            subtitle = "自动同步",
            onClick = { /* TODO */ },
            modifier = Modifier.weight(1f)
        )
        FunctionButton(
            icon = Icons.Default.Star,
            title = "数据分析",
            subtitle = "趋势分析",
            onClick = { /* TODO */ },
            modifier = Modifier.weight(1f)
        )
        FunctionButton(
            icon = Icons.Default.Share,
            title = "数据分享",
            subtitle = "医生共享",
            onClick = { /* TODO */ },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * AI健康助手卡片
 * 
 * 显示AI助手功能和建议
 */
@Composable
fun AIHealthAssistantCard() {
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
                    text = "AI健康助手",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "智能健康建议和风险预警",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "今日建议: 适量运动，注意饮食",
                    fontSize = 11.sp,
                    color = IconTint,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        color = ButtonBackground,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "AI助手",
                    tint = IconTint,
                    modifier = Modifier
                        .size(35.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * 智能提醒设置卡片
 * 
 * 显示各种智能提醒设置选项
 */
@Composable
fun SmartReminderCard() {
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
            Text(
                text = "智能提醒设置",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoalItem(
                    icon = Icons.Default.Favorite,
                    title = "用药提醒",
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                )
                GoalItem(
                    icon = Icons.Default.Star,
                    title = "运动提醒",
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                )
                GoalItem(
                    icon = Icons.Default.CheckCircle,
                    title = "饮食提醒",
                    onClick = { /* TODO */ },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
} 