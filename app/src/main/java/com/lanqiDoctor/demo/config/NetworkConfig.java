package com.lanqiDoctor.demo.config;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 网络配置管理器
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class NetworkConfig {
    
    private static NetworkConfig instance;
    private JsonObject config;
    private Gson gson;
    
    private NetworkConfig(Context context) {
        gson = new Gson();
        loadConfig(context);
    }
    
    public static synchronized NetworkConfig getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkConfig(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * 从assets加载配置文件
     */
    private void loadConfig(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("network_config.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonString = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            
            config = gson.fromJson(jsonString.toString(), JsonObject.class);
            reader.close();
            inputStream.close();
            
        } catch (IOException e) {
            e.printStackTrace();
            // 使用默认配置
            createDefaultConfig();
        }
    }
    
    /**
     * 创建默认配置
     */
    private void createDefaultConfig() {
        String defaultConfig = "{\n" +
                "  \"server\": {\n" +
                "    \"baseUrl\": \"https://api.lanqi.edulearn.cn\",\n" +
                "    \"apiVersion\": \"v1\",\n" +
                "    \"timeout\": 30000\n" +
                "  },\n" +
                "  \"validation\": {\n" +
                "    \"email\": {\n" +
                "      \"pattern\": \"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\\\.[a-zA-Z]{2,}$\",\n" +
                "      \"maxLength\": 100\n" +
                "    },\n" +
                "    \"verificationCode\": {\n" +
                "      \"length\": 6\n" +
                "    }\n" +
                "  }\n" +
                "}";
        config = gson.fromJson(defaultConfig, JsonObject.class);
    }
    
    /**
     * 获取服务器基础URL
     */
    public String getBaseUrl() {
        return config.getAsJsonObject("server").get("baseUrl").getAsString();
    }
    
    /**
     * 获取API版本
     */
    public String getApiVersion() {
        return config.getAsJsonObject("server").get("apiVersion").getAsString();
    }
    
    /**
     * 获取请求超时时间
     */
    public int getTimeout() {
        return config.getAsJsonObject("server").get("timeout").getAsInt();
    }
    
    /**
     * 获取邮箱验证正则表达式
     */
    public String getEmailPattern() {
        return config.getAsJsonObject("validation")
                .getAsJsonObject("email")
                .get("pattern").getAsString();
    }
    
    /**
     * 获取邮箱最大长度
     */
    public int getEmailMaxLength() {
        return config.getAsJsonObject("validation")
                .getAsJsonObject("email")
                .get("maxLength").getAsInt();
    }
    
    /**
     * 获取验证码长度
     */
    public int getVerificationCodeLength() {
        return config.getAsJsonObject("validation")
                .getAsJsonObject("verificationCode")
                .get("length").getAsInt();
    }
}