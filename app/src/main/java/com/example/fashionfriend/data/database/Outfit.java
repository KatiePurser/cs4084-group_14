package com.example.fashionfriend.data.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "outfits")
public class Outfit {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "image_path")
    private String imagePath;

    // Store selected items as a JSON string
    @ColumnInfo(name = "items_json")
    private String itemsJson;

    public Outfit() {
    }

    public Outfit(String name, String imagePath, String itemsJson) {
        this.name = name;
        this.imagePath = imagePath;
        this.itemsJson = itemsJson;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getItemsJson() {
        return itemsJson;
    }

    public void setItemsJson(String itemsJson) {
        this.itemsJson = itemsJson;
    }
}

