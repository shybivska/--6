package com.deposits.storage;

import com.deposits.model.Vklad;
// 1. Імпорти Log4j2
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Клас, що відповідає за генерацію та збереження текстових звітів.
 */
public class HeneratorZvitiv {
    // 2. Ініціалізація логера
    private static final Logger logger = LogManager.getLogger(HeneratorZvitiv.class);

    /**
     * Головний метод для створення файлу.
     */
    public void zberegtyDogovir(Vklad vklad, double suma, double stavka, double prybutok, String pib) {

        // Логуємо намір створити договір
        logger.log(Level.INFO, "Спроба генерації договору для клієнта: " + pib);

        // 1. Формуємо назву файлу.
        String nazvaFailu = "dogovir_" + pib.replaceAll(" ", "_") + ".txt";

        // 2. Формуємо текст самого договору.
        String tekst = "=== ПОПЕРЕДНІЙ ДОГОВІР ===\n" +
                "Дата: " + LocalDate.now() + "\n" +
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
        try (FileWriter writer = new FileWriter(nazvaFailu)) {
            writer.write(tekst);
            System.out.println("Договір успішно збережено у файл: " + nazvaFailu);

            // Логуємо успіх
            logger.log(Level.INFO, "Файл успішно створено: " + nazvaFailu);

        } catch (IOException e) {
            System.out.println("Помилка запису: " + e.getMessage());

            // Логуємо помилку (Це піде на E-MAIL, бо рівень ERROR)
            // Ми передаємо 'e' третім параметром, щоб в лог записався повний StackTrace помилки
            logger.log(Level.ERROR, "Помилка I/O при збереженні договору у файл: " + nazvaFailu, e);
        }
    }
}