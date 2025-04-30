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
import com.example.fashionfriend.data.database.Outfit;
import com.example.fashionfriend.viewAndEditClothingItem.ViewAndEditClothingItemActivity;
import com.example.fashionfriend.viewAndEditOutfit.ViewAndEditOutfitActivity;

import java.util.List;

public class WardrobeOutfitAdapter extends RecyclerView.Adapter<WardrobeOutfitAdapter.WardrobeOutfitViewHolder> {

    private static final String TAG = "WardrobeOutfitAdapter";
    private List<Outfit> outfitList;
    private Context context;

    public WardrobeOutfitAdapter(List<Outfit> outfitList, Context context) {
        this.outfitList = outfitList;
        this.context = context;
    }

    @NonNull
    @Override
    public WardrobeOutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wardrobe_item_layout, parent, false);
        return new WardrobeOutfitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WardrobeOutfitAdapter.WardrobeOutfitViewHolder holder, int position) {
        Outfit outfit = outfitList.get(position);
        holder.setImage(outfit);
        holder.name.setText(outfit.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {  // <--- here
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Passing item " + outfit.getName() + "with id " + outfit.getId());
                Intent i = new Intent(context, ViewAndEditOutfitActivity.class);
                i.putExtra("outfitId", (long) outfit.getId());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return outfitList.size();
    }

    public class WardrobeOutfitViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView name;

        public View itemView;

        public WardrobeOutfitViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            image = itemView.findViewById(R.id.card_image);
            name = itemView.findViewById(R.id.card_text);
        }

        public void setImage(Outfit outfit){
            boolean imageLoaded = false;

            String imagePath = outfit.getImagePath();
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