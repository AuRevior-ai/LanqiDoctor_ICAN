# 🔧 502错误排查与解决方案

## 🚨 问题描述

**症状：**
- 上午能正常运行
- 下午登录时报502错误

**502错误含义：**
```
HTTP 502 Bad Gateway
= 网关错误
= 服务器无响应或返回无效响应
```

---

## 🔍 可能原因分析

### 1️⃣ 服务器问题（最常见）⭐⭐⭐⭐⭐

#### 1.1 服务器维护/重启
```
上午可用 → 下午维护 → 502错误
```

**排查方法：**
- 联系后端开发确认服务器状态
- 查看服务器监控面板
- 等待一段时间后重试

#### 1.2 服务器过载
```
下午访问量大 → 服务器崩溃 → 502错误
```

**排查方法：**
- 查看服务器CPU/内存使用率
- 检查日志是否有大量请求

#### 1.3 服务器宕机
```
服务器停止运行 → 无法响应 → 502错误
```

**排查方法：**
- ping服务器地址
- 浏览器访问API地址测试

---

### 2️⃣ 网络问题 ⭐⭐⭐⭐

#### 2.1 网络切换
```
上午：WiFi连接 → 正常
下午：切换到移动网络 → 防火墙拦截 → 502
```

**排查方法：**
- 检查设备网络连接
- 切换WiFi/移动数据试试
- 检查网络代理设置

#### 2.2 DNS解析失败
```
DNS服务器问题 → 无法解析域名 → 502
```

**排查方法：**
```bash
# 测试DNS解析
nslookup api.lanqi.edulearn.cn
ping api.lanqi.edulearn.cn
```

---

### 3️⃣ API配置问题 ⭐⭐⭐

#### 检查当前配置

**你的API地址（来自configs.gradle）：**
```gradle
// 测试环境和预发布
HOST_URL = "https://api.lanqi.edulearn.cn/"  ✅ 正确

// 正式环境 (第57行)
HOST_URL = "https:/api.lanqi.edulearn.cn/"   ❌ 缺少一个斜杠！
```

**⚠️ 发现问题：正式环境URL有错误！**

**修复方法：**
打开 `configs.gradle` 第57行，改为：
```gradle
HOST_URL = "https://api.lanqi.edulearn.cn/"  // 补上一个斜杠
```

---

### 4️⃣ 后端API问题 ⭐⭐

#### 4.1 API接口变更
```
后端修改了登录接口 → 旧接口返回502
```

**你的登录API：**
```
user/login  或  auth/login
```

**排查方法：**
- 联系后端确认接口地址是否变更
- 查看API文档

#### 4.2 数据库连接失败
```
后端连不上数据库 → 无法处理请求 → 502
```

---

## 🛠️ 立即排查步骤

### Step 1: 检查网络连接
```bash
# 在命令行执行
ping api.lanqi.edulearn.cn

# 预期结果：
64 bytes from ... time=20ms  ✅ 正常
Request timeout             ❌ 网络问题
```

### Step 2: 测试API可用性
```bash
# 使用curl测试（Windows PowerShell）
curl https://api.lanqi.edulearn.cn/user/login

# 预期结果：
{"code":400,...}  ✅ 服务器在线（参数错误是正常的）
502 Bad Gateway   ❌ 服务器问题
Connection refused ❌ 服务器宕机
```

### Step 3: 查看应用日志
在Android Studio中查看Logcat：
```
过滤器：tag:Http 或 tag:Network
查找关键词：502, error, exception
```

### Step 4: 切换网络
```
当前WiFi → 切换到移动数据
或
移动数据 → 切换到另一个WiFi
```

### Step 5: 检查时间
```
手机时间不准 → SSL证书验证失败 → 502

解决：
设置 → 日期和时间 → 自动设置
```

---

## ✅ 快速解决方案

### 方案1: 修复configs.gradle错误（推荐）⭐⭐⭐⭐⭐

```gradle
// 打开 configs.gradle 第57行
// 修改前：
HOST_URL = "https:/api.lanqi.edulearn.cn/"   ❌

// 修改后：
HOST_URL = "https://api.lanqi.edulearn.cn/"  ✅
```

**然后：**
1. Rebuild项目
2. 重新运行

### 方案2: 使用测试环境

如果正式服务器有问题，暂时使用测试环境：

```bash
# 运行Debug版本（自动使用测试服）
./gradlew assembleDebug
./gradlew installDebug
```

### 方案3: 添加网络错误处理

检查代码是否已有错误处理（已有）：
```java
@Override
public void onFail(Exception e) {
    String errorMessage = NetworkErrorHandler.getLoginErrorMessage(e);
    toast(errorMessage);  // 显示友好的错误提示
}
```

### 方案4: 添加重试机制

