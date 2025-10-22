package com.lanqiDoctor.demo.http.api;

import com.hjq.http.config.IRequestApi;
import com.lanqiDoctor.demo.http.model.HttpData;
/**
 * 删除好友/家庭成员API
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public final class RemoveFriendApi implements IRequestApi {

    private String friendUserId;

    public RemoveFriendApi(String friendUserId) {
        this.friendUserId = friendUserId;
    }

    @Override
    public String getApi() {
        return "friends/" + friendUserId;
    }

    public String getFriendUserId() {
        return friendUserId; 
    }

    public final static class Bean {
        private int code;
        private String message;
        private Object data;

        public boolean isSuccess() { 
            return code == 200; 
        }
        
        public String getMessage() { 
            return message; 
        }
        
        public int getCode() {
            return code;
        }
        
        public Object getData() {
            return data;
        }

        public Bean(HttpData h_data) {
            code = h_data.getCode();
            message = h_data.getMessage();
            data = h_data.getData();
        }
    }
}