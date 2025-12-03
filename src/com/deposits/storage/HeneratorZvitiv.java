package com.deposits.storage;

import com.deposits.model.Vklad;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Клас, що відповідає за генерацію та збереження текстових звітів.
 * Він реалізує функціональну вимогу FR6.3 (Генерація звіту/договору).
 * Знаходиться в пакеті storage, оскільки працює з файловою системою (збереженням).
 */
public class HeneratorZvitiv {

    /**
     * Головний метод для створення файлу.
     * Він приймає всі необхідні дані, форматує їх у рядок і записує на диск.
     *
     * @param vklad    Об'єкт обраного вкладу (для назви банку, валюти тощо).
     * @param suma     Сума вкладу, яку ввів користувач.
     * @param stavka   Персональна ставка (вже розрахована Сервісом, бо залежить від суми).
     * @param prybutok Сума прибутку (вже розрахована Сервісом).
     * @param pib      Прізвище, Ім'я, По батькові клієнта.
     */
    // ОНОВЛЕНИЙ МЕТОД (5 параметрів)
    public void zberegtyDogovir(Vklad vklad, double suma, double stavka, double prybutok, String pib) {

        // 1. Формуємо назву файлу.
        // replaceAll(" ", "_") замінює пробіли на підкреслення.
        // Якщо клієнт "Іванов Іван", файл буде "dogovir_Іванов_Іван.txt" (це безпечніше для Windows).
        String nazvaFailu = "dogovir_" + pib.replaceAll(" ", "_") + ".txt";

        // 2. Формуємо текст самого договору.
        // Використовуємо \n для переходу на новий рядок.
        // String.format("%.2f", prybutok) — округлює прибуток до 2 знаків після коми (копійки).
        String tekst = "=== ПОПЕРЕДНІЙ ДОГОВІР ===\n" +
                "Дата: " + LocalDate.now() + "\n" + // Поточна дата
                "Клієнт: " + pib + "\n" +
                "--------------------------\n" +
                "Банк: " + vklad.getNazvaBanku() + "\n" +
                "Вклад: " + vklad.getNazvaVkladu() + "\n" +
                "Сума вкладу: " + suma + " " + vklad.getValyuta() + "\n" +
                "Термін: " + vklad.getTerminMisyatsiv() + " міс.\n" +
                "Персональна ставка: " + stavka + "%\n" +
                "Очікуваний прибуток: " + String.format("%.2f", prybutok) + " " + vklad.getValyuta() + "\n" +
                "--------------------------\n" +
                "Дякуємо, що обрали наш сервіс!";

        // 3. Записуємо у файл.
        // Конструкція try(...) автоматично закриває файл після запису (close).
        try (FileWriter writer = new FileWriter(nazvaFailu)) {
            writer.write(tekst); // Записуємо сформований текст
            System.out.println("Договір успішно збережено у файл: " + nazvaFailu);
        } catch (IOException e) {
            // Якщо немає місця на диску або немає прав доступу
            System.out.println("Помилка запису: " + e.getMessage());
        }
    }
}