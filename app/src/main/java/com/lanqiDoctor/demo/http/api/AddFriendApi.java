package com.lanqiDoctor.demo.http.api;

import com.google.gson.Gson;
import com.hjq.http.config.IRequestApi;
import com.lanqiDoctor.demo.http.model.HttpData;

/**
 * 添加好友/家庭成员API
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public final class AddFriendApi implements IRequestApi {

    @Override
    public String getApi() {
        return "friends";
    }

    /** 好友邮箱 */
    private String friendEmail;
    /** 好友密码（用于验证） */
    private String friendPassword;
    /** 好友昵称（备注名） */
    private String friendNickname;

    public AddFriendApi setFriendEmail(String friendEmail) {
        this.friendEmail = friendEmail;
        return this;
    }

    public AddFriendApi setFriendPassword(String friendPassword) {
        this.friendPassword = friendPassword;
        return this;
    }

    public AddFriendApi setFriendNickname(String friendNickname) {
        this.friendNickname = friendNickname;
        return this;
    }

    public String getFriendEmail() { 
        return friendEmail; 
    }
    
    public String getFriendPassword() { 
        return friendPassword; 
    }
    
    public String getFriendNickname() { 
        return friendNickname; 
    }

    public final static class Bean {
        private int code;
        private String message;
        private Data data;

        public boolean isSuccess() { 
            return code == 200; 
        }
        
        public String getMessage() { 
            return message; 
        }
        
        public int getCode() {
            return code;
        }
        
        public Data getData() {
            return data;
        }

        public Bean(HttpData h_data) {
            code = h_data.getCode();
            message = h_data.getMessage();
            
            // 使用 Gson 进行安全的数据转换
            if (h_data.getData() != null) {
                try {
                    Gson gson = new Gson();
                    String jsonString = gson.toJson(h_data.getData());
                    data = gson.fromJson(jsonString, Data.class);
                } catch (Exception e) {
                    data = null;
                    // 可以记录日志或处理异常
                }
            } else {
                data = null;
            }
        }
        // 添加好友时返回的数据结构
        public static class Data {
            private Long friendUserId;
            private String friendEmail;
            private String friendNickname;
            private String relationshipId;
            
            public Long getFriendUserId() { 
                return friendUserId; 
            }
            
            public String getFriendEmail() { 
                return friendEmail; 
            }
            
            public String getFriendNickname() { 
                return friendNickname; 
            }
            
            public String getRelationshipId() { 
                return relationshipId; 
            }
        }

        @Override
        public String toString() {
            return "Bean{" +
                    "code=" + code +
                    ", message='" + message + '\'' +
                    ", data=" + data +
                    '}';
        }
    }
}