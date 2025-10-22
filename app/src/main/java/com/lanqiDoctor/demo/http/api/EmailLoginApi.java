package com.lanqiDoctor.demo.http.api;

import com.hjq.http.config.IRequestApi;

/**
 * 用户登录API - 使用邮箱
 * 
 * @author 蓝岐医童开发团队
 * @version 2.0
 */
public final class EmailLoginApi implements IRequestApi {

    @Override
    public String getApi() {
        return "auth/login";
    }

    /** 邮箱地址 */
    private String email;
    /** 登录密码 */
    private String password;
    /** 设备信息 */
    private String deviceInfo;
    /** 登录类型：email */
    private String loginType = "email";

    public EmailLoginApi setEmail(String email) {
        this.email = email;
        return this;
    }

    public EmailLoginApi setPassword(String password) {
        this.password = password;
        return this;
    }

    public EmailLoginApi setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
        return this;
    }

    public final static class Bean {
        private String token;
        private String refreshToken;
        private UserInfo userInfo;
        private long expiresIn;

        public String getToken() {
            return token;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public UserInfo getUserInfo() {
            return userInfo;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public static class UserInfo {
            private String userId;
            private String email;
            private String nickname;
            private String avatar;
            private boolean emailVerified;

            // Getters
            public String getUserId() { return userId; }
            public String getEmail() { return email; }
            public String getNickname() { return nickname; }
            public String getAvatar() { return avatar; }
            public boolean isEmailVerified() { return emailVerified; }
        }
    }
}