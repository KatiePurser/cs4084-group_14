package com.example.fashionfriend.addClothingItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fashionfriend.R;
import com.example.fashionfriend.viewAndEditClothingItem.ViewAndEditClothingItemActivity;
import com.example.fashionfriend.data.database.ClothingItem;

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

public class AddClothingItemActivity extends AppCompatActivity {

    private AddClothingItemViewModel addClothingItemViewModel;
    private ImageView imageView;
    private Uri selectedImageUri;
    private Button addItemButton;
    private EditText itemNameEditText;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<String[]> requestPermissionsLauncher;
    private boolean cameraPermissionGranted = false;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("imageUri", selectedImageUri); // Save the Uri
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            selectedImageUri = savedInstanceState.getParcelable("imageUri"); // Restore the Uri
            displayImage(selectedImageUri); // Redisplay the image
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_clothing_item);

        addClothingItemViewModel = new ViewModelProvider(this).get(AddClothingItemViewModel.class);
        addClothingItemViewModel.getInsertClothingItemStatus().observe(this, insertClothingItemStatus -> {
            if (insertClothingItemStatus instanceof AddClothingItemViewModel.InsertClothingItemStatus.Success) {
                long clothingItemId = ((AddClothingItemViewModel.InsertClothingItemStatus.Success) insertClothingItemStatus).itemId;
                navigateToViewAndEditClothingItemActivity(clothingItemId);
            } else if (insertClothingItemStatus instanceof AddClothingItemViewModel.InsertClothingItemStatus.Error) {
                String errorMessage = ((AddClothingItemViewModel.InsertClothingItemStatus.Error) insertClothingItemStatus).message;
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            } else if (insertClothingItemStatus instanceof AddClothingItemViewModel.InsertClothingItemStatus.Inserting) {
                Toast.makeText(this, "Saving clothing item...", Toast.LENGTH_SHORT).show();
            }
        });

        imageView = findViewById(R.id.clothingItemImageView);
        imageView.setOnClickListener(v -> openImagePicker());

        addItemButton = findViewById(R.id.addItemButton);
        itemNameEditText = findViewById(R.id.itemNameEditText);

        addItemButton.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select an image first.", Toast.LENGTH_SHORT).show();
            } else if (itemNameEditText.getText().toString().trim().isEmpty()) {
                itemNameEditText.setError("Please enter an item name.");
                itemNameEditText.setText("");
            }else {
                saveImageToPrivateStorageAndDatabase(selectedImageUri);
            }
        });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            selectedImageUri = data.getData();
                            displayImage(selectedImageUri);
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


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner spinner = findViewById(R.id.clothingItemTypeSpinner);

        if (spinner != null) {
            List<String> categories = loadCategoriesFromCSV("clothing_categories.csv");
            if (categories != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            } else {
                Log.e("AddClothingItemActivity", "Failed to load clothing categories from CSV.");
            }

        } else {
            Log.e("AddClothingItemActivity", "Spinner not found in the layout.");
        }
    }

    public void navigateToViewAndEditClothingItemActivity(long clothingItemId) {
        Intent intent = new Intent(this, ViewAndEditClothingItemActivity.class);
        intent.putExtra("clothingItemId", clothingItemId);
        startActivity(intent);
        finish();
    }

    private void saveImageToPrivateStorageAndDatabase(Uri imageUri) {
        String copiedImagePath = copyImageToPrivateStorage(this, imageUri);
        if (copiedImagePath != null) {
            Log.d("ImageStorage", "Image copied to: " + copiedImagePath);

            String itemName = itemNameEditText.getText().toString().trim();

            Spinner clothingItemTypeSpinner = findViewById(R.id.clothingItemTypeSpinner);
            String itemType = clothingItemTypeSpinner.getSelectedItem().toString();

            addClothingItemViewModel.insertClothingItem(new ClothingItem(itemName, itemType, copiedImagePath));

//            Toast.makeText(this, "Image with name " + itemName + " and type " + itemType + " saved to private storage: " + copiedImagePath, Toast.LENGTH_LONG).show();
        } else {
            Log.e("ImageStorage", "Failed to copy image to private storage.");
//            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    public static String copyImageToPrivateStorage(Context context, Uri sourceUri) {
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
                    .into(imageView);
        } else {
            Glide.with(this)
                    .load(R.drawable.ic_hanger)
                    .into(imageView);
        }
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionsLauncher.launch(new String[]{android.Manifest.permission.CAMERA});
        } else {
            cameraPermissionGranted = true;
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

    private void resetViews() {
        itemNameEditText.setText("");
    }
}