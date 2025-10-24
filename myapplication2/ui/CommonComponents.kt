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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication2.ui.theme.*

/**
 * 顶部导航栏组件 - 美化版本
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
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
        
        // 右侧用户头像按钮 - 使用美化图标按钮
        NeonIconButton(
            icon = Icons.Default.Person,
            onClick = onUserClick,
            modifier = Modifier.align(Alignment.CenterEnd),
            backgroundColor = Color.White,
            iconTint = IconTint
        )
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
 * 标签芯片组件 - 使用美化版本
 * 
 * 单个功能标签的实现，带有荧光效果
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
    NeonTagChip(
        text = text,
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier
    )
}

/**
 * 底部导航栏组件 - 方形长条版本
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
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(0.dp),
                spotColor = ShadowMedium
            ),
        color = CardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 首页导航项
            NeonBottomNavItem(
                icon = Icons.Default.Home,
                label = "首页",
                isSelected = true,
                onClick = onHomeClick
            )
            
            // 中央添加记录按钮
            NeonFloatingButton(
                icon = Icons.Default.Add,
                onClick = onAddClick,
                backgroundColor = ButtonPrimary,
                iconTint = Color.White,
                size = 52.dp
            )
            
            // 我的页面导航项
            NeonBottomNavItem(
                icon = Icons.Default.Person,
                label = "我的",
                isSelected = false,
                onClick = onProfileClick
            )
        }
    }
} 