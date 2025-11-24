package com.example.lab4_v2;

// Імпорти системних класів
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences; // Для збереження стану "галочки"
import android.os.Build; // Для перевірки версії Android
import android.os.Bundle;
import android.text.Html; // Для обробки HTML-тегів у тексті
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

/**
 * Актівіті (екран) для показу юридичних текстів.
 * Отримує текст з MainActivity, показує CheckBox "Приймаю"
 * і зберігає стан цього CheckBox.
 */
public class LegalActivity extends AppCompatActivity {

    // Змінні для UI елементів
    TextView titleView;
    TextView contentView;
    CheckBox checkBox;
    Button okButton;

    // Об'єкт для роботи з SharedPreferences (маленьке сховище ключ-значення)
    private SharedPreferences prefs;
    // Назва файлу, де Android зберігатиме наші налаштування
    private static final String PREFS_NAME = "LegalAgreements";
    // Унікальний ключ для конкретної "галочки" (напр. "privacy_agreed" або "terms_agreed")
    private String preferenceKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // "Надуваємо" (відображаємо) наш XML-макет
        setContentView(R.layout.activity_legal);

        // Знаходимо елементи на макеті за їх ID
        titleView = findViewById(R.id.legal_title);
        contentView = findViewById(R.id.legal_text_content);
        checkBox = findViewById(R.id.checkbox_agree);
        okButton = findViewById(R.id.button_ok);

        // Ініціалізуємо сховище, відкриваючи наш файл налаштувань
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Отримання даних з MainActivity
        Intent intent = getIntent(); // Отримуємо "намір", яким нас запустили
        // Витягуємо дані (тексти, ключ), які MainActivity поклала в інтент
        String title = intent.getStringExtra("PAGE_TITLE");
        String content = intent.getStringExtra("PAGE_CONTENT");
        preferenceKey = intent.getStringExtra("PREFERENCE_KEY"); // Ключ, за яким будемо зберігати стан

        // Налаштування UI
        // Встановлюємо заголовок вікна
        titleView.setText(title);

        // Встановлюємо основний текст.
        // Текст може містити HTML-теги (напр. <b> для жирного),
        // тому використовуємо Html.fromHtml() для їх коректної обробки.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Новий, правильний метод для Android 7.0 (API 24) і вище
            contentView.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
        } else {
            // Старий (deprecated) метод для версій Android нижче 7.0
            contentView.setText(Html.fromHtml(content));
        }

        // Логіка CheckBox та збереження

        // Завантажуємо збережений стан "галочки" (true/false) за нашим ключем.
        // Якщо нічого не збережено (перший запуск), `false` буде значенням за замовчуванням.
        boolean isAgreed = prefs.getBoolean(preferenceKey, false);

        // Встановлюємо "галочку" відповідно до збереженого стану
        checkBox.setChecked(isAgreed);
        // Робимо кнопку "ОК" активною/неактивною залежно від стану
        okButton.setEnabled(isAgreed);

        // Встановлюємо "слухача", який спрацює щоразу, коли користувач
        // натискає на CheckBox.
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Синхронізуємо стан кнопки "ОК": "галочка" є - кнопка активна.
                okButton.setEnabled(isChecked);

                // Негайно зберігаємо новий стан "галочки" у SharedPreferences
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(preferenceKey, isChecked); // Кладемо true або false
                editor.apply(); // Зберігаємо (швидко, асинхронно)
            }
        });

        // Встановлюємо "слухача" на кнопку "ОК"
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Закриваємо поточну Актівіті (LegalActivity)
                // і повертаємося до попередньої (MainActivity).
                finish();
            }
        });
    }
}