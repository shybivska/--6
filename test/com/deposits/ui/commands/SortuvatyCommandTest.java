package com.deposits.ui.commands;

import com.deposits.model.Valyuta;
import com.deposits.model.Vklad;
import com.deposits.service.ServisPidboru;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SortuvatyCommandTest {

    private ServisPidboru mockServis;
    private SortuvatyCommand command;

    // Перехоплення консолі
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        // Встановлюємо локаль US, щоб у printf числа виводились з крапкою (10.50),
        // бо це стандарт для більшості CI/CD систем.
        Locale.setDefault(Locale.US);

        mockServis = mock(ServisPidboru.class);
        command = new SortuvatyCommand(mockServis);

        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // --- ТЕСТ 1: Список порожній ---

    @Test
    @DisplayName("execute: якщо список порожній -> виводить помилку і виходить")
    void testExecute_EmptyList() {
        // GIVEN
        when(mockServis.getOstanniZnaydeni()).thenReturn(Collections.emptyList());

        // WHEN
        command.execute();

        // THEN
        String output = outContent.toString();
        assertTrue(output.contains("Список порожній"), "Має попередити користувача");

        // Переконуємось, що сортування НЕ запускалось
        verify(mockServis, never()).sortuvatyZaVygodoyu();
    }

    // --- ТЕСТ 2: Сума НЕВІДОМА (клієнт не вводив суму) ---

    @Test
    @DisplayName("execute: сума = 0 -> сортує і виводить простий список")
    void testExecute_UnknownSum() {
        // GIVEN
        // 1. Мокаємо вклад
        Vklad v1 = mock(Vklad.class);
        when(v1.toString()).thenReturn("Bank 'Test'"); // Простий toString

        // 2. Налаштовуємо сервіс
        when(mockServis.getOstanniZnaydeni()).thenReturn(List.of(v1));
        when(mockServis.getOstanniaSuma()).thenReturn(0.0); // Сума невідома

        // WHEN
        command.execute();

        // THEN
        String output = outContent.toString();

        // Перевіряємо виклик сортування
        verify(mockServis).sortuvatyZaVygodoyu();

        // Перевіряємо вивід (має бути просто toString)
        assertTrue(output.contains("ВІДСОРТОВАНІ РЕЗУЛЬТАТИ"));
        assertTrue(output.contains("1. Bank 'Test'"));

        // Перевіряємо, що складний формат (з прибутком) НЕ виводився
        assertFalse(output.contains("Прибуток:"), "Не має показувати прибуток, якщо сума 0");
    }

    // --- ТЕСТ 3: Сума ВІДОМА (клієнт шукав на конкретну суму) ---

    @Test
    @DisplayName("execute: сума > 0 -> сортує і виводить детальний розрахунок")
    void testExecute_KnownSum() {
        // GIVEN
        double knownSum = 10000.0;

        // 1. Мокаємо вклад
        Vklad v1 = mock(Vklad.class);
        when(v1.toString()).thenReturn("Privat 'Deposit'");
        when(v1.getValyuta()).thenReturn(Valyuta.UAH);
        // Мокаємо методи розрахунків (які викликаються всередині printf)
        when(v1.getStavkaDlyaSumy(knownSum)).thenReturn(12.5); // 12.5%

        // 2. Налаштовуємо сервіс
        when(mockServis.getOstanniZnaydeni()).thenReturn(List.of(v1));
        when(mockServis.getOstanniaSuma()).thenReturn(knownSum); // Сума є!
        when(mockServis.rozrakhuvatyPrybutok(v1, knownSum)).thenReturn(1250.0); // Прибуток

        // WHEN
        command.execute();

        // THEN
        String output = outContent.toString();

        verify(mockServis).sortuvatyZaVygodoyu();

        // Перевіряємо наявність детальної інформації у виводі
        // Очікуваний рядок: "1. Privat 'Deposit' | Ставка: 12.50% | Прибуток: 1250.00 UAH"

        assertTrue(output.contains("Privat 'Deposit'"), "Має бути назва");
        assertTrue(output.contains("Ставка: 12.50%"), "Має бути відформатована ставка");
        assertTrue(output.contains("Прибуток: 1250.00 UAH"), "Має бути розрахований прибуток");
    }

    @Test
    @DisplayName("getName: повертає коректну назву")
    void testGetName() {
        assertEquals("Відсортувати результати", command.getName());
    }
}