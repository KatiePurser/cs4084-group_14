package com.example.fashionfriend.outfitCreation;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fashionfriend.BaseActivity;
import com.example.fashionfriend.R;
import com.example.fashionfriend.data.database.ClothingItem;
import com.example.fashionfriend.data.database.FashionFriendDatabase;
import com.example.fashionfriend.data.database.Outfit;
import com.example.fashionfriend.home.MainActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

public class OutfitImageActivity extends BaseActivity {

    private static final String TAG = "OutfitImageActivity";

    private ImageView outfitImageView;
    private Button saveButton;
    private EditText outfitNameEditText;
    private Bitmap selectedImageBitmap;
    private String itemsJson;
    private HashMap<String, String> selectedItems;
    private LinearLayout previewContainer;
    private View previewScroll;
    private TextView noItemsText;
    private HashMap<String, HashMap<String, String>> clothingItemPaths = new HashMap<>();
    private HashMap<String, HashMap<String, Integer>> clothingItems = new HashMap<>();

    // Register the activity result launcher for picking images
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            try {
                                selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                                        this.getContentResolver(),
                                        result.getData().getData());
                                outfitImageView.setImageBitmap(selectedImageBitmap);
                                saveButton.setEnabled(true);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_image);

        setupToolbar();
        configureBackButton(true);

        applySystemBarInsets(R.id.outfit_image);

        // Get outfit data from intent
        itemsJson = getIntent().getStringExtra("OUTFIT_ITEMS_JSON");

        if (itemsJson == null) {
            itemsJson = "{}";
        }

        // Parse the JSON to get selected items
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, String>>(){}.getType();
        selectedItems = gson.fromJson(itemsJson, type);

        // Initialize views
        outfitImageView = findViewById(R.id.outfit_image_view);
        Button selectImageButton = findViewById(R.id.select_image_button);
        saveButton = findViewById(R.id.save_button);
        outfitNameEditText = findViewById(R.id.outfit_name_edit_text);
        previewContainer = findViewById(R.id.preview_container);
        previewScroll = findViewById(R.id.preview_scroll);
        noItemsText = findViewById(R.id.no_items_text);

        // Disable save button until image is selected
        saveButton.setEnabled(false);

        // Set up select image button
        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        });

        // Set up save button
        saveButton.setOnClickListener(v -> {
            if (validateInput()) {
                saveOutfitWithImage();
            }
        });

        // Load clothing data to show preview
        loadClothingData();
    }

    private boolean validateInput() {
        String outfitName = outfitNameEditText.getText().toString().trim();
        if (outfitName.isEmpty()) {
            outfitNameEditText.setError("Please enter an outfit name");
            return false;
        }

        if (selectedImageBitmap == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveOutfitWithImage() {
        // Show loading indicator
        Toast.makeText(this, "Saving outfit...", Toast.LENGTH_SHORT).show();
        saveButton.setEnabled(false);

        // Get outfit name
        String outfitName = outfitNameEditText.getText().toString().trim();

        // Save image to internal storage and get path
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Save image to file
                String imagePath = saveImageToInternalStorage(selectedImageBitmap);

                // Create outfit object
                Outfit outfit = new Outfit(outfitName, imagePath, itemsJson);

                // Save to database
                FashionFriendDatabase db = FashionFriendDatabase.getDatabase(this);
                long outfitId = db.outfitDao().insertOutfit(outfit);

                Log.d(TAG, "Saved outfit with ID: " + outfitId);

                // Update UI on main thread
                runOnUiThread(() -> {
                    if (outfitId > 0) {
                        Toast.makeText(OutfitImageActivity.this,
                                "Outfit saved successfully!", Toast.LENGTH_SHORT).show();

                        // Return to main activity with success result
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(OutfitImageActivity.this,
                                "Error saving outfit", Toast.LENGTH_SHORT).show();
                        saveButton.setEnabled(true);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error saving outfit", e);

                // Update UI on main thread
                runOnUiThread(() -> {
                    Toast.makeText(OutfitImageActivity.this,
                            "Error saving outfit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveButton.setEnabled(true);
                });
            }
        });
    }

    private String saveImageToInternalStorage(Bitmap bitmap) {
        try {
            // Use a simple filename based on the outfit name instead of a timestamp
            String outfitName = outfitNameEditText.getText().toString().trim();
            // Convert spaces to hyphens and make lowercase for filename
            String fileName = outfitName.toLowerCase().replace(' ', '-') + ".jpg";

            // Save directly to the files directory (not in a subdirectory)
            File file = new File(getFilesDir(), fileName);

            // Save the image
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            Log.d(TAG, "Saved image to: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadClothingData() {
        // Try to load from database in background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Load categories from CSV first
                List<String> allCategories = loadCategoriesFromCSV("clothing_categories.csv");

                // Get database instance
                FashionFriendDatabase db = FashionFriendDatabase.getDatabase(this);

                // Get all clothing items
                List<ClothingItem> items = db.clothingItemDao().getAllClothingItems();

                // Organize by category
                HashMap<String, HashMap<String, Integer>> categoryItems = new HashMap<>();
                HashMap<String, HashMap<String, String>> categoryItemPaths = new HashMap<>();

                // Initialize all categories from CSV
                for (String category : allCategories) {
                    categoryItems.put(category, new HashMap<>());
                    categoryItemPaths.put(category, new HashMap<>());
                }

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
                if (items.isEmpty()) {
                    categoryItems = getDefaultClothingItems();
                }

                final HashMap<String, HashMap<String, Integer>> resultItems = categoryItems;
                final HashMap<String, HashMap<String, String>> resultPaths = categoryItemPaths;

                // Update UI on main thread
                runOnUiThread(() -> {
                    clothingItems = resultItems;
                    clothingItemPaths = resultPaths;
                    updatePreview();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading clothing items from database", e);

                // On error, use default data
                runOnUiThread(() -> {
                    clothingItems = getDefaultClothingItems();
                    updatePreview();
                });
            }
        });
    }

    private List<String> loadCategoriesFromCSV(String filename) {
        List<String> categories = new ArrayList<>();
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                categories.add(line.trim()); // Add each line (category)
            }
            reader.close();
            inputStream.close();
            return categories;
        } catch (IOException e) {
            Log.e(TAG, "Error reading CSV: " + e.getMessage());
            return new ArrayList<>(); // Return empty list on failure
        }
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
                        Integer imageResource = clothingItems.get(category) != null ?
                                clothingItems.get(category).get(item) : null;
                        if (imageResource != null) {
                            itemPreview.setImageResource(imageResource);
                        } else {
                            itemPreview.setImageResource(R.drawable.ic_hanger);
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

        return clothingItems;
    }

    @Override
    protected boolean shouldRestartOnResume() {
        return false;
    }
}