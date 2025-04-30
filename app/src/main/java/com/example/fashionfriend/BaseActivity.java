package com.example.fashionfriend;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import android.content.Intent;
import android.widget.PopupWindow;
import com.example.fashionfriend.addClothingItem.AddClothingItemActivity;
import com.example.fashionfriend.home.MainActivity;
import com.example.fashionfriend.outfitCreation.CreateOutfitActivity;

public abstract class BaseActivity extends AppCompatActivity {

    protected ImageButton menuButton;
    protected ImageButton profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolbar();
    }

    protected void setupToolbar() {
        menuButton = findViewById(R.id.menu_icon);
        profileButton = findViewById(R.id.profile_icon);

        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                Context themedContext = new ContextThemeWrapper(this, R.style.PopupMenuStyle);
                PopupMenu popup = new PopupMenu(themedContext, v, 0, 0, R.style.PopupMenuStyle);

                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.navigation_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();

                    if (itemId == R.id.menu_home) {
                        startActivity(new Intent(this, MainActivity.class));
                        return true;
                    } else if (itemId == R.id.menu_add_item) {
                        startActivity(new Intent(this, AddClothingItemActivity.class));
                        return true;
                    } else if (itemId == R.id.menu_outfits) {
                        startActivity(new Intent(this, CreateOutfitActivity.class));
                        return true;
                    } else {
                        return false;
                    }
                });

                popup.show();
            });
        }

        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                View popupView = getLayoutInflater().inflate(R.layout.popup_welcome, null);

                PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        true
                );

                profileButton.postDelayed(popupWindow::dismiss, 2000);
                popupWindow.showAsDropDown(profileButton, 0, 0);

            });
        }

    }
}
