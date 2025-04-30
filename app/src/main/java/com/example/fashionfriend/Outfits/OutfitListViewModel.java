package com.example.fashionfriend.Outfits;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fashionfriend.data.database.FashionFriendDatabase;
import com.example.fashionfriend.data.database.Outfit;
import com.example.fashionfriend.data.database.OutfitDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OutfitListViewModel extends AndroidViewModel {

    private static final String TAG = "OutfitListViewModel";

    private final OutfitDao outfitDao;
    private final ExecutorService executorService;
    private final MutableLiveData<List<Outfit>> outfits = new MutableLiveData<>();

    public LiveData<List<Outfit>> getOutfits() {
        return outfits;
    }

    public OutfitListViewModel(Application application) {
        super(application);
        FashionFriendDatabase fashionFriendDatabase = FashionFriendDatabase.getDatabase(application);
        outfitDao = fashionFriendDatabase.outfitDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void loadOutfits() {
        executorService.execute(() -> {
            try {
                List<Outfit> outfitList = outfitDao.getAllOutfits();
                outfits.postValue(outfitList);
                Log.d(TAG, "Loaded " + outfitList.size() + " outfits");
            } catch (Exception e) {
                Log.e(TAG, "Error loading outfits", e);
            }
        });
    }
}
