package com.example.lab4_v2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class CardDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cards_db"; // Назва файлу БД
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "cards";

    // Назви стовпців
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_NUMBER = "number";

    public CardDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL-запит для створення таблиці
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT, " +
                COL_NUMBER + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Метод для додавання картки (Замість запису у файл)
    public boolean addCard(String name, String number) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_NUMBER, number);

        long result = db.insert(TABLE_NAME, null, contentValues);
        // Якщо result == -1, сталася помилка
        return result != -1;
    }

    // Метод для пошуку карток за ім'ям (Для вікна пошуку)
    public List<CardItem> searchCards(String query) {
        List<CardItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Використовуємо оператор LIKE для пошуку підрядка
        // %query% означає "будь-який текст до і після запиту"
        String selection = COL_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%"};

        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                // Отримуємо дані з курсора (індекси: 0-id, 1-name, 2-number)
                String name = cursor.getString(1);
                String number = cursor.getString(2);
                list.add(new CardItem(name, number));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // Метод для отримання ВСІХ карток
    public List<CardItem> getAllCards() {
        List<CardItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new CardItem(cursor.getString(1), cursor.getString(2)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}