在 `LoginActivity.java` 中添加重试逻辑：
```java
private int retryCount = 0;
private static final int MAX_RETRY = 3;

private void performLogin(String email, String password) {
    if (!isNetworkAvailable()) {
        toast("网络连接不可用，请检查网络设置");
        return;
    }
    
    // 显示重试提示
    if (retryCount > 0) {
        toast("正在重试... (" + retryCount + "/" + MAX_RETRY + ")");
    }
    
    EasyHttp.post(this)
        .api(new LoginApi()...)
        .request(new HttpCallback<...>() {
            @Override
            public void onFail(Exception e) {
                if (retryCount < MAX_RETRY && is502Error(e)) {
                    retryCount++;
                    postDelayed(() -> performLogin(email, password), 2000);
                } else {
                    retryCount = 0;
                    toast(NetworkErrorHandler.getLoginErrorMessage(e));
                }
            }
            
            @Override
            public void onSucceed(...) {
                retryCount = 0;  // 重置计数
                // 原有成功逻辑
            }
        });
}
```

---

## 📊 错误代码对照表

| 状态码 | 含义 | 可能原因 | 解决方法 |
|--------|------|----------|----------|
| 502 | 网关错误 | 服务器宕机/维护 | 联系后端/等待恢复 |
| 503 | 服务不可用 | 服务器过载 | 稍后重试 |
| 504 | 网关超时 | 响应太慢 | 增加超时时间 |
| 500 | 服务器错误 | 后端代码错误 | 联系后端修复 |
| 404 | 未找到 | API地址错误 | 检查API路径 |
| 401 | 未授权 | Token过期 | 重新登录 |

---

## 🔧 代码层面优化建议

### 1. 添加更详细的错误日志

在 `LoginActivity.java` 中：
```java
@Override
public void onFail(Exception e) {
    // 记录详细错误信息
    Log.e("LoginError", "登录失败", e);
    Log.e("LoginError", "错误类型: " + e.getClass().getName());
    if (e instanceof HttpException) {
        HttpException httpException = (HttpException) e;
        Log.e("LoginError", "HTTP状态码: " + httpException.getCode());
        Log.e("LoginError", "错误消息: " + httpException.getMessage());
    }
    
    // 显示友好提示
    String errorMessage = NetworkErrorHandler.getLoginErrorMessage(e);
    toast(errorMessage);
}
```

### 2. 添加服务器健康检查

创建一个简单的ping接口检查：
```java
private void checkServerHealth() {
    EasyHttp.get(this)
        .api(new HealthCheckApi())  // GET /health
        .request(new HttpCallback<String>(this) {
            @Override
            public void onSucceed(String result) {
                Log.d("ServerHealth", "服务器正常");
            }
            
            @Override
            public void onFail(Exception e) {
                Log.e("ServerHealth", "服务器异常: " + e.getMessage());
                toast("服务器连接异常，请稍后重试");
            }
        });
}
```

### 3. 添加网络状态监听

```java
private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (isNetworkAvailable()) {
            toast("网络已连接");
        } else {
            toast("网络已断开");
        }
    }
};

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // 注册网络监听
    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    registerReceiver(networkReceiver, filter);
}

@Override
protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(networkReceiver);
}
```

---

## 🎯 快速诊断清单

### ✅ 检查清单

- [ ] 网络是否连接？
  - 打开浏览器测试
  - 检查WiFi/移动数据

- [ ] 服务器是否在线？
  - ping api.lanqi.edulearn.cn
  - curl测试API

- [ ] configs.gradle配置是否正确？
  - 检查第57行URL是否有拼写错误
  - 确认https://后有两个斜杠

- [ ] 手机时间是否正确？
  - 设置 → 自动设置时间

- [ ] 是否使用了代理？
  - 关闭VPN/代理

- [ ] 其他应用网络是否正常？
  - 测试浏览器/微信等

---

## 💡 临时解决方案

### 方案A: 跳过登录（仅测试用）

在 `LoginActivity.java` 中添加测试模式：
```java
// 仅用于测试，正式版需删除
private static final boolean TEST_MODE = true;

private void performLogin(String email, String password) {
    if (TEST_MODE) {
        // 跳过网络请求，直接进入
        toast("测试模式：跳过登录");
        checkPermissionsAndGoToHome();
        return;
    }
    
    // 正常登录逻辑...
}
```

### 方案B: 使用本地Mock数据

```java
@Override
public void onFail(Exception e) {
    // 如果是502错误，使用mock数据
    if (e.getMessage().contains("502")) {
        toast("服务器维护中，使用离线模式");
        // 创建模拟的登录数据
        LoginApi.Bean mockData = new LoginApi.Bean();
        // ... 设置模拟数据
        saveUserInfo(mockData);
        checkPermissionsAndGoToHome();
    } else {
        toast(NetworkErrorHandler.getLoginErrorMessage(e));
    }
}
```

---

## 🎯 建议操作（按优先级）

### 立即尝试（0-5分钟）

1. **检查网络**
   ```
   - 关闭WiFi，用移动数据
   - 或切换到另一个WiFi
   - 重启路由器
   ```

2. **重启应用**
   ```
   - 完全关闭应用
   - 清除缓存（设置 → 应用 → 清除缓存）
   - 重新启动
   ```

