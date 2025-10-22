package com.lanqiDoctor.demo.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lanqiDoctor.demo.database.DatabaseHelper;
import com.lanqiDoctor.demo.database.entity.MedicationRecord;
import com.lanqiDoctor.demo.manager.UserStateManager; // 修改为正确的包路径

import java.util.ArrayList;
import java.util.List;

/**
 * 用药记录数据访问对象
 *
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class MedicationRecordDao {
    private Context context; // 添加context成员变量
    private DatabaseHelper dbHelper;//用于获取数据库连接

    public MedicationRecordDao(Context context) {
        this.context = context; // 保存context
        this.dbHelper = new DatabaseHelper(context);
    }

    // ==================== 基于名称的CRUD方法 ====================

    /**
     * 根据药物名称插入或更新用药记录（云同步专用）
     */
    public long insertOrUpdateByName(MedicationRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = -1;
        try {
            // 确保userId不为空，如果为空则尝试从UserStateManager获取
            if (record.getUserId() == null || record.getUserId().isEmpty()) {
                android.util.Log.w("MedicationRecordDao", "用药记录的userId为空，尝试从UserStateManager获取");
                
                UserStateManager userStateManager = UserStateManager.getInstance(context);
                String userId = userStateManager.getUserId(); // 使用getUserId()方法
                
                if (userId != null && !userId.isEmpty()) {
                    record.setUserId(userId);
                    android.util.Log.d("MedicationRecordDao", "从UserStateManager获取到用户ID: " + userId);
                } else {
                    android.util.Log.e("MedicationRecordDao", "无法获取有效的用户ID，插入失败");
                    return -1;
                }
            }
            
            ContentValues values = medicationRecordToContentValues(record);
            
            // 调试信息
            android.util.Log.d("MedicationRecordDao", "准备插入/更新药物: " + record.getMedicationName() + ", userId: " + record.getUserId());
            
            // 先尝试更新
            int updatedRows = db.update(DatabaseHelper.TABLE_MEDICATION_RECORD, values,
                    DatabaseHelper.COLUMN_MEDICATION_NAME + " = ? AND " + DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{record.getMedicationName(), record.getUserId()});

            if (updatedRows == 0) {
                // 如果没有更新任何行，说明记录不存在，执行插入
                result = db.insert(DatabaseHelper.TABLE_MEDICATION_RECORD, null, values);
                android.util.Log.d("MedicationRecordDao", "插入结果: " + result);
            } else {
                // 更新成功，返回受影响的行数
                result = updatedRows;
                android.util.Log.d("MedicationRecordDao", "更新结果: " + result);
            }
        } catch (Exception e) {
            android.util.Log.e("MedicationRecordDao", "插入/更新失败: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            db.close();
        }
        return result;
    }

    /**
     * 根据药物名称查询用药记录
     */
    public MedicationRecord findByName(String medicationName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        MedicationRecord record = null;
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_RECORD, null,
                    DatabaseHelper.COLUMN_MEDICATION_NAME + " = ?",
                    new String[]{medicationName}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                record = cursorToMedicationRecord(cursor);
            }
        } catch (Exception e) {
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
     * 根据药物名称更新用药记录
     */
    public int updateByName(MedicationRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = 0;
        try {
            record.setUpdateTime(System.currentTimeMillis());
            ContentValues values = medicationRecordToContentValues(record);
            rows = db.update(DatabaseHelper.TABLE_MEDICATION_RECORD, values,
                    DatabaseHelper.COLUMN_MEDICATION_NAME + " = ?",
                    new String[]{record.getMedicationName()});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return rows;
    }

    /**
     * 根据药物名称删除用药记录
     */
    public int deleteByName(String medicationName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = 0;
        try {
            // 先删除相关的服药记录
            db.delete(DatabaseHelper.TABLE_MEDICATION_INTAKE,
                    DatabaseHelper.COLUMN_MEDICATION_NAME + " = ?",
                    new String[]{medicationName});

            // 再删除用药记录
            rows = db.delete(DatabaseHelper.TABLE_MEDICATION_RECORD,
                    DatabaseHelper.COLUMN_MEDICATION_NAME + " = ?",
                    new String[]{medicationName});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return rows;
    }

    /**
     * 根据药物名称更新状态
     */
    public int updateStatusByName(String medicationName, int status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_STATUS, status);
            values.put(DatabaseHelper.COLUMN_UPDATE_TIME, System.currentTimeMillis());

            rows = db.update(DatabaseHelper.TABLE_MEDICATION_RECORD, values,
                    DatabaseHelper.COLUMN_MEDICATION_NAME + " = ?",
                    new String[]{medicationName});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return rows;
    }

    /**
     * 检查药物名称是否已存在
     */
    public boolean existsByName(String medicationName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        boolean exists = false;
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_RECORD,
                    new String[]{"COUNT(*)"},
                    DatabaseHelper.COLUMN_MEDICATION_NAME + " = ?",
                    new String[]{medicationName}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                exists = cursor.getInt(0) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return exists;
    }



    // ==================== 保留原有方法以向后兼容 ====================

    /**
     * 添加用药记录
     * @deprecated 建议使用 insertOrUpdateByName 方法
     */
    @Deprecated
    public long insert(MedicationRecord record) {
        return insertOrUpdateByName(record);
    }

    /**
     * 更新用药记录
     * @deprecated 建议使用 updateByName 方法
     */
    @Deprecated
    public int update(MedicationRecord record) {
        return updateByName(record);
    }

    /**
     * 删除用药记录
     * @deprecated 建议使用 deleteByName 方法
     */
    @Deprecated
    public int delete(long id) {
        // 先根据ID找到药物名称，然后调用基于名称的删除方法
        MedicationRecord record = findById(id);
        if (record != null) {
            return deleteByName(record.getMedicationName());
        }
        return 0;
    }

    /**
     * 根据ID查询用药记录
     * @deprecated 建议使用 findByName 方法
     */
    @Deprecated
    public MedicationRecord findById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        MedicationRecord record = null;
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_RECORD, null,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)}, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                record = cursorToMedicationRecord(cursor);
            }
        } catch (Exception e) {
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
     * 查询指定用户的所有正在服用的药物
     */
    public List<MedicationRecord> findActiveMedications(String userId) {
        return findByStatusAndUserId(1, userId);
    }


    /**
     * 根据状态和用户ID查询用药记录
     */
    public List<MedicationRecord> findByStatusAndUserId(int status, String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<MedicationRecord> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_RECORD, null,
                    DatabaseHelper.COLUMN_STATUS + " = ? AND " + DatabaseHelper.COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(status), userId},
                    null, null, DatabaseHelper.COLUMN_CREATE_TIME + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MedicationRecord record = cursorToMedicationRecord(cursor);
                    list.add(record);
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
     * 查询所有用药记录
     */
    public List<MedicationRecord> findAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<MedicationRecord> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_RECORD, null, null, null,
                    null, null, DatabaseHelper.COLUMN_CREATE_TIME + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MedicationRecord record = cursorToMedicationRecord(cursor);
                    list.add(record);
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
     * 查询所有正在服用的药物（兼容旧版本，不区分用户）
     */
    public List<MedicationRecord> findActiveMedications_1() {
        return findByStatus(1);
    }

    /**
     * 根据状态查询用药记录（不区分用户）
     */
    public List<MedicationRecord> findByStatus(int status) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<MedicationRecord> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_RECORD, 
                    null,
                    DatabaseHelper.COLUMN_STATUS + " = ?",
                    new String[]{String.valueOf(status)},
                    null, null, DatabaseHelper.COLUMN_CREATE_TIME + " DESC");
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MedicationRecord record = cursorToMedicationRecord(cursor);
                    list.add(record);
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
     * 根据药品名称模糊查询
     */
    public List<MedicationRecord> findByNameLike(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<MedicationRecord> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(DatabaseHelper.TABLE_MEDICATION_RECORD, null,
                    DatabaseHelper.COLUMN_MEDICATION_NAME + " LIKE ?",
                    new String[]{"%" + name + "%"},
                    null, null, DatabaseHelper.COLUMN_CREATE_TIME + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MedicationRecord record = cursorToMedicationRecord(cursor);
                    list.add(record);
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
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_MEDICATION_RECORD, null);
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
     * 将MedicationRecord对象转换为ContentValues
     */
    private ContentValues medicationRecordToContentValues(MedicationRecord record) {
        ContentValues values = new ContentValues();        
        if (record.getUserId() != null) {
            values.put(DatabaseHelper.COLUMN_USER_ID, record.getUserId());
        }
        if (record.getMedicationName() != null) {
            values.put(DatabaseHelper.COLUMN_MEDICATION_NAME, record.getMedicationName());
        }
        if (record.getDosage() != null) {
            values.put(DatabaseHelper.COLUMN_DOSAGE, record.getDosage());
        }
        if (record.getFrequency() != null) {
            values.put(DatabaseHelper.COLUMN_FREQUENCY, record.getFrequency());
        }
        if (record.getUnit() != null) {
            values.put(DatabaseHelper.COLUMN_UNIT, record.getUnit());
        }
        if (record.getStartDate() != null) {
            values.put(DatabaseHelper.COLUMN_START_DATE, record.getStartDate());
        }
        if (record.getEndDate() != null) {
            values.put(DatabaseHelper.COLUMN_END_DATE, record.getEndDate());
        }
        if (record.getReminderTimes() != null) {
            values.put(DatabaseHelper.COLUMN_REMINDER_TIMES, record.getReminderTimes());
        }
        if (record.getNotes() != null) {
            values.put(DatabaseHelper.COLUMN_NOTES, record.getNotes());
        }
        if (record.getStatus() != null) {
            values.put(DatabaseHelper.COLUMN_STATUS, record.getStatus());
        }

        values.put(DatabaseHelper.COLUMN_CREATE_TIME, record.getCreateTime());
        values.put(DatabaseHelper.COLUMN_UPDATE_TIME, record.getUpdateTime());
        return values;
    }

    /**
     * 将Cursor转换为MedicationRecord对象
     */
    private MedicationRecord cursorToMedicationRecord(Cursor cursor) {
        MedicationRecord record = new MedicationRecord();
        record.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));

        int nameIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MEDICATION_NAME);
        if (!cursor.isNull(nameIndex)) {
            record.setMedicationName(cursor.getString(nameIndex));
        }

        int dosageIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DOSAGE);
        if (!cursor.isNull(dosageIndex)) {
            record.setDosage(cursor.getString(dosageIndex));
        }

        int frequencyIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FREQUENCY);
        if (!cursor.isNull(frequencyIndex)) {
            record.setFrequency(cursor.getString(frequencyIndex));
        }

        int unitIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UNIT);
        if (!cursor.isNull(unitIndex)) {
            record.setUnit(cursor.getString(unitIndex));
        }

        int startDateIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_START_DATE);
        if (!cursor.isNull(startDateIndex)) {
            record.setStartDate(cursor.getLong(startDateIndex));
        }

        int endDateIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_END_DATE);
        if (!cursor.isNull(endDateIndex)) {
            record.setEndDate(cursor.getLong(endDateIndex));
        }

        int reminderTimesIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMINDER_TIMES);
        if (!cursor.isNull(reminderTimesIndex)) {
            record.setReminderTimes(cursor.getString(reminderTimesIndex));
        }

        int notesIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTES);
        if (!cursor.isNull(notesIndex)) {
            record.setNotes(cursor.getString(notesIndex));
        }

        int statusIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS);
        if (!cursor.isNull(statusIndex)) {
            record.setStatus(cursor.getInt(statusIndex));
        }

        int userIdIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID);
        if (userIdIndex != -1 && !cursor.isNull(userIdIndex)) {
            record.setUserId(cursor.getString(userIdIndex));
        }

        record.setCreateTime(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATE_TIME)));
        record.setUpdateTime(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATE_TIME)));

        return record;
    }
}