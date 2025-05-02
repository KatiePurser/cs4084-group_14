package com.example.fashionfriend.home;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GalleryHelper {

    public static void loadImagesToGallery(Context context) {
        try {
            String folderPath = "images";
            String[] imageFiles = context.getAssets().list(folderPath);

            if (imageFiles != null) {
                for (String fileName : imageFiles) {
                    String displayName = fileName.substring(0, fileName.lastIndexOf("."));
                    if (!imageExistsInGallery(context, displayName)) {
                        String fullAssetPath = folderPath + "/" + fileName;
                        saveAssetImageToGallery(context, fullAssetPath, displayName);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        copyImagesFromAssetsToInternalStorage(context);
    }

    private static boolean imageExistsInGallery(Context context, String displayName) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.DISPLAY_NAME + " = ?";
        String[] selectionArgs = {displayName + ".jpg"};

        try (Cursor cursor = contentResolver.query(collection, projection, selection, selectionArgs, null)) {
            return cursor != null && cursor.moveToFirst();
        }
    }

    private static void saveAssetImageToGallery(Context context, String assetFileName, String displayName) {
        try {
            InputStream in = context.getAssets().open(assetFileName);
            saveInputStreamToGallery(context, in, displayName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveInputStreamToGallery(Context context, InputStream in, String displayName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp");
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try (OutputStream out = resolver.openOutputStream(uri)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                in.close();
                out.flush();

                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                resolver.update(uri, values, null, null);

                // Trigger media scanner to index the file
                scanMediaFile(context, uri);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void scanMediaFile(Context context, Uri uri) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(uri);
        context.sendBroadcast(scanIntent);
    }


    private static void copyImagesFromAssetsToInternalStorage(Context context) {
        String[] imageNames;
        AssetManager assetManager = context.getAssets();
        try {
            imageNames = assetManager.list("images");
            if (imageNames != null) {
                for (String name : imageNames) {
                    InputStream in = assetManager.open("images/" + name);
                    File outFile = new File(context.getFilesDir(), name);
                    OutputStream out = new FileOutputStream(outFile);

                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }

                    in.close();
                    out.flush();
                    out.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


