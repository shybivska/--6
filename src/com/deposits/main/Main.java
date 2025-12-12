package com.deposits.main;

import com.deposits.service.ServisPidboru;
import com.deposits.storage.JsonSkhovyshche;
import com.deposits.ui.KonsolneMenyu;
import com.deposits.ui.commands.*;
//import com.dinstone.loghub.LoggerFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Головний клас (Client у термінології патерну Command).
 * Його завдання — створити всі компоненти ("деталі конструктора")
 * і з'єднати їх між собою перед запуском.
 */
public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) {
        // --- 1. Етап Ініціалізації Даних (Data Layer) ---

        // Вказуємо шлях до нашої "бази даних" (JSON-файлу)
        String shlyakh = "resources/deposits.json";

        // Створюємо об'єкт Сховища, який вміє читати JSON
        JsonSkhovyshche skhovyshche = new JsonSkhovyshche();

        // Створюємо Сервіс (Receiver/Виконавець).
        // Ми передаємо йому 'skhovyshche' та 'shlyakh', щоб він одразу
        // завантажив список вкладів у пам'ять при старті програми.
        ServisPidboru servis = new ServisPidboru(skhovyshche, shlyakh);

        // --- 2. Етап Ініціалізації Інтерфейсу (UI Layer) ---

        // Створюємо Меню (Invoker/Викликач). Поки що воно пусте.
        KonsolneMenyu menu = new KonsolneMenyu();

        // --- 3. Налаштування Патерну Command ---
        // Ми "програмуємо" кнопки меню.
        // Кожній цифрі ("1", "2"...) ми призначаємо об'єкт конкретної Команди.
        // Важливо: ми передаємо 'servis' у кожну команду, щоб вона знала, до кого звертатися.

        menu.addCommand("1", new ZnaytyVkladCommand(servis)); // Запуск майстра підбору
        menu.addCommand("2", new PokazatyVsiCommand(servis)); // Вивід всього списку
        menu.addCommand("3", new SortuvatyCommand(servis));   // Сортування результатів
        menu.addCommand("4", new DetaliVkladuCommand(servis));
        menu.addCommand("5", new OformytyCommand(servis));    // Генерація договору
        menu.addCommand("0", new VykhidCommand());            // Завершення роботи

        logger.log(Level.ERROR,"ERROR!!!");
        // --- 4. Запуск Програми ---
        // Запускаємо нескінченний цикл відображення меню.
        // Програма працюватиме всередині цього методу, доки не викличуть VykhidCommand.
        menu.run();
    }
}