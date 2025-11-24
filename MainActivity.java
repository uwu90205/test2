package com.example.lab4_v2;

// ІМПОРТИ
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.Settings; // Для варіанту 19 (Налаштування)
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lab4_v2.databinding.ActivityMainBinding;

import java.util.Calendar;

/**
 * Головна активність (екран) додатка.
 * Відповідає за:
 * 1. Відображення форми введення даних картки.
 * 2. Валідацію введених даних.
 * 3. Збереження даних у базу даних SQLite.
 * 4. Роботу з меню трьох типів: Options, Popup, Context.
 */
public class MainActivity extends AppCompatActivity {

    // Об'єкт для прив'язки до елементів .xml-файлу (View Binding)
    private ActivityMainBinding binding;

    // Об'єкт для роботи з базою даних SQLite (замість файлової системи)
    private CardDatabaseHelper dbHelper;

    // Лаунчер для запуску іншої Activity (CardListActivity) та отримання результату (номеру картки)
    private ActivityResultLauncher<Intent> cardListLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Ініціалізуємо view binding (Першим кроком, щоб уникнути NullPointerException!)
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // 2. Встановлюємо макет на екран
        setContentView(binding.getRoot());

        // 3. Налаштовуємо Toolbar як ActionBar (щоб з'явилося меню "три крапки")
        setSupportActionBar(binding.toolbar);

        // Вимикаємо стандартний заголовок Toolbar-а, оскільки у нас є свій TextView
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Ініціалізація помічника бази даних
        dbHelper = new CardDatabaseHelper(this);

