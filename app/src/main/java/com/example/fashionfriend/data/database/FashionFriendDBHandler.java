package com.example.fashionfriend.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FashionFriendDBHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fashion_friend.db";
    private static final int DATABASE_VERSION = 1;

    public FashionFriendDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String CREATE_TABLES =
            "CREATE TABLE clothing_items (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "category TEXT NOT NULL, " +
            "image_uri TEXT NOT NULL" +
            ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
