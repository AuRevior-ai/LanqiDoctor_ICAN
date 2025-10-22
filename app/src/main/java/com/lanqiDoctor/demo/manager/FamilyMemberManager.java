package com.lanqiDoctor.demo.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.lanqiDoctor.demo.http.api.FriendsApi;
import com.lanqiDoctor.demo.http.api.RemoveFriendApi;
import com.lanqiDoctor.demo.http.model.HttpData;
import com.lanqiDoctor.demo.model.FamilyMember;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

/**
 * 家庭成员管理器
 * 负责家庭成员的本地缓存和服务器同步
 * 
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class FamilyMemberManager {
    
    private static final String TAG = "FamilyMemberManager";
    private static final String PREFS_NAME = "family_members";
    private static final String KEY_MEMBERS_LIST = "members_list";
    private static final String KEY_LAST_UPDATE = "last_update";
    
    private static volatile FamilyMemberManager instance;
    
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    private List<FamilyMember> familyMembers;
    private UserStateManager userStateManager;
    
    private FamilyMemberManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.familyMembers = new ArrayList<>();
        this.userStateManager = UserStateManager.getInstance(context);
        loadFamilyMembersFromLocal();
    }
    
    public static synchronized FamilyMemberManager getInstance(Context context) {
        if (instance == null) {
            instance = new FamilyMemberManager(context);
        }
        return instance;
    }
    
    /**
     * 从本地加载家庭成员列表
     */
    private void loadFamilyMembersFromLocal() {
        try {
            String membersJson = prefs.getString(KEY_MEMBERS_LIST, "");
            if (!membersJson.isEmpty()) {
                Type listType = new TypeToken<List<FamilyMember>>(){}.getType();
                familyMembers = gson.fromJson(membersJson, listType);
                if (familyMembers == null) {
                    familyMembers = new ArrayList<>();
                }
                Log.d(TAG, "从本地加载了 " + familyMembers.size() + " 个家庭成员");
            }
        } catch (Exception e) {
            Log.e(TAG, "从本地加载家庭成员失败", e);
            familyMembers = new ArrayList<>();
        }
    }
    
    /**
     * 保存家庭成员列表到本地
     */
    private void saveFamilyMembersToLocal() {
        try {
            String membersJson = gson.toJson(familyMembers);
            prefs.edit()
                    .putString(KEY_MEMBERS_LIST, membersJson)
                    .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                    .apply();
            Log.d(TAG, "保存了 " + familyMembers.size() + " 个家庭成员到本地");
        } catch (Exception e) {
            Log.e(TAG, "保存家庭成员到本地失败", e);
        }
    }
    
    /**
     * 获取家庭成员列表
     */
    public List<FamilyMember> getFamilyMembers() {
        return new ArrayList<>(familyMembers);
    }
    
    /**
     * 添加家庭成员
     */
    public void addFamilyMember(FamilyMember member) {
        if (member == null || member.getUserId() == null) {
            Log.w(TAG, "尝试添加无效的家庭成员");
            return;
        }
        
        // 检查是否已存在
        for (FamilyMember existing : familyMembers) {
            if (member.getUserId().equals(existing.getUserId())) {
                Log.d(TAG, "家庭成员已存在，更新信息: " + member.getNickname());
                existing.setNickname(member.getNickname());
                existing.setEmail(member.getEmail());
                existing.setOnline(member.isOnline());
                saveFamilyMembersToLocal();
                return;
            }
        }
        
        familyMembers.add(member);
        saveFamilyMembersToLocal();
        Log.d(TAG, "添加家庭成员: " + member.getNickname());
    }
    
    /**
     * 删除家庭成员
     */
    public boolean removeFamilyMember(String userId) {
        if (userId == null) {
            return false;
        }
        
        for (int i = 0; i < familyMembers.size(); i++) {
            if (userId.equals(familyMembers.get(i).getUserId())) {
                FamilyMember removed = familyMembers.remove(i);
                saveFamilyMembersToLocal();
                Log.d(TAG, "删除家庭成员: " + removed.getNickname());
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 根据用户ID查找家庭成员
     */
    public FamilyMember findMemberByUserId(String userId) {
        if (userId == null) {
            return null;
        }
        
        for (FamilyMember member : familyMembers) {
            if (userId.equals(member.getUserId())) {
                return member;
            }
        }
        
        return null;
    }
    
    /**
     * 获取家庭成员数量
     */
    public int getFamilyMemberCount() {
        return familyMembers.size();
    }
    
    /**
     * 从服务器同步家庭成员列表
     */
    public void syncFamilyMembersFromServer(SyncCallback callback) {
        Log.d(TAG, "开始从服务器同步家庭成员列表");
        
        if (!userStateManager.isUserLoggedIn()) {
            Log.w(TAG, "用户未登录，无法同步家庭成员");
            if (callback != null) {
                callback.onError("用户未登录");
            }
            return;
        }
        
        EasyHttp.get(new ApplicationLifecycle())
                .api(new FriendsApi())
                .request(new OnHttpListener<HttpData<FriendsApi.Bean>>() {
                    
                    @Override
                    public void onStart(Call call) {
                        Log.d(TAG, "开始同步家庭成员网络请求");
                    }
                    
                    @Override
                    public void onEnd(Call call) {
                        Log.d(TAG, "家庭成员同步网络请求结束");
                    }
                    
                    @Override
                    public void onSucceed(HttpData<FriendsApi.Bean> data) {
                        Log.d(TAG, "家庭成员同步网络请求成功");
                        try {
                            FriendsApi.Bean result = data.getData();
                            if (result != null && result.getFriends() != null) {
                                List<FamilyMember> serverMembers = new ArrayList<>();
                                
                                for (FriendsApi.Bean.FriendInfo friendInfo : result.getFriends()) {
                                    FamilyMember member = new FamilyMember();
                                    member.setUserId(friendInfo.getFriendUserId());
                                    member.setEmail(friendInfo.getFriendEmail());
                                    member.setNickname(friendInfo.getFriendNickname());
                                    member.setRelationshipId(friendInfo.getRelationshipId());
                                    member.setAddedTime(friendInfo.getCreatedAt());
                                    member.setOnline(friendInfo.isOnline());
                                    serverMembers.add(member);
                                }
                                
                                // 更新本地列表
                                familyMembers.clear();
                                familyMembers.addAll(serverMembers);
                                saveFamilyMembersToLocal();
                                
                                Log.d(TAG, "家庭成员同步成功，共 " + familyMembers.size() + " 个成员");
                                if (callback != null) {
                                    callback.onSuccess("同步成功，共 " + familyMembers.size() + " 个家庭成员");
                                }
                            } else {
                                Log.d(TAG, "服务器返回空的家庭成员列表");
                                familyMembers.clear();
                                saveFamilyMembersToLocal();
                                if (callback != null) {
                                    callback.onSuccess("暂无家庭成员");
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "处理家庭成员同步响应时发生异常", e);
                            if (callback != null) {
                                callback.onError("数据处理失败: " + e.getMessage());
                            }
                        }
                    }
                    
                    @Override
                    public void onFail(Exception e) {
                        Log.e(TAG, "家庭成员同步网络请求失败", e);
                        if (callback != null) {
                            callback.onError("同步失败: " + e.getMessage());
                        }
                    }
                });
    }
    
    /**
     * 从服务器删除家庭成员
     */
    public void removeFamilyMemberFromServer(String userId, RemoveCallback callback) {
        Log.d(TAG, "开始从服务器删除家庭成员: " + userId);
        
        if (!userStateManager.isUserLoggedIn()) {
            Log.w(TAG, "用户未登录，无法删除家庭成员");
            if (callback != null) {
                callback.onError("用户未登录");
            }
            return;
        }
        
        EasyHttp.delete(new ApplicationLifecycle())
                .api(new RemoveFriendApi(userId))
                .request(new OnHttpListener<HttpData<RemoveFriendApi.Bean>>() {
                    
                    @Override
                    public void onStart(Call call) {
                        Log.d(TAG, "开始删除家庭成员网络请求");
                    }
                    
                    @Override
                    public void onEnd(Call call) {
                        Log.d(TAG, "删除家庭成员网络请求结束");
                    }
                    
                    @Override
                    public void onSucceed(HttpData<RemoveFriendApi.Bean> data) {
                        Log.d(TAG, "删除家庭成员网络请求成功");
                        try {
                            RemoveFriendApi.Bean result = data.getData();
                            if (result != null && result.isSuccess()) {
                                // 从本地列表中删除
                                boolean removed = removeFamilyMember(userId);
                                
                                Log.d(TAG, "家庭成员删除成功: " + userId);
                                if (callback != null) {
                                    callback.onSuccess("家庭成员删除成功");
                                }
                            } else {
                                String errorMsg = result != null ? result.getMessage() : "删除失败";
                                Log.e(TAG, "删除家庭成员失败: " + errorMsg);
                                if (callback != null) {
                                    callback.onError(errorMsg);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "处理删除家庭成员响应时发生异常", e);
                            if (callback != null) {
                                callback.onError("响应处理失败: " + e.getMessage());
                            }
                        }
                    }
                    
                    @Override
                    public void onFail(Exception e) {
                        Log.e(TAG, "删除家庭成员网络请求失败", e);
                        if (callback != null) {
                            callback.onError("删除失败: " + e.getMessage());
                        }
                    }
                });
    }
    
    /**
     * 清除所有家庭成员数据
     */
    public void clearAllData() {
        Log.d(TAG, "清除所有家庭成员数据");
        familyMembers.clear();
        prefs.edit().clear().apply();
    }
    
    /**
     * 同步回调接口
     */
    public interface SyncCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    /**
     * 删除回调接口
     */
    public interface RemoveCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}