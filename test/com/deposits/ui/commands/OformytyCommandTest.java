package com.deposits.ui.commands;

import com.deposits.model.Valyuta;
import com.deposits.model.Vklad;
import com.deposits.service.ServisPidboru;
import com.deposits.storage.HeneratorZvitiv;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OformytyCommandTest {

    private ServisPidboru mockService;
    private HeneratorZvitiv mockHenerator; // Цей мок ми "підсунемо" через рефлексію

    // Зберігаємо стандартні потоки
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        // Встановлюємо локаль US, щоб Scanner коректно читав дробові числа з крапкою (1000.0),
        // а не вимагав кому, як в українській локалі.
        Locale.setDefault(Locale.US);

        mockService = mock(ServisPidboru.class);
        mockHenerator = mock(HeneratorZvitiv.class);

        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    // --- ТЕСТ 1: Порожній список ---

    @Test
    @DisplayName("Якщо список пустий -> вивести помилку і вийти")
    void testExecute_EmptyList() {
        // GIVEN
        when(mockService.getOstanniZnaydeni()).thenReturn(Collections.emptyList());
        provideInput("\n"); // Пустий ввід

        OformytyCommand command = createCommandWithMockHenerator();

        // WHEN
        command.execute();

        // THEN
        assertTrue(outContent.toString().contains("Спочатку знайдіть вклади"),
                "Має вивести попередження");
        verifyNoInteractions(mockHenerator); // Генератор не мав викликатись
    }

    // --- ТЕСТ 2: Невірний номер ---

    @Test
    @DisplayName("Невірний вибір номера -> помилка і вихід")
    void testExecute_InvalidSelection() {
        // GIVEN
        Vklad v1 = mock(Vklad.class);
        when(v1.getNazvaBanku()).thenReturn("TestBank");
        when(mockService.getOstanniZnaydeni()).thenReturn(List.of(v1));

        // Вводимо "5" (невірний номер), потім Enter
        provideInput("5\n");

        OformytyCommand command = createCommandWithMockHenerator();

        // WHEN
        command.execute();

        // THEN
        assertTrue(outContent.toString().contains("Невірний номер"));
        verifyNoInteractions(mockHenerator);
    }

    // --- ТЕСТ 3: Сума ВІДОМА (сценарій після пошуку) ---

    @Test
    @DisplayName("Сума вже є в сервісі -> спитати тільки номер і ПІБ")
    void testExecute_SumKnown() {
        // GIVEN
        double knownSum = 1000.0;
        Vklad vklad = mock(Vklad.class);
        when(vklad.getNazvaBanku()).thenReturn("Privat");
        when(vklad.getValyuta()).thenReturn(Valyuta.UAH);

        // Налаштування сервісу
        when(mockService.getOstanniZnaydeni()).thenReturn(List.of(vklad));
        when(mockService.getOstanniaSuma()).thenReturn(knownSum); // Сума відома!

        // Розрахунки
        when(mockService.rozrakhuvatyPrybutok(vklad, knownSum)).thenReturn(100.0);
        when(vklad.getStavkaDlyaSumy(knownSum)).thenReturn(10.0);

        // INPUT: "1" (номер) -> ENTER -> "Ivanov Ivan" (ПІБ) -> ENTER
        // Scanner.nextInt() не читає символ нового рядка, тому наступний nextLine() його з'їдає.
        // Але у твоєму коді є явний scanner.nextLine() для очищення буфера.
        // Тому ввід має бути: "1\nIvanov Ivan\n"
        provideInput("1\nIvanov Ivan\n");

        OformytyCommand command = createCommandWithMockHenerator();

        // WHEN
        command.execute();

        // THEN
        // Перевіряємо, що викликався метод збереження з правильною сумою
        verify(mockHenerator).zberegtyDogovir(
                eq(vklad),
                eq(knownSum), // 1000.0
                eq(10.0),
                eq(100.0),
                eq("Ivanov Ivan")
        );

        // Перевіряємо, що програма підтвердила суму у консолі
        assertTrue(outContent.toString().contains("Сума вкладу: 1000.0"));
    }

    // --- ТЕСТ 4: Сума НЕВІДОМА (сценарій "показати всі") ---

    @Test
    @DisplayName("Суми немає (0) -> спитати номер, СУМУ і ПІБ")
    void testExecute_SumUnknown() {
        // GIVEN
        double userEnteredSum = 5000.0;
        Vklad vklad = mock(Vklad.class);
        when(vklad.getNazvaBanku()).thenReturn("Mono");
        when(vklad.getValyuta()).thenReturn(Valyuta.USD);

        when(mockService.getOstanniZnaydeni()).thenReturn(List.of(vklad));
        when(mockService.getOstanniaSuma()).thenReturn(0.0); // Сума НЕ відома

        when(mockService.rozrakhuvatyPrybutok(vklad, userEnteredSum)).thenReturn(50.0);
        when(vklad.getStavkaDlyaSumy(userEnteredSum)).thenReturn(5.0);

        // INPUT: "1" (номер) -> ENTER -> "5000" (сума) -> ENTER -> "Petro" (ПІБ) -> ENTER
        provideInput("1\n5000\nPetro\n");

        OformytyCommand command = createCommandWithMockHenerator();

        // WHEN
        command.execute();

        // THEN
        verify(mockHenerator).zberegtyDogovir(
                eq(vklad),
                eq(userEnteredSum), // 5000.0, яку ввів користувач
                eq(5.0),
                eq(50.0),
                eq("Petro")
        );
    }

    // ==========================================
    // Допоміжні методи
    // ==========================================

    private void provideInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
    }

    /**
     * Цей метод створює команду, а потім "хакає" її через Reflection API,
     * замінюючи справжній HeneratorZvitiv на наш Mock.
     * Це потрібно, бо в класі написано `henerator = new HeneratorZvitiv()`,
     * і ми не можемо передати мок через конструктор.
     */
    private OformytyCommand createCommandWithMockHenerator() {
        // 1. Створюємо команду (Scanner створиться тут і підхопить наш ByteArrayInputStream)
        OformytyCommand cmd = new OformytyCommand(mockService);

        try {
            // 2. Знаходимо приватне поле "henerator"
            Field field = OformytyCommand.class.getDeclaredField("henerator");
            field.setAccessible(true); // Дозволяємо доступ

            // 3. Замінюємо реальний об'єкт на мок
            field.set(cmd, mockHenerator);

        } catch (Exception e) {
            throw new RuntimeException("Не вдалося підмінити поле через рефлексію", e);
        }

        return cmd;
    }
}