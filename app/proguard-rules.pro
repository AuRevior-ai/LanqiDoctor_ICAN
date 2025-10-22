# 保护 LLM 相关类
-keep class com.lanqiDoctor.demo.ui.activity.LocalLLMActivity {*;}
-dontwarn com.lanqiDoctor.demo.ui.activity.LocalLLMActivity

# 保护原生方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保护 vla 命名空间相关的类
-keep class vla.**{*;}
-dontwarn vla.**