package com.deposits.storage;

import com.deposits.model.*;
import com.google.gson.*; // Імпортуємо бібліотеку GSON для роботи з JSON
import java.io.FileReader;
import java.io.IOException;

/**
 * Реалізація інтерфейсу SkhovyshcheVkladiv.
 * Цей клас відповідає за зчитування даних із файлу deposits.json
 * та створення на їх основі об'єктів-вкладів.
 */
public class JsonSkhovyshche implements SkhovyshcheVkladiv {

    /**
     * Основний метод завантаження.
     * @param shlyakh Шлях до файлу (наприклад, "src/main/resources/deposits.json")
     * @return Заповнений каталог вкладів.
     */
    @Override
    public KatalogVkladiv zavantazhyty(String shlyakh) {
        // Створюємо пустий каталог, куди будемо класти знайдені вклади
        KatalogVkladiv katalog = new KatalogVkladiv();

        // Створюємо інструмент Gson (це "магія", яка перетворює текст на об'єкти)
        Gson gson = new Gson();

        // Конструкція try-with-resources: автоматично закриє файл після читання
        try (FileReader reader = new FileReader(shlyakh)) {

            // 1. Читаємо весь файл і розбираємо його як масив JSON ([...])
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();

            // 2. Проходимося по кожному елементу масиву (по кожному вкладу)
            for (JsonElement element : array) {
                // Перетворюємо елемент на об'єкт JSON ({...})
                JsonObject obj = element.getAsJsonObject();

                // 3. Дивимось на поле "typ" ("TERM", "FLEXIBLE" або "SAVINGS"),
                // щоб зрозуміти, який саме клас створювати.
                // Це називається поліморфна десеріалізація.
                String typ = obj.get("typ").getAsString();

                Vklad vklad = null;

                // 4. Створюємо конкретний об'єкт залежно від типу
                switch (typ) {
                    case "TERM":
                        // Якщо тип TERM, створюємо StrokovyVklad
                        vklad = gson.fromJson(obj, StrokovyVklad.class);
                        break;
                    case "FLEXIBLE":
                        // Якщо тип FLEXIBLE, створюємо HnuchkyVklad
                        vklad = gson.fromJson(obj, HnuchkyVklad.class);
                        break;
                    case "SAVINGS":
                        // Якщо тип SAVINGS, створюємо OschadnyVklad
                        vklad = gson.fromJson(obj, OschadnyVklad.class);
                        break;
                }

                // 5. Якщо вклад успішно створено, додаємо його в наш каталог
                if (vklad != null) {
                    katalog.dodatyVklad(vklad);
                }
            }
        } catch (IOException e) {
            // Якщо файлу немає або його не можна прочитати, виводимо помилку
            System.out.println("Помилка завантаження JSON: " + e.getMessage());
        }

        // Повертаємо готовий каталог, наповнений даними
        return katalog;
    }
}