package com.lanqiDoctor.demo.database;

//import com.lanqiDoctor.demo.aop.Log;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;  // 🔥 添加这行导入！

/**
 * 数据库帮助类
 *
 * @author 蓝岐医童开发团队
 * @version 1.0
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lanqi_doctor.db";
    private static final int DATABASE_VERSION = 10; // 修复习惯表字段缺失问题

    // 健康信息表
    public static final String TABLE_HEALTH_INFO = "health_info";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_WEIGHT = "weight";
    public static final String COLUMN_HEART_RATE = "heart_rate";
    public static final String COLUMN_SYSTOLIC_PRESSURE = "systolic_pressure";
    public static final String COLUMN_DIASTOLIC_PRESSURE = "diastolic_pressure";
    public static final String COLUMN_BLOOD_SUGAR = "blood_sugar";
    public static final String COLUMN_REMARKS = "remarks";
    public static final String COLUMN_CREATE_TIME = "create_time";
    public static final String COLUMN_UPDATE_TIME = "update_time";
    //新增睡眠和步数字段
    public static final String COLUMN_STEPS = "steps";
    public static final String COLUMN_SLEEP_DURATION = "sleep_duration";
    // 用药记录表
    public static final String TABLE_MEDICATION_RECORD = "medication_record";
    public static final String COLUMN_MEDICATION_NAME = "medication_name";
    public static final String COLUMN_DOSAGE = "dosage";
    public static final String COLUMN_FREQUENCY = "frequency";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_END_DATE = "end_date";
    public static final String COLUMN_REMINDER_TIMES = "reminder_times";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_STATUS = "status";

    // 服药记录表
    public static final String TABLE_MEDICATION_INTAKE = "medication_intake_record";
    public static final String COLUMN_MEDICATION_ID = "medication_id"; // 保留但不再作为主要关联字段
    public static final String COLUMN_PLANNED_TIME = "planned_time";
    public static final String COLUMN_ACTUAL_TIME = "actual_time";
    public static final String COLUMN_ACTUAL_DOSAGE = "actual_dosage";

    //用户ID字段
    public static final String COLUMN_USER_ID = "user_id";
    
    // 既往病史表
    public static final String TABLE_MEDICAL_HISTORY = "medical_history";
    public static final String COLUMN_DISEASE_NAME = "disease_name";
    public static final String COLUMN_DIAGNOSIS_DATE = "diagnosis_date";
    public static final String COLUMN_SEVERITY = "severity";
    public static final String COLUMN_TREATMENT_STATUS = "treatment_status";
    public static final String COLUMN_HOSPITAL = "hospital";
    public static final String COLUMN_DOCTOR = "doctor";
    public static final String COLUMN_SYMPTOMS = "symptoms";
    public static final String COLUMN_TREATMENT = "treatment";

    // 习惯表
    public static final String TABLE_HABITS = "habits";
    public static final String COLUMN_HABIT_NAME = "habit_name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_FREQUENCY_VALUE = "frequency_value";
    public static final String COLUMN_FREQUENCY_UNIT = "frequency_unit";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_CYCLE_DAYS = "cycle_days";
    public static final String COLUMN_PRIORITY = "priority";
    public static final String COLUMN_BLOCK_TIMES = "block_times";
    public static final String COLUMN_IS_ACTIVE = "is_active";
    public static final String COLUMN_ENABLE_NOTIFICATION = "enable_notification";
    public static final String COLUMN_ENABLE_SYSTEM_ALARM = "enable_system_alarm";
    public static final String COLUMN_COMPLETED_DAYS = "completed_days";
    public static final String COLUMN_TOTAL_CHECK_INS = "total_check_ins";
    // 创建健康信息表的SQL语句
    private static final String CREATE_HEALTH_INFO_TABLE = "CREATE TABLE " + TABLE_HEALTH_INFO + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
            COLUMN_AGE + " INTEGER, " +
            COLUMN_HEIGHT + " REAL, " +
            COLUMN_WEIGHT + " REAL, " +
            COLUMN_HEART_RATE + " INTEGER, " +
            COLUMN_SYSTOLIC_PRESSURE + " REAL, " +
            COLUMN_DIASTOLIC_PRESSURE + " REAL, " +
            COLUMN_BLOOD_SUGAR + " REAL, " +
            COLUMN_REMARKS + " TEXT, " +
            COLUMN_STEPS + " INTEGER, " + // 新增
            COLUMN_SLEEP_DURATION + " REAL, " + // 新增
            COLUMN_CREATE_TIME + " INTEGER NOT NULL, " +
            COLUMN_UPDATE_TIME + " INTEGER NOT NULL" +
            ")";

    // 创建用药记录表的SQL语句 - 药物名称作为主键
    private static final String CREATE_MEDICATION_RECORD_TABLE = "CREATE TABLE " + TABLE_MEDICATION_RECORD + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_MEDICATION_NAME + " TEXT UNIQUE NOT NULL, " + // 设置为唯一约束
            COLUMN_USER_ID + " TEXT NOT NULL, " + // 新增：用户ID字段
            COLUMN_DOSAGE + " TEXT NOT NULL, " +
            COLUMN_FREQUENCY + " TEXT NOT NULL, " +
            COLUMN_UNIT + " TEXT NOT NULL, " +
            COLUMN_START_DATE + " INTEGER, " +
            COLUMN_END_DATE + " INTEGER, " +
            COLUMN_REMINDER_TIMES + " TEXT, " +
            COLUMN_NOTES + " TEXT, " +
            COLUMN_STATUS + " INTEGER DEFAULT 1, " +
            COLUMN_CREATE_TIME + " INTEGER NOT NULL, " +
            COLUMN_UPDATE_TIME + " INTEGER NOT NULL" +
            ")";

    // 创建服药记录表的SQL语句 - 使用药物名称关联
    private static final String CREATE_MEDICATION_INTAKE_TABLE = "CREATE TABLE " + TABLE_MEDICATION_INTAKE + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USER_ID + " TEXT NOT NULL, " + // 修改：改为TEXT类型
            COLUMN_MEDICATION_ID + " INTEGER, " + // 保留但可选
            COLUMN_MEDICATION_NAME + " TEXT NOT NULL, " +
            COLUMN_PLANNED_TIME + " INTEGER NOT NULL, " +
            COLUMN_ACTUAL_TIME + " INTEGER, " +
            COLUMN_ACTUAL_DOSAGE + " TEXT, " +
            COLUMN_STATUS + " INTEGER DEFAULT 0, " +
            COLUMN_NOTES + " TEXT, " +
            COLUMN_CREATE_TIME + " INTEGER NOT NULL, " +
            COLUMN_UPDATE_TIME + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + COLUMN_MEDICATION_NAME + ") REFERENCES " + TABLE_MEDICATION_RECORD + "(" + COLUMN_MEDICATION_NAME + "), " +
            // 🔥 修复：唯一约束必须包含 user_id
            "UNIQUE(" + COLUMN_USER_ID + ", " + COLUMN_MEDICATION_NAME + ", " + COLUMN_PLANNED_TIME + ")" +
            ")";

    // 创建既往病史表的SQL语句
    private static final String CREATE_MEDICAL_HISTORY_TABLE = "CREATE TABLE " + TABLE_MEDICAL_HISTORY + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USER_ID + " TEXT NOT NULL, " +
            COLUMN_DISEASE_NAME + " TEXT NOT NULL, " +
            COLUMN_DIAGNOSIS_DATE + " TEXT, " +
            COLUMN_SEVERITY + " TEXT, " +
            COLUMN_TREATMENT_STATUS + " TEXT, " +
            COLUMN_HOSPITAL + " TEXT, " +
            COLUMN_DOCTOR + " TEXT, " +
            COLUMN_SYMPTOMS + " TEXT, " +
            COLUMN_TREATMENT + " TEXT, " +
            COLUMN_NOTES + " TEXT, " +
            COLUMN_STATUS + " INTEGER DEFAULT 1, " +
            COLUMN_CREATE_TIME + " INTEGER NOT NULL, " +
            COLUMN_UPDATE_TIME + " INTEGER NOT NULL" +
            ")";

    // 创建习惯表的SQL语句
    private static final String CREATE_HABITS_TABLE = "CREATE TABLE " + TABLE_HABITS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USER_ID + " TEXT NOT NULL, " +
            COLUMN_HABIT_NAME + " TEXT NOT NULL, " +
            COLUMN_DESCRIPTION + " TEXT, " +
            COLUMN_CATEGORY + " TEXT, " +
            COLUMN_FREQUENCY + " TEXT, " +
            COLUMN_FREQUENCY_VALUE + " INTEGER, " +
            COLUMN_FREQUENCY_UNIT + " TEXT, " +
            COLUMN_DURATION + " INTEGER, " +
            COLUMN_CYCLE_DAYS + " INTEGER, " +
            COLUMN_PRIORITY + " INTEGER, " +
            COLUMN_START_DATE + " TEXT, " +
            COLUMN_END_DATE + " TEXT, " +
            COLUMN_REMINDER_TIMES + " TEXT, " +
            COLUMN_BLOCK_TIMES + " TEXT, " +
            COLUMN_NOTES + " TEXT, " +
            COLUMN_IS_ACTIVE + " INTEGER DEFAULT 1, " +
            COLUMN_STATUS + " INTEGER DEFAULT 1, " +
            COLUMN_ENABLE_NOTIFICATION + " INTEGER DEFAULT 1, " +
            COLUMN_ENABLE_SYSTEM_ALARM + " INTEGER DEFAULT 0, " +
            COLUMN_COMPLETED_DAYS + " INTEGER DEFAULT 0, " +
            COLUMN_TOTAL_CHECK_INS + " INTEGER DEFAULT 0, " +
            COLUMN_CREATE_TIME + " INTEGER NOT NULL, " +
            COLUMN_UPDATE_TIME + " INTEGER NOT NULL" +
            ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_HEALTH_INFO_TABLE);
        db.execSQL(CREATE_MEDICATION_RECORD_TABLE);
        db.execSQL(CREATE_MEDICATION_INTAKE_TABLE);
        db.execSQL(CREATE_MEDICAL_HISTORY_TABLE);
        db.execSQL(CREATE_HABITS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // 升级到版本2，添加用药相关表
            db.execSQL(CREATE_MEDICATION_RECORD_TABLE);
            db.execSQL(CREATE_MEDICATION_INTAKE_TABLE);
        }
        if (oldVersion < 3) {
            // 升级到版本3，修改表结构支持基于名称的CRUD
            // 为药物名称添加唯一约束
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_medication_name ON " +
                    TABLE_MEDICATION_RECORD + "(" + COLUMN_MEDICATION_NAME + ")");

            // 为服药记录添加联合唯一约束
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_intake_name_time ON " +
                    TABLE_MEDICATION_INTAKE + "(" + COLUMN_MEDICATION_NAME + ", " + COLUMN_PLANNED_TIME + ")");
        }
            // 新增：升级到版本4，添加步数和睡眠时长字段
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_HEALTH_INFO + " ADD COLUMN " + COLUMN_STEPS + " INTEGER");
            db.execSQL("ALTER TABLE " + TABLE_HEALTH_INFO + " ADD COLUMN " + COLUMN_SLEEP_DURATION + " REAL");
        }
            // 新增：升级到版本5，添加 user_id 字段
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + TABLE_MEDICATION_INTAKE + " ADD COLUMN user_id INTEGER NOT NULL DEFAULT 0");
        }
            // 新增：升级到版本6，添加 user_id 字段到 medication_record 表
        if (oldVersion < 6) {
            // 为 medication_record 表添加 user_id 字段
            db.execSQL("ALTER TABLE " + TABLE_MEDICATION_RECORD + " ADD COLUMN " + COLUMN_USER_ID + " TEXT NOT NULL DEFAULT ''");
            
            // 更新 medication_intake_record 表的 user_id 字段类型（如果需要的话）
            // 由于SQLite不支持直接修改字段类型，这里先跳过，使用默认值0表示老数据
        }
        // 🔥 新增：升级到版本7，修复唯一约束
        if (oldVersion < 7) {
            Log.d("DatabaseHelper", "开始修复数据库唯一约束到版本7");
            
            try {
                // 删除旧的索引
                db.execSQL("DROP INDEX IF EXISTS idx_intake_name_time");
                Log.d("DatabaseHelper", "删除旧索引成功");
                
                // 创建新的包含user_id的唯一索引
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_intake_user_name_time ON " +
                        TABLE_MEDICATION_INTAKE + "(" + COLUMN_USER_ID + ", " + 
                        COLUMN_MEDICATION_NAME + ", " + COLUMN_PLANNED_TIME + ")");
                
                Log.d("DatabaseHelper", "创建新的多用户唯一索引成功");
                
                // 🔥 重要：清理可能存在的重复数据
                cleanDuplicateRecords(db);
                
            } 
            catch (Exception e) {
                Log.e("DatabaseHelper", "升级数据库到版本7失败", e);
            }
        }
        
        // 升级到版本8，添加既往病史表
        if (oldVersion < 8) {
            Log.d("DatabaseHelper", "开始升级数据库到版本8，添加既往病史表");
            try {
                db.execSQL(CREATE_MEDICAL_HISTORY_TABLE);
                Log.d("DatabaseHelper", "创建既往病史表成功");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "升级数据库到版本8失败", e);
            }
        }
        
        // 升级到版本9，添加习惯表
        if (oldVersion < 9) {
            Log.d("DatabaseHelper", "开始升级数据库到版本9，添加习惯表");
            try {
                db.execSQL(CREATE_HABITS_TABLE);
                Log.d("DatabaseHelper", "创建习惯表成功");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "升级数据库到版本9失败", e);
            }
        }
        
        // 升级到版本10，修复习惯表缺失字段
        if (oldVersion < 10) {
            Log.d("DatabaseHelper", "开始升级数据库到版本10，修复习惯表字段");
            try {
                // 删除旧的习惯表
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_HABITS);
                // 重新创建具有完整字段的习惯表
                db.execSQL(CREATE_HABITS_TABLE);
                Log.d("DatabaseHelper", "重新创建习惯表成功，包含所有必需字段");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "升级数据库到版本10失败", e);
            }
        }
    }
        /**
     * 清理重复的服药记录数据
     */
    private void cleanDuplicateRecords(SQLiteDatabase db) {
        try {
            // 查找所有重复记录（相同药物名称和时间，但user_id不同或为空）
            String findDuplicatesSql = "SELECT medication_name, planned_time, COUNT(*) as count " +
                    "FROM " + TABLE_MEDICATION_INTAKE + " " +
                    "GROUP BY medication_name, planned_time " +
                    "HAVING count > 1";
            
            Cursor cursor = db.rawQuery(findDuplicatesSql, null);
            
            if (cursor != null && cursor.moveToFirst()) {
                Log.d("DatabaseHelper", "发现重复数据，开始清理");
                
                do {
                    String medicationName = cursor.getString(0);
                    long plannedTime = cursor.getLong(1);
                    int count = cursor.getInt(2);
                    
                    Log.d("DatabaseHelper", "清理重复记录: " + medicationName + 
                          " 时间: " + plannedTime + " 重复数: " + count);
                    
                    // 删除user_id为空或为'0'的重复记录
                    int deletedRows = db.delete(TABLE_MEDICATION_INTAKE,
                            COLUMN_MEDICATION_NAME + " = ? AND " +
                            COLUMN_PLANNED_TIME + " = ? AND " +
                            "(" + COLUMN_USER_ID + " IS NULL OR " + 
                            COLUMN_USER_ID + " = '' OR " + 
                            COLUMN_USER_ID + " = '0')",
                            new String[]{medicationName, String.valueOf(plannedTime)});
                    
                    Log.d("DatabaseHelper", "删除了 " + deletedRows + " 条无效重复记录");
                    
                } while (cursor.moveToNext());
            }
            
            if (cursor != null) {
                cursor.close();
            }
            
            Log.d("DatabaseHelper", "重复数据清理完成");
            
        } catch (Exception e) {
            Log.e("DatabaseHelper", "清理重复数据失败", e);
        }
    
    }

}