package com.example.fashionfriend;

import android.app.AlertDialog;
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

public class MainActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private String selectedDate;
    private ReminderDatabaseHandler dbHelper;
    private List<EventDay> eventList = new ArrayList<>();

    private ImageButton add_item_button, wardrobe_button, outfits_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dbHelper = new ReminderDatabaseHandler(this);

        // Setting up app toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialising Buttons
        add_item_button = findViewById(R.id.add_button);
        wardrobe_button = findViewById(R.id.wardrobe_button);
        outfits_button = findViewById(R.id.outfits_button);
        setButtonClickListener(add_item_button);
        setButtonClickListener(wardrobe_button);
        setButtonClickListener(outfits_button);

        // Initialising Applandeo CalendarView
        calendarView = findViewById(R.id.calendarView);

        //Applying custom styling to current day on calender
        Calendar today = Calendar.getInstance();
        CalendarDay todayDay = new CalendarDay(today);
        todayDay.setBackgroundResource(R.drawable.today_background);
        List<CalendarDay> days = new ArrayList<>();
        days.add(todayDay);
        calendarView.setCalendarDays(days);

        loadExistingReminders(); // Load stored reminders as events

        // Handling Date Selection
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDay = eventDay.getCalendar();
            selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d",
                    clickedDay.get(Calendar.DAY_OF_MONTH),
                    clickedDay.get(Calendar.MONTH) + 1,
                    clickedDay.get(Calendar.YEAR));

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
            reminderTitle.setText(existingReminder);
        }

        AlertDialog dialog = builder.create();
        dialog.show();

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

    private void setButtonClickListener(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int id = button.getId();

                if (id == R.id.add_button) {
                    // Navigates to add item view
                } else if (id == R.id.wardrobe_button) {
                    // Navigates to wardrobe view
                } else if (id == R.id.outfits_button) {
                    // Navigates to outfits view
                }
            }
        });
    }
}
