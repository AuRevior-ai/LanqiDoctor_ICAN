# 通义千问API集成说明

## 概述
本项目已成功集成阿里通义千问API，替换了原有的蓝心大模型。通义千问为医疗健康应用提供了强大的AI对话能力。

## 配置步骤

### 1. 获取通义千问API Key
1. 访问阿里云官网：https://www.aliyun.com/
2. 注册/登录阿里云账号
3. 进入控制台，搜索"DashScope"或"灵积模型服务"
4. 开通服务并获取API Key

### 2. 配置API Key
在 `app/src/main/java/com/lanqiDoctor/demo/config/AiConfig.java` 文件中：

```java
/** 通义千问API配置 */
public static final String QWEN_API_KEY = "your-qwen-api-key-here";
```

将 `"your-qwen-api-key-here"` 替换为您的实际API Key。

### 3. 选择模型
当前默认使用 `qwen-turbo` 模型，您也可以选择其他模型：
- `qwen-turbo`：响应快速，适合日常对话
- `qwen-plus`：更强的推理能力
- `qwen-max`：最强的通用能力

修改 `AiConfig.java` 中的模型配置：
```java
/** 通义千问模型名称 */
public static final String QWEN_MODEL = "qwen-turbo";
```

## 主要变更

### 1. 新增文件
- `QwenChatApi.java` - 通义千问API客户端

### 2. 修改文件
- `AiConfig.java` - 添加通义千问配置
- `AiService.java` - 集成通义千问调用逻辑

### 3. 功能特性
- ✅ 支持多轮对话
- ✅ 支持系统人设（医疗场景优化）
- ✅ 支持流式响应
- ✅ 完全兼容现有聊天框架
- ✅ 自动错误处理和重试机制

## 使用说明

### 当前使用的模型
项目已配置为使用通义千问，在 `AiConfig.java` 中：
```java
/** 当前使用的模型 */
public static final ModelType CURRENT_MODEL = ModelType.QWEN;
```

### 切换模型
如需切换回其他模型，修改 `CURRENT_MODEL`：
```java
// 使用蓝心模型（已废弃）
public static final ModelType CURRENT_MODEL = ModelType.LANXIN;

// 使用OpenAI模型
public static final ModelType CURRENT_MODEL = ModelType.OPENAI;

// 使用通义千问模型（推荐）
public static final ModelType CURRENT_MODEL = ModelType.QWEN;
```

## API使用费用

通义千问提供免费额度：
- 新用户可获得免费的API调用额度
- 超出免费额度后按实际使用量付费
- 费用相对较低，适合开发和小规模应用

## 技术细节

### API端点
```
https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
```

### 请求格式
```json
{
  "model": "qwen-turbo",
  "input": {
    "messages": [
      {"role": "system", "content": "医疗AI助手人设..."},
      {"role": "user", "content": "用户问题"}
    ]
  },
  "parameters": {
    "max_tokens": 2048,
    "temperature": 0.9,
    "top_p": 0.7,
    "repetition_penalty": 1.02
  }
}
```

### 响应格式
```json
{
  "output": {
    "text": "AI响应内容",
    "choices": [...]
  },
  "usage": {...},
  "request_id": "..."
}
```

## 故障排除

### 1. API Key错误
确保API Key正确配置，检查是否有多余的空格或特殊字符。

### 2. 网络连接问题
确保服务器可以访问 `dashscope.aliyuncs.com`，检查防火墙设置。

### 3. 模型响应异常
检查日志输出，通义千问的错误信息会详细说明问题原因。

### 4. 流式响应问题
如果流式响应有问题，可以先使用同步模式测试基本功能。

## 开发注意事项

1. **API限流**：通义千问有QPS限制，如需高并发请联系阿里云提升限额
2. **内容安全**：通义千问内置内容安全机制，某些敏感内容可能被拒绝
3. **模型版本**：定期关注阿里云更新，新版本模型可能有更好的性能
4. **日志记录**：项目已集成详细的日志记录，便于调试和监控

## 联系支持

如遇到技术问题，可以：
1. 查看阿里云DashScope官方文档
2. 提交工单到阿里云技术支持
3. 查看项目日志输出进行初步诊断

---

**注意：请妥善保管您的API Key，不要将其提交到公共代码仓库中。**