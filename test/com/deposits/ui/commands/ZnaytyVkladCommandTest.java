package com.deposits.ui.commands;

import com.deposits.model.Valyuta;
import com.deposits.model.Vklad;
import com.deposits.service.ServisPidboru;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ZnaytyVkladCommandTest {

    private ServisPidboru mockService;

    // Потоки
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        // ВАЖЛИВО: Фіксуємо локаль, щоб scanner.nextDouble() читав "1000.0" з крапкою.
        Locale.setDefault(Locale.US);

        mockService = mock(ServisPidboru.class);
        System.setOut(new PrintStream(outContent));

        // Налаштовуємо сервіс, щоб він завжди повертав список банків
        // Це потрібно для першого етапу роботи команди
        when(mockService.otrymatyUnikalniNazvyBankiv()).thenReturn(List.of("MonoBank", "PrivatBank"));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    // --- ТЕСТ 1: Успішний пошук (Конкретний банк) ---

    @Test
    @DisplayName("execute: вибір конкретного банку та успішний пошук")
    void testExecute_SpecificBank_Found() {
        // GIVEN
        // Емулюємо відповіді користувача:
        // 1. Вибір банку: "1" (це MonoBank, бо він перший у списку)
        // 2. Сума: "10000"
        // 3. Валюта: "1" (UAH)
        // 4. Термін: "12"
        // 5. Капіталізація: "1" (Так)
        // 6. Поповнення: "0" (Ні)
        // 7. Зняття: "0" (Ні)
        String inputs = "1\n10000\n1\n12\n1\n0\n0\n";
        provideInput(inputs);

        // Налаштовуємо результат пошуку
        Vklad mockVklad = mock(Vklad.class);
        when(mockVklad.toString()).thenReturn("Mono 'Deposit'");
        when(mockVklad.getValyuta()).thenReturn(Valyuta.UAH);
        // Мокаємо розрахунки для виводу
        when(mockVklad.getStavkaDlyaSumy(10000)).thenReturn(14.0);
        when(mockService.rozrakhuvatyPrybutok(mockVklad, 10000)).thenReturn(1400.0);

        // Повертаємо цей вклад при пошуку
        // Зверни увагу: ми очікуємо "MonoBank", бо користувач ввів "1"
        when(mockService.pidibraty("MonoBank", 10000, Valyuta.UAH, false, false, true, 12))
                .thenReturn(List.of(mockVklad));

        // Створюємо команду (вона прочитає inputs через Scanner)
        ZnaytyVkladCommand command = new ZnaytyVkladCommand(mockService);

        // WHEN
        command.execute();

        // THEN
        String output = outContent.toString();

        // 1. Перевіряємо, що ми правильно передали параметри у сервіс
        verify(mockService).pidibraty(
                eq("MonoBank"), // Банк
                eq(10000.0),    // Сума
                eq(Valyuta.UAH),// Валюта
                eq(false),      // Поповнення (0)
                eq(false),      // Зняття (0)
                eq(true),       // Капіталізація (1)
                eq(12)          // Термін
        );

        // 2. Перевіряємо вивід результатів
        assertTrue(output.contains("ЗНАЙДЕНО: 1"));
        assertTrue(output.contains("Mono 'Deposit'"));
        assertTrue(output.contains("Ставка: 14.00%"));
        assertTrue(output.contains("Прибуток: 1400.00 UAH"));
    }

    // --- ТЕСТ 2: Всі банки (Вибір 0) ---

    @Test
    @DisplayName("execute: вибір 'Всі банки' (0) -> передає null у сервіс")
    void testExecute_AllBanks() {
        // GIVEN
        // 1. Вибір банку: "0" (Неважливо) -> очікуємо null
        // 2. Сума: "500"
        // 3. Валюта: "2" (USD)
        // 4. Термін: "0" (Неважливо)
        // 5. Кап: "0"
        // 6. Поп: "0"
        // 7. Зн: "0"
        String inputs = "0\n500\n2\n0\n0\n0\n0\n";
        provideInput(inputs);

        // Пошук нічого не знайде (порожній список)
        when(mockService.pidibraty(any(), anyDouble(), any(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt()))
                .thenReturn(Collections.emptyList());

        ZnaytyVkladCommand command = new ZnaytyVkladCommand(mockService);

        // WHEN
        command.execute();

        // THEN
        // Перевіряємо, що у сервіс пішло null замість назви банку
        verify(mockService).pidibraty(
                isNull(),       // Очікуємо null, бо обрали "0"
                eq(500.0),
                eq(Valyuta.USD), // Валюта 2 -> USD
                eq(false),
                eq(false),
                eq(false),
                eq(0)
        );

        // Перевіряємо повідомлення про порожній результат
        assertTrue(outContent.toString().contains("нічого не знайдено"));
    }

    // --- ТЕСТ 3: Перевірка логіки валюти (EUR) ---

    @Test
    @DisplayName("execute: вибір валюти 3 -> EUR")
    void testExecute_EUR_Currency() {
        // GIVEN
        // Вводимо мінімальні дані, головне - третій пункт "3" (EUR)
        String inputs = "0\n100\n3\n0\n0\n0\n0\n";
        provideInput(inputs);

        when(mockService.pidibraty(any(), anyDouble(), any(), anyBoolean(), anyBoolean(), anyBoolean(), anyInt()))
                .thenReturn(Collections.emptyList());

        ZnaytyVkladCommand command = new ZnaytyVkladCommand(mockService);

        // WHEN
        command.execute();

        // THEN
        verify(mockService).pidibraty(
                isNull(),
                eq(100.0),
                eq(Valyuta.EUR), // Перевіряємо, що 3 перетворилось на EUR
                anyBoolean(), anyBoolean(), anyBoolean(), anyInt()
        );
    }

    @Test
    @DisplayName("getName: перевірка назви")
    void testGetName() {
        provideInput(""); // Пустишка для сканера
        ZnaytyVkladCommand command = new ZnaytyVkladCommand(mockService);
        assertEquals("Знайти вклад (Помічник)", command.getName());
    }

    // --- Helper Method ---
    private void provideInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
    }
}