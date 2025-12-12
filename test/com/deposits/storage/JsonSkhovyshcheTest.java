package com.deposits.storage;

import com.deposits.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonSkhovyshcheTest {

    private JsonSkhovyshche skhovyshche;
    private Path tempFile; // Шлях до тимчасового файлу

    @BeforeEach
    void setUp() {
        skhovyshche = new JsonSkhovyshche();
    }

    @AfterEach
    void tearDown() throws IOException {
        // Очищення: видаляємо тимчасовий файл після кожного тесту
        if (tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("Успішне завантаження: розпізнає всі 3 типи вкладів")
    void testZavantazhyty_AllTypes() throws IOException {
        // GIVEN
        // Створюємо JSON рядок, який імітує вміст deposits.json
        // Ми додаємо по одному об'єкту кожного типу: TERM, FLEXIBLE, SAVINGS
        String jsonContent = "[\n" +
                "  { \"typ\": \"TERM\", \"nazvaBanku\": \"Bank Term\", \"nazvaVkladu\": \"T1\" },\n" +
                "  { \"typ\": \"FLEXIBLE\", \"nazvaBanku\": \"Bank Flex\", \"nazvaVkladu\": \"F1\", \"maksSuma\": 50000.0 },\n" +
                "  { \"typ\": \"SAVINGS\", \"nazvaBanku\": \"Bank Save\", \"nazvaVkladu\": \"S1\" }\n" +
                "]";

        // Створюємо справжній файл у тимчасовій папці системи
        tempFile = Files.createTempFile("test_deposits", ".json");
        Files.writeString(tempFile, jsonContent);

        // WHEN
        // Передаємо шлях до нашого тимчасового файлу
        KatalogVkladiv katalog = skhovyshche.zavantazhyty(tempFile.toString());
        List<Vklad> vklady = katalog.otrymatyVsiVklady();

        // THEN
        assertNotNull(katalog);
        assertEquals(3, vklady.size(), "Має завантажити рівно 3 вклади");

        // Перевіряємо, чи правильні класи були створені (Поліморфізм)
        assertTrue(vklady.get(0) instanceof StrokovyVklad, "Перший має бути StrokovyVklad");
        assertEquals("Bank Term", vklady.get(0).getNazvaBanku());

        assertTrue(vklady.get(1) instanceof HnuchkyVklad, "Другий має бути HnuchkyVklad");
        assertEquals("Bank Flex", vklady.get(1).getNazvaBanku());
        // Перевіримо унікальне поле гнучкого вкладу
        assertEquals(50000.0, ((HnuchkyVklad) vklady.get(1)).getMaksSuma());

        assertTrue(vklady.get(2) instanceof OschadnyVklad, "Третій має бути OschadnyVklad");
        assertEquals("Bank Save", vklady.get(2).getNazvaBanku());
    }

    @Test
    @DisplayName("Пропуск невідомих типів: ігнорує об'єкти з неправильним 'typ'")
    void testZavantazhyty_UnknownType() throws IOException {
        // GIVEN
        // JSON з одним правильним і одним невідомим типом
        String jsonContent = "[\n" +
                "  { \"typ\": \"TERM\", \"nazvaBanku\": \"Good Bank\" },\n" +
                "  { \"typ\": \"SUPER_UNKNOWN\", \"nazvaBanku\": \"Bad Bank\" }\n" +
                "]";

        tempFile = Files.createTempFile("test_unknown", ".json");
        Files.writeString(tempFile, jsonContent);

        // WHEN
        KatalogVkladiv katalog = skhovyshche.zavantazhyty(tempFile.toString());

        // THEN
        assertEquals(1, katalog.otrymatyVsiVklady().size(), "Має завантажити тільки відомий тип");
        assertEquals("Good Bank", katalog.otrymatyVsiVklady().get(0).getNazvaBanku());
    }

    @Test
    @DisplayName("Файл не знайдено: повертає порожній каталог (не падає)")
    void testZavantazhyty_FileNotFound() {
        // GIVEN
        String nonExistentPath = "shlyakh/yakoho/nemaie.json";

        // WHEN
        KatalogVkladiv katalog = skhovyshche.zavantazhyty(nonExistentPath);

        // THEN
        assertNotNull(katalog, "Метод не повинен повертати null навіть при помилці");
        assertTrue(katalog.otrymatyVsiVklady().isEmpty(), "Каталог має бути порожнім");

        // Примітка: В консолі ми побачимо "Помилка завантаження...", це нормально.
    }

    @Test
    @DisplayName("Порожній файл JSON: повертає порожній каталог")
    void testZavantazhyty_EmptyJson() throws IOException {
        // GIVEN
        String jsonContent = "[]"; // Порожній масив
        tempFile = Files.createTempFile("test_empty", ".json");
        Files.writeString(tempFile, jsonContent);

        // WHEN
        KatalogVkladiv katalog = skhovyshche.zavantazhyty(tempFile.toString());

        // THEN
        assertTrue(katalog.otrymatyVsiVklady().isEmpty());
    }
}