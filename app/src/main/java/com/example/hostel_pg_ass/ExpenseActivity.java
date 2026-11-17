package com.example.hostel_pg_ass;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;
import java.util.Calendar;
import android.app.DatePickerDialog;

public class ExpenseActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView tvTotalExpenses;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        SharedPreferences prefs = getSharedPreferences("HostelPrefs", MODE_PRIVATE);
        currentUser = prefs.getString("USERNAME", "default");

        dbHelper = new DatabaseHelper(this);

        EditText etExpenseAmount = findViewById(R.id.etExpenseAmount);
        Spinner spinnerCategory = findViewById(R.id.spinnerCategory);
        EditText etExpenseDesc = findViewById(R.id.etExpenseDesc);
        EditText etExpenseDate = findViewById(R.id.etExpenseDate);
        Button btnAddExpense = findViewById(R.id.btnAddExpense);
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);

        String[] categories = {"Food", "Transport", "Groceries", "Education", "Other"};
        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));

        // Date Picker Dialog for Expense Date
        etExpenseDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(ExpenseActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        String dateFormatted = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                        etExpenseDate.setText(dateFormatted);
                    }, year, month, day);
            datePickerDialog.show();
        });

        updateTotalExpenses();

        btnAddExpense.setOnClickListener(v -> {
            String amountStr = etExpenseAmount.getText().toString();
            String desc = etExpenseDesc.getText().toString();
            String date = etExpenseDate.getText().toString();
            String category = spinnerCategory.getSelectedItem().toString();

            if (amountStr.isEmpty()) {
                Toast.makeText(ExpenseActivity.this, "Amount is required", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = 0.0;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                amount = 0.0;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USERNAME_FK, currentUser);
            values.put(DatabaseHelper.COLUMN_EXPENSE_AMOUNT, amount);
            values.put(DatabaseHelper.COLUMN_EXPENSE_CATEGORY, category);
            values.put(DatabaseHelper.COLUMN_EXPENSE_DESC, desc);
            values.put(DatabaseHelper.COLUMN_EXPENSE_DATE, date);

            db.insert(DatabaseHelper.TABLE_EXPENSES, null, values);

            etExpenseAmount.getText().clear();
            etExpenseDesc.getText().clear();
            etExpenseDate.getText().clear();

            updateTotalExpenses();
            Toast.makeText(ExpenseActivity.this, "Expense Added", Toast.LENGTH_SHORT).show();
        });

        // Split Calculator logic
        EditText etSplitAmount = findViewById(R.id.etSplitAmount);
        EditText etSplitPeople = findViewById(R.id.etSplitPeople);
        Button btnCalculateSplit = findViewById(R.id.btnCalculateSplit);
        TextView tvSplitResult = findViewById(R.id.tvSplitResult);

        btnCalculateSplit.setOnClickListener(v -> {
            double amount = 0.0;
            try {
                amount = Double.parseDouble(etSplitAmount.getText().toString());
            } catch (NumberFormatException ignored) {}

            int people = 1;
            try {
                people = Integer.parseInt(etSplitPeople.getText().toString());
            } catch (NumberFormatException ignored) {}

            if (people > 0) {
                double perPerson = amount / people;
                tvSplitResult.setText(String.format(Locale.getDefault(), "Each Person: ₹%.2f", perPerson));
            } else {
                Toast.makeText(ExpenseActivity.this, "Enter valid number of people", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotalExpenses() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_EXPENSES, 
                new String[]{"SUM(" + DatabaseHelper.COLUMN_EXPENSE_AMOUNT + ")"}, 
                DatabaseHelper.COLUMN_USERNAME_FK + "=?", 
                new String[]{currentUser}, 
                null, null, null
        );
        double total = 0.0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        tvTotalExpenses.setText(String.format(Locale.getDefault(), "Total Expenses: ₹%.2f", total));
    }
}
