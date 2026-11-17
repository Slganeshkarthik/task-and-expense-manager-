package com.example.hostel_pg_ass;

import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.Chronometer;
import androidx.appcompat.app.AppCompatActivity;

public class TimerActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private boolean isRunning = false;
    private long pauseOffset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        chronometer = findViewById(R.id.chronometer);
        Button btnStartTimer = findViewById(R.id.btnStartTimer);
        Button btnStopTimer = findViewById(R.id.btnStopTimer);

        btnStartTimer.setOnClickListener(v -> {
            if (!isRunning) {
                chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                chronometer.start();
                isRunning = true;
            }
        });

        btnStopTimer.setOnClickListener(v -> {
            if (isRunning) {
                chronometer.stop();
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
                isRunning = false;
            } else {
                // If stopped again, reset it
                chronometer.setBase(SystemClock.elapsedRealtime());
                pauseOffset = 0;
            }
        });
    }
}
