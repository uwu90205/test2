package com.example.lab4_v2;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Кастомний адаптер для Spinner, який приховує перший елемент (підказку)
 * у випадаючому списку.
 */
public class HintSpinnerAdapter extends ArrayAdapter<CharSequence> {

    public HintSpinnerAdapter(@NonNull Context context, int resource, @NonNull CharSequence[] objects) {
        super(context, resource, objects);
    }

    @Override
    public int getCount() {
        // Повертаємо кількість елементів.
        // Якщо список порожній, повертаємо 0. Інакше повертаємо повну кількість.
        int count = super.getCount();
        return count > 0 ? count : 0;
    }

    @NonNull
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Цей метод відповідає за вигляд *випадаючого* списку

        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = (TextView) view;

        if (position == 0) {
            // Якщо це перший елемент (наша підказка "Платіжна адреса")
            // Робимо його невидимим і неклікабельним у випадаючому списку
            textView.setVisibility(View.GONE);
            textView.setHeight(0); // Повністю приховуємо
        } else {
            // Для всіх інших елементів (країн)
            // Робимо їх видимими
            textView.setVisibility(View.VISIBLE);
            textView.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT); // Повертаємо нормальну висоту
        }

        return view;
    }
}