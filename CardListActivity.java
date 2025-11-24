package com.example.lab4_v2;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CardListActivity extends AppCompatActivity {

    ListView listView;
    List<CardItem> cardList;
    CardAdapter adapter;
    CardDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        listView = findViewById(R.id.listView_cards);
        dbHelper = new CardDatabaseHelper(this);
        cardList = new ArrayList<>();

        // Завантаження даних з БД
        loadCardDataFromDB();

        adapter = new CardAdapter(this, cardList);
        listView.setAdapter(adapter);

        // Звичайний клік: повернення результату (вибір картки)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CardItem selectedCard = cardList.get(position);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedCardNumber", selectedCard.getCardNumber());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        // ЗАВДАННЯ 2.3: Contextual Action Mode (CAM)

        // Вмикаємо режим вибору (для довгого натискання)
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        // Встановлюємо слухача подій CAM
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // "Надуваємо" меню з XML файлу
                getMenuInflater().inflate(R.menu.menu_cab, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.action_copy) {
                    android.util.SparseBooleanArray checked = listView.getCheckedItemPositions();

                    if (checked != null) {
                        for (int i = 0; i < checked.size(); i++) {
                            if (checked.valueAt(i)) {
                                int position = checked.keyAt(i);

                                // Додаткова перевірка, щоб не вийти за межі списку
                                if (position >= 0 && position < cardList.size()) {
                                    CardItem card = cardList.get(position);
                                    String textToCopy = "Власник: " + card.getCardName() + ", Номер: " + card.getCardNumber();

                                    // БЕЗПЕЧНЕ КОПІЮВАННЯ
                                    try {
                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        if (clipboard != null) {
                                            ClipData clip = ClipData.newPlainText("Card Data", textToCopy);
                                            clipboard.setPrimaryClip(clip);
                                        }
                                    } catch (Exception e) {
                                        // Якщо буфер емулятора "відвалився", просто ігноруємо помилку
                                        e.printStackTrace();
                                    }

                                    // Все одно показуємо Toast з текстом, як вимагає завдання
                                    Toast.makeText(CardListActivity.this, textToCopy, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                        }
                    }
                    mode.finish();
                    return true;
                }

                // 2. Сортування (Прямий порядок А-Я)
                else if (id == R.id.action_sort_asc) {
                    Collections.sort(cardList, new Comparator<CardItem>() {
                        @Override
                        public int compare(CardItem o1, CardItem o2) {
                            return o1.getCardName().compareToIgnoreCase(o2.getCardName());
                        }
                    });
                    adapter.notifyDataSetChanged();
                    Toast.makeText(CardListActivity.this, "Сортування: А-Я", Toast.LENGTH_SHORT).show();
                    return true;
                }

                // 2. Сортування (Зворотний порядок Я-А)
                else if (id == R.id.action_sort_desc) {
                    Collections.sort(cardList, new Comparator<CardItem>() {
                        @Override
                        public int compare(CardItem o1, CardItem o2) {
                            return o2.getCardName().compareToIgnoreCase(o1.getCardName());
                        }
                    });
                    adapter.notifyDataSetChanged();
                    Toast.makeText(CardListActivity.this, "Сортування: Я-А", Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Дія при закритті меню (зняття виділення)
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Оновлення заголовка (наприклад, кількість вибраних)
                int count = listView.getCheckedItemCount();
                mode.setTitle("Вибрано: " + count);
            }
        });
    }

    private void loadCardDataFromDB() {
        List<CardItem> dbData = dbHelper.getAllCards();
        cardList.clear();
        cardList.addAll(dbData);

        if (cardList.isEmpty()) {
            Toast.makeText(this, "Список порожній (додайте картку в головному вікні)", Toast.LENGTH_LONG).show();
        }
    }
}