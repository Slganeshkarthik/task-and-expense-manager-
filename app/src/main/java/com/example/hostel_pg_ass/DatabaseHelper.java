package com.example.hostel_pg_ass;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HostelPgAssist.db";
    // Incremented to v2 to recreate tables with username_fk column
    private static final int DATABASE_VERSION = 2;

    // Users Table
    public static final String TABLE_USERS = "Users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // Common Foreign Key for Data Isolation
    public static final String COLUMN_USERNAME_FK = "username_fk";

    // Tasks Table
    public static final String TABLE_TASKS = "Tasks";
    public static final String COLUMN_TASK_ID = "id";
    public static final String COLUMN_TASK_TITLE = "title";
    public static final String COLUMN_TASK_DATE = "date";
    public static final String COLUMN_TASK_TIME = "time";
    public static final String COLUMN_TASK_PRIORITY = "priority";
    public static final String COLUMN_TASK_STATUS = "status";

    // Expenses Table
    public static final String TABLE_EXPENSES = "Expenses";
    public static final String COLUMN_EXPENSE_ID = "id";
    public static final String COLUMN_EXPENSE_AMOUNT = "amount";
    public static final String COLUMN_EXPENSE_CATEGORY = "category";
    public static final String COLUMN_EXPENSE_DESC = "description";
    public static final String COLUMN_EXPENSE_DATE = "date";

    // Reminders Table
    public static final String TABLE_REMINDERS = "Reminders";
    public static final String COLUMN_REMINDER_ID = "id";
    public static final String COLUMN_REMINDER_TITLE = "title";
    public static final String COLUMN_REMINDER_DATE = "date";
    public static final String COLUMN_REMINDER_TIME = "time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT)";
        db.execSQL(createUsersTable);

        String createTasksTable = "CREATE TABLE " + TABLE_TASKS + " ("
                + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME_FK + " TEXT,"
                + COLUMN_TASK_TITLE + " TEXT,"
                + COLUMN_TASK_DATE + " TEXT,"
                + COLUMN_TASK_TIME + " TEXT,"
                + COLUMN_TASK_PRIORITY + " TEXT,"
                + COLUMN_TASK_STATUS + " TEXT)";
        db.execSQL(createTasksTable);

        String createExpensesTable = "CREATE TABLE " + TABLE_EXPENSES + " ("
                + COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME_FK + " TEXT,"
                + COLUMN_EXPENSE_AMOUNT + " REAL,"
                + COLUMN_EXPENSE_CATEGORY + " TEXT,"
                + COLUMN_EXPENSE_DESC + " TEXT,"
                + COLUMN_EXPENSE_DATE + " TEXT)";
        db.execSQL(createExpensesTable);

        String createRemindersTable = "CREATE TABLE " + TABLE_REMINDERS + " ("
                + COLUMN_REMINDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USERNAME_FK + " TEXT,"
                + COLUMN_REMINDER_TITLE + " TEXT,"
                + COLUMN_REMINDER_DATE + " TEXT,"
                + COLUMN_REMINDER_TIME + " TEXT)";
        db.execSQL(createRemindersTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        onCreate(db);
    }
}
