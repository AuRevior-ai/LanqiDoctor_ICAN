package com.lanqiDoctor.demo.http.api;

import com.hjq.http.config.IRequestApi;

/**
 * 发送邮箱验证码API
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public final class SendEmailCodeApi implements IRequestApi {

    @Override
    public String getApi() {
        return "user/send-email-code";
    }

    /** 邮箱地址 */
    private String email;
    /** 验证码类型：register, login, reset_password */
    private String type;
    /** 客户端类型 */
    private String clientType = "android";

    public SendEmailCodeApi setEmail(String email) {
        this.email = email;
        return this;
    }

    public SendEmailCodeApi setType(String type) {
        this.type = type;
        return this;
    }

    public SendEmailCodeApi setClientType(String clientType) {
        this.clientType = clientType;
        return this;
    }

    public final static class Bean {
        private boolean success;
        private String message;
        private int expiresIn; // 验证码有效期（秒）

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getExpiresIn() { return expiresIn; }
    }
}