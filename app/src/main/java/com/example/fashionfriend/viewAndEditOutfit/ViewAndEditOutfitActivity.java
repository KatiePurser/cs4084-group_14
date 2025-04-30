package com.example.fashionfriend.viewAndEditOutfit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.fashionfriend.BaseActivity;
import com.example.fashionfriend.R;
import com.example.fashionfriend.data.database.ClothingItem;
import com.example.fashionfriend.data.database.FashionFriendDatabase;
import com.example.fashionfriend.data.database.Outfit;
import com.example.fashionfriend.home.MainActivity;
import com.example.fashionfriend.outfitCreation.CreateOutfitActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewAndEditOutfitActivity extends BaseActivity {
    private static final String TAG = "ViewAndEditOutfitActivity";

    private ViewAndEditOutfitViewModel viewModel;
    private Outfit currentOutfit;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // UI Elements
    private TextView outfitNameTextView;
    private EditText outfitNameEditText;
    private ImageView outfitImageView;
    private Button editOutfitButton;
    private Button saveOutfitButton;
    private Button cancelEditButton;
    private Button deleteOutfitButton;
    private LinearLayout previewContainer;
    private View previewScroll;
    private TextView noItemsText;

    // For image selection
    private Uri selectedImageUri;
    private boolean newImageSelected = false;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    // For tracking selected items
    private HashMap<String, String> selectedItems = new HashMap<>();
    private HashMap<String, HashMap<String, String>> clothingItemPaths = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_and_edit_outfit);

        setupToolbar();
        configureBackButton(true, () -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ViewAndEditOutfitViewModel.class);
        // Find views
        outfitNameTextView = findViewById(R.id.outfit_name_text_view);
        outfitNameEditText = findViewById(R.id.outfit_name_edit_text);
        outfitImageView = findViewById(R.id.outfit_image_view);
        editOutfitButton = findViewById(R.id.edit_outfit_button);
        saveOutfitButton = findViewById(R.id.save_outfit_button);
        cancelEditButton = findViewById(R.id.cancel_edit_button);
        deleteOutfitButton = findViewById(R.id.delete_outfit_button);
        previewContainer = findViewById(R.id.preview_container);
        previewScroll = findViewById(R.id.preview_scroll);
        noItemsText = findViewById(R.id.no_items_text);


        // Load clothing data to populate clothingItemPaths
        loadClothingData();

        // Set up image picker
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        try {
                            selectedImageUri = result.getData().getData();
                            if (selectedImageUri != null) {
                                Glide.with(this).load(selectedImageUri).into(outfitImageView);
                                newImageSelected = true;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error loading image", e);
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Set up button listeners
        editOutfitButton.setOnClickListener(v -> toggleEditMode(true));
        saveOutfitButton.setOnClickListener(v -> saveChanges());
        cancelEditButton.setOnClickListener(v -> toggleEditMode(false));
        deleteOutfitButton.setOnClickListener(v -> confirmDelete());

        // Set up image click listener (only active in edit mode)
        outfitImageView.setOnClickListener(v -> {
            if (outfitNameEditText.getVisibility() == View.VISIBLE) {
                openImagePicker();
            }
        });

        // Get outfit ID from intent
        long outfitId = getIntent().getLongExtra("outfitId", -1);
        Log.d(TAG, "Received outfit ID: " + outfitId);
        if (outfitId != -1) {
            loadOutfit(outfitId);
        } else {
            Toast.makeText(this, "Error: No outfit ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Observe ViewModel data
        viewModel.getOutfit().observe(this, outfit -> {
            if (outfit != null) {
                currentOutfit = outfit;
                displayOutfit(outfit);
            }
        });

        viewModel.getUpdateOutfitStatus().observe(this, status -> {
            if (status instanceof ViewAndEditOutfitViewModel.UpdateOutfitStatus.Success) {
                Toast.makeText(this, "Outfit updated successfully", Toast.LENGTH_SHORT).show();
                toggleEditMode(false);
                viewModel.clearUpdateStatus();
            } else if (status instanceof ViewAndEditOutfitViewModel.UpdateOutfitStatus.Error) {
                String message = ((ViewAndEditOutfitViewModel.UpdateOutfitStatus.Error) status).message;
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                viewModel.clearUpdateStatus();
            } else if (status instanceof ViewAndEditOutfitViewModel.UpdateOutfitStatus.Deleted) {
                Toast.makeText(this, "Outfit deleted", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadClothingData() {
        // Try to load from database in background thread
        executorService.execute(() -> {
            try {
                // Get database instance
                FashionFriendDatabase db = FashionFriendDatabase.getDatabase(this);

                // Get all clothing items
                List<ClothingItem> items = db.clothingItemDao().getAllClothingItems();
                Log.d(TAG, "Loaded " + items.size() + " clothing items from database");

                // Organize by category
                HashMap<String, HashMap<String, String>> categoryItemPaths = new HashMap<>();

                for (ClothingItem item : items) {
                    String category = item.getCategory();
                    if (!categoryItemPaths.containsKey(category)) {
                        categoryItemPaths.put(category, new HashMap<>());
                    }

                    // Store the actual image path
                    categoryItemPaths.get(category).put(item.getName(), item.getImagePath());
                    Log.d(TAG, "Added item: " + item.getName() + " in category: " + category + " with path: " + item.getImagePath());
                }

                // Update UI on main thread
                runOnUiThread(() -> {
                    clothingItemPaths = categoryItemPaths;
                    // If we already have outfit data, update the preview
                    if (currentOutfit != null) {
                        updateItemsPreview();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading clothing items from database", e);
            }
        });
    }

    private void loadOutfit(long outfitId) {
        viewModel.getOutfitById(outfitId);
    }

    private void displayOutfit(Outfit outfit) {
        // Set name
        outfitNameTextView.setText(outfit.getName());
        outfitNameEditText.setText(outfit.getName());

        // Load image
        if (outfit.getImagePath() != null && !outfit.getImagePath().isEmpty()) {
            File imageFile = new File(outfit.getImagePath());
            if (imageFile.exists()) {
                Glide.with(this).load(imageFile).into(outfitImageView);
            } else {
                // Try to find the file by name in the files directory
                String fileName = imageFile.getName();
                File alternateFile = new File(getFilesDir(), fileName);

                if (alternateFile.exists()) {
                    Log.d(TAG, "Loading image from alternate path: " + alternateFile.getAbsolutePath());
                    Glide.with(this).load(alternateFile).into(outfitImageView);

                    // Update the path in the outfit object for future use
                    outfit.setImagePath(alternateFile.getAbsolutePath());
                    viewModel.updateOutfit(outfit);
                } else {
                    Log.e(TAG, "Image file not found: " + outfit.getImagePath());
                    // Load default image
                    Glide.with(this).load(R.drawable.ic_hanger).into(outfitImageView);
                }
            }
        } else {
            // Load default image
            Glide.with(this).load(R.drawable.ic_hanger).into(outfitImageView);
        }

        // Parse and display selected items
        if (outfit.getItemsJson() != null && !outfit.getItemsJson().isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String, String>>(){}.getType();
            selectedItems = gson.fromJson(outfit.getItemsJson(), type);
            Log.d(TAG, "Parsed items JSON: " + selectedItems.toString());
            updateItemsPreview();
        }
    }

    private void updateItemsPreview() {
        // Check if we have at least one item selected
        boolean hasSelection = false;
        for (String item : selectedItems.values()) {
            if (item != null) {
                hasSelection = true;
                break;
            }
        }

        Log.d(TAG, "updateItemsPreview - hasSelection: " + hasSelection);
        Log.d(TAG, "updateItemsPreview - clothingItemPaths size: " + clothingItemPaths.size());

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
                    Log.d(TAG, "Processing item: " + item + " in category: " + category);

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
                        Log.d(TAG, "Found image path: " + imagePath + " for item: " + item);

                        if (imagePath != null && !imagePath.isEmpty()) {
                            File imageFile = new File(imagePath);
                            if (imageFile.exists()) {
                                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                                if (bitmap != null) {
                                    itemPreview.setImageBitmap(bitmap);
                                    imageLoaded = true;
                                    Log.d(TAG, "Successfully loaded image for: " + item);
                                } else {
                                    Log.e(TAG, "Failed to decode bitmap for: " + imagePath);
                                }
                            } else {
                                // Try alternate path in files directory
                                String fileName = imageFile.getName();
                                File alternateFile = new File(getFilesDir(), fileName);

                                if (alternateFile.exists()) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(alternateFile.getAbsolutePath());
                                    if (bitmap != null) {
                                        itemPreview.setImageBitmap(bitmap);
                                        imageLoaded = true;
                                        Log.d(TAG, "Successfully loaded image from alternate path for: " + item);
                                    }
                                } else {
                                    Log.e(TAG, "Image file not found at either path for: " + item);
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "No path found for item: " + item + " in category: " + category);
                    }

                    // Fallback to placeholder if needed
                    if (!imageLoaded) {
                        Log.d(TAG, "Using placeholder image for: " + item);
                        itemPreview.setImageResource(R.drawable.ic_hanger);
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

    private void toggleEditMode(boolean editMode) {
        if (editMode) {
            // Switch to edit mode
            outfitNameTextView.setVisibility(View.GONE);
            outfitNameEditText.setVisibility(View.VISIBLE);

            // Hide outfit items section
            findViewById(R.id.items_title).setVisibility(View.GONE);
            findViewById(R.id.edit_items_button).setVisibility(View.GONE);
            findViewById(R.id.preview_card).setVisibility(View.GONE);

            editOutfitButton.setVisibility(View.GONE);
            saveOutfitButton.setVisibility(View.VISIBLE);
            cancelEditButton.setVisibility(View.VISIBLE);
            deleteOutfitButton.setVisibility(View.GONE); // Hide delete button in edit mode

            outfitImageView.setClickable(true);
            outfitImageView.setBackgroundColor(Color.LTGRAY);
        } else {
            // Switch to view mode
            outfitNameTextView.setVisibility(View.VISIBLE);
            outfitNameEditText.setVisibility(View.GONE);

            // Show outfit items section
            findViewById(R.id.items_title).setVisibility(View.VISIBLE);
            findViewById(R.id.edit_items_button).setVisibility(View.VISIBLE);
            findViewById(R.id.preview_card).setVisibility(View.VISIBLE);

            editOutfitButton.setVisibility(View.VISIBLE);
            saveOutfitButton.setVisibility(View.GONE);
            cancelEditButton.setVisibility(View.GONE);
            deleteOutfitButton.setVisibility(View.VISIBLE); // Show delete button in view mode

            outfitImageView.setClickable(false);
            outfitImageView.setBackgroundColor(Color.TRANSPARENT);

            // Reset to current outfit data
            if (currentOutfit != null) {
                displayOutfit(currentOutfit);
            }

            // Reset flags
            newImageSelected = false;
            selectedImageUri = null;
        }
    }



    private void saveChanges() {
        if (currentOutfit == null) {
            Toast.makeText(this, "Error: No outfit data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate input
        String outfitName = outfitNameEditText.getText().toString().trim();
        if (outfitName.isEmpty()) {
            outfitNameEditText.setError("Please enter an outfit name");
            return;
        }

        // Update outfit object
        currentOutfit.setName(outfitName);

        // Handle image if a new one was selected
        if (newImageSelected && selectedImageUri != null) {
            String previousImagePath = currentOutfit.getImagePath();

            // Save new image
            String newImagePath = saveImageToInternalStorage(selectedImageUri);
            if (newImagePath != null) {
                currentOutfit.setImagePath(newImagePath);

                // Delete old image if it exists
                if (previousImagePath != null && !previousImagePath.isEmpty()) {
                    File previousImageFile = new File(previousImagePath);
                    if (previousImageFile.exists() && previousImageFile.delete()) {
                        Log.d(TAG, "Deleted previous image: " + previousImagePath);
                    }
                }
            } else {
                Toast.makeText(this, "Failed to save new image", Toast.LENGTH_SHORT).show();
            }
        }

        // Save to database
        viewModel.updateOutfit(currentOutfit);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Outfit")
                .setMessage("Are you sure you want to delete this outfit?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (currentOutfit != null) {
                        // Delete the image file
                        if (currentOutfit.getImagePath() != null && !currentOutfit.getImagePath().isEmpty()) {
                            File imageFile = new File(currentOutfit.getImagePath());
                            if (imageFile.exists() && imageFile.delete()) {
                                Log.d(TAG, "Deleted outfit image: " + currentOutfit.getImagePath());
                            }
                        }

                        // Delete from database
                        viewModel.deleteOutfit(currentOutfit.getId());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            // Use the outfit name for the filename
            String outfitName = outfitNameEditText.getText().toString().trim();
            String fileName = outfitName.toLowerCase(Locale.getDefault()).replace(' ', '-') + ".jpg";

            // Save directly to files directory
            File file = new File(getFilesDir(), fileName);

            // Copy the image
            try (InputStream inputStream = getContentResolver().openInputStream(imageUri);
                 OutputStream outputStream = new FileOutputStream(file)) {

                if (inputStream != null) {
                    byte[] buffer = new byte[4 * 1024]; // 4KB buffer
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    outputStream.flush();
                    Log.d(TAG, "Saved image to: " + file.getAbsolutePath());
                    return file.getAbsolutePath();
                }
            }

            return null;
        } catch (IOException e) {
            Log.e(TAG, "Error saving image", e);
            return null;
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public void editOutfitItems(View view) {
        // Navigate to CreateOutfitActivity to edit the selected items
        if (currentOutfit != null) {
            Intent intent = new Intent(this, CreateOutfitActivity.class);
            intent.putExtra("EDIT_MODE", true);
            intent.putExtra("outfitId", currentOutfit.getId());
            intent.putExtra("OUTFIT_ITEMS_JSON", currentOutfit.getItemsJson());
            startActivityForResult(intent, 1001);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Refresh outfit data after editing items
            if (currentOutfit != null) {
                viewModel.getOutfitById(currentOutfit.getId());
                // Also reload clothing data to ensure we have the latest paths
                loadClothingData();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown the executor service
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
