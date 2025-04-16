package com.example.fashionfriend.addClothingItem;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fashionfriend.data.database.ClothingItem;
import com.example.fashionfriend.data.database.ClothingItemDao;
import com.example.fashionfriend.data.database.FashionFriendDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddClothingItemViewModel extends AndroidViewModel {

    private final ClothingItemDao clothingItemDao;
    private final ExecutorService executorService;
    private final MutableLiveData<InsertClothingItemStatus> insertClothingItemStatus = new MutableLiveData<>();

    public LiveData<InsertClothingItemStatus> getInsertClothingItemStatus() {
        return insertClothingItemStatus;
    }

    public AddClothingItemViewModel(Application application) {
        super(application);
        FashionFriendDatabase fashionFriendDatabase = FashionFriendDatabase.getDatabase(application);
        clothingItemDao = fashionFriendDatabase.clothingItemDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insertClothingItem(ClothingItem clothingItem) {
        insertClothingItemStatus.postValue(new InsertClothingItemStatus.Inserting());
        executorService.execute(() -> {
            long clothingItemId = clothingItemDao.insertClothingItem(clothingItem);
            if (clothingItemId != -1) {
                insertClothingItemStatus.postValue(new InsertClothingItemStatus.Success(clothingItemId));
            } else {
                insertClothingItemStatus.postValue(new InsertClothingItemStatus.Error("Failed to insert clothing item"));
            }
        });
    }

    public abstract static class InsertClothingItemStatus {
        private InsertClothingItemStatus() {}

        public static final class Inserting extends InsertClothingItemStatus {}

        public static final class Success extends InsertClothingItemStatus {
            public final long itemId;
            public Success(long itemId) { this.itemId = itemId; }
        }

        public static final class Error extends InsertClothingItemStatus {
            public final String message;
            public Error(String message) { this.message = message; }
        }
    }
}
