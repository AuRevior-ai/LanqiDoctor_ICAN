package com.lanqiDoctor.demo.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.lanqiDoctor.demo.dao.ChatSessionDao;
import com.lanqiDoctor.demo.dao.converter.ChatMessageListConverter;
import com.lanqiDoctor.demo.entity.ChatSession;
import com.lanqiDoctor.demo.dao.MoodRecordDao;
import com.lanqiDoctor.demo.entity.MoodRecord;

/**
 * Room 数据库主类
 */
@Database(
    entities = {ChatSession.class, MoodRecord.class},
    version = 4,
    exportSchema = false
)
@TypeConverters({ChatMessageListConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static volatile AppDatabase INSTANCE;
    
    public abstract ChatSessionDao chatSessionDao();
    public abstract MoodRecordDao moodRecordDao();
    
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "lanqidoctor_database"
                    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `mood_record` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`recordDate` INTEGER NOT NULL, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL, " +
                    "`moodLevel` TEXT NOT NULL, " +
                    "`energyLevel` INTEGER NOT NULL, " +
                    "`stressLevel` INTEGER NOT NULL, " +
                    "`activities` TEXT, " +
                    "`note` TEXT, " +
                    "`favorite` INTEGER NOT NULL DEFAULT 0)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_mood_record_recordDate` ON `mood_record` (`recordDate`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_mood_record_favorite_recordDate` ON `mood_record` (`favorite`, `recordDate`)");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `mood_record` ADD COLUMN `imageUri` TEXT");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `mood_record` ADD COLUMN `imageUris` TEXT");
            database.execSQL("UPDATE `mood_record` SET `imageUris` = `imageUri` WHERE `imageUri` IS NOT NULL");
        }
    };
}
