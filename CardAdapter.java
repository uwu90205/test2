package com.example.lab4_v2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class CardAdapter extends ArrayAdapter<CardItem> {

    public CardAdapter(@NonNull Context context, @NonNull List<CardItem> cardList) {
        super(context, 0, cardList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Отримуємо вигляд, якщо він ще не створений
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item_card, parent, false);
        }

        // Отримуємо поточний об'єкт CardItem
        CardItem currentCard = getItem(position);

        // Знаходимо TextView у нашому макеті list_item_card.xml
        TextView nameTextView = listItemView.findViewById(R.id.text_card_name);
        TextView numberTextView = listItemView.findViewById(R.id.text_card_number);

        if (currentCard != null) {
            // Встановлюємо дані
            nameTextView.setText(currentCard.getCardName());

            // Маскуємо номер картки, показуючи лише останні 4 цифри
            String fullNumber = currentCard.getCardNumber();
            String maskedNumber = "**** **** **** " + fullNumber.substring(fullNumber.length() - 4);
            numberTextView.setText(maskedNumber);
        }

        return listItemView;
    }
}