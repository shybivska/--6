package com.deposits.storage;

import com.deposits.model.KatalogVkladiv;

/**
 * Інтерфейс Сховища (Repository Interface).
 * * Його головна мета — приховати від решти програми те, ЯК САМЕ ми зберігаємо дані.
 * "Сервіс Підбору" не повинен знати, чи читаємо ми дані з JSON, з бази даних SQL,
 * чи з Excel-таблиці. Він просто знає, що існує "Сховище", яке вміє "завантажувати".
 * * Це дозволяє легко змінювати формат файлів у майбутньому, не ламаючи всю програму.
 */
public interface SkhovyshcheVkladiv {

    /**
     * Метод-контракт для завантаження даних.
     * * Будь-який клас, що реалізує (implements) цей інтерфейс, ЗОБОВ'ЯЗАНИЙ
     * написати код для цього методу.
     * * @param shlyakh Шлях до файлу (наприклад, "src/resources/deposits.json").
     * @return Готовий об'єкт KatalogVkladiv, наповнений даними.
     */
    KatalogVkladiv zavantazhyty(String shlyakh);
}