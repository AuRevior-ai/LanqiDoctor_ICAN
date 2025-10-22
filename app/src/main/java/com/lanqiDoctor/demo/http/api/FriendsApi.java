package com.lanqiDoctor.demo.http.api;

import com.hjq.http.config.IRequestApi;

import java.util.List;

/**
 * 好友/家庭成员管理API
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public final class FriendsApi implements IRequestApi {

    @Override
    public String getApi() {
        return "friends";
    }

    public final static class Bean {
        private List<FriendInfo> friends;
        private int totalCount;

        public List<FriendInfo> getFriends() { 
            return friends; 
        }
        
        public int getTotalCount() { 
            return totalCount; 
        }

        public static class FriendInfo {
            private String friendUserId;
            private String friendEmail;
            private String friendNickname;
            private String relationshipId;
            private String createdAt;
            private boolean isOnline;

            public String getFriendUserId() {
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
            
            public String getCreatedAt() { 
                return createdAt; 
            }
            
            public boolean isOnline() { 
                return isOnline; 
            }

            @Override
            public String toString() {
                return "FriendInfo{" +
                        "friendUserId=" + friendUserId +
                        ", friendEmail='" + friendEmail + '\'' +
                        ", friendNickname='" + friendNickname + '\'' +
                        ", relationshipId='" + relationshipId + '\'' +
                        ", createdAt='" + createdAt + '\'' +
                        ", isOnline=" + isOnline +
                        '}';
            }
        }
    }
}