package com.lanqiDoctor.demo.config;

/**
 * AI对话配置
 */
public class AiConfig {
    
    // === OpenAI配置 ===
    /** OpenAI API Base URL */
    public static final String API_BASE_URL = "https://api.openai.com/v1/";
    
    /** API Key */
    public static final String API_KEY = "your-api-key-here";
    
    /** OpenAI模型名称 */
    public static final String OPENAI_MODEL = "gpt-3.5-turbo";
    
    // === 蓝心大模型配置 ===
    /** 蓝心API配置 */
    public static final String LANXIN_APP_ID = "2025652764";
    public static final String LANXIN_APP_KEY = "UmHoXRDiNkktFxZZ";
    
    /** 蓝心模型名称 */
    public static final String LANXIN_MODEL = "vivo-BlueLM-TB-Pro";
    
    // === 阿里通义千问配置 ===
    /** 通义千问API配置 */
    public static final String QWEN_API_KEY = "your-qwen-api-key-here";
    
    /** 通义千问API Base URL */
    public static final String QWEN_API_BASE_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    
    /** 通义千问模型名称 */
    public static final String QWEN_MODEL = "qwen-turbo";
    
    // === 系统人设配置 ===
    /** 默认系统人设 */
    public static final String DEFAULT_SYSTEM_PROMPT = 
        "# 角色与目标\n" +
                "作为蓝歧医童，你的任务是提供专业的医疗建议和用药监护、症状预诊、知识验证、结合症状分析智能推荐就诊科室。\n" +
                "\n" +
                "# 思考路径\n" +
                "你需要考虑患者的病情和需求，并在诊断和治疗的过程中找到解决方案。\n" +
                "如果用户的描述过于模糊， 你应进一步询问而不是列出各种可能。\n" +
                "\n" +
                "# 个性化\n" +
                "为了确保内容的个性化，你需要根据患者的具体情况提供个性化的医疗建议和治疗方案，不要忘记使用特征变量。\n" +
                "\n" +
                "# 角色设定\n" +
                "1. 你的角色是蓝歧医童，并且你拥有医学背景。\n" +
                "2. 你需要从患者的角度出发，考虑他们的需求和感受。\n" +
                "\n" +
                "# 组件能力\n" +
                "你具备医学知识能力，包括但不限于诊断、治疗和预防疾病，以及提供健康咨询和建议。\n" +
                "回答精准锚定国家药典知识库条目，对未验证内容自动触发风险警示\n" +
                "降低“庸医误诊”导致的用药危害\n" +
                "# 技能\n" +
                "1. 你可以根据用户的个性化需求制定相应的策略。\n" +
                "2. 用户在发出模糊化的需求时，你应该通过推理进一步询问用户的信息，用以进一步推理。\n" +
                "3. 你应该在用户说出一些未经证实的医学知识、偏方、可能存在用药风险的内容时，主动识别并及时提醒。\n" +
                "# 要求与限制\n" +
                "1. 输出内容的风格要求专业、准确、易懂、口语化、亲和力高，以“医童”的口吻描述。\n" +
                "2. 输出结果的格式为文字描述。\n" +
                "3. 你需要特别注意保护患者的隐私和权益。\n" +
                "4. 在完成任务时，请确保遵守相关法律法规和职业道德规范，回复时要温和、专业、负责任。";
    
    /** 当前使用的系统人设 */
    public static String CURRENT_SYSTEM_PROMPT = DEFAULT_SYSTEM_PROMPT;
    
    // === 对话配置 ===
    /** 是否启用多轮对话历史 */
    public static final boolean ENABLE_CONVERSATION_HISTORY = true;
    
    /** 最大历史对话轮数 */
    public static final int MAX_CONVERSATION_ROUNDS = 10;
    
    /** 是否在每次对话中包含系统人设 */
    public static final boolean INCLUDE_SYSTEM_PROMPT = true;
    
    // === 通用配置 ===
    /** 当前使用的模型类型 */
    public enum ModelType {
        OPENAI,
        LANXIN,
        QWEN
    }
    
    /** 当前使用的模型 */
    public static final ModelType CURRENT_MODEL = ModelType.QWEN;
    
    /** 根据当前模型获取模型名称 */
    public static String getModelName() {
        switch (CURRENT_MODEL) {
            case LANXIN:
                return LANXIN_MODEL;
            case QWEN:
                return QWEN_MODEL;
            case OPENAI:
            default:
                return OPENAI_MODEL;
        }
    }
    
    /** 获取当前模型名称（兼容旧代码） */
    public static final String MODEL = LANXIN_MODEL; // 临时设置为蓝心模型，实际使用时会被getModelName()覆盖
    
    /** 最大token数 */
    public static final int MAX_TOKENS = 2048;
    
    /** 温度参数 */
    public static final double TEMPERATURE = 0.9;
    
    /** top_p参数 */
    public static final double TOP_P = 0.7;
    
    /** top_k参数 */
    public static final int TOP_K = 50;
    
    /** 重复惩罚 */
    public static final double REPETITION_PENALTY = 1.02;
    
    /** 流式响应 */
    public static final boolean STREAM = true;
    
    /** 是否使用流式模式 */
    public static final boolean USE_STREAM_MODE = true;
    
    // === 获取方法 ===
    public static String getAppId() {
        return LANXIN_APP_ID;
    }
    
    public static String getAppKey() {
        return LANXIN_APP_KEY;
    }
    
    public static String getQwenApiKey() {
        return QWEN_API_KEY;
    }
    
    public static boolean isLanXinModel() {
        return CURRENT_MODEL == ModelType.LANXIN;
    }
    
    public static boolean isOpenAIModel() {
        return CURRENT_MODEL == ModelType.OPENAI;
    }
    
    public static boolean isQwenModel() {
        return CURRENT_MODEL == ModelType.QWEN;
    }
    
    /**
     * 获取当前系统人设
     */
    public static String getSystemPrompt() {
        return CURRENT_SYSTEM_PROMPT;
    }
    
    /**
     * 设置系统人设
     */
    public static void setSystemPrompt(String systemPrompt) {
        CURRENT_SYSTEM_PROMPT = systemPrompt != null ? systemPrompt : DEFAULT_SYSTEM_PROMPT;
    }
    
    /**
     * 重置为默认系统人设
     */
    public static void resetSystemPrompt() {
        CURRENT_SYSTEM_PROMPT = DEFAULT_SYSTEM_PROMPT;
    }
    
    /**
     * 是否启用多轮对话
     */
    public static boolean isConversationHistoryEnabled() {
        return ENABLE_CONVERSATION_HISTORY;
    }
    
    /**
     * 是否包含系统人设
     */
    public static boolean shouldIncludeSystemPrompt() {
        return INCLUDE_SYSTEM_PROMPT && CURRENT_SYSTEM_PROMPT != null && !CURRENT_SYSTEM_PROMPT.trim().isEmpty();
    }
}