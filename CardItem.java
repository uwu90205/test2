package com.example.lab4_v2; // Ваш пакет

/**
 * Простий клас-модель (POJO - Plain Old Java Object).
 * Його єдина мета - зберігати дані про одну банківську картку:
 * ім'я власника та її номер.
 */
public class CardItem {

    // Поля класу, що зберігають дані
    private String cardName;   // Ім'я власника картки
    private String cardNumber; // Повний номер картки

    /**
     * Конструктор класу.
     * Викликається, коли ми створюємо новий об'єкт CardItem.
     * @param cardName Ім'я, яке треба зберегти
     * @param cardNumber Номер, який треба зберегти
     */
    public CardItem(String cardName, String cardNumber) {
        this.cardName = cardName;     // this.cardName - це поле класу (вгорі)
        // cardName - це параметр, який прийшов у метод
        this.cardNumber = cardNumber;
    }

    /**
     * Геттер (Getter) - метод для отримання імені власника.
     * @return Збережене ім'я
     */
    public String getCardName() {
        return cardName;
    }

    /**
     * Геттер (Getter) - метод для отримання повного номера картки.
     * @return Збережений номер картки
     */
    public String getCardNumber() {
        return cardNumber;
    }
}