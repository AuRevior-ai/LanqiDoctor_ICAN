package com.lanqiDoctor.demo.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lanqiDoctor.demo.database.DatabaseHelper;
import com.lanqiDoctor.demo.database.entity.HealthInfo;

import java.util.ArrayList;
import java.util.List;

/**
 *    author : rrrrrzy
 *    github : https://github.com/rrrrrzy
 *    time   : 2025/6/19
 *    desc   : 健康信息数据访问对象
 */
public class HealthInfoDao {

    private DatabaseHelper dbHelper;

    public HealthInfoDao(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * 插入健康信息
     */
    public long insert(HealthInfo healthInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = -1;
        try {
            ContentValues values = healthInfoToContentValues(healthInfo);
            id = db.insert(DatabaseHelper.TABLE_HEALTH_INFO, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return id;
    }

    /**
     * 更新健康信息
     */
    public int update(HealthInfo healthInfo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = 0;
        try {
            healthInfo.setUpdateTime(System.currentTimeMillis());
            ContentValues values = healthInfoToContentValues(healthInfo);
            rows = db.update(DatabaseHelper.TABLE_HEALTH_INFO, values, 
                DatabaseHelper.COLUMN_ID + " = ?", 
                new String[]{String.valueOf(healthInfo.getId())});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return rows;
    }

    /**
     * 删除健康信息
     */
    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = 0;
        try {
            rows = db.delete(DatabaseHelper.TABLE_HEALTH_INFO, 
                DatabaseHelper.COLUMN_ID + " = ?", 
                new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return rows;
    }

    /**
     * 根据ID查询健康信息
     */
    public HealthInfo findById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        HealthInfo healthInfo = null;
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_HEALTH_INFO, null,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                healthInfo = cursorToHealthInfo(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return healthInfo;
    }

    /**
     * 查询所有健康信息（按时间倒序）
     */
    public List<HealthInfo> findAll() {
        return findAll(DatabaseHelper.COLUMN_TIMESTAMP + " DESC", -1);
    }

    /**
     * 分页查询健康信息
     */
    public List<HealthInfo> findAll(String orderBy, int limit) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<HealthInfo> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            String limitStr = limit > 0 ? String.valueOf(limit) : null;
            cursor = db.query(DatabaseHelper.TABLE_HEALTH_INFO, null, null, null, 
                null, null, orderBy, limitStr);
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    HealthInfo healthInfo = cursorToHealthInfo(cursor);
                    list.add(healthInfo);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return list;
    }

    /**
     * 根据时间范围查询
     */
    public List<HealthInfo> findByTimeRange(long startTime, long endTime) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<HealthInfo> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_HEALTH_INFO, null,
                DatabaseHelper.COLUMN_TIMESTAMP + " BETWEEN ? AND ?",
                new String[]{String.valueOf(startTime), String.valueOf(endTime)},
                null, null, DatabaseHelper.COLUMN_TIMESTAMP + " DESC");
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    HealthInfo healthInfo = cursorToHealthInfo(cursor);
                    list.add(healthInfo);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return list;
    }

    /**
     * 获取记录总数
     */
    public int getCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_HEALTH_INFO, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return count;
    }

    /**
     * 清空所有记录
     */
    public int deleteAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = 0;
        try {
            rows = db.delete(DatabaseHelper.TABLE_HEALTH_INFO, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return rows;
    }

    /**
     * 将HealthInfo对象转换为ContentValues
     */
    private ContentValues healthInfoToContentValues(HealthInfo healthInfo) {
        ContentValues values = new ContentValues();
        if (healthInfo.getTimestamp() != null) {
            values.put(DatabaseHelper.COLUMN_TIMESTAMP, healthInfo.getTimestamp());
        }
        if (healthInfo.getAge() != null) {
            values.put(DatabaseHelper.COLUMN_AGE, healthInfo.getAge());
        }
        if (healthInfo.getHeight() != null) {
            values.put(DatabaseHelper.COLUMN_HEIGHT, healthInfo.getHeight());
        }
        if (healthInfo.getWeight() != null) {
            values.put(DatabaseHelper.COLUMN_WEIGHT, healthInfo.getWeight());
        }
        if (healthInfo.getHeartRate() != null) {
            values.put(DatabaseHelper.COLUMN_HEART_RATE, healthInfo.getHeartRate());
        }
        if (healthInfo.getSystolicPressure() != null) {
            values.put(DatabaseHelper.COLUMN_SYSTOLIC_PRESSURE, healthInfo.getSystolicPressure());
        }
        if (healthInfo.getDiastolicPressure() != null) {
            values.put(DatabaseHelper.COLUMN_DIASTOLIC_PRESSURE, healthInfo.getDiastolicPressure());
        }
        if (healthInfo.getBloodSugar() != null) {
            values.put(DatabaseHelper.COLUMN_BLOOD_SUGAR, healthInfo.getBloodSugar());
        }
        if (healthInfo.getRemarks() != null) {
            values.put(DatabaseHelper.COLUMN_REMARKS, healthInfo.getRemarks());
        }
        //新增睡眠和步数字段
        if (healthInfo.getSteps() != null) {
        values.put(DatabaseHelper.COLUMN_STEPS, healthInfo.getSteps());
        }
        if (healthInfo.getSleepDuration() != null) {
            values.put(DatabaseHelper.COLUMN_SLEEP_DURATION, healthInfo.getSleepDuration());
        }
        values.put(DatabaseHelper.COLUMN_CREATE_TIME, healthInfo.getCreateTime());
        values.put(DatabaseHelper.COLUMN_UPDATE_TIME, healthInfo.getUpdateTime());
        return values;
    }

    /**
     * 将Cursor转换为HealthInfo对象
     */
    private HealthInfo cursorToHealthInfo(Cursor cursor) {
        HealthInfo healthInfo = new HealthInfo();
        healthInfo.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        healthInfo.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIMESTAMP)));
        
