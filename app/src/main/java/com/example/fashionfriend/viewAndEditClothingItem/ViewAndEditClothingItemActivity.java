package com.example.fashionfriend.viewAndEditClothingItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fashionfriend.R;
import com.example.fashionfriend.data.database.ClothingItem;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ViewAndEditClothingItemActivity extends AppCompatActivity {

    private ArrayAdapter<String> clothingItemCategorySpinnerAdapter;
    private TextView clothingItemNameTextView;
    private EditText clothingItemNameEditText;

    private TextView clothingItemCategoryTextView;
    private Spinner clothingItemCategorySpinner;

    private ImageView clothingItemImageView;
    private Button editItemButton;
    private Button saveItemButton;
    private Button cancelEditButton;
    private boolean newImageSelected = true;
    private boolean cameraPermissionGranted = false;
    private ActivityResultLauncher<String[]> requestPermissionsLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Uri selectedImageUri;

    private ClothingItem clothingItem;

    private ViewAndEditClothingItemViewModel viewAndEditClothingItemViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_and_edit_clothing_item);

        clothingItemNameTextView = findViewById(R.id.clothingItemNameTextView);
        clothingItemNameEditText = findViewById(R.id.clothingItemNameEditText);

        clothingItemCategoryTextView = findViewById(R.id.clothingItemCategoryTextView);

        clothingItemCategorySpinner = findViewById(R.id.clothingItemTypeSpinner);
        populateClothingItemCategorySpinner(clothingItemCategorySpinner);

        clothingItemImageView = findViewById(R.id.clothingItemImageView);

        clothingItemImageView.setOnClickListener(v -> openImagePicker());
        // setOnClickListener makes the clothingItemImageView clickable by default, so change it to false immediately
        clothingItemImageView.setClickable(false);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            selectedImageUri = data.getData();
                            displayImage(selectedImageUri);
                            newImageSelected = true;
                        } else {
                            Log.e("ImagePicker", "No image selected");
                        }
                    } else {
                        Log.e("ImagePicker", "Image picking cancelled or failed.");
                    }
                }
        );

        requestPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                isGranted -> {
                    cameraPermissionGranted = Boolean.TRUE.equals(isGranted.getOrDefault(android.Manifest.permission.CAMERA, false));
                });
        requestCameraPermission();


        editItemButton = findViewById(R.id.EditItemButton);
        saveItemButton = findViewById(R.id.SaveItemButton);
        cancelEditButton = findViewById(R.id.CancelEditButton);


        viewAndEditClothingItemViewModel = new ViewModelProvider(this).get(ViewAndEditClothingItemViewModel.class);

        Intent intent = getIntent();
        long clothingItemId = intent.getLongExtra("clothingItemId", -1);

        if (clothingItemId != -1) {

            viewAndEditClothingItemViewModel.getClothingItem().observe(this, clothingItem -> {
                if (clothingItem != null) {
                    ViewAndEditClothingItemActivity.this.clothingItem = clothingItem;
                    setClothingItemImage(clothingItem.getImagePath());
                    setClothingItemTextViews(clothingItem);
                    clothingItemCategorySpinner.setSelection(clothingItemCategorySpinnerAdapter.getPosition(clothingItem.getCategory()));
                }
            });
            viewAndEditClothingItemViewModel.getUpdateClothingItemStatus().observe(this, updateClothingItemStatus -> {
                if (updateClothingItemStatus instanceof ViewAndEditClothingItemViewModel.UpdateClothingItemStatus.Success) {
                    Snackbar.make(findViewById(android.R.id.content), "Clothing item updated successfully!", Snackbar.LENGTH_SHORT).show();
                    viewAndEditClothingItemViewModel.clearUpdateStatus();
                } else if (updateClothingItemStatus instanceof ViewAndEditClothingItemViewModel.UpdateClothingItemStatus.Error) {
                    String errorMessage = ((ViewAndEditClothingItemViewModel.UpdateClothingItemStatus.Error) updateClothingItemStatus).message;
                    Snackbar.make(findViewById(android.R.id.content), "Error updating item: " + errorMessage, Snackbar.LENGTH_LONG).show();
                } else if (updateClothingItemStatus instanceof ViewAndEditClothingItemViewModel.UpdateClothingItemStatus.Updating) {
                    Toast.makeText(this, "Saving changes...", Toast.LENGTH_SHORT).show();
                }
            });

            viewAndEditClothingItemViewModel.getClothingItemById(clothingItemId);

        }
        editItemButton.setOnClickListener(v -> toggleEditMode());
        saveItemButton.setOnClickListener(v -> saveChanges());
        cancelEditButton.setOnClickListener(v -> exitEditMode());
    }

    private void saveChanges() {
        if (clothingItem == null) {
            Toast.makeText(this, "Clothing item not found", Toast.LENGTH_SHORT).show();
        }

        if (clothingItemNameEditText.getText().toString().trim().isEmpty()) {
            clothingItemNameEditText.setError("Item name cannot be empty.");
            clothingItemNameEditText.setText("");
            return;
        }

        if (newImageSelected && selectedImageUri != null) {

            String previousImagePath = clothingItem.getImagePath();
            String imagePath = saveImageToPrivateStorage(this, selectedImageUri);
            if (imagePath != null) {
                clothingItem.setImagePath(imagePath);
            }
            File previousImageFile = new File(previousImagePath);
            if (previousImageFile.delete()) {
                Log.d("saveChanges", "Deleted previous image for clothing item with ID: " + clothingItem.getId() + " at path: " + previousImagePath);
            } else {
                Log.d("saveChanges", "Failed to delete previous image for clothing item with ID: " + clothingItem.getId() + " at path: " + previousImagePath);
            }
        }

        clothingItem.setName(clothingItemNameEditText.getText().toString());
        clothingItem.setCategory(clothingItemCategorySpinner.getSelectedItem().toString());

        viewAndEditClothingItemViewModel.updateClothingItem(clothingItem);
        newImageSelected = false;
        exitEditMode();

    }
    private void exitEditMode() {
        clothingItemNameTextView.setText(clothingItem.getName());
        clothingItemCategoryTextView.setText(clothingItem.getCategory());
        setClothingItemImage(clothingItem.getImagePath());

        clothingItemNameEditText.setText(clothingItem.getName());
        clothingItemNameEditText.setError(null);
        clothingItemCategorySpinner.setSelection(clothingItemCategorySpinnerAdapter.getPosition(clothingItem.getCategory()));

        clothingItemImageView.setClickable(false);
        clothingItemImageView.setBackgroundColor(Color.TRANSPARENT);

        clothingItemNameTextView.setVisibility(View.VISIBLE);
        clothingItemNameEditText.setVisibility(View.GONE);

        clothingItemCategoryTextView.setVisibility(View.VISIBLE);
        clothingItemCategorySpinner.setVisibility(View.GONE);

        editItemButton.setVisibility(View.VISIBLE);
        saveItemButton.setVisibility(View.GONE);
        cancelEditButton.setVisibility(View.GONE);
    }
    private void toggleEditMode() {
        clothingItemImageView.setClickable(true);
        clothingItemImageView.setBackgroundColor(Color.LTGRAY);

        clothingItemNameTextView.setVisibility(View.GONE);
        clothingItemNameEditText.setVisibility(View.VISIBLE);
        clothingItemNameEditText.setText(clothingItemNameTextView.getText()); // Copy text to EditText

        clothingItemCategoryTextView.setVisibility(View.GONE);
        clothingItemCategorySpinner.setVisibility(View.VISIBLE);

        editItemButton.setVisibility(View.GONE);
        saveItemButton.setVisibility(View.VISIBLE);
        cancelEditButton.setVisibility(View.VISIBLE);
//            itemNameEditText.requestFocus(); // Optional: Put focus on EditText
    }

    private void setClothingItemTextViews(ClothingItem clothingItem) {
        clothingItemNameTextView.setText(clothingItem.getName());
        clothingItemCategoryTextView.setText(clothingItem.getCategory());

    }

    private void populateClothingItemCategorySpinner(Spinner spinner) {
        if (spinner != null) {
            List<String> categories = loadCategoriesFromCSV("clothing_categories.csv");
            if (categories != null) {
                clothingItemCategorySpinnerAdapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories
                );
                clothingItemCategorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(clothingItemCategorySpinnerAdapter);
            } else {
                Log.e("AddClothingItemActivity", "Failed to load clothing categories from CSV.");
            }

        } else {
            Log.e("AddClothingItemActivity", "Spinner not found in the layout.");
        }
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
            Log.e("AddClothingItemActivity", "Error reading CSV: " + e.getMessage());
            return null; // Indicate failure
        }
    }

    private void openImagePicker() {
        List<Intent> intents = new ArrayList<>();

        // Intent for picking an image from the gallery
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intents.add(pickIntent);

        // Intent for taking a picture with the camera (if permission is granted)
        if (cameraPermissionGranted) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intents.add(takePictureIntent);
        }

        // Create a chooser intent to let the user select
        Intent chooserIntent;
        if (intents.size() == 1) {
            chooserIntent = intents.get(0);
        } else {
            chooserIntent = Intent.createChooser(pickIntent, "Select Image");
            chooserIntent.putExtra(
                    Intent.EXTRA_INITIAL_INTENTS,
                    intents.subList(1, intents.size()).toArray(new Intent[0])
            );
        }
        pickImageLauncher.launch(chooserIntent);
    }

    private void displayImage(Uri imageUri) {
        if (imageUri != null) {
            Glide.with(this)
                    .load(imageUri)
                    .into(clothingItemImageView);
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_hanger)
                    .into(clothingItemImageView);
        }
    }

    private void setClothingItemImage(String imagePath) {
        File imageFile = new File(imagePath);
        Glide.with(this).load(imageFile).into(clothingItemImageView);
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionsLauncher.launch(new String[]{android.Manifest.permission.CAMERA});
        } else {
            cameraPermissionGranted = true;
        }
    }

    public static String saveImageToPrivateStorage(Context context, Uri sourceUri) {
        String filename = "image_" + UUID.randomUUID().toString() + ".jpg"; // Unique filename
        File destinationFile = new File(context.getFilesDir(), filename); // Internal storage
        try (InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
             OutputStream outputStream = new FileOutputStream(destinationFile)) {
            if (inputStream != null) {
                byte[] buffer = new byte[4 * 1024]; // 4KB buffer
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
                return destinationFile.getAbsolutePath();
            } else {
                Log.e("ImageHelper", "InputStream is null for URI: " + sourceUri);
                return null;
            }
        } catch (IOException e) {
            Log.e("ImageHelper", "Error copying image: " + e.getMessage());
            return null;
        }
    }
}
