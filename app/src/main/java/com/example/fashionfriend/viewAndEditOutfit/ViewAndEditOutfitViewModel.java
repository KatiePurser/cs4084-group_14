package com.example.fashionfriend.viewAndEditOutfit;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fashionfriend.data.database.FashionFriendDatabase;
import com.example.fashionfriend.data.database.Outfit;
import com.example.fashionfriend.data.database.OutfitDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewAndEditOutfitViewModel extends AndroidViewModel {

    private final OutfitDao outfitDao;
    private final ExecutorService executorService;

    private final MutableLiveData<Outfit> outfit = new MutableLiveData<>();
    public LiveData<Outfit> getOutfit() {
        return outfit;
    }

    private final MutableLiveData<UpdateOutfitStatus> updateOutfitStatus = new MutableLiveData<>();
    public LiveData<UpdateOutfitStatus> getUpdateOutfitStatus() {
        return updateOutfitStatus;
    }

    public ViewAndEditOutfitViewModel(Application application) {
        super(application);
        FashionFriendDatabase fashionFriendDatabase = FashionFriendDatabase.getDatabase(application);
        outfitDao = fashionFriendDatabase.outfitDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void getOutfitById(long outfitId) {
        Log.d(TAG, "Getting outfit by ID: " + outfitId);
        executorService.execute(() -> {
            Outfit outfit = outfitDao.getOutfitById(outfitId);
            if (outfit != null) {
                Log.d(TAG, "Found outfit: " + outfit.getName() + " with ID: " + outfit.getId());
            } else {
                Log.e(TAG, "No outfit found with ID: " + outfitId);
            }
            this.outfit.postValue(outfit);
        });
    }

    public void updateOutfit(Outfit outfit) {
        updateOutfitStatus.postValue(new UpdateOutfitStatus.Updating());
        executorService.execute(() -> {
            int rowsAffected = outfitDao.updateOutfit(outfit);
            if (rowsAffected == 1) {
                updateOutfitStatus.postValue(new UpdateOutfitStatus.Success());
            } else {
                updateOutfitStatus.postValue(new UpdateOutfitStatus.Error("Failed to update outfit"));
            }
        });
    }

    public void deleteOutfit(int outfitId) {
        executorService.execute(() -> {
            outfitDao.deleteOutfit(outfitId);
            updateOutfitStatus.postValue(new UpdateOutfitStatus.Deleted());
        });
    }

    public abstract static class UpdateOutfitStatus {
        private UpdateOutfitStatus() {}

        public static final class Updating extends UpdateOutfitStatus {}

        public static final class Success extends UpdateOutfitStatus {}

        public static final class Deleted extends UpdateOutfitStatus {}

        public static final class Error extends UpdateOutfitStatus {
            public final String message;
            public Error(String message) { this.message = message; }
        }
    }

    public void clearUpdateStatus() {
        updateOutfitStatus.postValue(null);
    }
}

