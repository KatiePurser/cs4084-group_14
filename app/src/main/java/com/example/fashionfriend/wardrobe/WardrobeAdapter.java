package com.example.fashionfriend.wardrobe;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionfriend.R;
import com.example.fashionfriend.data.database.ClothingItem;
import com.example.fashionfriend.viewAndEditClothingItem.ViewAndEditClothingItemActivity;

import java.util.ArrayList;
import java.util.List;

public class WardrobeAdapter extends RecyclerView.Adapter<WardrobeAdapter.WardrobeViewHolder> {

    private static final String TAG = "WardrobeAdapter";
    private List<ClothingItem> clothingItemList;
    private Context context;

    public WardrobeAdapter(List<ClothingItem> clothingItemList, Context context) {
        this.clothingItemList = clothingItemList;
        this.context = context;
    }

    @NonNull
    @Override
    public WardrobeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wardrobe_item_layout, parent, false);
        return new WardrobeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WardrobeViewHolder holder, int position) {
        ClothingItem item = clothingItemList.get(position);
        holder.setImage(item);
        holder.name.setText(item.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {  // <--- here
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Passing item " + item.getName() + "with id " + item.getId());
                Intent i = new Intent(context, ViewAndEditClothingItemActivity.class);
                i.putExtra("clothingItemId", (long) item.getId());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, clothingItemList.size() + " items found in database");
        return clothingItemList.size();
    }




    public class WardrobeViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView name;

        public View itemView;

        public WardrobeViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            image = itemView.findViewById(R.id.card_image);
            name = itemView.findViewById(R.id.card_text);
        }

        public void setImage(ClothingItem item){
            boolean imageLoaded = false;

            String imagePath = item.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                // Load image from file path
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    image.setImageBitmap(bitmap);
                    image.setVisibility(View.VISIBLE);
                    imageLoaded = true;
                }
            }

            // Fallback to placeholder only if path loading fails
//            if (!imageLoaded) {
//                Integer imageResource = items.get(selectedItem);
//                if (imageResource != null) {
//                    itemImage.setImageResource(imageResource);
//                    itemImage.setVisibility(View.VISIBLE);
//                } else {
//                    itemImage.setVisibility(View.INVISIBLE);
//                    selectedItems.put(category, null);
//                    listener.onItemSelected(category, null);
//                }
//            }
        }
    }
}
