/**
 * 家庭监护组件
 * 
 * 包含家庭监护功能相关的UI组件
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication2.ui.theme.*

/**
 * 家庭监护模式页面组件
 * 
 * 显示家庭成员健康监护相关功能，包括成员状态、监护功能、紧急联系和健康报告
 * 
 * @param onNavigate 页面导航回调
 */
@Composable
fun FamilyMonitoringPage(
    onNavigate: (NavigationScreen) -> Unit
) {
    // 滚动状态管理
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // 家庭成员健康状态监护卡片
        FamilyMembersCard()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 监护功能按钮：实时监控、位置追踪、紧急求助
        MonitoringFunctionButtons(onNavigate)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 紧急联系方式卡片
        EmergencyContactCard()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 健康报告卡片
        HealthReportCard()
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

/**
 * 家庭成员监护卡片
 * 
 * 显示家庭成员的健康状态监护信息
 */
@Composable
fun FamilyMembersCard() {
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
            Text(
                text = "家庭成员健康监护",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // 家庭成员状态列表：显示爸爸、妈妈、爷爷的健康状态
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FamilyMemberItem("爸爸", "正常", Color.Green, Modifier.weight(1f))
                FamilyMemberItem("妈妈", "正常", Color.Green, Modifier.weight(1f))
                FamilyMemberItem("爷爷", "正常", Color.Green, Modifier.weight(1f))
            }
        }
    }
}

/**
 * 家庭成员项目组件
 * 
 * 显示单个家庭成员的头像、姓名和健康状态
 * 
 * @param name 成员姓名
 * @param status 健康状态文字描述
 * @param statusColor 状态颜色
 * @param modifier 修饰符
 */
@Composable
fun FamilyMemberItem(
    name: String,
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 成员头像圆形背景
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    color = ButtonBackground,
                    shape = CircleShape
                )
        ) {
            // 用户图标
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = name,
                tint = IconTint,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // 成员姓名
        Text(
            text = name,
            fontSize = 12.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        // 健康状态
        Text(
            text = status,
            fontSize = 10.sp,
            color = statusColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 监护功能按钮组
 * 
 * 显示家庭监护相关的功能按钮：实时监控、位置追踪、紧急求助
 * 
 * @param onNavigate 页面导航回调
 */
@Composable
fun MonitoringFunctionButtons(onNavigate: (NavigationScreen) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 实时监控按钮：监控家庭成员的心率血压等生命体征
        FunctionButton(
            icon = Icons.Default.Favorite,
            title = "实时监控",
            subtitle = "心率血压",
            onClick = { /* TODO */ },
            modifier = Modifier.weight(1f)
        )
        // 位置追踪按钮：安全定位功能
        FunctionButton(
            icon = Icons.Default.LocationOn,
            title = "位置追踪",
            subtitle = "安全定位",
            onClick = { /* TODO */ },
            modifier = Modifier.weight(1f)
        )
        // 紧急求助按钮：一键呼救功能，带有通知提醒
        FunctionButton(
            icon = Icons.Default.Warning,
            title = "紧急求助",
            subtitle = "一键呼救",
            onClick = { /* TODO */ },
            hasNotification = true,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 紧急联系卡片
 * 
 * 显示紧急联系人信息
 */
@Composable
fun EmergencyContactCard() {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "紧急联系",
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "紧急联系人",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "120急救: 120\n家庭医生: 138-xxxx-1234\n紧急联系人: 139-xxxx-5678",
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * 健康报告卡片
 * 
 * 显示家庭健康报告信息
 */
@Composable
fun HealthReportCard() {
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
                    text = "家庭健康周报",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "本周家庭成员健康数据汇总",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = ButtonBackground,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "健康报告",
                    tint = IconTint,
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }
} 