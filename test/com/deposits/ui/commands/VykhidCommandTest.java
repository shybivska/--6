package com.deposits.ui.commands;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class VykhidCommandTest {

    private VykhidCommand command;
    private Consumer<Integer> mockExit;

    // Перехоплення консолі
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        // 1. Налаштовуємо перехоплення консолі
        System.setOut(new PrintStream(outContent));

        // 2. Створюємо МОК для дії виходу
        // Ми кажемо: "Ось це фейкова функція, яка нічого не робить,
        // але вміє рахувати, скільки разів її викликали".
        mockExit = mock(Consumer.class);

        // 3. Створюємо команду, використовуючи спеціальний конструктор для тестів
        command = new VykhidCommand(mockExit);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("execute: друкує повідомлення і викликає механізм виходу з кодом 0")
    void testExecute() {
        // WHEN
        command.execute();

        // THEN

        // 1. Перевіряємо, чи було надруковано прощання
        assertTrue(outContent.toString().contains("До побачення!"),
                "Має вивести повідомлення перед виходом");

        // 2. Перевіряємо, чи викликався наш mockExit
        // Ми перевіряємо, що метод accept викликали РІВНО 1 раз з аргументом 0.
        verify(mockExit, times(1)).accept(0);
    }

    @Test
    @DisplayName("getName: повертає правильну назву")
    void testGetName() {
        // Для цього тесту можна використати навіть звичайний конструктор,
        // бо execute() ми тут не викликаємо.
        VykhidCommand cmd = new VykhidCommand();
        assertEquals("Вихід", cmd.getName());
    }
}