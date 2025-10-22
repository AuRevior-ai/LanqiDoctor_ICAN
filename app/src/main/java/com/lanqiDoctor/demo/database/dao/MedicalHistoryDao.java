package com.lanqiDoctor.demo.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lanqiDoctor.demo.database.DatabaseHelper;
import com.lanqiDoctor.demo.database.entity.MedicalHistory;
import com.lanqiDoctor.demo.manager.UserStateManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 既往病史记录数据访问对象
 *
 * @author 蓝旗医生开发团队
 * @version 1.0
 */
public class MedicalHistoryDao {
    private Context context;
    private DatabaseHelper dbHelper;

    public MedicalHistoryDao(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * 插入既往病史记录
     */
    public long insert(MedicalHistory history) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = -1;
        try {
            // 确保userId不为空
            if (history.getUserId() == null || history.getUserId().isEmpty()) {
                UserStateManager userStateManager = UserStateManager.getInstance(context);
                String userId = userStateManager.getUserId();
                
                if (userId != null && !userId.isEmpty()) {
                    history.setUserId(userId);
                } else {
                    android.util.Log.e("MedicalHistoryDao", "无法获取有效的用户ID，插入失败");
                    return -1;
                }
            }

            ContentValues values = new ContentValues();
            values.put("user_id", history.getUserId());
            values.put("disease_name", history.getDiseaseName());
            values.put("diagnosis_date", history.getDiagnosisDate());
            values.put("severity", history.getSeverity());
            values.put("treatment_status", history.getTreatmentStatus());
            values.put("hospital", history.getHospital());
            values.put("doctor", history.getDoctor());
            values.put("symptoms", history.getSymptoms());
            values.put("treatment", history.getTreatment());
            values.put("notes", history.getNotes());
            values.put("status", history.getStatus());
            values.put("create_time", history.getCreateTime());
            values.put("update_time", history.getUpdateTime());

            result = db.insert("medical_history", null, values);
            if (result != -1) {
                history.setId(result);
                android.util.Log.d("MedicalHistoryDao", "成功插入既往病史记录: " + history.getDiseaseName());
            }
        } catch (Exception e) {
            android.util.Log.e("MedicalHistoryDao", "插入既往病史记录失败", e);
        } finally {
            db.close();
        }
        return result;
    }

    /**
     * 更新既往病史记录
     */
    public boolean update(MedicalHistory history) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;
        try {
            history.setUpdateTime(System.currentTimeMillis());
            
            ContentValues values = new ContentValues();
            values.put("disease_name", history.getDiseaseName());
            values.put("diagnosis_date", history.getDiagnosisDate());
            values.put("severity", history.getSeverity());
            values.put("treatment_status", history.getTreatmentStatus());
            values.put("hospital", history.getHospital());
            values.put("doctor", history.getDoctor());
            values.put("symptoms", history.getSymptoms());
            values.put("treatment", history.getTreatment());
            values.put("notes", history.getNotes());
            values.put("status", history.getStatus());
            values.put("update_time", history.getUpdateTime());

            int rowsAffected = db.update("medical_history", values, "id = ?", 
                new String[]{String.valueOf(history.getId())});
            success = rowsAffected > 0;
            
            if (success) {
                android.util.Log.d("MedicalHistoryDao", "成功更新既往病史记录: " + history.getDiseaseName());
            }
        } catch (Exception e) {
            android.util.Log.e("MedicalHistoryDao", "更新既往病史记录失败", e);
        } finally {
            db.close();
        }
        return success;
    }

    /**
     * 删除既往病史记录
     */
    public boolean delete(Long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;
        try {
            int rowsAffected = db.delete("medical_history", "id = ?", 
                new String[]{String.valueOf(id)});
            success = rowsAffected > 0;
            
            if (success) {
                android.util.Log.d("MedicalHistoryDao", "成功删除既往病史记录 ID: " + id);
            }
        } catch (Exception e) {
            android.util.Log.e("MedicalHistoryDao", "删除既往病史记录失败", e);
        } finally {
            db.close();
        }
        return success;
    }

    /**
     * 根据ID查找既往病史记录
     */
    public MedicalHistory findById(Long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        MedicalHistory history = null;
        Cursor cursor = null;
        
        try {
            cursor = db.query("medical_history", null, "id = ? AND status = 1", 
                new String[]{String.valueOf(id)}, null, null, null);
            
            if (cursor.moveToFirst()) {
                history = cursorToMedicalHistory(cursor);
            }
        } catch (Exception e) {
            android.util.Log.e("MedicalHistoryDao", "根据ID查找既往病史记录失败", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return history;
    }

    /**
     * 查找所有既往病史记录
     */
    public List<MedicalHistory> findAll() {
        List<MedicalHistory> historyList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            // 获取当前用户ID
            UserStateManager userStateManager = UserStateManager.getInstance(context);
            String currentUserId = userStateManager.getUserId();
            
            if (currentUserId == null || currentUserId.isEmpty()) {
                android.util.Log.w("MedicalHistoryDao", "当前用户ID为空，无法查询既往病史记录");
                return historyList;
            }

            cursor = db.query("medical_history", null, 
                "user_id = ? AND status = 1", 
                new String[]{currentUserId}, 
                null, null, "create_time DESC");
            
            while (cursor.moveToNext()) {
                MedicalHistory history = cursorToMedicalHistory(cursor);
                if (history != null) {
                    historyList.add(history);
                }
            }
            
            android.util.Log.d("MedicalHistoryDao", "查询到 " + historyList.size() + " 条既往病史记录");
        } catch (Exception e) {
            android.util.Log.e("MedicalHistoryDao", "查询所有既往病史记录失败", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return historyList;
    }

    /**
     * 将Cursor转换为MedicalHistory对象
     */
    private MedicalHistory cursorToMedicalHistory(Cursor cursor) {
        try {
            MedicalHistory history = new MedicalHistory();
            
            history.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
            history.setUserId(cursor.getString(cursor.getColumnIndexOrThrow("user_id")));
            history.setDiseaseName(cursor.getString(cursor.getColumnIndexOrThrow("disease_name")));
            history.setDiagnosisDate(cursor.getString(cursor.getColumnIndexOrThrow("diagnosis_date")));
            history.setSeverity(cursor.getString(cursor.getColumnIndexOrThrow("severity")));
            history.setTreatmentStatus(cursor.getString(cursor.getColumnIndexOrThrow("treatment_status")));
            history.setHospital(cursor.getString(cursor.getColumnIndexOrThrow("hospital")));
            history.setDoctor(cursor.getString(cursor.getColumnIndexOrThrow("doctor")));
            history.setSymptoms(cursor.getString(cursor.getColumnIndexOrThrow("symptoms")));
            history.setTreatment(cursor.getString(cursor.getColumnIndexOrThrow("treatment")));
            history.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
            history.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow("status")));
            history.setCreateTime(cursor.getLong(cursor.getColumnIndexOrThrow("create_time")));
            history.setUpdateTime(cursor.getLong(cursor.getColumnIndexOrThrow("update_time")));
            
            return history;
        } catch (Exception e) {
            android.util.Log.e("MedicalHistoryDao", "转换Cursor为MedicalHistory对象失败", e);
            return null;
        }
    }
}
