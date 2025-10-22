package com.lanqiDoctor.demo.http.api;

import com.hjq.http.config.IRequestApi;

/**
 * 用户登录 - 邮箱版本
 * 
 * @author 蓝岐医童开发团队
 * @version 2.0
 */
public final class LoginApi implements IRequestApi {

    @Override
    public String getApi() {
        return "user/login";
    }

    /** 邮箱地址 */
    private String email;
    /** 登录密码 */
    private String password;
    /** 设备信息 */
    private String deviceInfo;
    /** 登录类型 */
    private String loginType = "email";

    // 修改：支持链式调用
    public LoginApi setEmail(String email) {
        this.email = email;
        return this;
    }

    public LoginApi setPassword(String password) {
        this.password = password;
        return this;
    }

    public LoginApi setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
        return this;
    }

    public LoginApi setLoginType(String loginType) {
        this.loginType = loginType;
        return this;
    }

    // 新增：getter 方法，用于调试
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getDeviceInfo() { return deviceInfo; }
    public String getLoginType() { return loginType; }

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

        // 新增：toString 方法，用于调试
        @Override
        public String toString() {
            return "Bean{" +
                    "token='" + (token != null ? token.substring(0, Math.min(10, token.length())) + "..." : "null") + '\'' +
                    ", refreshToken='" + (refreshToken != null ? refreshToken.substring(0, Math.min(10, refreshToken.length())) + "..." : "null") + '\'' +
                    ", userInfo=" + userInfo +
                    ", expiresIn=" + expiresIn +
                    '}';
        }

        public static class UserInfo {
            private String userId;
            private String email;
            private String nickname;
            private String avatar;
            private boolean emailVerified;

            public String getUserId() {
                return userId;
            }

            public String getEmail() {
                return email;
            }

            public String getNickname() {
                return nickname;
            }

            public String getAvatar() {
                return avatar;
            }

            public boolean isEmailVerified() {
                return emailVerified;
            }

            // 新增：toString 方法，用于调试
            @Override
            public String toString() {
                return "UserInfo{" +
                        "userId='" + userId + '\'' +
                        ", email='" + email + '\'' +
                        ", nickname='" + nickname + '\'' +
                        ", avatar='" + avatar + '\'' +
                        ", emailVerified=" + emailVerified +
                        '}';
            }
        }
    }
}