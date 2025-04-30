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
import androidx.core.view.WindowInsetsControllerCompat;

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
        setupSystemBarAppearance();
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

    /**
     * Call this in child activities to configure the back button behavior.
     */
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

    /**
     * Sets dark system bar icons (black) for status and nav bars on light backgrounds.
     */
    protected void setupSystemBarAppearance() {
        View decorView = getWindow().getDecorView();
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(decorView);
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);     // dark icons for status bar (on light background)
            controller.setAppearanceLightNavigationBars(true); // dark icons for nav bar (on light background)
        }

        // Set status bar color
        getWindow().setStatusBarColor(getColor(R.color.colorPrimary));

        // Set navigation bar color
        getWindow().setNavigationBarColor(getColor(R.color.colorPrimary));
    }

    private boolean firstLaunch = true;

    @Override
    protected void onResume() {
        super.onResume();

        if (firstLaunch) {
            firstLaunch = false; //Skipping First Run
        } else {
            recreate();
            setupSystemBarAppearance();
        }
    }
}
