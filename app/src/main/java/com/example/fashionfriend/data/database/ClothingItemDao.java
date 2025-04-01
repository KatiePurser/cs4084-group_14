package com.example.fashionfriend.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ClothingItemDao {
    @Query("SELECT * FROM clothing_items")
    List<ClothingItem> getAllClothingItems();

    @Query("SELECT * FROM clothing_items WHERE id = :id")
    ClothingItem getClothingItemById(long id);

    @Insert
    long insertClothingItem(ClothingItem clothingItem);

    @Update
    int updateClothingItem(ClothingItem clothingItem);
}
