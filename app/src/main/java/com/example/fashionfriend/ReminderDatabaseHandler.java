package com.example.fashionfriend;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ReminderDatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fashion_friend.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_REMINDERS = "reminders";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_REMINDER = "reminder";

    private static final String CREATE_TABLE_REMINDERS =
            "CREATE TABLE " + TABLE_REMINDERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DATE + " TEXT UNIQUE, " + // One reminder per date
                    COLUMN_REMINDER + " TEXT)";

    public ReminderDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_REMINDERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        onCreate(db);
    }

    public void saveReminder(String date, String reminder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_REMINDER, reminder);

        long result = db.insertWithOnConflict(TABLE_REMINDERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public String getReminderForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_REMINDER + " FROM " + TABLE_REMINDERS + " WHERE " + COLUMN_DATE + "=?", new String[]{date});

        if (cursor.moveToFirst()) {
            String reminder = cursor.getString(0);
            cursor.close();
            return reminder;
        }
        cursor.close();
        return null;
    }

    public Cursor getAllReminders() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_REMINDERS + " ORDER BY " + COLUMN_DATE, null);
    }

    public void deleteReminder(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REMINDERS, COLUMN_DATE + "=?", new String[]{date});
        db.close();
    }
}
