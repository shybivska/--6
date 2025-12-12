package com.deposits.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KonsolneMenyuTest {

    private KonsolneMenyu menu;
    private Command mockCommand;

    // Зберігаємо оригінальні потоки
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        // Перехоплюємо вивід (System.out)
        System.setOut(new PrintStream(outContent));

        // Створюємо мок команди
        mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("Test Command");
    }

    @AfterEach
    void tearDown() {
        // Відновлюємо потоки
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("run: відображає меню та виконує обрану команду")
    void testRun_ValidSelection() {
        // GIVEN
        // Емулюємо ввід: користувач вводить "1", натискає Enter.
        provideInput("1\n");

        // Ініціалізуємо меню ПІСЛЯ налаштування вводу (щоб Scanner підхопив наш потік)
        menu = new KonsolneMenyu();
        menu.addCommand("1", mockCommand);

        // ХИТРІСТЬ: Щоб розірвати нескінченний цикл while(true),
        // ми змушуємо команду кинути RuntimeException.
        doThrow(new BreakLoopException()).when(mockCommand).execute();

        // WHEN
        try {
            menu.run();
        } catch (BreakLoopException e) {
            // Ігноруємо цю помилку, бо ми самі її викликали, щоб вийти з тесту
        }

        // THEN
        String output = outContent.toString();

        // 1. Перевіряємо, що меню відобразилось
        assertTrue(output.contains("=== БАНКІВСЬКИЙ ПОМІЧНИК ==="));
        assertTrue(output.contains("1. Test Command"), "Має вивести назву команди");

        // 2. Перевіряємо, що метод execute() був викликаний
        verify(mockCommand, times(1)).execute();
    }

    @Test
    @DisplayName("run: обробляє невірний ввід і продовжує роботу")
    void testRun_InvalidInput() {
        // GIVEN
        // Емулюємо ввід:
        // "99" (невірний код) -> Enter -> "1" (вірний код, щоб вийти з циклу) -> Enter
        provideInput("99\n1\n");

        menu = new KonsolneMenyu();
        menu.addCommand("1", mockCommand);

        // Налаштовуємо вихід з циклу на команді "1"
        doThrow(new BreakLoopException()).when(mockCommand).execute();

        // WHEN
        try {
            menu.run();
        } catch (BreakLoopException e) {
            // Вихід з циклу
        }

        // THEN
        String output = outContent.toString();

        // Перевіряємо, що програма повідомила про помилку
        assertTrue(output.contains("Невірна команда"), "Має повідомити про невірний вибір");

        // Перевіряємо, що після помилки програма все одно виконала команду "1"
        verify(mockCommand, times(1)).execute();
    }

    @Test
    @DisplayName("addCommand: команди зберігаються у порядку додавання")
    void testMenuOrder() {
        // GIVEN
        Command cmd1 = mock(Command.class);
        when(cmd1.getName()).thenReturn("First");

        Command cmd2 = mock(Command.class);
        when(cmd2.getName()).thenReturn("Second");

        provideInput("1\n"); // Просто щоб запустити і одразу вийти

        menu = new KonsolneMenyu();
        menu.addCommand("1", cmd1);
        menu.addCommand("2", cmd2);

        doThrow(new BreakLoopException()).when(cmd1).execute();

        // WHEN
        try {
            menu.run();
        } catch (BreakLoopException e) {}

        // THEN
        String output = outContent.toString();

        // Перевіряємо порядок у рядку виводу
        int index1 = output.indexOf("1. First");
        int index2 = output.indexOf("2. Second");

        assertTrue(index1 < index2, "Перша команда має бути вище за другу (LinkedHashMap)");
    }

    // --- Допоміжний клас виключення ---
    // Це просто маркер, щоб зупинити while(true)
    private static class BreakLoopException extends RuntimeException {}

    // --- Допоміжний метод ---
    private void provideInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
    }
}