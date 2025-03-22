package com.example.fashionfriend;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    CalendarView calendarView;
    TextView selectedDateText;
    SharedPreferences sharedPreferences;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    String selectedDate;
    ReminderDatabaseHandler dbHelper;
    ListView reminderListView;
    ArrayAdapter<String> reminderAdapter;
    ArrayList<String> remindersList = new ArrayList<>();


    private ImageButton add_item_button, wardrobe_button, outfits_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dbHelper = new ReminderDatabaseHandler(this);

        reminderListView = findViewById(R.id.reminderListView);
        reminderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, remindersList);
        reminderListView.setAdapter(reminderAdapter);

        // Setting up app toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find buttons by ID
        add_item_button = findViewById(R.id.add_button);
        wardrobe_button = findViewById(R.id.wardrobe_button);
        outfits_button = findViewById(R.id.outfits_button);

        // Click listeners
        setButtonClickListener(add_item_button);
        setButtonClickListener(wardrobe_button);
        setButtonClickListener(outfits_button);


        // Initialise Calender Components
        calendarView = findViewById(R.id.calendarView);
        calendarView.setFirstDayOfWeek(2); // Setting Monday as first day of the week

        sharedPreferences = getSharedPreferences("Reminders", Context.MODE_PRIVATE);

        // Handle Date Selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", dayOfMonth, month + 1, year);

            // Get the reminders for the selected date
            String reminder = dbHelper.getReminderForDate(selectedDate);

            // Show Reminder Form
            showReminderDialog(reminder);
        });
    }

    private void showReminderDialog(String existingReminder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_reminder, null);
        builder.setView(dialogView);

        EditText reminderTitle = dialogView.findViewById(R.id.reminderTitle);
        Button saveReminderButton = dialogView.findViewById(R.id.saveReminderButton);

        // Show existing reminder if it is not null
        if (existingReminder != null) {
            reminderTitle.setText(existingReminder);
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        saveReminderButton.setOnClickListener(v -> {
            String reminderText = reminderTitle.getText().toString().trim();
            if (!reminderText.isEmpty()) {
                dbHelper.saveReminder(selectedDate, reminderText);
                dialog.dismiss();
                Toast.makeText(this, "Reminder Saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter a reminder title", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getReminderForDate(String date) {
        return dbHelper.getReminderForDate(date);
    }


    private void saveReminder(String date, String reminder) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(date, reminder);
        editor.apply();
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
