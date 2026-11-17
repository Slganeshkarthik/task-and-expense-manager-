package com.example.hostel_pg_ass;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SharedPreferences prefs = getSharedPreferences("HostelPrefs", MODE_PRIVATE);
        String username = prefs.getString("USERNAME", "User");

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvWelcome.setText("Welcome, " + username + "! 👋");

        findViewById(R.id.btnTasks).setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, TaskActivity.class)));
        findViewById(R.id.btnExpenses).setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ExpenseActivity.class)));
        findViewById(R.id.btnTimer).setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, TimerActivity.class)));
        findViewById(R.id.btnReminders).setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ReminderActivity.class)));
        findViewById(R.id.btnRoomVibes).setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, WallpaperActivity.class)));

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            prefs.edit().clear().apply();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        });
    }
}
