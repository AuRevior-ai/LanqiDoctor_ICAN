package com.lanqiDoctor.demo.util;

import android.content.Context;
import android.content.Intent;

import com.lanqiDoctor.demo.manager.UserStateManager;
import com.lanqiDoctor.demo.ui.activity.LoginActivity;
import com.lanqiDoctor.demo.ui.activity.HealthMainActivity;

/**
 * 登录状态检查工具类
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class LoginCheckUtil {
    
    /**
     * 检查登录状态并跳转到相应页面
     */
    public static void checkLoginAndNavigate(Context context) {
        UserStateManager userStateManager = UserStateManager.getInstance(context);
        
        if (userStateManager.isUserLoggedIn()) {
            // 已登录，跳转到首页
            Intent intent = new Intent(context, HealthMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } else {
            // 未登录，跳转到登录页
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }
    
    /**
     * 强制用户重新登录
     */
    public static void forceRelogin(Context context, String reason) {
        UserStateManager userStateManager = UserStateManager.getInstance(context);
        userStateManager.logout();
        
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra("force_login_reason", reason);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}