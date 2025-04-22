package com.example.fashionfriend;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GalleryHelper {

    public static void saveAssetImageToGallery(Context context, String assetFileName, String displayName) {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


