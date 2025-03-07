package com.example.fashionfriend;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private ImageButton add_item_button, wardrobe_button, outfits_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Setting up app toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find buttons by ID
        add_item_button = findViewById(R.id.add_button);
        wardrobe_button = findViewById(R.id.wardrobe_button);
        outfits_button = findViewById(R.id.outfits_button);

        // Click listeners
        setButtonClickListener(add_item_button);
        setButtonClickListener(wardrobe_button);
        setButtonClickListener(outfits_button);
    }

    private void setButtonClickListener(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int id = button.getId();

                if (id == R.id.add_button) {
                    // Navigates to add item view
                } else if (id == R.id.wardrobe_button) {
                    // Navigates to wardrobe view
                } else if (id == R.id.outfits_button) {
                    // Navigates to outfits view
                }
            }
        });
    }

}
