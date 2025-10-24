package com.example.myapplication2.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// ============ 医疗健康应用颜色主题 - 浅色简约荧光风格 ============

// 渐变背景色 - 温暖的浅米色渐变
val BlueGradientStart = Color(0xFFFFFBF5)  // 极浅米白色渐变起始
val BlueGradientEnd = Color(0xFFF5EFE7)    // 浅米色渐变结束

// 荧光主色调 - 带有发光感的蓝色系
val NeonBlue = Color(0xFF5B9FFF)           // 荧光蓝（主要按钮）
val NeonBlueDark = Color(0xFF4A8FEF)       // 深荧光蓝（按钮按下）
val NeonBlueLight = Color(0xFF7BB4FF)      // 浅荧光蓝（荧光效果）
val NeonBlueSoft = Color(0xFFD6EBFF)       // 柔和荧光蓝（背景）

// 辅助荧光色
val NeonPink = Color(0xFFFFB4C8)           // 荧光粉（状态指示）
val NeonPinkDark = Color(0xFFFF9BB4)       // 深荧光粉
val NeonMint = Color(0xFFB4FFE8)           // 荧光薄荷绿（成功状态）
val NeonPurple = Color(0xFFD4C4FF)         // 荧光紫（特殊状态）

// 背景和卡片
val CardBackground = Color(0xFFFFFFFE)     // 纯白卡片背景
val CardBackgroundSoft = Color(0xFFF8FBFF) // 柔和白底
val SurfaceGlow = Color(0xFFF0F7FF)        // 发光表面

// 文字颜色 - 柔和的深色
val TextPrimary = Color(0xFF2D3748)        // 主要文字（柔和黑）
val TextSecondary = Color(0xFF718096)      // 次要文字（柔和灰）
val TextTertiary = Color(0xFFA0AEC0)       // 第三级文字（浅灰）
val TextOnBlue = Color(0xFFFFFFFF)         // 蓝色背景上的文字
val TextOnNeon = Color(0xFFFFFFFF)         // 荧光色背景上的文字

// 图标和装饰
val IconTint = Color(0xFF5B9FFF)           // 图标色调（荧光蓝）
val IconTintLight = Color(0xFF8EBFFF)      // 浅色图标
val IconTintDark = Color(0xFF4A8FEF)       // 深色图标

// 按钮颜色系统
val ButtonPrimary = Color(0xFF5B9FFF)      // 主按钮背景（荧光蓝）
val ButtonPrimaryPressed = Color(0xFF4A8FEF) // 主按钮按下
val ButtonSecondary = Color(0xFFF8FBFF)    // 次按钮背景（浅色）
val ButtonSecondaryBorder = Color(0xFFD6EBFF) // 次按钮边框
val ButtonTertiary = Color(0xFFFFFFFF)     // 第三级按钮

// 阴影和发光效果
val ShadowLight = Color(0x08000000)        // 轻阴影
val ShadowMedium = Color(0x12000000)       // 中阴影
val ShadowStrong = Color(0x1A000000)       // 强阴影
val GlowBlue = Color(0x805B9FFF)          // 蓝色发光（增强透明度）
val GlowPink = Color(0x80FFB4C8)          // 粉色发光（增强透明度）
val GlowOrange = Color(0x80FF9B7A)        // 橙色发光
val GlowGreen = Color(0x8084E8B4)         // 绿色发光
val GlowTeal = Color(0x807AC8E8)          // 青绿色发光

// 状态颜色
val StatusSuccess = Color(0xFF81E6D9)      // 成功（薄荷绿）
val StatusWarning = Color(0xFFFBD38D)      // 警告（柔和橙）
val StatusError = Color(0xFFFCA5A5)        // 错误（柔和红）
val StatusInfo = Color(0xFFBEE3F8)         // 信息（柔和蓝）

// 旧版兼容
val LightBlue = NeonBlueLight              // 浅蓝色（兼容）
val CardShadow = ShadowMedium              // 卡片阴影（兼容）
val ButtonBackground = SurfaceGlow         // 按钮背景色（兼容）