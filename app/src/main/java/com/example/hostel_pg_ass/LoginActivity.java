package com.example.hostel_pg_ass;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int loginAttempts = 0;
    private static final int maxAttempts = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        EditText etUsername = findViewById(R.id.etUsername);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvSignupRedirect = findViewById(R.id.tvSignupRedirect);

        btnLogin.setOnClickListener(v -> {
            if (loginAttempts >= maxAttempts) {
                Toast.makeText(LoginActivity.this, "Maximum login attempts reached", Toast.LENGTH_SHORT).show();
                btnLogin.setEnabled(false);
                return;
            }

            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_USER_ID},
                    DatabaseHelper.COLUMN_USERNAME + " = ? AND " + DatabaseHelper.COLUMN_PASSWORD + " = ?",
                    new String[]{username, password},
                    null, null, null
            );

            if (cursor != null && cursor.getCount() > 0) {
                cursor.close();
                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                // Save session using SharedPreferences
                SharedPreferences prefs = getSharedPreferences("HostelPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("USERNAME", username);
                editor.apply();

                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                loginAttempts++;
                int remaining = maxAttempts - loginAttempts;
                Toast.makeText(LoginActivity.this, "Login Failed. Attempts remaining: " + remaining, Toast.LENGTH_SHORT).show();

                if (loginAttempts >= maxAttempts) {
                    btnLogin.setEnabled(false);
                    Toast.makeText(LoginActivity.this, "Login disabled", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvSignupRedirect.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });
    }
}
