package com.example.fashionfriend.outfitCreation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionfriend.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final Context context;
    private final List<String> categories;
    private final HashMap<String, HashMap<String, Integer>> clothingItems;  // Category -> (Item Name -> Image Resource)
    private final OnItemSelectedListener listener; //For communication with the activity
    private final HashMap<String, String> selectedItems = new HashMap<>(); //Track selected item for each category
    private HashMap<String, HashMap<String, String>> imagePaths = new HashMap<>(); // Category -> (Item Name -> Image Path)

    public CategoryAdapter(Context context, List<String> categories, HashMap<String, HashMap<String, Integer>> clothingItems, OnItemSelectedListener listener) {
        this.context = context;
        this.categories = categories;
        this.clothingItems = clothingItems;
        this.listener = listener;
        for(String category: categories){
            selectedItems.put(category, null);
        }
    }

    public void setImagePaths(HashMap<String, HashMap<String, String>> imagePaths) {
        this.imagePaths = imagePaths;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView categoryLabel;
        private final Spinner itemSpinner;
        private final ImageView itemImage;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            categoryLabel = itemView.findViewById(R.id.category_label);
            itemSpinner = itemView.findViewById(R.id.item_spinner);
            itemImage = itemView.findViewById(R.id.item_image);
        }

        public void bind(String category) {
            categoryLabel.setText(category);

            HashMap<String, Integer> items = clothingItems.get(category);
            if (items != null) {
                List<String> itemNames = new ArrayList<>(items.keySet());
                itemNames.add(0, "Select " + category); // Placeholder

                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, itemNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                itemSpinner.setAdapter(adapter);

                //Restore previously selected item, if any
                String previouslySelectedItem = selectedItems.get(category);
                if(previouslySelectedItem != null){
                    int position = itemNames.indexOf(previouslySelectedItem);
                    if(position != -1){
                        itemSpinner.setSelection(position);
                        
                        // Try to load from path first
                        boolean imageLoaded = false;
                        if (imagePaths.containsKey(category) && 
                            imagePaths.get(category).containsKey(previouslySelectedItem)) {
                            
                            String imagePath = imagePaths.get(category).get(previouslySelectedItem);
                            if (imagePath != null && !imagePath.isEmpty()) {
                                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                                if (bitmap != null) {
                                    itemImage.setImageBitmap(bitmap);
                                    itemImage.setVisibility(View.VISIBLE);
                                    imageLoaded = true;
                                }
                            }
                        }
                        
                        // Only fallback to resource if image path loading failed
                        if (!imageLoaded) {
                            itemImage.setImageResource(items.get(previouslySelectedItem));
                            itemImage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        itemSpinner.setSelection(0);
                        itemImage.setVisibility(View.INVISIBLE);
                    }
                } else {
                    itemSpinner.setSelection(0);
                    itemImage.setVisibility(View.INVISIBLE);
                }

                itemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedItem = parent.getItemAtPosition(position).toString();
                        if (position > 0) { // Skip placeholder
                            // Try to load image from path if available
                            boolean imageLoaded = false;
                            if (imagePaths.containsKey(category) && 
                                imagePaths.get(category).containsKey(selectedItem)) {
                                
                                String imagePath = imagePaths.get(category).get(selectedItem);
                                if (imagePath != null && !imagePath.isEmpty()) {
                                    // Load image from file path
                                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                                    if (bitmap != null) {
                                        itemImage.setImageBitmap(bitmap);
                                        itemImage.setVisibility(View.VISIBLE);
                                        selectedItems.put(category, selectedItem);
                                        listener.onItemSelected(category, selectedItem);
                                        imageLoaded = true;
                                    }
                                }
                            }
                            
                            // Fallback to placeholder only if path loading fails
                            if (!imageLoaded) {
                                Integer imageResource = items.get(selectedItem);
                                if (imageResource != null) {
                                    itemImage.setImageResource(imageResource);
                                    itemImage.setVisibility(View.VISIBLE);
                                    selectedItems.put(category, selectedItem);
                                    listener.onItemSelected(category, selectedItem);
                                } else {
                                    itemImage.setVisibility(View.INVISIBLE);
                                    selectedItems.put(category, null);
                                    listener.onItemSelected(category, null);
                                }
                            }
                        } else {
                            itemImage.setVisibility(View.INVISIBLE);
                            selectedItems.put(category, null);
                            listener.onItemSelected(category, null);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        itemImage.setVisibility(View.INVISIBLE);
                        selectedItems.put(category, null);
                        listener.onItemSelected(category, null);
                    }
                });
            }
        }
    }

    // Define the OnItemSelectedListener interface
    public interface OnItemSelectedListener {
        void onItemSelected(String category, String selectedItem);
    }
}
