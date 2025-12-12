package com.deposits.storage;

import com.deposits.model.Valyuta;
import com.deposits.model.Vklad;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HeneratorZvitivTest {

    private HeneratorZvitiv henerator;
    private Vklad mockVklad;
    private String generatedFileName; // Зберігаємо назву файлу, щоб видалити його потім

    @BeforeEach
    void setUp() {
        henerator = new HeneratorZvitiv();

        // Налаштовуємо мок вкладу
        mockVklad = mock(Vklad.class);
        when(mockVklad.getNazvaBanku()).thenReturn("TestBank");
        when(mockVklad.getNazvaVkladu()).thenReturn("Deposit Gold");
        when(mockVklad.getValyuta()).thenReturn(Valyuta.UAH); // Припускаємо enum UAH
        when(mockVklad.getTerminMisyatsiv()).thenReturn(12);
    }

    @AfterEach
    void tearDown() throws IOException {
        // ПРИБИРАННЯ: Якщо тест створив файл, видаляємо його
        if (generatedFileName != null) {
            Files.deleteIfExists(Path.of(generatedFileName));
        }
    }

    @Test
    @DisplayName("Перевірка створення файлу та коректності його вмісту")
    void testZberegtyDogovir_Success() throws IOException {
        // GIVEN
        String pib = "Petrenko Petro";
        double suma = 10000.0;
        double stavka = 15.5;
        double prybutok = 1550.0;

        // Очікувана назва файлу згідно логіки класу (пробіли -> підкреслення)
        generatedFileName = "dogovir_Petrenko_Petro.txt";

        // WHEN
        henerator.zberegtyDogovir(mockVklad, suma, stavka, prybutok, pib);

        // THEN
        Path path = Path.of(generatedFileName);

        // 1. Перевіряємо, чи файл фізично існує
        assertTrue(Files.exists(path), "Файл повинен бути створений на диску");

        // 2. Читаємо весь вміст файлу в рядок
        String content = Files.readString(path);

        // 3. Перевіряємо наявність ключових даних у тексті
        assertTrue(content.contains("Petrenko Petro"), "Має містити ім'я клієнта");
        assertTrue(content.contains("TestBank"), "Має містити назву банку");
        assertTrue(content.contains("Deposit Gold"), "Має містити назву вкладу");
        assertTrue(content.contains("10000.0"), "Має містити суму");
        assertTrue(content.contains("15.5%"), "Має містити ставку");

        // Перевірка форматування прибутку (%.2f -> два знаки після коми)
        // 1550.0 -> "1550,00" або "1550.00" залежно від локалі системи,
        // тому безпечніше перевірити просто цифри, або конкретний формат, якщо ми впевнені в локалі.
        // Але краще перевірити наявність дати:
        assertTrue(content.contains(LocalDate.now().toString()), "Має містити сьогоднішню дату");
    }

    @Test
    @DisplayName("Перевірка формування імені файлу (replace spaces)")
    void testFileNameFormatting() {
        // GIVEN
        String pib = "Taras Hryhorovych Shevchenko";
        generatedFileName = "dogovir_Taras_Hryhorovych_Shevchenko.txt";

        // WHEN
        henerator.zberegtyDogovir(mockVklad, 100, 10, 10, pib);

        // THEN
        assertTrue(Files.exists(Path.of(generatedFileName)),
                "Назва файлу має містити підкреслення замість пробілів");
    }

    // Тестувати IOException (catch block) складно без зміни коду (Refactoring),
    // оскільки FileWriter дуже важко змусити "зламатися" на сучасних ОС
    // без хитрощів з правами доступу.
    // Тому ми фокусуємось на "Success path".
}