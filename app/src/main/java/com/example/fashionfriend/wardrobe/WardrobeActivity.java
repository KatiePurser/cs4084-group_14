
package com.example.fashionfriend.wardrobe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fashionfriend.BaseActivity;
import com.example.fashionfriend.R;
import com.example.fashionfriend.home.MainActivity;

public class WardrobeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wardrobe);

        ImageButton outfits_button = findViewById(R.id.outfits_button);
        setButtonClickListener(outfits_button);

        ImageButton tops_button = findViewById(R.id.tops_button);
        setButtonClickListener(tops_button);

        ImageButton bottoms_button = findViewById(R.id.bottoms_button);
        setButtonClickListener(bottoms_button);

        ImageButton shoes_button = findViewById(R.id.shoes_button);
        setButtonClickListener(shoes_button);

        ImageButton accessories_button = findViewById(R.id.accessories_button);
        setButtonClickListener(accessories_button);

        setupToolbar();
        configureBackButton(true, () -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        applySystemBarInsets(R.id.create_outfit);
    }

    private void setButtonClickListener(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = button.getId();
                // Handle button clicks based on their ID
                if (id == R.id.tops_button) {
                    Intent i = new Intent(WardrobeActivity.this, WardrobeCategoryActivity.class);
                    i.putExtra("type","Tops");
                    startActivity(i);
                } else if (id == R.id.bottoms_button) {
                    Intent i = new Intent(WardrobeActivity.this, WardrobeCategoryActivity.class);
                    i.putExtra("type","Bottoms");
                    startActivity(i);
                } else if (id == R.id.accessories_button) {
                    Intent i = new Intent(WardrobeActivity.this, WardrobeCategoryActivity.class);
                    i.putExtra("type","Accessories");
                    startActivity(i);
                } else if (id == R.id.shoes_button) {
                    Intent i = new Intent(WardrobeActivity.this, WardrobeCategoryActivity.class);
                    i.putExtra("type","Shoes");
                    startActivity(i);
                } else if (id == R.id.outfits_button) {
                    Intent i = new Intent(WardrobeActivity.this, WardrobeCategoryActivity.class);
                    i.putExtra("type","Outfits");
                    startActivity(i);
                }
            }
        });
    }


}