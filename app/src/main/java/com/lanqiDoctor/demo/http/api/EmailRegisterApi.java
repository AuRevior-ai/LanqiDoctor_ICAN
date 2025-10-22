package com.lanqiDoctor.demo.http.api;

import com.hjq.http.config.IRequestApi;

/**
 * 用户注册API - 使用邮箱
 * 
 * @author 蓝岐医童开发团队
 * @version 2.0
 */
public final class EmailRegisterApi implements IRequestApi {

    @Override
    public String getApi() {
        return "auth/register";
    }

    /** 邮箱地址 */
    private String email;
    /** 邮箱验证码 */
    private String verificationCode;
    /** 密码 */
    private String password;
    /** 确认密码 */
    private String confirmPassword;
    /** 昵称（可选） */
    private String nickname;
    /** 设备信息 */
    private String deviceInfo;

    public EmailRegisterApi setEmail(String email) {
        this.email = email;
        return this;
    }

    public EmailRegisterApi setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
        return this;
    }

    public EmailRegisterApi setPassword(String password) {
        this.password = password;
        return this;
    }

    public EmailRegisterApi setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
        return this;
    }

    public EmailRegisterApi setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public EmailRegisterApi setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
        return this;
    }

    public final static class Bean {
        private String userId;
        private String email;
        private String token;
        private boolean success;
        private String message;

        public String getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getToken() { return token; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}