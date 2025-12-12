package com.deposits.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        // Перехоплюємо вивід консолі, щоб читати, що пише програма
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        // Повертаємо все як було
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Main: програма успішно стартує, ініціалізує компоненти та виводить меню")
    void testMainStart() {
        // GIVEN
        // Ми даємо програмі порожній ввід.
        // Це змусить Scanner впасти з помилкою NoSuchElementException,
        // що розірве вічний цикл while(true) у меню.
        System.setIn(new ByteArrayInputStream("".getBytes()));

        // WHEN
        try {
            // Запускаємо головний метод
            Main.main(new String[]{});
        } catch (NoSuchElementException e) {
            // Ми ОЧІКУЄМО цю помилку. Це наш спосіб безпечно зупинити тест,
            // не викликаючи System.exit(0).
        }

        // THEN
        String output = outContent.toString();

        // 1. Перевіряємо, що запустився UI (заголовок)
        assertTrue(output.contains("=== БАНКІВСЬКИЙ ПОМІЧНИК ==="),
                "Програма має вивести заголовок");

        // 2. Перевіряємо, що Data Layer спрацював (навіть якщо файл не знайдено, помилка виводиться)
        // Якщо файлу немає, JsonSkhovyshche пише "Помилка завантаження JSON".
        // Якщо файл є, помилки немає.
        // Тому ми просто перевіряємо, що програма дійшла до UI.

        // 3. Перевіряємо "Wiring" (Зв'язування команд).
        // Якщо меню вивело назви команд, значить Main успішно виконав menu.addCommand(...)

        assertTrue(output.contains("1. Знайти вклад"), "Команда 1 має бути зареєстрована");
        assertTrue(output.contains("2. Переглянути всі"), "Команда 2 має бути зареєстрована");
        assertTrue(output.contains("3. Відсортувати"), "Команда 3 має бути зареєстрована");
        assertTrue(output.contains("4. Переглянути детальні умови"), "Команда 4 має бути зареєстрована");
        assertTrue(output.contains("5. Оформити заявку"), "Команда 5 має бути зареєстрована");
        assertTrue(output.contains("0. Вихід"), "Команда 0 має бути зареєстрована");
    }
}