package com.lanqiDoctor.demo.util;

import android.text.TextUtils;

import com.lanqiDoctor.demo.config.NetworkConfig;

import java.util.regex.Pattern;

/**
 * 邮箱验证工具类
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class EmailValidator {
    
    /**
     * 验证邮箱格式
     */
    public static boolean isValidEmail(String email, NetworkConfig config) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        
        // 长度检查
        if (email.length() > config.getEmailMaxLength()) {
            return false;
        }
        
        // 格式检查
        Pattern pattern = Pattern.compile(config.getEmailPattern());
        return pattern.matcher(email).matches();
    }
    
    /**
     * 验证邮箱格式（使用默认配置）
     */
    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailPattern);
        return pattern.matcher(email).matches();
    }
    
    /**
     * 获取邮箱错误提示信息
     */
    public static String getEmailErrorMessage(String email, NetworkConfig config) {
        if (TextUtils.isEmpty(email)) {
            return "请输入邮箱地址";
        }
        
        if (email.length() > config.getEmailMaxLength()) {
            return "邮箱地址过长";
        }
        
        if (!isValidEmail(email, config)) {
            return "请输入正确的邮箱格式";
        }
        
        return null;
    }
}