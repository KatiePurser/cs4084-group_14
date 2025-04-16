package com.example.fashionfriend.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ClothingItem.class}, version = 1)
public abstract class FashionFriendDatabase extends RoomDatabase {

    public abstract ClothingItemDao clothingItemDao();
    private static volatile FashionFriendDatabase INSTANCE;

    public static FashionFriendDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FashionFriendDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FashionFriendDatabase.class, "fashion_friend_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
