package com.deposits.ui.commands;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PokazatyVsiCommandTest {

    private ServisPidboru mockServis;
    private PokazatyVsiCommand command;

    // Для перехоплення консолі
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        // Створюємо мок сервісу
        mockServis = mock(ServisPidboru.class);

        // Створюємо команду з моком
        command = new PokazatyVsiCommand(mockServis);

        // Перенаправляємо вивід у нашу змінну outContent
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        // Обов'язково повертаємо консоль назад
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("execute: виводить повідомлення, якщо список порожній")
    void testExecute_EmptyList() {
        // GIVEN
        // Налаштовуємо сервіс на повернення порожнього списку
        when(mockServis.otrymatyVsi()).thenReturn(Collections.emptyList());

        // WHEN
        command.execute();

        // THEN
        String output = outContent.toString();

        // Перевіряємо заголовок
        assertTrue(output.contains("ВСІ ПРОПОЗИЦІЇ БАНКІВ"));
        // Перевіряємо повідомлення про порожнечу
        assertTrue(output.contains("(Список порожній)"));

        // Перевіряємо, що ми дійсно зверталися до сервісу
        verify(mockServis).otrymatyVsi();
    }

    @Test
    @DisplayName("execute: виводить нумерований список вкладів")
    void testExecute_WithData() {
        // GIVEN
        // Створюємо моки вкладів.
        // Важливо: команда використовує System.out.println(vklad), що викликає vklad.toString().
        // Тому ми повинні налаштувати поведінку саме toString().

        Vklad v1 = mock(Vklad.class);
        when(v1.toString()).thenReturn("PrivatBank 'Super'");

        Vklad v2 = mock(Vklad.class);
        when(v2.toString()).thenReturn("Mono 'Iron'");

        // Сервіс повертає список з двох вкладів
        when(mockServis.otrymatyVsi()).thenReturn(Arrays.asList(v1, v2));

        // WHEN
        command.execute();

        // THEN
        String output = outContent.toString();

        // Перевіряємо наявність заголовка
        assertTrue(output.contains("ВСІ ПРОПОЗИЦІЇ БАНКІВ"));

        // Перевіряємо, чи є перший елемент з правильним номером
        assertTrue(output.contains("1. PrivatBank 'Super'"));

        // Перевіряємо, чи є другий елемент з правильним номером
        assertTrue(output.contains("2. Mono 'Iron'"));

        // Перевіряємо розділювач в кінці
        assertTrue(output.contains("-----------------------------"));
    }

    @Test
    @DisplayName("getName: повертає правильну назву для меню")
    void testGetName() {
        String expectedName = "Переглянути всі пропозиції";
        assertEquals(expectedName, command.getName());
    }
}