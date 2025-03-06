package com.example.fashionfriend;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


public class ToolbarFragment extends Fragment {

    private Toolbar toolbar;
    private ImageButton profileIcon;

    public ToolbarFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.toolbar_fragment, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        profileIcon = view.findViewById(R.id.profile_icon);

        profileIcon.setOnClickListener(v -> {
            // Show options for logging out
        });

        return view;
    }
}
