package com.example.fashionfriend.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import android.database.Cursor;

import java.util.List;

@Dao
public interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Reminder reminder);

    @Query("SELECT reminder FROM reminders WHERE date = :date")
    String getReminderByDate(String date);

    @Query("SELECT * FROM reminders ORDER BY date")
    List<Reminder> getAllReminders();

    @Query("SELECT * FROM reminders ORDER BY date")
    Cursor getAllRemindersCursor(); // Optional if needed for legacy or adapters

    @Query("SELECT date FROM reminders ORDER BY date")
    List<String> getAllReminderDates();

    @Query("DELETE FROM reminders WHERE date = :date")
    void deleteReminder(String date);
}

