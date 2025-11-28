package com.lanqiDoctor.demo.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lanqiDoctor.demo.entity.MoodRecord;

import java.util.List;

/**
 * 心情记录数据访问对象
 */
@Dao
public interface MoodRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRecord(MoodRecord record);

    @Update
    void updateRecord(MoodRecord record);

    @Delete
    void deleteRecord(MoodRecord record);

    @Query("SELECT * FROM mood_record WHERE id = :id")
    MoodRecord getRecordById(long id);

    @Query("SELECT * FROM mood_record ORDER BY recordDate DESC, createdAt DESC")
    List<MoodRecord> getAllRecords();

    @Query("SELECT * FROM mood_record WHERE recordDate >= :startOfDay AND recordDate < :endOfDay ORDER BY createdAt DESC")
    List<MoodRecord> getRecordsForDay(long startOfDay, long endOfDay);

    @Query("SELECT * FROM mood_record WHERE recordDate BETWEEN :start AND :end ORDER BY recordDate DESC, createdAt DESC")
    List<MoodRecord> getRecordsBetween(long start, long end);

    @Query("SELECT * FROM mood_record WHERE favorite = 1 OR (note IS NOT NULL AND note != '') ORDER BY recordDate DESC, createdAt DESC")
    List<MoodRecord> getMemoryRecords();

    @Query("SELECT COUNT(*) FROM mood_record WHERE recordDate BETWEEN :start AND :end")
    int getRecordCountBetween(long start, long end);

    @Query("SELECT COUNT(*) FROM mood_record WHERE favorite = 1")
    int getFavoriteCount();

    @Query("SELECT COUNT(*) FROM mood_record WHERE moodLevel = :moodLevel AND recordDate BETWEEN :start AND :end")
    int getMoodCountInRange(String moodLevel, long start, long end);

    @Query("SELECT AVG(energyLevel) FROM mood_record WHERE recordDate BETWEEN :start AND :end")
    Float getAverageEnergy(long start, long end);

    @Query("SELECT AVG(stressLevel) FROM mood_record WHERE recordDate BETWEEN :start AND :end")
    Float getAverageStress(long start, long end);

    @Query("SELECT recordDate FROM mood_record ORDER BY recordDate DESC")
    List<Long> getAllRecordDatesDesc();

    @Query("SELECT MIN(recordDate) FROM mood_record")
    Long getFirstRecordDate();
}
