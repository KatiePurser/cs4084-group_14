package com.example.fashionfriend.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface OutfitDao {
    @Query("SELECT * FROM outfits")
    List<Outfit> getAllOutfits();

    @Query("SELECT * FROM outfits WHERE id = :id")
    Outfit getOutfitById(long id);

    @Insert
    long insertOutfit(Outfit outfit);

    @Update
    int updateOutfit(Outfit outfit);

    @Query("DELETE FROM outfits WHERE id = :id")
    void deleteOutfit(long id);
}
