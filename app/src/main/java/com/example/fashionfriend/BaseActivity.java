package com.example.fashionfriend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fashionfriend.addClothingItem.AddClothingItemActivity;
import com.example.fashionfriend.home.MainActivity;
import com.example.fashionfriend.outfitCreation.CreateOutfitActivity;
import com.example.fashionfriend.wardrobe.WardrobeActivity;


public abstract class BaseActivity extends AppCompatActivity {

    protected ImageButton menuButton;
    protected ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolbar();
    }

    protected void setupToolbar() {
        menuButton = findViewById(R.id.menu_icon);
        backButton = findViewById(R.id.back_button);

        // Setup menu
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                Context themedContext = new ContextThemeWrapper(this, R.style.PopupMenuStyle);
                PopupMenu popup = new PopupMenu(themedContext, v, 0, 0, R.style.PopupMenuStyle);

                MenuInflater inflater = popup.getMenuInflater();
                popup.getMenu().clear();
                inflater.inflate(R.menu.navigation_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_home) {
                        startActivity(new Intent(this, MainActivity.class));
                        return true;
                    } else if (itemId == R.id.menu_add_item) {
                        startActivity(new Intent(this, AddClothingItemActivity.class));
                        return true;
                    }else if (itemId == R.id.menu_wardrobe) {
                        startActivity(new Intent( this, WardrobeActivity.class));
                    } else if (itemId == R.id.menu_outfits) {
                        startActivity(new Intent(this, CreateOutfitActivity.class));
                        return true;
                    }
                    return false;
                });

                popup.show();
            });
        }
    }

    protected void configureBackButton(boolean showBackButton) {
        if (backButton != null) {
            if (showBackButton) {
                backButton.setVisibility(View.VISIBLE);
                backButton.setOnClickListener(v -> this.getOnBackPressedDispatcher().onBackPressed());

            } else {
                backButton.setVisibility(View.GONE);
            }
        }
    }

    protected void applySystemBarInsets(int rootViewId) {
        View rootView = findViewById(rootViewId);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private boolean firstLaunch = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (firstLaunch) {
            firstLaunch = false;
        } else if (shouldRestartOnResume()) {
            restartActivity();
        }
    }

    protected boolean shouldRestartOnResume() {
        return true;
    }

    protected void restartActivity() {
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);
    }
}
