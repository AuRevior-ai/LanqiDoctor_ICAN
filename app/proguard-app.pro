# 忽略警告
#-ignorewarning

# 混淆保护自己项目的部分代码以及引用的第三方jar包
#-libraryjars libs/xxxxxxxxx.jar

# 不混淆这个包下的类
-keep class com.lanqiDoctor.demo.http.api.** {
    <fields>;
}
-keep class com.lanqiDoctor.demo.http.response.** {
    <fields>;
}
-keep class com.lanqiDoctor.demo.http.model.** {
    <fields>;
}

# 不混淆被 Log 注解的方法信息
-keepclassmembernames class ** {
    @com.lanqiDoctor.demo.aop.Log <methods>;
}

# Vivo Health Kit SDK 混淆配置
#-dontwarn com.vivo.healthservice.kit.bean.**
#-keep class com.vivo.healthservice.kit.** { *;}
#-keep class com.vivo.healthservice.kit.cloud.AuthResult { *;}
#-keep class com.vivo.healthservice.kit.CallResult { *;}