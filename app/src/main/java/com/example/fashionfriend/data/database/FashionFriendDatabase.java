package com.example.fashionfriend.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ClothingItem.class, Outfit.class, Reminder.class}, version = 3, exportSchema = false)
public abstract class FashionFriendDatabase extends RoomDatabase {

    public abstract ClothingItemDao clothingItemDao();
    public abstract OutfitDao outfitDao();
    public abstract ReminderDao reminderDao();
    private static volatile FashionFriendDatabase INSTANCE;

    public static FashionFriendDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FashionFriendDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FashionFriendDatabase.class, "fashion_friend_database")
                            .fallbackToDestructiveMigration()
                            .createFromAsset("fashion_friend_database.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
