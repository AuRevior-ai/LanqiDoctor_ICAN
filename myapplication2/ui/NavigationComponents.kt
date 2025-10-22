/**
 * 导航组件
 * 
 * 包含应用导航相关的枚举定义和通用功能页面组件
 * 
 * @author 蓝岐健康提醒团队
 * @version 1.0
 * @since 2025-06-21
 */
package com.example.myapplication2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
 * 应用导航状态枚举
 * 
 * 定义了应用中所有可能的页面状态，用于页面导航和状态管理
 */
enum class NavigationScreen {
    HOME,               // 主页
    VOICE_CHAT,         // 语音对话功能页
    TEXT_INPUT,         // 文字输入功能页
    PHOTO_RECOGNITION,  // 药品图片识别功能页
    SYMPTOM_TRACKING,   // 症状轨迹可视化功能页
    GOAL_SETTING,       // 目标设置详情页
    USER_PROFILE,       // 用户信息页
    ADD_RECORD          // 添加健康记录功能页
}

/**
 * 通用功能页面组件
 * 
 * 用于显示各种功能详情页面的通用模板
 * 
 * @param title 页面标题
 * @param description 页面描述内容
 * @param onBack 返回按钮点击回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunctionScreen(
    title: String,
    description: String,
    onBack: () -> Unit
) {
    // 创建页面背景渐变色
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(BlueGradientStart, BlueGradientEnd)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 顶部导航栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = TextOnBlue
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnBlue
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 内容区域
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = description,
                        fontSize = 16.sp,
                        color = TextSecondary,
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IconTint
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "返回首页",
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 目标详情页面组件
 * 
 * 显示特定目标（在用药、医嘱识别、拍照）的详细信息和操作选项
 * 
 * @param goal 目标名称
 * @param onBack 返回按钮点击回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goal: String,
    onBack: () -> Unit
) {
    // 创建页面背景渐变色
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(BlueGradientStart, BlueGradientEnd)
    )
    
    val goalDetails = when (goal) {
        "在用药" -> "这是在用药功能页面\n\n• 每日按时服药提醒\n• 药物相互作用检查\n• 用药记录追踪\n• 剂量管理建议"
        "医嘱识别" -> "这是医嘱识别功能页面\n\n• 智能病症分析\n• 健康风险评估\n• 个性化建议\n• 医嘱解读服务"
        "拍照" -> "这是拍照功能页面\n\n• 健康知识学习\n• 日常保健指导\n• 专家建议推送\n• 图像识别分析"
        else -> "功能详情"
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 顶部导航栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = TextOnBlue
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${goal}功能",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnBlue
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 内容区域
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "${goal}功能",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = goalDetails,
                        fontSize = 16.sp,
                        color = TextSecondary,
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = IconTint
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(IconTint, IconTint))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "返回",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        Button(
                            onClick = { /* TODO: 开始设置功能 */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IconTint
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "开始设置",
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
} 