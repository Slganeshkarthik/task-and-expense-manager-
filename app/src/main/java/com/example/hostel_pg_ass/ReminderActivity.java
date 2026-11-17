package com.example.hostel_pg_ass;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReminderActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        SharedPreferences prefs = getSharedPreferences("HostelPrefs", MODE_PRIVATE);
        currentUser = prefs.getString("USERNAME", "default");

        dbHelper = new DatabaseHelper(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        EditText etReminderTitle = findViewById(R.id.etReminderTitle);
        EditText etReminderDate = findViewById(R.id.etReminderDate);
        EditText etReminderTime = findViewById(R.id.etReminderTime);
        Button btnSetReminder = findViewById(R.id.btnSetReminder);

        // Date Picker for Reminder
        etReminderDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(ReminderActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String dateFormatted = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                        etReminderDate.setText(dateFormatted);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Time Picker Dialog for Reminder Time
        etReminderTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(ReminderActivity.this,
                    (view, hourOfDay, minuteOfHour) -> {
                        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                        etReminderTime.setText(timeFormatted);
                    }, hour, minute, true); // true = 24 hour format for standard AlarmManager parsing
            timePickerDialog.show();
        });

        btnSetReminder.setOnClickListener(v -> {
            String title = etReminderTitle.getText().toString().trim();
            String dateStr = etReminderDate.getText().toString().trim();
            String timeStr = etReminderTime.getText().toString().trim();

            if (title.isEmpty() || dateStr.isEmpty() || timeStr.isEmpty()) {
                Toast.makeText(ReminderActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date = sdf.parse(dateStr + " " + timeStr);

                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                        Toast.makeText(ReminderActivity.this, "Cannot set reminder in the past", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Save to DB
                    SQLiteDatabase dbW = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_USERNAME_FK, currentUser);
                    values.put(DatabaseHelper.COLUMN_REMINDER_TITLE, title);
                    values.put(DatabaseHelper.COLUMN_REMINDER_DATE, dateStr);
                    values.put(DatabaseHelper.COLUMN_REMINDER_TIME, timeStr);
                    long id = dbW.insert(DatabaseHelper.TABLE_REMINDERS, null, values);

                    scheduleAlarm((int) id, title, calendar.getTimeInMillis());

                    Toast.makeText(ReminderActivity.this, "Reminder Set!", Toast.LENGTH_SHORT).show();
                    etReminderTitle.getText().clear();
                    etReminderDate.getText().clear();
                    etReminderTime.getText().clear();
                }
            } catch (Exception e) {
                Toast.makeText(ReminderActivity.this, "Invalid Date/Time format", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scheduleAlarm(int id, String title, long timeInMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("TITLE", title);
        intent.putExtra("MESSAGE", "It's time for your reminder!");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                }
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Exact alarm permission required", Toast.LENGTH_SHORT).show();
        }
    }
}
