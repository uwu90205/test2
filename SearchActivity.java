package com.example.lab4_v2;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    // Змінні для роботи з БД та елементами інтерфейсу
    private CardDatabaseHelper dbHelper;
    private ListView listView;
    private EditText editSearch;
    private List<CardItem> searchResults; // Список для зберігання знайдених карток
    private CardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Ініціалізація помічника бази даних
        dbHelper = new CardDatabaseHelper(this);

        // Прив'язка елементів макета
        listView = findViewById(R.id.list_search_results);
        editSearch = findViewById(R.id.edit_search_query);
        Button btnSearch = findViewById(R.id.btn_do_search);

        // Ініціалізація списку результатів
        searchResults = new ArrayList<>();

        // Обробник натискання кнопки "Знайти"
        btnSearch.setOnClickListener(v -> {
            String query = editSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                // Виконуємо пошук у базі даних за введеним запитом
                List<CardItem> results = dbHelper.searchCards(query);

                // Оновлюємо список результатів
                searchResults.clear();
                searchResults.addAll(results);

                // Перевіряємо, чи є результати
                if (searchResults.isEmpty()) {
                    Toast.makeText(this, "Нічого не знайдено", Toast.LENGTH_SHORT).show();
                    listView.setAdapter(null); // Очищаємо список, якщо порожньо
                } else {
                    // Створюємо та встановлюємо адаптер для відображення даних
                    adapter = new CardAdapter(this, searchResults);
                    listView.setAdapter(adapter);
                }
            } else {
                Toast.makeText(this, "Введіть текст для пошуку", Toast.LENGTH_SHORT).show();
            }
        });

        // --- НАЛАШТУВАННЯ CONTEXTUAL ACTION MODE (CAM) ---
        // Вмикаємо режим множинного вибору для списку
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        // Встановлюємо слухача подій для режиму дій (довге натискання)
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            // Створення меню (викликається при активації режиму)
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Завантажуємо меню з файлу ресурсів (кнопки Копіювати, Сортувати)
                getMenuInflater().inflate(R.menu.menu_cab, menu);
                return true;
            }

            // Підготовка меню (викликається перед відображенням)
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            // Обробка натискання на пункти меню
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int id = item.getItemId();

                // 1. Дія: КОПІЮВАННЯ
                if (id == R.id.action_copy) {
                    StringBuilder sb = new StringBuilder();
                    // Отримуємо позиції всіх вибраних елементів
                    android.util.SparseBooleanArray checked = listView.getCheckedItemPositions();

                    if (checked != null) {
                        // Проходимо по всіх елементах і шукаємо вибрані
                        for (int i = 0; i < checked.size(); i++) {
                            if (checked.valueAt(i)) {
                                int position = checked.keyAt(i);
                                // Перевіряємо, чи позиція в межах списку
                                if (position >= 0 && position < searchResults.size()) {
                                    CardItem card = searchResults.get(position);
                                    // Додаємо дані картки до рядка
                                    sb.append(card.getCardName())
                                            .append(" ")
                                            .append(card.getCardNumber())
                                            .append("\n");
                                }
                            }
                        }
                    }

                    String textToCopy = sb.toString().trim();

                    if (!textToCopy.isEmpty()) {
                        try {
                            // Отримуємо системний буфер обміну та копіюємо текст
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Search Result", textToCopy);
                            clipboard.setPrimaryClip(clip);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Виводимо скопійований текст у Toast
                        Toast.makeText(SearchActivity.this, textToCopy, Toast.LENGTH_SHORT).show();
                    }
                    mode.finish(); // Закриваємо режим дій
                    return true;
                }
                // 2. Дія: СОРТУВАННЯ А-Я
                else if (id == R.id.action_sort_asc) {
                    // Сортуємо список за ім'ям (прямий порядок)
                    Collections.sort(searchResults, (o1, o2) -> o1.getCardName().compareToIgnoreCase(o2.getCardName()));
                    adapter.notifyDataSetChanged(); // Оновлюємо відображення
                    return true;
                }
                // 3. Дія: СОРТУВАННЯ Я-А
                else if (id == R.id.action_sort_desc) {
                    // Сортуємо список за ім'ям (зворотний порядок)
                    Collections.sort(searchResults, (o1, o2) -> o2.getCardName().compareToIgnoreCase(o1.getCardName()));
                    adapter.notifyDataSetChanged(); // Оновлюємо відображення
                    return true;
                }

                return false;
            }

            // Дія при закритті меню (зняття виділення)
            @Override
            public void onDestroyActionMode(ActionMode mode) {}

            // Оновлення заголовка при зміні виділення
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Отримуємо кількість вибраних елементів
                int count = listView.getCheckedItemCount();
                // Встановлюємо заголовок панелі
                mode.setTitle("Вибрано: " + count);
            }
        });
    }
}