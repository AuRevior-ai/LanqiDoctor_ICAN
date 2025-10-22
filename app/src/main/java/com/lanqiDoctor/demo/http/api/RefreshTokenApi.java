package com.lanqiDoctor.demo.http.api;

import com.hjq.http.config.IRequestApi;

/**
 * 刷新Token API
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public final class RefreshTokenApi implements IRequestApi {

    @Override
    public String getApi() {
        return "api/user/refresh-token";
    }

    /** 刷新Token */
    private String refreshToken;

    public RefreshTokenApi setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public final static class Bean {
        private String token;
        private String refreshToken;
        private long expiresIn;
        private boolean success;
        private String message;

        public String getToken() {
            return token;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}