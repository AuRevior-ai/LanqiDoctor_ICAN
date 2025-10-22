package com.lanqiDoctor.demo.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.lanqiDoctor.demo.dao.ChatSessionDao;
import com.lanqiDoctor.demo.dao.converter.ChatMessageListConverter;
import com.lanqiDoctor.demo.entity.ChatSession;

/**
 * Room 数据库主类
 */
@Database(
    entities = {ChatSession.class},
    version = 1,
    exportSchema = false
)
@TypeConverters({ChatMessageListConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static volatile AppDatabase INSTANCE;
    
    public abstract ChatSessionDao chatSessionDao();
    
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "lanqidoctor_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
