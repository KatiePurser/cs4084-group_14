package com.example.fashionfriend;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import androidx.core.splashscreen.SplashScreen;

public class MainActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private String selectedDate;
    private ReminderDatabaseHandler dbHelper;
    private List<EventDay> eventList = new ArrayList<>();

    private ImageButton add_item_button, wardrobe_button, outfits_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set up splash screen during app launch
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Request permission for notifications on Android API 33 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialise database handler for reminders
        dbHelper = new ReminderDatabaseHandler(this);

        // Check for any reminders for today
        checkForTodayReminder();

        // Setting up the toolbar for the app
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Apply insets for system bars like the status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialising buttons and setting click listeners
        add_item_button = findViewById(R.id.add_button);
        wardrobe_button = findViewById(R.id.wardrobe_button);
        outfits_button = findViewById(R.id.outfits_button);
        setButtonClickListener(add_item_button);
        setButtonClickListener(wardrobe_button);
        setButtonClickListener(outfits_button);

        // Initialise Applandeo calendar view
        calendarView = findViewById(R.id.calendarView);

        // Apply custom styling to today's date on the calendar
        Calendar today = Calendar.getInstance();
        CalendarDay todayDay = new CalendarDay(today);
        todayDay.setBackgroundResource(R.drawable.today_background);
        List<CalendarDay> days = new ArrayList<>();
        days.add(todayDay);
        calendarView.setCalendarDays(days);

        // Load existing reminders from the database
        loadExistingReminders();

        // Handle date selection on the calendar
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDay = eventDay.getCalendar();
            selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d",
                    clickedDay.get(Calendar.DAY_OF_MONTH),
                    clickedDay.get(Calendar.MONTH) + 1,
                    clickedDay.get(Calendar.YEAR));

            // Get the reminder for the selected date from the database and show dialog if it exists
            String reminder = dbHelper.getReminderForDate(selectedDate);
            showReminderDialog(reminder, clickedDay);
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
            reminderTitle.setText(existingReminder); // Prepopulate text if the reminder already exists (for editing)
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        // Save the reminder when the save button is clicked
        saveReminderButton.setOnClickListener(v -> {
            String reminderText = reminderTitle.getText().toString().trim();
            if (!reminderText.isEmpty()) {
                dbHelper.saveReminder(selectedDate, reminderText);
                addEventMarker(date);
                dialog.dismiss();
                Toast.makeText(this, "Reminder Saved!", Toast.LENGTH_SHORT).show();
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

    // Loads all existing reminders and mark them on the calendar
    private void loadExistingReminders() {
        List<String> datesWithReminders = dbHelper.getAllReminderDates();
        for (String date : datesWithReminders) {
            Calendar calendar = Calendar.getInstance();
            String[] parts = date.split("-");
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
            calendar.set(Calendar.YEAR, Integer.parseInt(parts[2]));
            addEventMarker(calendar);
        }
    }

    private void checkForTodayReminder() {
        Calendar today = Calendar.getInstance();
        String todayDate = String.format(Locale.getDefault(), "%02d-%02d-%04d",
                today.get(Calendar.DAY_OF_MONTH),
                today.get(Calendar.MONTH) + 1,
                today.get(Calendar.YEAR));

        String reminder = dbHelper.getReminderForDate(todayDate);
        if (reminder != null) {
            showTodayReminderNotification(reminder);
        }
    }

    private void showTodayReminderNotification(String reminderText) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Outfit Reminder!")
                .setContentText(reminderText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // Check permission for posting notifications
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermission(); // Request permission if not already granted
            return;
        }
        notificationManager.notify(1, builder.build()); // Displaying the notification
    }

    private void setButtonClickListener(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = button.getId();
                // Handle button clicks based on their ID
                if (id == R.id.add_button) {
                    // Navigate to add item view (implementation pending)
                } else if (id == R.id.wardrobe_button) {
                    // Navigate to wardrobe view (implementation pending)
                } else if (id == R.id.outfits_button) {
                    // Navigate to outfits view (implementation pending)
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
                        101 // Request code
                );
            }
        }
    }

    // Create a notification channel for app notifications on Android 8.0+
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
