# 权限管理系统使用说明

## 功能概述

本系统提供了统一的权限管理界面，用于检查和申请Android应用运行所需的各种权限，特别是针对用药提醒功能。

## 主要特性

1. **统一权限管理** - 将所有权限检查集中在一个页面
2. **权限分类** - 将权限分为必需、推荐、可选三类
3. **自动跳转** - 权限检查完成后自动跳转到指定页面
4. **状态可视化** - 清晰显示每个权限的授予状态
5. **智能重试** - 支持重新检查和申请权限

## 权限类型

### 必需权限（Essential）
- **精确闹钟权限** (SCHEDULE_EXACT_ALARM) - 用于设置精确的用药提醒时间
- **通知权限** (POST_NOTIFICATIONS) - 用于显示用药提醒通知（Android 13+）
- **唤醒锁权限** (WAKE_LOCK) - 确保在设备睡眠时也能触发提醒

### 推荐权限（Recommended）
- **悬浮窗权限** (SYSTEM_ALERT_WINDOW) - 用于在其他应用上方显示提醒弹窗
- **开机自启动** - 确保设备重启后提醒功能正常工作

### 可选权限（Optional）
- **忽略电池优化** - 防止系统杀死后台提醒服务
- **自启动管理** - 部分厂商定制权限

## 使用方法

### 1. 基本使用
```java
// 简单启动权限检查页面
PermissionCheckActivity.start(context);
```

### 2. 带跳转的使用（推荐）
```java
// 权限检查完成后跳转到指定Activity
Intent intent = new Intent(this, PermissionCheckActivity.class);
intent.putExtra("next_activity", HealthMainActivity.class.getName());
intent.putExtra("finish_current", true);
startActivity(intent);
finish();
```

### 3. 集成到登录流程
在 `LoginActivity.java` 中，登录成功后的跳转逻辑已修改为：
```java
private void checkPermissionsAndGoToHome() {
    Intent intent = new Intent(this, PermissionCheckActivity.class);
    intent.putExtra("next_activity", HealthMainActivity.class.getName());
    intent.putExtra("finish_current", true);
    startActivity(intent);
    finish();
}
```

## 技术实现

### 主要类文件
1. **PermissionCheckActivity.java** - 主要的权限检查Activity
2. **PermissionItem.java** - 权限项数据模型
3. **PermissionAdapter.java** - RecyclerView适配器

### 布局文件
1. **permission_check_activity.xml** - 主界面布局
2. **item_permission.xml** - 权限项布局

### 资源文件
1. **ic_security.xml** - 安全图标
2. **ic_check_circle.xml** - 已授权图标
3. **ic_error_circle.xml** - 未授权图标
4. **bg_permission_type.xml** - 权限类型背景

## 权限检查流程

1. **界面初始化** - 加载所有需要检查的权限项
2. **权限检查** - 遍历检查每个权限的授予状态
3. **状态显示** - 在界面上显示权限检查结果
4. **用户交互** - 用户可以点击单个权限项进行申请
5. **完成处理** - 用户点击"继续使用"后跳转到指定页面

## 注意事项

1. **版本兼容性** - 针对Android 13+的POST_NOTIFICATIONS权限进行了特殊处理
2. **权限申请** - 使用XXPermissions库处理标准权限，特殊权限需要手动跳转设置页面
3. **用户体验** - 即使部分权限未授予，用户仍可选择继续使用应用
4. **状态同步** - 从设置页面返回时会自动重新检查权限状态

## 扩展说明

如需添加新的权限检查，只需在 `initPermissionList()` 方法中添加新的 PermissionItem 即可：

```java
permissionList.add(new PermissionItem(
    "权限名称",
    "权限描述",
    Permission.PERMISSION_NAME,
    PermissionItem.Type.ESSENTIAL, // 或 RECOMMENDED, OPTIONAL
    true // 是否使用XXPermissions处理
));
```

## 集成建议

1. **首次启动** - 在应用首次启动时进行权限检查
2. **登录后** - 在用户登录成功后进行权限检查
3. **功能使用前** - 在使用特定功能前检查相关权限
4. **设置页面** - 在设置页面提供权限管理入口
