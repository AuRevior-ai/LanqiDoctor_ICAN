package com.lanqiDoctor.demo.manager;

import android.content.Context;

import com.lanqiDoctor.demo.database.dao.HealthInfoDao;
import com.lanqiDoctor.demo.database.entity.HealthInfo;
import com.lanqiDoctor.demo.database.DatabaseHelper;

import java.util.List;

/**
 *    author : rrrrrzy
 *    github : https://github.com/rrrrrzy
 *    time   : 2025/6/19
 *    desc   : 数据库管理器，提供统一的数据库操作接口
 */
public class DatabaseManager {
    
    private static DatabaseManager instance;
    private HealthInfoDao healthInfoDao;
    
    private DatabaseManager(Context context) {
        this.healthInfoDao = new HealthInfoDao(context.getApplicationContext());
    }
    
    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }
    
    /**
     * 保存健康信息
     */
    public long saveHealthInfo(HealthInfo healthInfo) {
        if (healthInfo.getTimestamp() == null) {
            healthInfo.setTimestamp(System.currentTimeMillis() / 1000); // 精确到秒
        }
        return healthInfoDao.insert(healthInfo);
    }
    
    /**
     * 更新健康信息
     */
    public boolean updateHealthInfo(HealthInfo healthInfo) {
        return healthInfoDao.update(healthInfo) > 0;
    }
    
    /**
     * 删除健康信息
     */
    public boolean deleteHealthInfo(long id) {
        return healthInfoDao.delete(id) > 0;
    }
    
    /**
     * 根据ID获取健康信息
     * Deprecated: 废弃原因：药物ID不是唯一的，没有固定的生成逻辑
     */
    @Deprecated
    public HealthInfo getHealthInfo(long id) {
        return healthInfoDao.findById(id);
    }
    
    /**
     * 获取所有健康信息
     */
    public List<HealthInfo> getAllHealthInfo() {
        return healthInfoDao.findAll();
    }
    
    /**
     * 获取最近的健康信息
     */
    public List<HealthInfo> getRecentHealthInfo(int limit) {
        return healthInfoDao.findAll(DatabaseHelper.COLUMN_TIMESTAMP + " DESC", limit);
    }
    
    /**
     * 根据时间范围获取健康信息
     */
    public List<HealthInfo> getHealthInfoByTimeRange(long startTime, long endTime) {
        return healthInfoDao.findByTimeRange(startTime, endTime);
    }
    
    /**
     * 获取记录总数
     */
    public int getHealthInfoCount() {
        return healthInfoDao.getCount();
    }
    
    /**
     * 清空所有记录
     */
    public boolean clearAllHealthInfo() {
        return healthInfoDao.deleteAll() >= 0;
    }
    
    public HealthInfo getLatestHealthInfo() {
        List<HealthInfo> list = getRecentHealthInfo(1);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
}