        // 处理可能为null的字段
        int ageIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AGE);
        if (!cursor.isNull(ageIndex)) {
            healthInfo.setAge(cursor.getInt(ageIndex));
        }
        
        int heightIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HEIGHT);
        if (!cursor.isNull(heightIndex)) {
            healthInfo.setHeight(cursor.getDouble(heightIndex));
        }
        
        int weightIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WEIGHT);
        if (!cursor.isNull(weightIndex)) {
            healthInfo.setWeight(cursor.getDouble(weightIndex));
        }
        
        int heartRateIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HEART_RATE);
        if (!cursor.isNull(heartRateIndex)) {
            healthInfo.setHeartRate(cursor.getInt(heartRateIndex));
        }
        
        int systolicIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SYSTOLIC_PRESSURE);
        if (!cursor.isNull(systolicIndex)) {
            healthInfo.setSystolicPressure(cursor.getDouble(systolicIndex));
        }
        
        int diastolicIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIASTOLIC_PRESSURE);
        if (!cursor.isNull(diastolicIndex)) {
            healthInfo.setDiastolicPressure(cursor.getDouble(diastolicIndex));
        }
        
        int bloodSugarIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BLOOD_SUGAR);
        if (!cursor.isNull(bloodSugarIndex)) {
            healthInfo.setBloodSugar(cursor.getDouble(bloodSugarIndex));
        }
        
        int remarksIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMARKS);
        if (!cursor.isNull(remarksIndex)) {
            healthInfo.setRemarks(cursor.getString(remarksIndex));
        }
        int stepsIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STEPS);
        if (!cursor.isNull(stepsIndex)) {
            healthInfo.setSteps(cursor.getInt(stepsIndex));
        }
        int sleepIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SLEEP_DURATION);
        if (!cursor.isNull(sleepIndex)) {
            healthInfo.setSleepDuration(cursor.getDouble(sleepIndex));
        }
        healthInfo.setCreateTime(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATE_TIME)));
        healthInfo.setUpdateTime(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATE_TIME)));
        
        return healthInfo;
    }

    /**
     * 查找最新的N条健康信息记录
     */
    public List<HealthInfo> findLatest(int limit) {
        List<HealthInfo> healthInfos = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try {
            String sql = "SELECT * FROM " + DatabaseHelper.TABLE_HEALTH_INFO +
                    " ORDER BY " + DatabaseHelper.COLUMN_CREATE_TIME + " DESC LIMIT ?";
            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(limit)});
            
            while (cursor.moveToNext()) {
                HealthInfo healthInfo = cursorToHealthInfo(cursor);
                healthInfos.add(healthInfo);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        
        return healthInfos;
    }
}