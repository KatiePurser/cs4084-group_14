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
import com.example.fashionfriend.data.database.Outfit;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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
    private boolean editMode = false;
    private long outfitId = -1;

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

        //logging for intent extras
        Intent intent = getIntent();
        Log.d(TAG, "Intent extras: " + intent.getExtras());

        // Check if we're in edit mode
        editMode = getIntent().getBooleanExtra("EDIT_MODE", false);
        int outfitId = getIntent().getIntExtra("outfitId", -1);
        String outfitItemsJson = getIntent().getStringExtra("OUTFIT_ITEMS_JSON");

// Add logging to debug
        Log.d(TAG, "Edit mode: " + editMode);
        Log.d(TAG, "Outfit ID: " + outfitId);
        Log.d(TAG, "Outfit Items JSON: " + (outfitItemsJson != null ? outfitItemsJson : "null"));

        // If in edit mode, load the existing selections
        if (editMode && outfitItemsJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, String>>(){}.getType();
            HashMap<String, String> existingItems = gson.fromJson(outfitItemsJson, type);

            // Copy to our selectedItems
            for (String category : existingItems.keySet()) {
                selectedItems.put(category, existingItems.get(category));
            }

            // Update UI for edit mode
            TextView titleTextView = findViewById(R.id.create_outfit_title);
            if (titleTextView != null) {
                titleTextView.setText("Edit Outfit Items");
            }

            if (nextButton != null) {
                nextButton.setText("Save Changes");
            }
        }

        // Hide the menu icon in toolbar since we're using the cancel button
        ImageView menuIcon = findViewById(R.id.menu_icon);
        if (menuIcon != null) {
            menuIcon.setVisibility(View.GONE);
        }

        loadClothingData();  // Load data from database

        // Next Button Listener
        nextButton.setOnClickListener(v -> {
            if (validateOutfitSelection()) {
                if (editMode) {
                    // In edit mode, just save the changes and return
                    Log.d(TAG, "Next button clicked in edit mode. Outfit ID: " + outfitId);
                    saveOutfitChanges(outfitId);
                } else {
                    // In create mode, proceed to next step
                    proceedToNextStep();
                }
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

        // Initialize selectedItems map to null for categories without selections
        for (String category : categories) {
            if (!selectedItems.containsKey(category)) {
                selectedItems.put(category, null);
            }
        }

        // Set image paths in adapter
        if (adapter != null && !clothingItemPaths.isEmpty()) {
            adapter.setImagePaths(clothingItemPaths);
        }

        // Set pre-selected items in adapter
        if (adapter != null && !selectedItems.isEmpty()) {
            adapter.setSelectedItems(selectedItems);
        }

        // Update the preview with existing selections
        updatePreview();
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

    private void saveOutfitChanges(long outfitId) {
        Log.d(TAG, "saveOutfitChanges called with outfitId: " + outfitId);

        if (outfitId == -1) {
            Log.e(TAG, "Invalid outfit ID (-1). Cannot save changes.");
            Toast.makeText(this, "Error: Invalid outfit ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert selectedItems to JSON string for storage
        Gson gson = new Gson();
        String itemsJson = gson.toJson(selectedItems);
        Log.d(TAG, "Items JSON for update: " + itemsJson);

        // Show progress indicator
        Toast.makeText(this, "Saving changes...", Toast.LENGTH_SHORT).show();

        // Update the outfit in the database
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                FashionFriendDatabase db = FashionFriendDatabase.getDatabase(this);

                // First verify the outfit exists
                Outfit outfit = db.outfitDao().getOutfitById(outfitId);

                if (outfit == null) {
                    Log.e(TAG, "Outfit with ID " + outfitId + " not found in database");
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error: Outfit not found", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                Log.d(TAG, "Found outfit: " + outfit.getName() + " with ID: " + outfit.getId());

                // Update the outfit
                outfit.setItemsJson(itemsJson);
                int result = db.outfitDao().updateOutfit(outfit);

                Log.d(TAG, "Update result: " + result + " rows affected");

                runOnUiThread(() -> {
                    if (result > 0) {
                        Toast.makeText(this, "Outfit items updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update outfit items", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error updating outfit: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
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

                // If no items found, show a message instead of using default data
                final HashMap<String, HashMap<String, Integer>> resultItems = categoryItems;
                final HashMap<String, HashMap<String, String>> resultPaths = categoryItemPaths;

                // Update UI on main thread
                runOnUiThread(() -> {
                    clothingItems = resultItems;
                    clothingItemPaths = resultPaths;  // Store the paths
                    setupRecyclerView();

                    // Show message if no items found
                    if (clothingItems.isEmpty()) {
                        Toast.makeText(CreateOutfitActivity.this,
                                "No clothing items found. Please add some items first.",
                                Toast.LENGTH_LONG).show();
                    }

                    Log.d(TAG, "UI updated with clothing items");
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading clothing items from database", e);

                // On error, show error message but don't use default data
                runOnUiThread(() -> {
                    clothingItems = new HashMap<>(); // Empty map instead of default data
                    setupRecyclerView();
                    Toast.makeText(CreateOutfitActivity.this,
                            "Error loading from database: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}