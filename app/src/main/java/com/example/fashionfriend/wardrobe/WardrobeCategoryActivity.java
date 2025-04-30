
package com.example.fashionfriend.wardrobe;

import static java.lang.Character.toUpperCase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionfriend.BaseActivity;
import com.example.fashionfriend.R;
import com.example.fashionfriend.addClothingItem.AddClothingItemActivity;
import com.example.fashionfriend.data.database.ClothingItem;
import com.example.fashionfriend.data.database.FashionFriendDatabase;
import com.example.fashionfriend.data.database.Outfit;
import com.example.fashionfriend.outfitCreation.CategoryAdapter;
import com.example.fashionfriend.outfitCreation.CreateOutfitActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

public class WardrobeCategoryActivity extends BaseActivity {

    private static final String TAG = "WardrobeActivity";
    private List<ClothingItem> clothingItems = new ArrayList<>();
    private List<Outfit> outfitsList = new ArrayList<>();
    private WardrobeAdapter adapter;
    private WardrobeOutfitAdapter outfitAdapter;
    private RecyclerView recyclerView;
    private String category;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wardrobe_category);
        recyclerView = findViewById(R.id.recycler_view);

        Intent i = getIntent();
        category = i.getStringExtra("type");

        TextView title = findViewById(R.id.category_title);
        title.setText(category.toUpperCase());

        ImageButton add = findViewById(R.id.add_button);
        setButtonClickListener(add, category);

        setupToolbar();
        configureBackButton(true, () -> {
            Intent intent = new Intent(this, WardrobeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        applySystemBarInsets(R.id.create_outfit);

        if (category.equals("Outfits")) {
            //add appropriate text to add button
            TextView add_text = findViewById(R.id.add_button_text);
            add_text.setText("Add New Outfit");
            //retrieve outfits from the DB
            loadOutfitData();
        } else {
            //add appropriate text to add button
            TextView add_text = findViewById(R.id.add_button_text);
            add_text.setText("Add New Clothing Item");
            //retrieve all clothing items in the category from the DB
            loadClothingData(category);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (category.equals("Outfits")) {
            loadOutfitData();
        } else {
            loadClothingData(category);
        }
    }

    private void setButtonClickListener(ImageButton button, String category) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = button.getId();
                // Handle button clicks based on their ID
                if (id == R.id.add_button) {
                    if (category.equals("Outfits")) {
                        Intent i = new Intent(WardrobeCategoryActivity.this, CreateOutfitActivity.class);
                        startActivity(i);
                    } else {
                        Intent i = new Intent(WardrobeCategoryActivity.this, AddClothingItemActivity.class);
                        i.putExtra("category", category);
                        startActivity(i);
                    }
                }
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new WardrobeAdapter(clothingItems, this); // Pass 'this' as the listener
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void loadClothingData(String category) {
        // Show loading indicator
        Toast.makeText(this, "Loading clothing items...", Toast.LENGTH_SHORT).show();

        // Try to load from database in background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Log that we're attempting to access the database
                Log.d(TAG, "Attempting to access database");

                // Get database instance
                FashionFriendDatabase db = FashionFriendDatabase.getDatabase(this);
                Log.d(TAG, "Database instance obtained");

                // Get all clothing items
                List<ClothingItem> items = db.clothingItemDao().getClothingItemsByCategory(category);
                Log.d(TAG, "Retrieved " + items.size() + " clothing items from database");


                // Update UI on main thread
                runOnUiThread(() -> {
                    clothingItems = items;
//                    setupRecyclerView();
                    adapter = new WardrobeAdapter(clothingItems, this);
                    GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(adapter);

                    Log.d(TAG, "UI updated with clothing items");
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading clothing items from database", e);

                // On error, use default data
                runOnUiThread(() -> {
//                    clothingItems = getDefaultClothingItems();
                    setupRecyclerView();
                    Toast.makeText(WardrobeCategoryActivity.this,
                            "Error loading from database: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void loadOutfitData() {
        // Show loading indicator
        Toast.makeText(this, "Loading outfits...", Toast.LENGTH_SHORT).show();

        // Try to load from database in background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Log that we're attempting to access the database
                Log.d(TAG, "Attempting to access database");

                // Get database instance
                FashionFriendDatabase db = FashionFriendDatabase.getDatabase(this);
                Log.d(TAG, "Database instance obtained");

                // Get all clothing items
                List<Outfit> outfits = db.outfitDao().getAllOutfits();
                Log.d(TAG, "Retrieved " + outfits.size() + " clothing items from database");


                // Update UI on main thread
                runOnUiThread(() -> {
                    outfitsList = outfits;
                    Log.d(TAG, outfitsList.size() + " items in outfitsList");
//                    setupRecyclerView();
                    outfitAdapter = new WardrobeOutfitAdapter(outfitsList, this);
                    GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(outfitAdapter);

                    Log.d(TAG, "UI updated with clothing items");
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading clothing items from database", e);

                // On error, use default data
                runOnUiThread(() -> {
//                    clothingItems = getDefaultClothingItems();
                    setupRecyclerView();
                    Toast.makeText(WardrobeCategoryActivity.this,
                            "Error loading from database: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}