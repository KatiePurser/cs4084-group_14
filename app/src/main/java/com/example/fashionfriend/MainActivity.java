package com.example.fashionfriend;


import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.example.fashionfriend.addClothingItem.AddClothingItemActivity;
import com.example.fashionfriend.data.database.FashionFriendDatabase;
import com.example.fashionfriend.data.database.ReminderDao;
import com.example.fashionfriend.data.database.Reminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import androidx.core.splashscreen.SplashScreen;

public class MainActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private String selectedDate;
    private ReminderDao reminderDao;
    private List<EventDay> eventList = new ArrayList<>();

    private ImageButton add_item_button, wardrobe_button, outfits_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        createNotificationChannel();

        GalleryHelper.saveAssetImageToGallery(this, "white-dress.jpg", "white-dress");
        GalleryHelper.saveAssetImageToGallery(this, "red-pants.jpg", "red-pants");
        GalleryHelper.saveAssetImageToGallery(this, "coat.jpg", "coat");

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Room DAO
        reminderDao = FashionFriendDatabase.getDatabase(this).reminderDao();

        checkForTodayReminder();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        add_item_button = findViewById(R.id.add_button);
        wardrobe_button = findViewById(R.id.wardrobe_button);
        outfits_button = findViewById(R.id.outfits_button);
        setButtonClickListener(add_item_button);
        setButtonClickListener(wardrobe_button);
        setButtonClickListener(outfits_button);

        calendarView = findViewById(R.id.calendarView);

        Calendar today = Calendar.getInstance();
        CalendarDay todayDay = new CalendarDay(today);
        todayDay.setBackgroundResource(R.drawable.today_background);
        List<CalendarDay> days = new ArrayList<>();
        days.add(todayDay);
        calendarView.setCalendarDays(days);

        loadExistingReminders();

        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDay = eventDay.getCalendar();
            selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d",
                    clickedDay.get(Calendar.DAY_OF_MONTH),
                    clickedDay.get(Calendar.MONTH) + 1,
                    clickedDay.get(Calendar.YEAR));

            Executors.newSingleThreadExecutor().execute(() -> {
                String reminder = reminderDao.getReminderByDate(selectedDate);
                runOnUiThread(() -> showReminderDialog(reminder, clickedDay));
            });
        });
    }

    private void showReminderDialog(String existingReminder, Calendar date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_reminder, null);
        builder.setView(dialogView);

        EditText reminderTitle = dialogView.findViewById(R.id.reminderTitle);
        Button saveReminderButton = dialogView.findViewById(R.id.saveReminderButton);

        if (existingReminder != null) {
            reminderTitle.setText(existingReminder);
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        saveReminderButton.setOnClickListener(v -> {
            String reminderText = reminderTitle.getText().toString().trim();
            if (!reminderText.isEmpty()) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    reminderDao.insert(new Reminder(selectedDate, reminderText));
                    runOnUiThread(() -> {
                        addEventMarker(date);
                        dialog.dismiss();
                        Toast.makeText(this, "Reminder Saved!", Toast.LENGTH_SHORT).show();
                    });
                });
            } else {
                Toast.makeText(this, "Please enter a reminder title", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addEventMarker(Calendar date) {
        EventDay event = new EventDay(date, R.drawable.ic_reminder);
        eventList.add(event);
        calendarView.setEvents(eventList);
    }

    private void loadExistingReminders() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<String> datesWithReminders = reminderDao.getAllReminderDates();
            runOnUiThread(() -> {
                for (String date : datesWithReminders) {
                    Calendar calendar = Calendar.getInstance();
                    String[] parts = date.split("-");
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[0]));
                    calendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
                    calendar.set(Calendar.YEAR, Integer.parseInt(parts[2]));
                    addEventMarker(calendar);
                }
            });
        });
    }

    private void checkForTodayReminder() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Calendar today = Calendar.getInstance();
            String todayDate = String.format(Locale.getDefault(), "%02d-%02d-%04d",
                    today.get(Calendar.DAY_OF_MONTH),
                    today.get(Calendar.MONTH) + 1,
                    today.get(Calendar.YEAR));

            String reminder = reminderDao.getReminderByDate(todayDate);
            if (reminder != null) {
                runOnUiThread(() -> showTodayReminderNotification(reminder));
            }
        });
    }

    private void showTodayReminderNotification(String reminderText) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Outfit Reminder!")
                .setContentText(reminderText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermission();
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private void setButtonClickListener(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = button.getId();
                // Handle button clicks based on their ID
                if (id == R.id.add_button) {
                    startActivity(new Intent(MainActivity.this, AddClothingItemActivity.class));
                } else if (id == R.id.wardrobe_button) {
                    // Navigate to wardrobe view (implementation pending)
                } else if (id == R.id.outfits_button) {
                    // Navigate to outfits view (implementation pending)
                      // Navigates to outfits view
                      Intent intent = new Intent(MainActivity.this, CreateOutfitActivity.class); //  CreateOutfitActivity is where you want to go
                      startActivity(intent);
                      Toast.makeText(MainActivity.this, "Outfits Clicked", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default Channel";
            String description = "Used for general notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("default_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
