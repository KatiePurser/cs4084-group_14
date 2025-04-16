package com.example.fashionfriend.viewAndEditClothingItem;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fashionfriend.data.database.ClothingItem;
import com.example.fashionfriend.data.database.ClothingItemDao;
import com.example.fashionfriend.data.database.FashionFriendDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewAndEditClothingItemViewModel extends AndroidViewModel {

    private final ClothingItemDao clothingItemDao;
    private final ExecutorService executorService;

    private final MutableLiveData<ClothingItem> clothingItem = new MutableLiveData<>();
    public LiveData<ClothingItem> getClothingItem() {
        return clothingItem;
    }

    private final MutableLiveData<UpdateClothingItemStatus> updateClothingItemStatus = new MutableLiveData<>();
    public LiveData<UpdateClothingItemStatus> getUpdateClothingItemStatus() {
        return updateClothingItemStatus;
    }

    public ViewAndEditClothingItemViewModel(Application application) {
        super(application);
        FashionFriendDatabase fashionFriendDatabase = FashionFriendDatabase.getDatabase(application);
        clothingItemDao = fashionFriendDatabase.clothingItemDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void getClothingItemById(long clothingItemId) {
        executorService.execute(() -> {
            ClothingItem clothingItem = clothingItemDao.getClothingItemById(clothingItemId);
            this.clothingItem.postValue(clothingItem);
        });

    }
    public void updateClothingItem(ClothingItem clothingItem) {
        updateClothingItemStatus.postValue(new UpdateClothingItemStatus.Updating());
        executorService.execute(() -> {
            long rowsAffected = clothingItemDao.updateClothingItem(clothingItem);
            if (rowsAffected == 1) {
                updateClothingItemStatus.postValue(new UpdateClothingItemStatus.Success());
            } else if (rowsAffected == 0) {
                updateClothingItemStatus.postValue(new UpdateClothingItemStatus.Error("Failed to update clothing item"));
            }
        });
    }

    public abstract static class UpdateClothingItemStatus {
        private UpdateClothingItemStatus() {}

        public static final class Updating extends UpdateClothingItemStatus {} // Optional: Loading state

        public static final class Success extends UpdateClothingItemStatus {}

        public static final class Error extends UpdateClothingItemStatus {
            public final String message;
            public Error(String message) { this.message = message; }
        }
    }

    public void clearUpdateStatus() {
        updateClothingItemStatus.postValue(null);
    }
}
