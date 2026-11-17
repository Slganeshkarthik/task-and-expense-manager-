package com.example.hostel_pg_ass;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TaskAdapter taskAdapter;
    private final List<TaskAdapter.Task> taskList = new ArrayList<>();
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        SharedPreferences prefs = getSharedPreferences("HostelPrefs", MODE_PRIVATE);
        currentUser = prefs.getString("USERNAME", "default");

        dbHelper = new DatabaseHelper(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        EditText etTaskTitle = findViewById(R.id.etTaskTitle);
        EditText etTaskDate = findViewById(R.id.etTaskDate);
        EditText etTaskTime = findViewById(R.id.etTaskTime);
        Spinner spinnerPriority = findViewById(R.id.spinnerPriority);
        Button btnAddTask = findViewById(R.id.btnAddTask);
        ListView lvTasks = findViewById(R.id.lvTasks);

        // Date Picker for Task Date
        etTaskDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(TaskActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String dateFormatted = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                        etTaskDate.setText(dateFormatted);
                    }, year, month, day);
            datePickerDialog.show();
        });

        String[] priorities = {"Low", "Medium", "High"};
        spinnerPriority.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities));

        taskAdapter = new TaskAdapter(this, taskList, new TaskAdapter.OnDeleteListener() {
            @Override
            public void onDelete(TaskAdapter.Task task) {
                deleteTask(task.getId());
            }
        });
        lvTasks.setAdapter(taskAdapter);

        // Time Picker Dialog for Task Time
        etTaskTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(TaskActivity.this,
                    (view, hourOfDay, minuteOfHour) -> {
                        String amPm = hourOfDay >= 12 ? "PM" : "AM";
                        int formattedHour = hourOfDay % 12;
                        if (formattedHour == 0) formattedHour = 12;
                        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d %s", formattedHour, minuteOfHour, amPm);
                        etTaskTime.setText(timeFormatted);
                    }, hour, minute, false); // false = 12 hour format (AM/PM)
            timePickerDialog.show();
        });

        loadTasks();

        btnAddTask.setOnClickListener(v -> {
            String title = etTaskTitle.getText().toString().trim();
            String date = etTaskDate.getText().toString().trim();
            String time = etTaskTime.getText().toString().trim();
            String priority = spinnerPriority.getSelectedItem().toString();

            if (title.isEmpty()) {
                Toast.makeText(TaskActivity.this, "Title is required", Toast.LENGTH_SHORT).show();
                return;
            }

            addTask(title, date, time, priority);
            etTaskTitle.getText().clear();
            etTaskDate.getText().clear();
            etTaskTime.getText().clear();
        });
    }

    private void addTask(String title, String date, String time, String priority) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME_FK, currentUser);
        values.put(DatabaseHelper.COLUMN_TASK_TITLE, title);
        values.put(DatabaseHelper.COLUMN_TASK_DATE, date);
        values.put(DatabaseHelper.COLUMN_TASK_TIME, time);
        values.put(DatabaseHelper.COLUMN_TASK_PRIORITY, priority);
        values.put(DatabaseHelper.COLUMN_TASK_STATUS, "Pending");
        long id = db.insert(DatabaseHelper.TABLE_TASKS, null, values);

        if (id != -1 && !date.isEmpty() && !time.isEmpty()) {
            scheduleTaskAlarm((int) id, title, date, time);
        }

        loadTasks();
    }

    private void scheduleTaskAlarm(int id, String title, String dateStr, String timeStr) {
        try {
            // Task time is stored in 12-hour format "hh:mm a", e.g., "02:30 PM"
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            Date date = sdf.parse(dateStr + " " + timeStr);

            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent intent = new Intent(this, ReminderReceiver.class);
                    intent.putExtra("TITLE", "Task: " + title);
                    intent.putExtra("MESSAGE", "It's time to do your task!");

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            this,
                            id + 10000, // Offset ID so it doesn't collide with Reminders
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );

                    if (alarmManager != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        } else {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Task saved but alarm failed: Invalid time format", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateTaskStatus(int id, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TASK_STATUS, status);
        db.update(DatabaseHelper.TABLE_TASKS, values, DatabaseHelper.COLUMN_TASK_ID + "=?", new String[]{String.valueOf(id)});
    }

    private void deleteTask(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_TASKS, DatabaseHelper.COLUMN_TASK_ID + "=?", new String[]{String.valueOf(id)});
        loadTasks();
    }

    private void loadTasks() {
        taskList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_TASKS, 
                null, 
                DatabaseHelper.COLUMN_USERNAME_FK + "=?", 
                new String[]{currentUser}, 
                null, null, null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_TITLE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_DATE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_TIME));
                String priority = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_PRIORITY));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TASK_STATUS));

                taskList.add(new TaskAdapter.Task(id, title, date, time, priority, status));
            }
            cursor.close();
        }
        taskAdapter.notifyDataSetChanged();
    }
}
