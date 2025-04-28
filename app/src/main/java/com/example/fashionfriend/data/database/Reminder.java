package com.example.fashionfriend.data.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders")
public class Reminder {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String date;
    public String reminder;

    public Reminder(String date, String reminder) {
        this.date = date;
        this.reminder = reminder;
    }
}

