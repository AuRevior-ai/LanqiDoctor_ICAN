package com.lanqiDoctor.demo.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lanqiDoctor.demo.database.DatabaseHelper;
import com.lanqiDoctor.demo.database.entity.Habit;
import com.lanqiDoctor.demo.manager.UserStateManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 习惯数据访问对象
 *
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class HabitDao {
    private Context context;
    private DatabaseHelper dbHelper;

    public HabitDao(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * 插入习惯记录
     */
    public long insert(Habit habit) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = -1;
        try {
            // 确保userId不为空
            if (habit.getUserId() == null || habit.getUserId().isEmpty()) {
                UserStateManager userStateManager = UserStateManager.getInstance(context);
                String userId = userStateManager.getUserId();
                
                if (userId != null && !userId.isEmpty()) {
                    habit.setUserId(userId);
                } else {
                    android.util.Log.e("HabitDao", "无法获取有效的用户ID，插入失败");
                    return -1;
                }
            }
            
            habit.setCreateTime(System.currentTimeMillis());
            habit.setUpdateTime(System.currentTimeMillis());
            
            ContentValues values = new ContentValues();
            values.put("user_id", habit.getUserId());
            values.put("habit_name", habit.getHabitName());
            values.put("description", habit.getDescription());
            values.put("frequency", habit.getFrequency());
            values.put("frequency_value", habit.getFrequencyValue());
            values.put("frequency_unit", habit.getFrequencyUnit());
            values.put("duration", habit.getDuration());
            values.put("cycle_days", habit.getCycleDays());
            values.put("reminder_times", habit.getReminderTimes());
            values.put("block_times", habit.getBlockTimes());
            values.put("is_active", habit.getIsActive() ? 1 : 0);
            values.put("enable_notification", habit.getEnableNotification() ? 1 : 0);
            values.put("enable_system_alarm", habit.getEnableSystemAlarm() ? 1 : 0);
            values.put("completed_days", habit.getCompletedDays());
            values.put("total_check_ins", habit.getTotalCheckIns());
            values.put("start_date", habit.getStartDate());
            values.put("end_date", habit.getEndDate());
            values.put("category", habit.getCategory());
            values.put("priority", habit.getPriority());
            values.put("notes", habit.getNotes());
            values.put("status", habit.getStatus());
            values.put("create_time", habit.getCreateTime());
            values.put("update_time", habit.getUpdateTime());

            result = db.insert("habits", null, values);
            if (result != -1) {
                habit.setId(result);
                android.util.Log.d("HabitDao", "成功插入习惯记录: " + habit.getHabitName());
            }
        } catch (Exception e) {
            android.util.Log.e("HabitDao", "插入习惯记录失败", e);
        } finally {
            db.close();
        }
        return result;
    }

    /**
     * 更新习惯记录
     */
    public boolean update(Habit habit) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;
        try {
            habit.setUpdateTime(System.currentTimeMillis());
            
            ContentValues values = new ContentValues();
            values.put("habit_name", habit.getHabitName());
            values.put("description", habit.getDescription());
            values.put("frequency", habit.getFrequency());
            values.put("frequency_value", habit.getFrequencyValue());
            values.put("frequency_unit", habit.getFrequencyUnit());
            values.put("duration", habit.getDuration());
            values.put("cycle_days", habit.getCycleDays());
            values.put("reminder_times", habit.getReminderTimes());
            values.put("block_times", habit.getBlockTimes());
            values.put("is_active", habit.getIsActive() ? 1 : 0);
            values.put("enable_notification", habit.getEnableNotification() ? 1 : 0);
            values.put("enable_system_alarm", habit.getEnableSystemAlarm() ? 1 : 0);
            values.put("completed_days", habit.getCompletedDays());
            values.put("total_check_ins", habit.getTotalCheckIns());
            values.put("start_date", habit.getStartDate());
            values.put("end_date", habit.getEndDate());
            values.put("category", habit.getCategory());
            values.put("priority", habit.getPriority());
            values.put("notes", habit.getNotes());
            values.put("status", habit.getStatus());
            values.put("update_time", habit.getUpdateTime());

            int rowsAffected = db.update("habits", values, "id = ?", 
                new String[]{String.valueOf(habit.getId())});
            success = rowsAffected > 0;
            
            if (success) {
                android.util.Log.d("HabitDao", "成功更新习惯记录: " + habit.getHabitName());
            }
        } catch (Exception e) {
            android.util.Log.e("HabitDao", "更新习惯记录失败", e);
        } finally {
            db.close();
        }
        return success;
    }

    /**
     * 删除习惯记录（软删除）
     */
    public boolean delete(Long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;
        try {
            ContentValues values = new ContentValues();
            values.put("status", 0); // 0表示删除
            values.put("update_time", System.currentTimeMillis());
            
            int rowsAffected = db.update("habits", values, "id = ?", 
                new String[]{String.valueOf(id)});
            success = rowsAffected > 0;
            
            if (success) {
                android.util.Log.d("HabitDao", "成功删除习惯记录 ID: " + id);
            }
        } catch (Exception e) {
            android.util.Log.e("HabitDao", "删除习惯记录失败", e);
        } finally {
            db.close();
        }
        return success;
    }

    /**
     * 根据ID查找习惯记录
     */
    public Habit findById(Long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Habit habit = null;
        Cursor cursor = null;
        
        try {
            cursor = db.query("habits", null, "id = ? AND status > 0", 
                new String[]{String.valueOf(id)}, null, null, null);
            
            if (cursor.moveToFirst()) {
                habit = cursorToHabit(cursor);
            }
        } catch (Exception e) {
            android.util.Log.e("HabitDao", "根据ID查找习惯记录失败", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return habit;
    }

    /**
     * 查找所有习惯记录
     */
    public List<Habit> findAll() {
        List<Habit> habitList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            // 获取当前用户ID
            UserStateManager userStateManager = UserStateManager.getInstance(context);
            String currentUserId = userStateManager.getUserId();
            
            if (currentUserId == null || currentUserId.isEmpty()) {
                android.util.Log.w("HabitDao", "当前用户ID为空，无法查询习惯记录");
                return habitList;
            }

            cursor = db.query("habits", null, 
                "user_id = ? AND status > 0", 
                new String[]{currentUserId}, 
                null, null, "create_time DESC");
            
            while (cursor.moveToNext()) {
                Habit habit = cursorToHabit(cursor);
                if (habit != null) {
                    habitList.add(habit);
                }
            }
            
            android.util.Log.d("HabitDao", "查询到 " + habitList.size() + " 条习惯记录");
        } catch (Exception e) {
            android.util.Log.e("HabitDao", "查询所有习惯记录失败", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return habitList;
    }

    /**
     * 查找激活的习惯记录
     */
    public List<Habit> findActiveHabits() {
        List<Habit> habitList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            UserStateManager userStateManager = UserStateManager.getInstance(context);
            String currentUserId = userStateManager.getUserId();
            
            if (currentUserId == null || currentUserId.isEmpty()) {
                android.util.Log.w("HabitDao", "当前用户ID为空，无法查询激活习惯记录");
                return habitList;
            }

            cursor = db.query("habits", null, 
                "user_id = ? AND status = 1 AND is_active = 1", 
                new String[]{currentUserId}, 
                null, null, "priority DESC, create_time DESC");
            
            while (cursor.moveToNext()) {
                Habit habit = cursorToHabit(cursor);
                if (habit != null) {
                    habitList.add(habit);
                }
            }
            
            android.util.Log.d("HabitDao", "查询到 " + habitList.size() + " 条激活习惯记录");
        } catch (Exception e) {
            android.util.Log.e("HabitDao", "查询激活习惯记录失败", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return habitList;
    }

    /**
     * 根据分类查找习惯记录
     */
    public List<Habit> findByCategory(String category) {
        List<Habit> habitList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        
        try {
            UserStateManager userStateManager = UserStateManager.getInstance(context);
            String currentUserId = userStateManager.getUserId();
            
            if (currentUserId == null || currentUserId.isEmpty()) {
                return habitList;
            }

            cursor = db.query("habits", null, 
                "user_id = ? AND category = ? AND status > 0", 
                new String[]{currentUserId, category}, 
                null, null, "create_time DESC");
            
            while (cursor.moveToNext()) {
                Habit habit = cursorToHabit(cursor);
                if (habit != null) {
                    habitList.add(habit);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("HabitDao", "根据分类查询习惯记录失败", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        
        return habitList;
    }

    /**
     * 更新习惯打卡信息
     */
    public boolean updateCheckIn(Long habitId, int completedDays, int totalCheckIns) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;
        try {
            ContentValues values = new ContentValues();
            values.put("completed_days", completedDays);
            values.put("total_check_ins", totalCheckIns);
            values.put("update_time", System.currentTimeMillis());

            int rowsAffected = db.update("habits", values, "id = ?", 
                new String[]{String.valueOf(habitId)});
            success = rowsAffected > 0;
            
            if (success) {
                android.util.Log.d("HabitDao", "成功更新习惯打卡信息 ID: " + habitId);
            }
        } catch (Exception e) {
            android.util.Log.e("HabitDao", "更新习惯打卡信息失败", e);
        } finally {
            db.close();
        }
        return success;
    }

    /**
     * 将Cursor转换为Habit对象
     */
    private Habit cursorToHabit(Cursor cursor) {
        try {
            Habit habit = new Habit();
            
            habit.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
            habit.setUserId(cursor.getString(cursor.getColumnIndexOrThrow("user_id")));
            habit.setHabitName(cursor.getString(cursor.getColumnIndexOrThrow("habit_name")));
            habit.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            habit.setFrequency(cursor.getString(cursor.getColumnIndexOrThrow("frequency")));
            habit.setFrequencyValue(cursor.getInt(cursor.getColumnIndexOrThrow("frequency_value")));
            habit.setFrequencyUnit(cursor.getString(cursor.getColumnIndexOrThrow("frequency_unit")));
            habit.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow("duration")));
            habit.setCycleDays(cursor.getInt(cursor.getColumnIndexOrThrow("cycle_days")));
            habit.setReminderTimes(cursor.getString(cursor.getColumnIndexOrThrow("reminder_times")));
            habit.setBlockTimes(cursor.getString(cursor.getColumnIndexOrThrow("block_times")));
            habit.setIsActive(cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1);
            habit.setEnableNotification(cursor.getInt(cursor.getColumnIndexOrThrow("enable_notification")) == 1);
            habit.setEnableSystemAlarm(cursor.getInt(cursor.getColumnIndexOrThrow("enable_system_alarm")) == 1);
            habit.setCompletedDays(cursor.getInt(cursor.getColumnIndexOrThrow("completed_days")));
            habit.setTotalCheckIns(cursor.getInt(cursor.getColumnIndexOrThrow("total_check_ins")));
            habit.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow("start_date")));
            habit.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow("end_date")));
            habit.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
            habit.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow("priority")));
            habit.setNotes(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
            habit.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow("status")));
            habit.setCreateTime(cursor.getLong(cursor.getColumnIndexOrThrow("create_time")));
            habit.setUpdateTime(cursor.getLong(cursor.getColumnIndexOrThrow("update_time")));
            
            return habit;
        } catch (Exception e) {
            android.util.Log.e("HabitDao", "转换Cursor为Habit对象失败", e);
            return null;
        }
    }
}
