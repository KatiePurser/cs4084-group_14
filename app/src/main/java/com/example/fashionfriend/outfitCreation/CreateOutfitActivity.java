package com.example.fashionfriend.outfitCreation;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionfriend.BaseActivity;
import com.example.fashionfriend.home.MainActivity;
import com.example.fashionfriend.R;
import com.example.fashionfriend.data.database.ClothingItem;
import com.example.fashionfriend.data.database.FashionFriendDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

public class CreateOutfitActivity extends BaseActivity implements CategoryAdapter.OnItemSelectedListener {

    private static final String TAG = "CreateOutfitActivity";
    private static final int REQUEST_OUTFIT_IMAGE = 1001;

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private final HashMap<String, String> selectedItems = new HashMap<>();
    private HashMap<String, HashMap<String, Integer>> clothingItems = new HashMap<>();
    private HashMap<String, HashMap<String, String>> clothingItemPaths = new HashMap<>();
    private Button nextButton;
    private Button cancelButton;
    private LinearLayout previewContainer;
    private View previewScroll;
    private TextView noItemsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_outfit);

        setupToolbar();
        configureBackButton(true, () -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        applySystemBarInsets(R.id.create_outfit);

        // Initialize views
        recyclerView = findViewById(R.id.categories_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        nextButton = findViewById(R.id.next_button);
        cancelButton = findViewById(R.id.cancel_button);
        previewContainer = findViewById(R.id.preview_container);
        previewScroll = findViewById(R.id.preview_scroll);
        noItemsText = findViewById(R.id.no_items_text);

        loadClothingData();  // Load data from database

        // Next Button Listener
        nextButton.setOnClickListener(v -> {
            if (validateOutfitSelection()) {
                proceedToNextStep();
            } else {
                Toast.makeText(this, "Please select at least one clothing item.", Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel Button Listener
        cancelButton.setOnClickListener(v -> {
            finish();  // Closes the activity, returning to the previous one.
        });
    }

    private void setupRecyclerView() {
        List<String> categories = new ArrayList<>(clothingItems.keySet()); // Get categories from the HashMap
        adapter = new CategoryAdapter(this, categories, clothingItems, this); // Pass 'this' as the listener
        recyclerView.setAdapter(adapter);

        // Initialize selectedItems map to null
        for (String category : categories) {
            selectedItems.put(category, null);
        }
        
        // Set image paths in adapter
        if (adapter != null && !clothingItemPaths.isEmpty()) {
            adapter.setImagePaths(clothingItemPaths);
        }
    }

    // Implement the OnItemSelectedListener method
    @Override
    public void onItemSelected(String category, String selectedItem) {
        selectedItems.put(category, selectedItem); // Update the activity's tracking of selections
        updatePreview(); // Update the preview when an item is selected
    }

    private void updatePreview() {
        // Check if we have at least one item selected
        boolean hasSelection = false;
        for (String item : selectedItems.values()) {
            if (item != null) {
                hasSelection = true;
                break;
            }
        }

        // Show or hide the preview section based on selections
        if (hasSelection) {
            noItemsText.setVisibility(View.GONE);
            previewScroll.setVisibility(View.VISIBLE);
            
            // Clear previous preview
            previewContainer.removeAllViews();
            
            // Add selected items to preview
            for (String category : selectedItems.keySet()) {
                String item = selectedItems.get(category);
                if (item != null) {
                    // Create an ImageView for this item
                    ImageView itemPreview = new ImageView(this);
                    itemPreview.setLayoutParams(new LinearLayout.LayoutParams(
                            dpToPx(100), dpToPx(100)));
                    itemPreview.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
                    itemPreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    
                    // Try to load from path first
                    boolean imageLoaded = false;
                    if (clothingItemPaths.containsKey(category) && 
                        clothingItemPaths.get(category).containsKey(item)) {
                        
                        String imagePath = clothingItemPaths.get(category).get(item);
                        if (imagePath != null && !imagePath.isEmpty()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            if (bitmap != null) {
                                itemPreview.setImageBitmap(bitmap);
                                imageLoaded = true;
                            }
                        }
                    }
                    
                    // Fallback to placeholder if needed
                    if (!imageLoaded) {
                        Integer imageResource = clothingItems.get(category).get(item);
                        if (imageResource != null) {
                            itemPreview.setImageResource(imageResource);
                        }
                    }
                    
                    // Add to preview container
                    previewContainer.addView(itemPreview);
                }
            }
        } else {
            noItemsText.setVisibility(View.VISIBLE);
            previewScroll.setVisibility(View.GONE);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private boolean validateOutfitSelection() {
        // Check if at least one item is selected
        boolean hasSelection = false;
        for (String item : selectedItems.values()) {
            if (item != null) {
                hasSelection = true;
                break;
            }
        }
        
        return hasSelection;
    }

    private void proceedToNextStep() {
        try {
            // Convert selectedItems to JSON string for storage
            Gson gson = new Gson();
            String itemsJson = gson.toJson(selectedItems);
            Log.d(TAG, "Items JSON: " + itemsJson);
            
            // Create the intent with explicit component name to avoid ambiguity
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(this, OutfitImageActivity.class));
            intent.putExtra("OUTFIT_ITEMS_JSON", itemsJson);
            
            // Start the activity for result instead of just starting it
            startActivityForResult(intent, REQUEST_OUTFIT_IMAGE);
            Log.d(TAG, "Started OutfitImageActivity for result");
        } catch (Exception e) {
            Log.e(TAG, "Error proceeding to next step", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OUTFIT_IMAGE) {
            // The OutfitImageActivity has finished
            Log.d(TAG, "Returned from OutfitImageActivity with result: " + resultCode);
            
            if (resultCode == RESULT_OK) {
                // Outfit was successfully saved
                setResult(RESULT_OK);
                finish();
            }
            // If result is not OK, stay on this screen to let user try again
        }
    }

    private void loadClothingData() {
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
                List<ClothingItem> items = db.clothingItemDao().getAllClothingItems();
                Log.d(TAG, "Retrieved " + items.size() + " clothing items from database");
                
                // Organise by category
                HashMap<String, HashMap<String, Integer>> categoryItems = new HashMap<>();
                HashMap<String, HashMap<String, String>> categoryItemPaths = new HashMap<>();
                
                for (ClothingItem item : items) {
                    String category = item.getCategory();
                    if (!categoryItems.containsKey(category)) {
                        categoryItems.put(category, new HashMap<>());
                        categoryItemPaths.put(category, new HashMap<>());
                    }
                    
                    // For simplicity, using a placeholder image
                    categoryItems.get(category).put(item.getName(), R.drawable.ic_hanger);
                    // Store the actual image path
                    categoryItemPaths.get(category).put(item.getName(), item.getImagePath());
                }
                
                // If no items found, use default data
                if (categoryItems.isEmpty()) {
                    Log.d(TAG, "No items found in database, using default data");
                    categoryItems = getDefaultClothingItems();
                }
                
                final HashMap<String, HashMap<String, Integer>> resultItems = categoryItems;
                final HashMap<String, HashMap<String, String>> resultPaths = categoryItemPaths;
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    clothingItems = resultItems;
                    clothingItemPaths = resultPaths;  // Store the paths
                    setupRecyclerView();
                    
                    Log.d(TAG, "UI updated with clothing items");
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading clothing items from database", e);
                
                // On error, use default data
                runOnUiThread(() -> {
                    clothingItems = getDefaultClothingItems();
                    setupRecyclerView();
                    Toast.makeText(CreateOutfitActivity.this, 
                        "Error loading from database: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    // Default data in case the database is empty
    private HashMap<String, HashMap<String, Integer>> getDefaultClothingItems() {
        HashMap<String, HashMap<String, Integer>> clothingItems = new HashMap<>();
        
        HashMap<String, Integer> tops = new HashMap<>();
        tops.put("White T-Shirt", R.drawable.ic_hanger);
        tops.put("Striped Shirt", R.drawable.ic_hanger);
        tops.put("Hoodie", R.drawable.ic_hanger);
        clothingItems.put("Tops", tops);

        HashMap<String, Integer> bottoms = new HashMap<>();
        bottoms.put("Black Pants", R.drawable.ic_hanger);
        bottoms.put("Khaki Shorts", R.drawable.ic_hanger);
        bottoms.put("Denim Skirt", R.drawable.ic_hanger);
        clothingItems.put("Bottoms", bottoms);

        HashMap<String, Integer> shoes = new HashMap<>();
        shoes.put("Sneakers", R.drawable.ic_hanger);
        shoes.put("Sandals", R.drawable.ic_hanger);
        shoes.put("Boots", R.drawable.ic_hanger);
        clothingItems.put("Shoes", shoes);
        
        HashMap<String, Integer> accessories = new HashMap<>();
        accessories.put("Watch", R.drawable.ic_hanger);
        accessories.put("Necklace", R.drawable.ic_hanger);
        accessories.put("Sunglasses", R.drawable.ic_hanger);
        clothingItems.put("Accessories", accessories);
        
        return clothingItems;
    }
}
