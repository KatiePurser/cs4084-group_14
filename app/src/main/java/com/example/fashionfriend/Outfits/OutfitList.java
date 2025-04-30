package com.example.fashionfriend.Outfits;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionfriend.R;
import com.example.fashionfriend.data.database.Outfit;
import com.example.fashionfriend.outfitCreation.CreateOutfitActivity;
import com.example.fashionfriend.viewAndEditOutfit.ViewAndEditOutfitActivity;

public class OutfitList extends AppCompatActivity implements OutfitAdapter.OnOutfitClickListener {

    private static final String TAG = "OutfitListActivity";
    private static final int REQUEST_CREATE_OUTFIT = 1001;
    private static final int REQUEST_VIEW_EDIT_OUTFIT = 1002;

    private com.example.fashionfriend.Outfits.OutfitListViewModel viewModel;
    private RecyclerView recyclerView;
    private OutfitAdapter adapter;
    private TextView emptyView;
    private Button createOutfitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_outfit_list);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(com.example.fashionfriend.Outfits.OutfitListViewModel.class);

        // Find views
        recyclerView = findViewById(R.id.outfits_recycler_view);
        emptyView = findViewById(R.id.empty_view);
        createOutfitButton = findViewById(R.id.create_outfit_button);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new OutfitAdapter(this, this);
        recyclerView.setAdapter(adapter);

        // Set up button listener
        createOutfitButton.setOnClickListener(v -> {
            Intent intent = new Intent(OutfitList.this, CreateOutfitActivity.class);
            startActivityForResult(intent, REQUEST_CREATE_OUTFIT);
        });

        // Load outfits
        loadOutfits();

        // Observe ViewModel data
        viewModel.getOutfits().observe(this, outfits -> {
            if (outfits != null) {
                adapter.setOutfits(outfits);

                // Show empty view if no outfits
                if (outfits.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadOutfits() {
        viewModel.loadOutfits();
    }

    @Override
    public void onOutfitClick(Outfit outfit) {
        long outfitId = outfit.getId();
        Log.d(TAG, "Clicked on outfit with ID: " + outfitId);

        // Navigate to ViewAndEditOutfitActivity
        Intent intent = new Intent(this, ViewAndEditOutfitActivity.class);
        intent.putExtra("outfitId", outfitId);
        startActivityForResult(intent, REQUEST_VIEW_EDIT_OUTFIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CREATE_OUTFIT || requestCode == REQUEST_VIEW_EDIT_OUTFIT) {
                // Refresh the outfit list
                loadOutfits();
                Toast.makeText(this, "Outfit list updated", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
