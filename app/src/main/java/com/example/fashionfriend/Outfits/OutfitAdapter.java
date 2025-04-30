package com.example.fashionfriend.Outfits;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fashionfriend.R;
import com.example.fashionfriend.data.database.Outfit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OutfitAdapter extends RecyclerView.Adapter<OutfitAdapter.OutfitViewHolder> {

    private static final String TAG = "OutfitAdapter";
    private final Context context;
    private List<Outfit> outfits = new ArrayList<>();
    private final OnOutfitClickListener listener;

    public OutfitAdapter(Context context, OnOutfitClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setOutfits(List<Outfit> outfits) {
        this.outfits = outfits;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_outfit, parent, false);
        return new OutfitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OutfitViewHolder holder, int position) {
        Outfit outfit = outfits.get(position);
        holder.bind(outfit);
    }

    @Override
    public int getItemCount() {
        return outfits.size();
    }

    public class OutfitViewHolder extends RecyclerView.ViewHolder {
        private final ImageView outfitImageView;
        private final TextView outfitNameTextView;

        public OutfitViewHolder(@NonNull View itemView) {
            super(itemView);
            outfitImageView = itemView.findViewById(R.id.outfit_image);
            outfitNameTextView = itemView.findViewById(R.id.outfit_name);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onOutfitClick(outfits.get(position));
                }
            });
        }

        public void bind(Outfit outfit) {
            outfitNameTextView.setText(outfit.getName());

            // Load outfit image
            if (outfit.getImagePath() != null && !outfit.getImagePath().isEmpty()) {
                File imageFile = new File(outfit.getImagePath());

                // Check if file exists at the exact path
                if (imageFile.exists()) {
                    Log.d(TAG, "Loading image from path: " + imageFile.getAbsolutePath());
                    Glide.with(context)
                            .load(imageFile)
                            .placeholder(R.drawable.ic_hanger)
                            .into(outfitImageView);
                } else {
                    // Try to find the file by name in the files directory
                    String fileName = imageFile.getName();
                    File alternateFile = new File(context.getFilesDir(), fileName);

                    if (alternateFile.exists()) {
                        Log.d(TAG, "Loading image from alternate path: " + alternateFile.getAbsolutePath());
                        Glide.with(context)
                                .load(alternateFile)
                                .placeholder(R.drawable.ic_hanger)
                                .into(outfitImageView);
                    } else {
                        Log.e(TAG, "Image file not found: " + outfit.getImagePath());
                        Glide.with(context)
                                .load(R.drawable.ic_hanger)
                                .into(outfitImageView);
                    }
                }
            } else {
                Glide.with(context)
                        .load(R.drawable.ic_hanger)
                        .into(outfitImageView);
            }
        }
    }

    public interface OnOutfitClickListener {
        void onOutfitClick(Outfit outfit);
    }
}
