package com.lanqiDoctor.demo.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lanqiDoctor.demo.database.DatabaseHelper;
import com.lanqiDoctor.demo.database.entity.MedicationIntakeRecord;
import com.lanqiDoctor.demo.manager.UserStateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

/**
 * 服药记录数据访问对象
 *
 * @author 蓝岐医童开发团队
 * @version 1.0
 */

public class MedicationIntakeRecordDao {

    private static final String TAG = "MedicationIntakeRecordDao";//定义日志标签
    private DatabaseHelper dbHelper;//数据库帮助类实例,用于获取数据库连接
    private Context context; // 添加这一行

    public MedicationIntakeRecordDao(Context context) {
        this.context = context; // 添加这一行
        this.dbHelper = new DatabaseHelper(context);
    }

    // ==================== 基于名称的CRUD方法 ====================

    /**
     * 插入或更新服药记录（基于药物名称和时间）
     */
    public long insertOrUpdateByNameAndTime(MedicationIntakeRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        try {
            // 🔥 关键修复：更严格的userId检查
            if (record.getUserId() == null || record.getUserId().isEmpty()) {
                Log.w(TAG, "服药记录的userId为空，尝试从UserStateManager获取");
                
                // 尝试从UserStateManager获取用户ID
                UserStateManager userStateManager = UserStateManager.getInstance(context);
                
                // 添加调试日志
                Log.d("DEBUG", "=== 用户状态检查 ===");
                Log.d("DEBUG", "用户ID: " + userStateManager.getUserId());
                Log.d("DEBUG", "用户邮箱: " + userStateManager.getUserEmail());
                Log.d("DEBUG", "用户昵称: " + userStateManager.getUserNickname());
                Log.d("DEBUG", "是否已登录: " + userStateManager.isUserLoggedIn());
                Log.d("DEBUG", "==================");
                
                String userId = userStateManager.getUserId();
                
                if (userId != null && !userId.isEmpty() && !"000000".equals(userId)) {
                    record.setUserId(userId);
                    Log.d(TAG, "从UserStateManager获取到用户ID: " + userId);
                } else {
                    Log.e(TAG, "无法获取有效的用户ID，插入失败");
                    Log.e(TAG, "用户邮箱: " + userStateManager.getUserEmail());
                    Log.e(TAG, "用户登录状态: " + userStateManager.isUserLoggedIn());
                    return -1;
                }
            }
             else {
            // 🔥 新增：记录userId来源，便于调试
            Log.d(TAG, "服药记录已有userId: " + record.getUserId() + 
                  " (药物: " + record.getMedicationName() + ")");
            }
            Log.d(TAG, "插入或更新服药记录: " + record.getMedicationName() + 
                    " userId: " + record.getUserId() + 
                    " 时间: " + record.getPlannedTime());
            
            // 先检查是否已存在
            String selectSql = "SELECT id FROM " + DatabaseHelper.TABLE_MEDICATION_INTAKE +
                    " WHERE " + DatabaseHelper.COLUMN_USER_ID + " = ? AND " +
                    DatabaseHelper.COLUMN_MEDICATION_NAME + " = ? AND " +
                    DatabaseHelper.COLUMN_PLANNED_TIME + " = ?";
            
            Cursor cursor = db.rawQuery(selectSql, new String[]{
                    record.getUserId(),
                    record.getMedicationName(),
                    String.valueOf(record.getPlannedTime())
            });
            
            if (cursor.moveToFirst()) {
                // 记录已存在，更新
                long existingId = cursor.getLong(0);
                cursor.close();
                
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_STATUS, record.getStatus());
                values.put(DatabaseHelper.COLUMN_ACTUAL_TIME, record.getActualTime());
                values.put(DatabaseHelper.COLUMN_ACTUAL_DOSAGE, record.getActualDosage());
                values.put(DatabaseHelper.COLUMN_UPDATE_TIME, System.currentTimeMillis());
                
                int affectedRows = db.update(DatabaseHelper.TABLE_MEDICATION_INTAKE,
                        values,
                        "id = ?",
                        new String[]{String.valueOf(existingId)});
                
                Log.d(TAG, "更新现有记录: ID=" + existingId + ", 影响行数=" + affectedRows);
                return existingId;
            } else {
                cursor.close();
                
                // 记录不存在，插入新记录
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_MEDICATION_ID, record.getMedicationId());
                values.put(DatabaseHelper.COLUMN_MEDICATION_NAME, record.getMedicationName());
                values.put(DatabaseHelper.COLUMN_PLANNED_TIME, record.getPlannedTime());
                values.put(DatabaseHelper.COLUMN_ACTUAL_TIME, record.getActualTime());
                values.put(DatabaseHelper.COLUMN_STATUS, record.getStatus());
                values.put(DatabaseHelper.COLUMN_ACTUAL_DOSAGE, record.getActualDosage());
                values.put(DatabaseHelper.COLUMN_NOTES, record.getNotes());
                values.put(DatabaseHelper.COLUMN_USER_ID, record.getUserId()); // 重要：确保设置userId
                values.put(DatabaseHelper.COLUMN_CREATE_TIME, System.currentTimeMillis());
                values.put(DatabaseHelper.COLUMN_UPDATE_TIME, System.currentTimeMillis());
                
                long newId = db.insert(DatabaseHelper.TABLE_MEDICATION_INTAKE, null, values);
                Log.d(TAG, "插入新的服药记录: " + record.getMedicationName() + 
                        " ID: " + newId + " userId: " + record.getUserId());
                return newId;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "插入或更新服药记录时发生异常", e);
            return -1;
        } finally {
            db.close();
        }
    }
    /**
     * 根据药物名称和计划时间查询服药记录
     */
    public MedicationIntakeRecord findByNameAndTime(String userId,String medicationName, long plannedTime) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        MedicationIntakeRecord record = null;
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE, null,
                    "user_id = ? AND " + DatabaseHelper.COLUMN_MEDICATION_NAME + " = ? AND " +
                            DatabaseHelper.COLUMN_PLANNED_TIME + " = ?",
                    new String[]{String.valueOf(userId), medicationName, String.valueOf(plannedTime)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                record = cursorToMedicationIntakeRecord(cursor);
                Log.d(TAG, "找到服药记录: " + medicationName + " 时间: " + plannedTime + " 状态: " + record.getStatus());
            } else {
                Log.d(TAG, "未找到服药记录: " + medicationName + " 时间: " + plannedTime);
            }
        } catch (Exception e) {
            Log.e(TAG, "查询服药记录失败", e);
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return record;
    }

    /**
     * 根据用户ID、药物名称和计划时间更新服药记录
     */
    public int updateByNameAndTime(String userId, MedicationIntakeRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // 添加调试日志
            Log.d(TAG, "开始更新服药记录: userId=" + userId +
                    ", 药物=" + record.getMedicationName() +
                    ", 计划时间=" + record.getPlannedTime() +
                    ", 新状态=" + record.getStatus());

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_STATUS, record.getStatus());
            values.put(DatabaseHelper.COLUMN_ACTUAL_TIME, record.getActualTime());
            values.put(DatabaseHelper.COLUMN_UPDATE_TIME, System.currentTimeMillis());

            // 修复：确保WHERE条件正确
            String whereClause = DatabaseHelper.COLUMN_USER_ID + " = ? AND " +
                    DatabaseHelper.COLUMN_MEDICATION_NAME + " = ? AND " +
                    DatabaseHelper.COLUMN_PLANNED_TIME + " = ?";

            String[] whereArgs = {
                    userId,
                    record.getMedicationName(),
                    String.valueOf(record.getPlannedTime())
            };

            // 先查询是否存在记录
            Cursor cursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE,
                    new String[]{"id", "status"},
                    whereClause,
                    whereArgs,
                    null, null, null);

            if (cursor.moveToFirst()) {
                long id = cursor.getLong(0);
                int currentStatus = cursor.getInt(1);
                Log.d(TAG, "找到要更新的记录: ID=" + id + ", 当前状态=" + currentStatus);
                cursor.close();

                // 执行更新
                int affectedRows = db.update(DatabaseHelper.TABLE_MEDICATION_INTAKE,
                        values,
                        whereClause,
                        whereArgs);

                Log.d(TAG, "更新服药记录完成，影响行数: " + affectedRows);

                if (affectedRows > 0) {
                    // 验证更新结果
                    Cursor verifyyCursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE,
                            new String[]{"status"},
                            whereClause,
                            whereArgs,
                            null, null, null);

                    if (verifyyCursor.moveToFirst()) {
                        int newStatus = verifyyCursor.getInt(0);
                        Log.d(TAG, "验证更新结果: 新状态=" + newStatus);
                    }
                    verifyyCursor.close();
                }

                return affectedRows;
            } else {
                cursor.close();
                Log.w(TAG, "未找到要更新的记录: userId=" + userId +
                        ", 药物=" + record.getMedicationName() +
                        ", 计划时间=" + record.getPlannedTime());

                // 尝试创建新记录
                record.setUserId(userId);
                record.setCreateTime(System.currentTimeMillis());
                record.setUpdateTime(System.currentTimeMillis());

                long newId = insertOrUpdateByNameAndTime(record);
                return newId > 0 ? 1 : 0;
            }

        } catch (Exception e) {
            Log.e(TAG, "更新服药记录时发生异常", e);
            return 0;
        } finally {
            db.close();
        }
    }

    /**
     * 根据药物名称和计划时间删除服药记录
     */
    public int deleteByNameAndTime(String userId,String medicationName, long plannedTime) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = 0;
        try {
            rows = db.delete(DatabaseHelper.TABLE_MEDICATION_INTAKE,
                    "user_id = ? AND " + DatabaseHelper.COLUMN_MEDICATION_NAME + " = ? AND " +
                            DatabaseHelper.COLUMN_PLANNED_TIME + " = ?",
                    new String[]{String.valueOf(userId), medicationName, String.valueOf(plannedTime)});

            Log.d(TAG, "删除服药记录: " + medicationName + " 时间: " + plannedTime + " 影响行数: " + rows);
        } catch (Exception e) {
            Log.e(TAG, "删除服药记录失败", e);
            e.printStackTrace();
        } finally {
            db.close();
        }
        return rows;
    }

    /**
     * 根据药物名称查询所有服药记录
     */
    public List<MedicationIntakeRecord> findByMedicationName(String userId,String medicationName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<MedicationIntakeRecord> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE, null,
                    "user_id = ? AND " + DatabaseHelper.COLUMN_MEDICATION_NAME + " = ?",
                    new String[]{String.valueOf(userId), medicationName},
                    null, null, DatabaseHelper.COLUMN_PLANNED_TIME + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MedicationIntakeRecord record = cursorToMedicationIntakeRecord(cursor);
                    list.add(record);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "根据药物名称查询服药记录失败", e);
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
     * 根据药物名称删除所有相关服药记录
     */
    public int deleteByMedicationName(String userId,String medicationName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = 0;
        try {
            rows = db.delete(DatabaseHelper.TABLE_MEDICATION_INTAKE,
                    "user_id = ? AND " + DatabaseHelper.COLUMN_MEDICATION_NAME + " = ?",
                    new String[]{String.valueOf(userId), medicationName});

            Log.d(TAG, "删除药物的所有服药记录: " + medicationName + " 影响行数: " + rows);
        } catch (Exception e) {
            Log.e(TAG, "删除药物服药记录失败", e);
            e.printStackTrace();
        } finally {
            db.close();
        }
        return rows;
    }

    /**
     * 检查今日是否已存在指定药物和时间的记录（基于名称）
     */
    public boolean existsTodayRecordByName(String userId,String medicationName, long plannedTime) {
        // 获取今天的开始和结束时间
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long todayStart = today.getTimeInMillis();
        long todayEnd = todayStart + 24 * 60 * 60 * 1000;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        boolean exists = false;
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE,
                    new String[]{"COUNT(*)"},
                    "user_id = ? AND " + DatabaseHelper.COLUMN_MEDICATION_NAME + " = ? AND " +
                            DatabaseHelper.COLUMN_PLANNED_TIME + " = ? AND " +
                            DatabaseHelper.COLUMN_PLANNED_TIME + " >= ? AND " +
                            DatabaseHelper.COLUMN_PLANNED_TIME + " < ?",
                    new String[]{
                            String.valueOf(userId),
                            medicationName,
                            String.valueOf(plannedTime),
                            String.valueOf(todayStart),
                            String.valueOf(todayEnd)
                    },
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                exists = count > 0;
            }

            Log.d(TAG, "检查今日记录存在性: 药物=" + medicationName +
                    ", 计划时间=" + plannedTime + ", 存在=" + exists);
        } catch (Exception e) {
            Log.e(TAG, "检查今日记录存在性失败", e);
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return exists;
    }

    /**
     * 查询指定日期的特定药物和时间段的服药记录（今日专用，基于名称）
     */
    public MedicationIntakeRecord findTodayRecordByNameAndTime(String userId,String medicationName, long plannedTime) {
        // 获取今天的开始和结束时间
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long todayStart = today.getTimeInMillis();
        long todayEnd = todayStart + 24 * 60 * 60 * 1000;

        Log.d(TAG, "开始查询今日服药记录: 药物='" + medicationName + "', 计划时间=" + plannedTime
                + ", 日期范围=" + formatDateTime(todayStart) + " 至 " + formatDateTime(todayEnd));

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        MedicationIntakeRecord record = null;
        Cursor cursor = null;
        try {
            String selection = "user_id = ? AND " + DatabaseHelper.COLUMN_MEDICATION_NAME + " = ? AND " +
                    DatabaseHelper.COLUMN_PLANNED_TIME + " = ? AND " +
                    DatabaseHelper.COLUMN_PLANNED_TIME + " >= ? AND " +
                    DatabaseHelper.COLUMN_PLANNED_TIME + " < ?";
            String[] selectionArgs = new String[]{
                    String.valueOf(userId),
                    medicationName,
                    String.valueOf(plannedTime),
                    String.valueOf(todayStart),
                    String.valueOf(todayEnd)
            };

            // 记录SQL查询条件
            Log.d(TAG, "SQL查询条件: " + selection + ", 参数=['" + medicationName + "', "
                    + plannedTime + ", " + todayStart + ", " + todayEnd + "]");

            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE, null,
                    selection, selectionArgs, null, null, null);

            if (cursor != null) {
                int count = cursor.getCount();
                Log.d(TAG, "查询结果: 找到 " + count + " 条记录");

                if (cursor.moveToFirst()) {
                    record = cursorToMedicationIntakeRecord(cursor);
                    Log.d(TAG, "找到今日服药记录: 药物='" + medicationName + "', 计划时间="
                            + plannedTime + "(" + formatDateTime(plannedTime) + "), 状态="
                            + record.getStatus() + "(" + record.getStatusDescription() + ")");
                } else {
                    Log.d(TAG, "今日暂无服药记录: 药物='" + medicationName + "', 计划时间="
                            + plannedTime + "(" + formatDateTime(plannedTime) + ")");

                    // 为诊断添加额外查询
                    checkRecordExistence(db, medicationName, plannedTime);
                }
            } else {
                Log.w(TAG, "查询返回空游标");
            }
        } catch (Exception e) {
            Log.e(TAG, "查询今日服药记录失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return record;
    }

    /**
     * 辅助方法：检查记录是否存在于数据库中（不考虑日期限制）
     */
    private void checkRecordExistence(SQLiteDatabase db, String medicationName, long plannedTime) {
        Cursor diagCursor = null;
        try {
            // 1. 检查药物名称是否存在
            diagCursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE,
                    new String[]{"COUNT(*)"},
                    DatabaseHelper.COLUMN_MEDICATION_NAME + " = ?",
                    new String[]{medicationName}, null, null, null);

            if (diagCursor != null && diagCursor.moveToFirst()) {
                int nameCount = diagCursor.getInt(0);
                Log.d(TAG, "诊断信息: 数据库中药物名称'" + medicationName + "'的记录共有 " + nameCount + " 条");
            }
            if (diagCursor != null) diagCursor.close();

            // 2. 检查是否有该计划时间的记录
            diagCursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE,
                    new String[]{"COUNT(*)"},
                    DatabaseHelper.COLUMN_PLANNED_TIME + " = ?",
                    new String[]{String.valueOf(plannedTime)}, null, null, null);

            if (diagCursor != null && diagCursor.moveToFirst()) {
                int timeCount = diagCursor.getInt(0);
                Log.d(TAG, "诊断信息: 数据库中计划时间为 " + plannedTime + "(" + formatDateTime(plannedTime) + ") 的记录共有 " + timeCount + " 条");
            }
            if (diagCursor != null) diagCursor.close();

            // 3. 检查数据库中共有多少条记录
            diagCursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_MEDICATION_INTAKE, null);
            if (diagCursor != null && diagCursor.moveToFirst()) {
                int totalCount = diagCursor.getInt(0);
                Log.d(TAG, "诊断信息: 数据库中服药记录总数为 " + totalCount + " 条");
            }
        } catch (Exception e) {
            Log.e(TAG, "诊断查询失败", e);
        } finally {
            if (diagCursor != null) {
                diagCursor.close();
            }
        }
    }


    // ==================== 保留原有方法以向后兼容 ====================

    /**
     * 添加服药记录
     * @deprecated 建议使用 insertOrUpdateByNameAndTime 方法
     */
    @Deprecated
    public long insert(MedicationIntakeRecord record) {
        return insertOrUpdateByNameAndTime(record);
    }

    /**
     * 更新服药记录
     * @deprecated 建议使用 updateByNameAndTime 方法
     */
    @Deprecated
    public int update(MedicationIntakeRecord record) {
        // 兼容旧代码，自动取 userId
        return updateByNameAndTime(record.getUserId(), record);
    }

    /**
     * 根据ID查询服药记录
     * @deprecated 建议使用 findByNameAndTime 方法
     */
    @Deprecated
    public MedicationIntakeRecord findById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        MedicationIntakeRecord record = null;
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE, null,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                record = cursorToMedicationIntakeRecord(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "根据ID查询服药记录失败", e);
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return record;
    }

    /**
     * 根据日期和药物ID查询服药记录（保持原有方法签名）
     */
    public MedicationIntakeRecord findByDateAndMedicationId(long date, long medicationId, long plannedTime) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        MedicationIntakeRecord record = null;
        Cursor cursor = null;
        try {
            // 查询指定日期、药物ID和计划时间的记录
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE, null,
                    DatabaseHelper.COLUMN_MEDICATION_ID + " = ? AND " +
                            DatabaseHelper.COLUMN_PLANNED_TIME + " = ?",
                    new String[]{String.valueOf(medicationId), String.valueOf(plannedTime)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                record = cursorToMedicationIntakeRecord(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "根据日期和药物ID查询服药记录失败", e);
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return record;
    }

    /**
     * 新增：查询指定日期的特定药物和时间段的服药记录（今日专用）
     */
    public MedicationIntakeRecord findTodayRecordByMedicationAndTime(long medicationId, long plannedTime) {
        // 获取今天的开始和结束时间
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long todayStart = today.getTimeInMillis();
        long todayEnd = todayStart + 24 * 60 * 60 * 1000;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        MedicationIntakeRecord record = null;
        Cursor cursor = null;
        try {
            // 查询今天的特定药物和计划时间的记录
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE, null,
                    DatabaseHelper.COLUMN_MEDICATION_ID + " = ? AND " +
                            DatabaseHelper.COLUMN_PLANNED_TIME + " = ? AND " +
                            DatabaseHelper.COLUMN_PLANNED_TIME + " >= ? AND " +
                            DatabaseHelper.COLUMN_PLANNED_TIME + " < ?",
                    new String[]{
                            String.valueOf(medicationId),
                            String.valueOf(plannedTime),
                            String.valueOf(todayStart),
                            String.valueOf(todayEnd)
                    },
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                record = cursorToMedicationIntakeRecord(cursor);
                Log.d(TAG, "找到今日服药记录: 药物ID=" + medicationId + ", 计划时间=" + plannedTime + ", 状态=" + record.getStatus());
            } else {
                Log.d(TAG, "今日暂无服药记录: 药物ID=" + medicationId + ", 计划时间=" + plannedTime);
            }
        } catch (Exception e) {
            Log.e(TAG, "查询今日服药记录失败", e);
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return record;
    }

    /**
     * 查询今日所有服药记录
     */
    public List<MedicationIntakeRecord> findTodayRecords(String userId) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long startTime = today.getTimeInMillis();

        today.add(Calendar.DAY_OF_MONTH, 1);
        long endTime = today.getTimeInMillis();

        List<MedicationIntakeRecord> records = findByTimeRange(userId,startTime, endTime);
        Log.d(TAG, "查询今日服药记录数量: " + records.size());
        return records;
    }

    /**
     * 根据时间范围查询服药记录 - 优化版本
     */
    public List<MedicationIntakeRecord> findByTimeRange(String userId, long startTime, long endTime) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<MedicationIntakeRecord> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE, null,
                    "user_id = ? AND " + DatabaseHelper.COLUMN_PLANNED_TIME + " >= ? AND " +
                            DatabaseHelper.COLUMN_PLANNED_TIME + " <= ?",
                    new String[]{String.valueOf(userId), String.valueOf(startTime), String.valueOf(endTime)},
                    null, null, DatabaseHelper.COLUMN_PLANNED_TIME + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MedicationIntakeRecord record = cursorToMedicationIntakeRecord(cursor);
                    list.add(record);
                    Log.d(TAG, "查询到服药记录: " + record.getMedicationName() +
                            " 计划时间: " + formatDateTime(record.getPlannedTime()) +
                            " 状态: " + record.getStatus());
                } while (cursor.moveToNext());
            }

            Log.d(TAG, "时间范围查询结果: " + formatDateTime(startTime) + " 至 " +
                    formatDateTime(endTime) + " 共 " + list.size() + " 条记录");
        } catch (Exception e) {
            Log.e(TAG, "根据时间范围查询服药记录失败", e);
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
     * 获取最近N天的服药记录
     */
    public List<MedicationIntakeRecord> findRecentRecords(String userId, int days) {
        Calendar calendar = Calendar.getInstance();

        // 设置结束时间为今天的23:59:59
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endTime = calendar.getTimeInMillis();

        // 设置开始时间为N天前的00:00:00
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();

        Log.d(TAG, "查询最近 " + days + " 天的服药记录: " + formatDateTime(startTime) + " 至 " + formatDateTime(endTime));

        return findByTimeRange(userId, startTime, endTime);
    }

    /**
     * 格式化日期时间用于日志显示
     */
    private String formatDateTime(Long timestamp) {
        if (timestamp == null) {
            return "null";
        }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(timestamp));
        } catch (Exception e) {
            return "时间格式错误";
        }
    }

    /**
     * 查询所有服药记录
     */
    public List<MedicationIntakeRecord> findAll(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<MedicationIntakeRecord> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE, null,
                    "user_id = ?", new String[]{String.valueOf(userId)},
                    null, null, DatabaseHelper.COLUMN_PLANNED_TIME + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MedicationIntakeRecord record = cursorToMedicationIntakeRecord(cursor);
                    list.add(record);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "查询所有服药记录失败", e);
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
     * 根据药物ID查询服药记录
     */
    public List<MedicationIntakeRecord> findByMedicationId(String userId, long medicationId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<MedicationIntakeRecord> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE, null,
                    "user_id = ? AND " + DatabaseHelper.COLUMN_MEDICATION_ID + " = ?",
                    new String[]{String.valueOf(userId), String.valueOf(medicationId)},
                    null, null, DatabaseHelper.COLUMN_PLANNED_TIME + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MedicationIntakeRecord record = cursorToMedicationIntakeRecord(cursor);
                    list.add(record);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "根据药物ID查询服药记录失败", e);
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
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_MEDICATION_INTAKE, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "获取记录总数失败", e);
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
     * 新增：清理指定天数之前的服药记录（数据清理）
     */
    public int cleanOldRecords(int daysToKeep, String userId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -daysToKeep);
        long cutoffTime = calendar.getTimeInMillis();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = 0;
        try {
            deletedRows = db.delete(DatabaseHelper.TABLE_MEDICATION_INTAKE,
                    "user_id = ? AND " + DatabaseHelper.COLUMN_PLANNED_TIME + " < ?",
                    new String[]{String.valueOf(userId), String.valueOf(cutoffTime)});

            Log.d(TAG, "清理了 " + daysToKeep + " 天前的服药记录，删除行数: " + deletedRows);
        } catch (Exception e) {
            Log.e(TAG, "清理旧记录失败", e);
            e.printStackTrace();
        } finally {
            db.close();
        }
        return deletedRows;
    }

    /**
     * 新增：删除今日指定药物和时间的服药记录
     */
    public int deleteTodayRecord(String userId, long medicationId, long plannedTime) {
        // 获取今天的开始和结束时间
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long todayStart = today.getTimeInMillis();
        long todayEnd = todayStart + 24 * 60 * 60 * 1000;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = 0;
        try {
            deletedRows = db.delete(DatabaseHelper.TABLE_MEDICATION_INTAKE,
                    "user_id = ? AND " + DatabaseHelper.COLUMN_MEDICATION_ID + " = ? AND " +
                            DatabaseHelper.COLUMN_PLANNED_TIME + " = ? AND " +
                            DatabaseHelper.COLUMN_PLANNED_TIME + " >= ? AND " +
                            DatabaseHelper.COLUMN_PLANNED_TIME + " < ?",
                    new String[]{
                            String.valueOf(userId),
                            String.valueOf(medicationId),
                            String.valueOf(plannedTime),
                            String.valueOf(todayStart),
                            String.valueOf(todayEnd)
                    });

            Log.d(TAG, "删除今日服药记录: 药物ID=" + medicationId + ", 计划时间=" + plannedTime + ", 删除行数=" + deletedRows);
        } catch (Exception e) {
            Log.e(TAG, "删除今日服药记录失败", e);
            e.printStackTrace();
        } finally {
            db.close();
        }
        return deletedRows;
    }

    /**
     * 将MedicationIntakeRecord对象转换为ContentValues
     */
    private ContentValues medicationIntakeRecordToContentValues(MedicationIntakeRecord record) {
        ContentValues values = new ContentValues();
        if (record.getUserId() != null) {
            values.put("user_id", record.getUserId());
        }
        if (record.getMedicationId() != null) {
            values.put(DatabaseHelper.COLUMN_MEDICATION_ID, record.getMedicationId());
        }
        if (record.getMedicationName() != null) {
            values.put(DatabaseHelper.COLUMN_MEDICATION_NAME, record.getMedicationName());
        }
        if (record.getPlannedTime() != null) {
            values.put(DatabaseHelper.COLUMN_PLANNED_TIME, record.getPlannedTime());
        }
        if (record.getActualTime() != null) {
            values.put(DatabaseHelper.COLUMN_ACTUAL_TIME, record.getActualTime());
        }
        if (record.getActualDosage() != null) {
            values.put(DatabaseHelper.COLUMN_ACTUAL_DOSAGE, record.getActualDosage());
        }
        if (record.getStatus() != null) {
            values.put(DatabaseHelper.COLUMN_STATUS, record.getStatus());
        }
        if (record.getNotes() != null) {
            values.put(DatabaseHelper.COLUMN_NOTES, record.getNotes());
        }
        values.put(DatabaseHelper.COLUMN_CREATE_TIME, record.getCreateTime());
        values.put(DatabaseHelper.COLUMN_UPDATE_TIME, record.getUpdateTime());
        return values;
    }
    /**
     * 根据时间范围和状态查询服药记录
     */
    public List<MedicationIntakeRecord> findByTimeRangeAndStatus(String userId,long startTime, long endTime, int status) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<MedicationIntakeRecord> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE,
                    null,
                    "user_id = ? AND " + DatabaseHelper.COLUMN_PLANNED_TIME + " >= ? AND " +
                            DatabaseHelper.COLUMN_PLANNED_TIME + " < ? AND " +
                            DatabaseHelper.COLUMN_STATUS + " = ?",
                    new String[]{String.valueOf(userId), String.valueOf(startTime), String.valueOf(endTime), String.valueOf(status)},
                    null, null,
                    DatabaseHelper.COLUMN_PLANNED_TIME + " ASC");

            // 其余处理逻辑...
        } catch (Exception e) {
            Log.e(TAG, "查询失败", e);
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return list;
    }
    /**
     * 根据用户ID、药物名称和计划时间查找服药记录（支持多用户）
     */
    public MedicationIntakeRecord findByUserMedicationAndTime(String userId, String medicationName, long plannedTime) {
        Log.d(TAG, "查询特定用户的服药记录: userId=" + userId + ", 药物=" + medicationName + ", 时间=" + plannedTime);
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            String selection = "user_id = ? AND medication_name = ? AND planned_time = ?";
            String[] selectionArgs = {userId, medicationName, String.valueOf(plannedTime)};
            
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_INTAKE, null, selection, selectionArgs, null, null, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                MedicationIntakeRecord record = cursorToMedicationIntakeRecord(cursor);
                Log.d(TAG, "找到用户服药记录: " + medicationName + " userId: " + userId);
                return record;
            }
            
            Log.d(TAG, "未找到用户服药记录: " + medicationName + " userId: " + userId);
            return null;
            
        } catch (Exception e) {
            Log.e(TAG, "查询用户服药记录失败", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close(); // 🔥 添加：关闭数据库连接
        }
    }
    /**
     * 将Cursor转换为MedicationIntakeRecord对象
     */
    private MedicationIntakeRecord cursorToMedicationIntakeRecord(Cursor cursor) {
        MedicationIntakeRecord record = new MedicationIntakeRecord();
        record.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        // 🔥 添加：读取 user_id 字段
        
        int userIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID);
        if (userIdIndex >= 0 && !cursor.isNull(userIdIndex)) {
            record.setUserId(cursor.getString(userIdIndex));
        }
        int medicationIdIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MEDICATION_ID);
        if (!cursor.isNull(medicationIdIndex)) {
            record.setMedicationId(cursor.getLong(medicationIdIndex));
        }

        int medicationNameIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MEDICATION_NAME);
        if (!cursor.isNull(medicationNameIndex)) {
            record.setMedicationName(cursor.getString(medicationNameIndex));
        }

        int plannedTimeIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLANNED_TIME);
        if (!cursor.isNull(plannedTimeIndex)) {
            record.setPlannedTime(cursor.getLong(plannedTimeIndex));
        }

        int actualTimeIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ACTUAL_TIME);
        if (!cursor.isNull(actualTimeIndex)) {
            record.setActualTime(cursor.getLong(actualTimeIndex));
        }

        int actualDosageIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ACTUAL_DOSAGE);
        if (!cursor.isNull(actualDosageIndex)) {
            record.setActualDosage(cursor.getString(actualDosageIndex));
        }

        int statusIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS);
        if (!cursor.isNull(statusIndex)) {
            record.setStatus(cursor.getInt(statusIndex));
        }

        int notesIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTES);
        if (!cursor.isNull(notesIndex)) {
            record.setNotes(cursor.getString(notesIndex));
        }

        record.setCreateTime(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATE_TIME)));
        record.setUpdateTime(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATE_TIME)));

        return record;
    }
    
        public void validateUserData(String currentUserId) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            
            try {
                // 查找所有没有正确userId的记录
                String findInvalidSql = "SELECT id, medication_name, planned_time, user_id FROM " + 
                        DatabaseHelper.TABLE_MEDICATION_INTAKE + " WHERE " +
                        "user_id IS NULL OR user_id = '' OR user_id = '0'";
                
                Cursor cursor = db.rawQuery(findInvalidSql, null);
                
                if (cursor != null && cursor.moveToFirst()) {
                    Log.d(TAG, "发现无效的服药记录，开始验证");
                    
                    do {
                        long id = cursor.getLong(0);
                        String medicationName = cursor.getString(1);
                        long plannedTime = cursor.getLong(2);
                        String userId = cursor.getString(3);
                        
                        Log.d(TAG, "无效记录: ID=" + id + " 药物=" + medicationName + 
                            " userId=" + userId + " 时间=" + plannedTime);
                        
                        // 检查是否与当前用户的记录重复
                        String checkDuplicateSql = "SELECT COUNT(*) FROM " + 
                                DatabaseHelper.TABLE_MEDICATION_INTAKE + " WHERE " +
                                "user_id = ? AND medication_name = ? AND planned_time = ?";
                        
                        Cursor checkCursor = db.rawQuery(checkDuplicateSql, 
                                new String[]{currentUserId, medicationName, String.valueOf(plannedTime)});
                        
                        if (checkCursor != null && checkCursor.moveToFirst()) {
                            int duplicateCount = checkCursor.getInt(0);
                            
                            if (duplicateCount > 0) {
                                // 有重复，删除无效记录
                                db.delete(DatabaseHelper.TABLE_MEDICATION_INTAKE, 
                                        "id = ?", new String[]{String.valueOf(id)});
                                Log.d(TAG, "删除重复的无效记录: " + medicationName);
                            } else {
                                // 没有重复，更新userId
                                ContentValues values = new ContentValues();
                                values.put(DatabaseHelper.COLUMN_USER_ID, currentUserId);
                                
                                db.update(DatabaseHelper.TABLE_MEDICATION_INTAKE, 
                                        values, "id = ?", new String[]{String.valueOf(id)});
                                Log.d(TAG, "修复无效记录的userId: " + medicationName);
                            }
                        }
                        
                        if (checkCursor != null) {
                            checkCursor.close();
                        }
                        
                    } while (cursor.moveToNext());
                }
                
                if (cursor != null) {
                    cursor.close();
                }
                
                Log.d(TAG, "用户数据验证完成");
                
            } catch (Exception e) {
                Log.e(TAG, "验证用户数据失败", e);
            } finally {
                db.close();
            }
        }
}