        // Встановлюємо слухача на кнопку КУПИТИ (запуск валідації та збереження)
        binding.buttonBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processPayment();
            }
        });

        // Налаштування допоміжних елементів UI
        setupLegalTextLinks(); // Клікабельні посилання "Умови..."
        setupCountrySpinner(); // Випадаючий список країн
        setupCardListLauncher(); // Підготовка до отримання результату з іншого вікна

        // ЗАВДАННЯ 2.2: Налаштування Popup Menu (Спливаюче меню)
        // При натисканні на текст "Додати кредитну..."
        binding.titleAddCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        // ЗАВДАННЯ 2.4: Налаштування Context Menu (Контекстне меню)
        // Реєструємо меню для TextView із заголовком комісії
        registerForContextMenu(binding.textFeeTitle);
        // Встановлюємо текст згідно з умовою завдання
        binding.textFeeTitle.setText("Build-In ContentProvider (Довго натисніть)");
    }

    // РОБОТА З МЕНЮ

    // 1. Options Menu (Головне меню - три крапки вгорі справа)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // "Надуваємо" меню з XML-ресурсу
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Обробка натискання на пункти Options Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String title = item.getTitle().toString();
        Intent intent = null;

        // Показуємо повідомлення про вибір
        Toast.makeText(this, "Обрано: " + title, Toast.LENGTH_SHORT).show();

        // Логіка вибору пунктів
        if (id == R.id.action_exit) {
            finishAffinity(); // Повністю закрити додаток
            return true;
        } else if (id == R.id.menu_location) {
            // Відкрити карту
            Uri gmmIntentUri = Uri.parse("geo:0,0");
            intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        } else if (id == R.id.menu_weather) {
            // Відкрити погоду в браузері
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=погода"));
        } else if (id == R.id.menu_date) {
            // Відкрити календар
            intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_CALENDAR);
        } else if (id == R.id.menu_time) {
            // Відкрити будильник
            intent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        } else if (id == R.id.menu_call) {
            // Відкрити набір номера
            intent = new Intent(Intent.ACTION_DIAL);
        }

        // Запуск відповідного Intent-у, якщо він був створений
        if (intent != null) {
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Помилка запуску", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // 2. Popup Menu (Спливаюче меню)
    private void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        // Завантажуємо структуру меню з XML
        popup.inflate(R.menu.menu_popup);

        // Обробка вибору пунктів
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.popup_show_all) {
                // Відкрити список всіх карток (CardListActivity)
                Intent intent = new Intent(MainActivity.this, CardListActivity.class);
                cardListLauncher.launch(intent);
                return true;
            } else if (id == R.id.popup_search) {
                // Відкрити вікно пошуку (SearchActivity)
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
        popup.show(); // Показати меню
    }

    // 3. Context Menu (Контекстне меню - при довгому натисканні)
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Вбудовані провайдери:");

        // Додаємо пункти меню згідно з умовою завдання
        menu.add(0, 1, 0, "Calendar");
        menu.add(0, 2, 0, "Contact lists");
        menu.add(0, 3, 0, "UserDictionary");
        menu.add(0, 4, 0, "Call logs");
        menu.add(0, 5, 0, "AlarmClock");
        menu.add(0, 6, 0, "Audio");
        menu.add(0, 7, 0, "Video");
        menu.add(0, 8, 0, "Images");
        // ВАРІАНТ 19: Settings (Налаштування)
        menu.add(0, 9, 0, "Settings");
        menu.add(0, 10, 0, "Browser");
    }

    // Обробка вибору пункту контекстного меню
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String title = item.getTitle().toString();

        // Якщо обрано ID 9 (Settings) - виконуємо дію
        if (item.getItemId() == 9) {
            try {
                // Відкриття системних налаштувань
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
                Toast.makeText(this, "Відкрито налаштування", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Помилка", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        // Для інших варіантів просто показуємо Toast, що ми їх натиснули
        else {
            Toast.makeText(this, "Обрано провайдер: " + title, Toast.LENGTH_SHORT).show();
        }

        return super.onContextItemSelected(item);
    }

    // ЛОГІКА ДОДАТКА (Допоміжні методи)

    // Налаштовує лаунчер для отримання результату з CardListActivity
    private void setupCardListLauncher() {
        cardListLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // Якщо користувач успішно обрав картку
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                // Отримуємо номер та заповнюємо поле
                                String cardNumber = data.getStringExtra("selectedCardNumber");
                                binding.editCardNumber.setText(cardNumber);
                                binding.errorCardNumber.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    }

    // Налаштовує клікабельні посилання в тексті (SpannableString)
    private void setupLegalTextLinks() {
        String fullText = getString(R.string.legal_text_plain);
        String linkText1 = "Примітка про конфіденційність";
        String linkText2 = "Умови використання Google Payments";

        SpannableString ss = new SpannableString(fullText);

        // Налаштування першого посилання
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openLegalPage("Примітка про конфіденційність", getString(R.string.privacy_policy_content), "privacy_agreed");
            }
        };
        int start1 = fullText.indexOf(linkText1);
        int end1 = start1 + linkText1.length();
        if(start1 != -1) { ss.setSpan(clickableSpan1, start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); }

        // Налаштування другого посилання
        ClickableSpan clickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openLegalPage("Умови використання Google Payments", getString(R.string.terms_of_service_content), "terms_agreed");
            }
        };
        int start2 = fullText.indexOf(linkText2);
        int end2 = start2 + linkText2.length();
        if(start2 != -1) { ss.setSpan(clickableSpan2, start2, end2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); }

        binding.textLegal.setText(ss);
        binding.textLegal.setMovementMethod(LinkMovementMethod.getInstance()); // Робимо посилання активними
    }

    // Допоміжний метод для відкриття сторінки з юридичною інформацією
    private void openLegalPage(String title, String content, String preferenceKey) {
        Intent intent = new Intent(this, LegalActivity.class);
        intent.putExtra("PAGE_TITLE", title);
        intent.putExtra("PAGE_CONTENT", content);
        intent.putExtra("PREFERENCE_KEY", preferenceKey);
        startActivity(intent);
    }

    // Налаштовує спінер країн з кастомним адаптером
    private void setupCountrySpinner() {
        CharSequence[] countries = getResources().getTextArray(R.array.countries_array);
        HintSpinnerAdapter adapter = new HintSpinnerAdapter(
                this,
                android.R.layout.simple_spinner_item,
                countries
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCountry.setAdapter(adapter);
    }

    // Основний метод обробки натискання кнопки КУПИТИ
    private void processPayment() {
        clearAllErrors(); // Очищаємо попередні помилки
        boolean hasError = false;

        // Зчитуємо дані з полів
        String cardNumber = binding.editCardNumber.getText().toString().trim();
        String cardMonthStr = binding.editCardMonth.getText().toString().trim();
        String cardYearStr = binding.editCardYear.getText().toString().trim();
        String cardCvc = binding.editCardCvc.getText().toString().trim();
        String cardName = binding.editCardName.getText().toString().trim();
        int countryPosition = binding.spinnerCountry.getSelectedItemPosition();

        // БЛОК ВАЛІДАЦІЇ
        // Перевірка на порожні поля
        if (TextUtils.isEmpty(cardNumber)) { showFieldError(binding.errorCardNumber, "Введіть номер карти"); hasError = true; }
        if (TextUtils.isEmpty(cardMonthStr)) { showFieldError(binding.errorCardMonth, "Введіть місяць"); hasError = true; }
        if (TextUtils.isEmpty(cardYearStr)) { showFieldError(binding.errorCardYear, "Введіть рік"); hasError = true; }
        if (TextUtils.isEmpty(cardCvc)) { showFieldError(binding.errorCardCvc, "ВВЕДІТЬ CVC"); hasError = true; }
        if (TextUtils.isEmpty(cardName)) { showFieldError(binding.errorCardName, "Введіть ім'я власника"); hasError = true; }
        if (countryPosition == 0) { showFieldError(binding.errorCountry, "Виберіть країну"); hasError = true; }

        if (hasError) return; // Якщо є порожні поля, зупиняємось

        // Детальна валідація (довжина, формат)
        if (cardNumber.length() != 16 || !TextUtils.isDigitsOnly(cardNumber)) { showFieldError(binding.errorCardNumber, "Неправильний номер"); hasError = true; }
        if (cardCvc.length() != 3 || !TextUtils.isDigitsOnly(cardCvc)) { showFieldError(binding.errorCardCvc, "3 цифри"); hasError = true; }
        if (cardName.matches(".*\\d.*")) { showFieldError(binding.errorCardName, "Ім'я не повинно містити цифр"); hasError = true; }

        // Валідація дати
        int month = -1;
        try {
            month = Integer.parseInt(cardMonthStr);
            if (month < 1 || month > 12) { showFieldError(binding.errorCardMonth, "01-12"); hasError = true; }
        } catch (NumberFormatException e) { showFieldError(binding.errorCardMonth, "Число!"); hasError = true; }

        int year = -1;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int currentYearShort = currentYear % 100;
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;

        try {
            year = Integer.parseInt(cardYearStr);
            int minYear = (currentYear - 5) % 100;
            int maxYear = (currentYear + 5) % 100;
            if (year < minYear || year > maxYear) { showFieldError(binding.errorCardYear, "Рік?"); hasError = true; }
            else if (year == currentYearShort && month != -1 && month < currentMonth) {
                showFieldError(binding.errorCardMonth, "Минув"); hasError = true;
            }
        } catch (NumberFormatException e) { showFieldError(binding.errorCardYear, "Число!"); hasError = true; }

        if (hasError) return; // Якщо детальна валідація не пройшла

        // ЗБЕРЕЖЕННЯ В БД
        // Викликаємо метод addCard нашого хелпера для запису в базу даних
        boolean isInserted = dbHelper.addCard(cardName, cardNumber);

        if (isInserted) {
            Toast.makeText(this, "Покупка успішна! (Дані збережено в БД)", Toast.LENGTH_LONG).show();
            // Очищення полів після успіху
            binding.editCardNumber.setText("");
            binding.editCardName.setText("");
        } else {
            Toast.makeText(this, "Помилка збереження в БД", Toast.LENGTH_SHORT).show();
        }
    }

    // Допоміжний метод: показує помилку під конкретним полем
    private void showFieldError(TextView errorTextView, String message) {
        if (errorTextView != null) {
            errorTextView.setText(message);
            errorTextView.setVisibility(View.VISIBLE);
        }
    }

    // Допоміжний метод: ховає всі повідомлення про помилки
    private void clearAllErrors() {
        binding.errorCardNumber.setVisibility(View.GONE);
        binding.errorCardMonth.setVisibility(View.GONE);
        binding.errorCardYear.setVisibility(View.GONE);
        binding.errorCardCvc.setVisibility(View.GONE);
        binding.errorCardName.setVisibility(View.GONE);
        binding.errorCountry.setVisibility(View.GONE);
    }
}