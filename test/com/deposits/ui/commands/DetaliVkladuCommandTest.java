package com.deposits.ui.commands;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DetaliVkladuCommandTest {

    private ServisPidboru mockService;

    // Зберігаємо оригінальні потоки, щоб відновити їх після тесту
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;

    // Перехоплювач виводу
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        // Мокаємо сервіс
        mockService = mock(ServisPidboru.class);

        // Налаштовуємо перехоплення виводу (System.out -> outContent)
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        // ВІДНОВЛЮЄМО СИСТЕМУ:
        // Повертаємо клавіатуру і консоль на місце, інакше інші тести зламаються
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("execute: якщо список порожній, виводить повідомлення і виходить")
    void testExecute_EmptyList() {
        // GIVEN
        // Сервіс повертає порожній список
        when(mockService.getOstanniZnaydeni()).thenReturn(Collections.emptyList());

        // Ввід нам тут не важливий, бо Scanner навіть не мав би дійти до читання,
        // але про всяк випадок дамо порожній ввід.
        provideInput("");

        // Створюємо команду (Scanner створиться всередині з нашого input)
        DetaliVkladuCommand command = new DetaliVkladuCommand(mockService);

        // WHEN
        command.execute();

        // THEN
        String output = outContent.toString();
        // Перевіряємо, що користувачу повідомили про пустий список
        assertTrue(output.contains("Список порожній"), "Має вивести повідомлення про пустий список");

        // Перевіряємо, що метод показу деталей НІКОЛИ не викликався
        verify(mockService, never()).pokazatyDetali(anyInt());
    }

    @Test
    @DisplayName("execute: виводить список і викликает сервіс з введеним номером")
    void testExecute_ValidSelection() {
        // GIVEN
        // 1. Готуємо мок вкладу для відображення
        Vklad v1 = mock(Vklad.class);
        when(v1.getNazvaBanku()).thenReturn("TestBank");
        when(v1.getNazvaVkladu()).thenReturn("SuperDeposit");

        // 2. Сервіс повертає список з цим вкладом
        when(mockService.getOstanniZnaydeni()).thenReturn(List.of(v1));

        // 3. ЕМУЛЮЄМО ВВІД КОРИСТУВАЧА: цифра "1" і Enter (\n)
        provideInput("1\n");

        // 4. Створюємо команду ПІСЛЯ того, як налаштували input
        DetaliVkladuCommand command = new DetaliVkladuCommand(mockService);

        // WHEN
        command.execute();

        // THEN
        String output = outContent.toString();

        // Перевіряємо, що список був виведений на екран
        assertTrue(output.contains("1. TestBank \"SuperDeposit\""), "Має показати список вкладів");

        // Перевіряємо, що команда передала введену цифру (1) у сервіс
        verify(mockService, times(1)).pokazatyDetali(1);
    }

    @Test
    @DisplayName("getName: повертає правильну назву пункту меню")
    void testGetName() {
        // Ввід не потрібен, але Scanner створюється в конструкторі, тому пустишка треба
        provideInput("");
        DetaliVkladuCommand command = new DetaliVkladuCommand(mockService);

        assertEquals("Переглянути детальні умови вкладу", command.getName());
    }

    // --- Допоміжний метод для підміни System.in ---
    private void provideInput(String data) {
        ByteArrayInputStream testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
    }
}