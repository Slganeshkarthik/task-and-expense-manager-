package com.example.hostel_pg_ass;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Use SharedPreferences to check if a user is already logged in
        SharedPreferences prefs = getSharedPreferences("HostelPrefs", MODE_PRIVATE);
        String username = prefs.getString("USERNAME", null);

        if (username != null && !username.isEmpty()) {
            // User is already logged in, go straight to Home
            startActivity(new Intent(this, HomeActivity.class));
        } else {
            // No user logged in, go to Login
            startActivity(new Intent(this, LoginActivity.class));
        }
        
        // Finish MainActivity so the user can't press 'Back' to return to this empty routing screen
        finish();
    }
}
