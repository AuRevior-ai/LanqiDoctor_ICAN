/**
 * 美化按钮组件系统
 * 
 * 提供统一的、带有荧光效果和柔和阴影的按钮组件
 * 设计风格：浅色简约、荧光效果、柔和圆角
 * 
 * @author 蓝岐健康提醒团队
 * @version 2.0
 * @since 2025-10-24
 */
package com.example.myapplication2.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication2.ui.theme.*

/**
 * 主按钮 - 荧光蓝色背景，带发光效果
 * 
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param enabled 是否启用
 * @param icon 可选图标
 */
@Composable
fun NeonPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 按下时的动画效果
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .height(48.dp)
    ) {
        // 发光层（荧光效果）
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(12.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GlowBlue,
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        )
        
        // 按钮主体
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (isPressed) 2.dp else 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = GlowBlue
                ),
            enabled = enabled,
            shape = RoundedCornerShape(24.dp),
            color = if (isPressed) ButtonPrimaryPressed else ButtonPrimary,
            interactionSource = interactionSource
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = TextOnNeon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = TextOnNeon,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * 次要按钮 - 浅色背景带边框
 * 
 * @param text 按钮文字
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param enabled 是否启用
 * @param icon 可选图标
 */
@Composable
fun NeonSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .border(
                width = 1.5.dp,
                color = ButtonSecondaryBorder,
                shape = RoundedCornerShape(24.dp)
            )
            .shadow(
                elevation = if (isPressed) 1.dp else 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = ShadowLight
            ),
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        color = if (isPressed) ButtonSecondaryBorder else ButtonSecondary,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = IconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 状态按钮 - 小型药丸形状，用于状态指示
 * 
 * @param text 状态文字
 * @param icon 可选图标
 * @param modifier 修饰符
 * @param backgroundColor 背景颜色
 */
@Composable
fun NeonStatusButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    backgroundColor: Color = NeonPink
) {
    Surface(
        modifier = modifier
            .height(28.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = backgroundColor.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 圆形浮动按钮 - 带柔和阴影
 * 
 * @param icon 图标
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param backgroundColor 背景颜色
 * @param iconTint 图标颜色
 * @param size 按钮大小
 */
@Composable
fun NeonFloatingButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ButtonPrimary,
    iconTint: Color = Color.White,
    size: Dp = 56.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Box(
        modifier = modifier.size(size)
    ) {
        // 发光层
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(16.dp)
                .background(
                    color = backgroundColor.copy(alpha = 0.4f),
                    shape = CircleShape
                )
        )
        
        // 按钮主体
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (isPressed) 4.dp else 12.dp,
                    shape = CircleShape,
                    spotColor = backgroundColor.copy(alpha = 0.3f)
                ),
            shape = CircleShape,
            color = if (isPressed) backgroundColor.copy(alpha = 0.9f) else backgroundColor,
            interactionSource = interactionSource
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(size * 0.45f)
                )
            }
        }
    }
}

/**
 * 小型圆形图标按钮 - 用于导航或辅助功能
 * 
 * @param icon 图标
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param backgroundColor 背景颜色
 * @param iconTint 图标颜色
 */
@Composable
fun NeonIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    iconTint: Color = IconTint
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                spotColor = ShadowLight
            ),
        shape = CircleShape,
        color = backgroundColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * 标签芯片按钮 - 用于选项卡切换，带选中效果
 * 
 * @param text 标签文字
 * @param isSelected 是否选中
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun NeonTagChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(modifier = modifier) {
        // 选中时的发光效果
        if (isSelected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(8.dp)
                    .background(
                        color = GlowBlue,
                        shape = RoundedCornerShape(20.dp)
                    )
            )
        }
        
        // 芯片主体
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .shadow(
                    elevation = if (isSelected) 6.dp else 2.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = if (isSelected) GlowBlue else ShadowLight
                ),
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            interactionSource = interactionSource
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = text,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 底部导航栏按钮项
 * 
 * @param icon 图标
 * @param label 标签文字
 * @param isSelected 是否选中
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun NeonBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 4.dp)
    ) {
        // 图标背景
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(if (isSelected) 32.dp else 28.dp)
                .background(
                    color = if (isSelected) NeonBlueSoft else Color.Transparent,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) IconTint else TextTertiary,
                modifier = Modifier.size(if (isSelected) 22.dp else 20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) IconTint else TextTertiary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * 功能卡片按钮 - 方形卡片带图标和标题
 * 
 * @param icon 图标
 * @param title 标题
 * @param subtitle 副标题
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param hasNotification 是否显示通知点
 */
@Composable
fun NeonFunctionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasNotification: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Box(modifier = modifier.aspectRatio(1f)) {
        // 发光层
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(4.dp)
                .blur(12.dp)
                .background(
                    color = if (isPressed) GlowBlue else ShadowLight,
                    shape = RoundedCornerShape(20.dp)
                )
        )
        
        // 卡片主体
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (isPressed) 4.dp else 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = ShadowMedium
                ),
            shape = RoundedCornerShape(20.dp),
            color = if (isPressed) CardBackgroundSoft else CardBackground,
            interactionSource = interactionSource
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 图标背景
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            color = SurfaceGlow,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = IconTint,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
        
        // 通知标识
        if (hasNotification) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopEnd)
                    .offset((-8).dp, 8.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        spotColor = GlowPink
                    )
                    .background(
                        color = StatusError,
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * 横排云朵样式功能按钮 - 模仿第二张图片样式
 * 
 * @param icon 图标
 * @param title 标题
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param iconBackgroundColor 图标背景颜色
 * @param glowColor 发光颜色
 */
@Composable
fun CloudStyleFunctionButton(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconBackgroundColor: Color = NeonBlue,
    glowColor: Color = GlowBlue
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Column(
        modifier = modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 云朵图标容器
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(72.dp)
        ) {
            // 外层发光效果 - 增强荧光
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .blur(20.dp)
                    .background(
                        color = glowColor,
                        shape = RoundedCornerShape(22.dp)
                    )
            )
            
            // 内层发光效果
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .blur(12.dp)
                    .background(
                        color = glowColor.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(20.dp)
                    )
            )
            
            // 图标背景主体 - 不规则云朵形状
            Surface(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(
                        elevation = if (isPressed) 4.dp else 8.dp,
                        shape = RoundedCornerShape(18.dp),
                        spotColor = glowColor
                    ),
                shape = RoundedCornerShape(18.dp),
                color = iconBackgroundColor
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 标题文字
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