3. **检查时间**
   ```
   - 设置 → 日期和时间 → 自动设置
   ```

### 短期方案（5-30分钟）

4. **修复configs.gradle**
   ```gradle
   // 第57行
   HOST_URL = "https://api.lanqi.edulearn.cn/"  // 补全https://
   ```

5. **联系后端**
   ```
   - 询问服务器是否维护
   - 确认API地址是否变更
   - 查看服务器日志
   ```

6. **查看Logcat**
   ```
   Android Studio → Logcat
   过滤：error, 502, Http
   查看详细错误信息
   ```

### 长期方案（1-2小时）

7. **添加重试机制**（见上面代码）

8. **添加服务器健康检查**（见上面代码）

9. **优化错误提示**
   ```java
   if (isServerError(e)) {
       toast("服务器暂时无法连接，请稍后重试\n" +
             "或联系客服：400-xxx-xxxx");
   }
   ```

---

## 📱 用户端快速解决

如果是发布给用户的版本出现502：

### 给用户的提示
```
"服务器正在维护升级中
预计恢复时间：XX:XX
给您带来不便，敬请谅解

您可以：
1. 稍后重试
2. 使用离线模式（部分功能）
3. 联系客服：xxx-xxxx"
```

### 应用内降级方案
```java
private void show502Dialog() {
    new AlertDialog.Builder(this)
        .setTitle("服务器维护中")
        .setMessage("服务器正在升级，暂时无法登录\n\n" +
                    "• 预计恢复时间：30分钟\n" +
                    "• 您可以稍后重试\n" +
                    "• 或使用离线模式浏览")
        .setPositiveButton("重试", (d, w) -> performLogin(email, password))
        .setNegativeButton("离线模式", (d, w) -> enterOfflineMode())
        .setNeutralButton("退出", (d, w) -> finish())
        .show();
}
```

---

## 🔍 详细排查命令

### Windows PowerShell
```powershell
# 1. 测试网络连接
ping api.lanqi.edulearn.cn

# 2. 测试API
curl https://api.lanqi.edulearn.cn/user/login

# 3. 查看DNS
nslookup api.lanqi.edulearn.cn

# 4. 测试端口
Test-NetConnection api.lanqi.edulearn.cn -Port 443
```

### Android adb命令
```bash
# 查看应用日志
adb logcat | findstr "502"
adb logcat | findstr "Http"
adb logcat | findstr "Error"

# 查看网络状态
adb shell dumpsys connectivity
```

---

## 🎯 最可能的原因排序

1. **服务器维护/重启** (70%)
   - 解决：等待或联系后端

2. **网络问题** (15%)
   - 解决：切换网络

3. **configs.gradle拼写错误** (10%)
   - 解决：修复第57行URL

4. **后端API变更** (3%)
   - 解决：联系后端确认

5. **其他** (2%)
   - DNS、防火墙、代理等

---

## 📞 联系后端时提供的信息

```
问题：登录时出现502错误
时间：下午XX:XX
环境：Android应用
API：https://api.lanqi.edulearn.cn/user/login
错误码：502 Bad Gateway
请求方法：POST
其他：上午还能正常使用

请后端检查：
1. 服务器是否在运行？
2. 数据库连接是否正常？
3. 登录接口是否有问题？
4. 服务器日志中是否有错误？
```

---

## 🔒 预防措施

### 1. 添加服务器状态页
创建一个简单的状态页面：
```
https://status.lanqi.edulearn.cn
显示：
✅ 服务器运行正常
⚠️ 部分功能维护中
❌ 服务器离线
```

### 2. 应用内添加公告
```java
// 启动时检查公告
checkServerAnnouncement();

// 如有维护公告，提前通知用户
if (announcement.contains("维护")) {
    showMaintenanceDialog();
}
```

### 3. 离线模式支持
```java
// 部分功能支持离线使用
- 查看历史记录
- 浏览健康知识
- 查看本地数据
```

---

## 📋 configs.gradle完整检查

**打开文件：** `configs.gradle`

**检查第57行：**
```gradle
case SERVER_TYPE_PRODUCT:
    LOG_ENABLE = false
    BUGLY_ID = "请自行替换 Bugly 上面的 AppID"
    HOST_URL = "https:/api.lanqi.edulearn.cn/"  ← 这里！缺少一个斜杠
    break
```

**应该改为：**
```gradle
HOST_URL = "https://api.lanqi.edulearn.cn/"  ← 补上//
```

---

## 🎯 总结

**502错误90%是服务器端问题，不是你的代码问题！**

**建议操作顺序：**
1. 检查网络（1分钟）
2. 等待5-10分钟后重试
3. 修复configs.gradle拼写错误（如果有）
4. 联系后端确认服务器状态
5. 如急需使用，添加重试机制或离线模式

**需要我帮你添加重试机制或错误处理优化吗？**

---

*创建时间：2025年10月25日*  
*问题：502登录错误*  
*状态：待排查*


