/**
 * 通用组件
 * 
 * 包含应用中通用的UI组件，这些组件可以在多个页面中复用
 * 
 * @author 蓝岐健康提醒团队
 * @version 1.0
 * @since 2025-06-21
 */
package com.example.myapplication2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication2.ui.theme.*

/**
 * 顶部导航栏组件
 * 
 * 显示应用标题和用户头像，处理状态栏间距
 * 
 * @param onUserClick 用户头像点击回调
 */
@Composable
fun TopNavigationBar(
    onUserClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()  // 处理状态栏间距
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 居中显示的应用标题
        Text(
            text = "蓝岐健康提醒",
            color = TextOnBlue,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.Center)
        )
        
        // 右侧用户头像按钮
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onUserClick() }
                .align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "用户头像",
                tint = IconTint,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

/**
 * 功能标签选择组件
 * 
 * 显示三个功能标签，支持切换选择不同的功能页面
 * 
 * @param selectedTag 当前选中的标签
 * @param onTagSelected 标签选择回调
 */
@Composable
fun FunctionTags(
    selectedTag: String,
    onTagSelected: (String) -> Unit
) {
    // 三个功能标签列表
    val tags = listOf("健康中心", "家庭监护", "健康助手")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 遍历创建标签按钮
        tags.forEach { tag ->
            TagChip(
                text = tag,
                isSelected = selectedTag == tag,
                onClick = { onTagSelected(tag) },
                modifier = Modifier.weight(1f)  // 等分宽度
            )
        }
    }
}

/**
 * 标签芯片组件
 * 
 * 单个功能标签的实现
 * 
 * @param text 标签文字
 * @param isSelected 是否选中
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun TagChip(
    text: String, 
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f)
        ),
        modifier = modifier
            .height(36.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                color = if (isSelected) IconTint else TextOnBlue,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

/**
 * 底部导航栏组件
 * 
 * 显示应用底部导航栏，包含首页、添加、我的三个功能入口
 * 
 * @param modifier 修饰符
 * @param onHomeClick 首页点击回调
 * @param onAddClick 添加按钮点击回调
 * @param onProfileClick 我的页面点击回调
 */
@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 首页导航项
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onHomeClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "首页",
                    tint = IconTint,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "首页",
                    fontSize = 10.sp,
                    color = IconTint
                )
            }
            
            // 中央添加记录按钮（圆形突出设计）
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = IconTint,
                        shape = CircleShape
                    )
                    .clickable { onAddClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.Center)
                )
            }
            
            // 我的页面导航项
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onProfileClick() }
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "我的",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "我的",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
} 