package com.deposits.service;

import com.deposits.model.*;
import com.deposits.storage.SkhovyshcheVkladiv;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ServisPidboruTest {

    private ServisPidboru servis;
    private SkhovyshcheVkladiv mockStorage;
    private KatalogVkladiv realKatalog;

    // Змінні для перехоплення консольного виводу (System.out)
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream standardOut = System.out;

    @BeforeEach
    void setUp() {
        // 1. Налаштовуємо перехоплення консолі (для тестування показатиДеталі)
        System.setOut(new PrintStream(outputStreamCaptor));

        // 2. Створюємо "справжній" каталог, але наповнюємо його моками
        realKatalog = new KatalogVkladiv();

        // --- Підготовка тестових даних (3 різні вклади) ---

        // Вклад 1: Приват, UAH, 12 міс, Гнучкий
        Vklad v1 = mock(Vklad.class);
        when(v1.getNazvaBanku()).thenReturn("PrivatBank");
        when(v1.getValyuta()).thenReturn(Valyuta.UAH);
        when(v1.chyMozhnaPopovnyuvaty()).thenReturn(true);
        when(v1.chyMozhnaZnyatyDostrokovo()).thenReturn(true);
        when(v1.isYeKapitalizatsiya()).thenReturn(true);
        when(v1.getTerminMisyatsiv()).thenReturn(12);
        when(v1.getStavkaDlyaSumy(1000.0)).thenReturn(10.0); // 10% для 1000 грн
        when(v1.getMaxStavka()).thenReturn(10.0);
        when(v1.getNazvaVkladu()).thenReturn("Super");

        // Вклад 2: Mono, UAH, 6 міс, Жорсткий
        Vklad v2 = mock(Vklad.class);
        when(v2.getNazvaBanku()).thenReturn("Monobank");
        when(v2.getValyuta()).thenReturn(Valyuta.UAH);
        when(v2.chyMozhnaPopovnyuvaty()).thenReturn(false);
        when(v2.chyMozhnaZnyatyDostrokovo()).thenReturn(false);
        when(v2.isYeKapitalizatsiya()).thenReturn(false);
        when(v2.getTerminMisyatsiv()).thenReturn(6);
        when(v2.getStavkaDlyaSumy(1000.0)).thenReturn(15.0); // 15% для 1000 грн
        when(v2.getMaxStavka()).thenReturn(15.0);
        when(v2.getNazvaVkladu()).thenReturn("Iron");

        // Вклад 3: Privat, USD, 12 міс
        Vklad v3 = mock(Vklad.class);
        when(v3.getNazvaBanku()).thenReturn("PrivatBank"); // Такий самий банк як v1
        when(v3.getValyuta()).thenReturn(Valyuta.USD);
        when(v3.getTerminMisyatsiv()).thenReturn(12);
        when(v3.getStavkaDlyaSumy(anyDouble())).thenReturn(2.0);

        realKatalog.dodatyVklad(v1);
        realKatalog.dodatyVklad(v2);
        realKatalog.dodatyVklad(v3);

        // 3. Мокаємо сховище
        mockStorage = mock(SkhovyshcheVkladiv.class);
        when(mockStorage.zavantazhyty(anyString())).thenReturn(realKatalog);

        // 4. Ініціалізуємо сервіс
        servis = new ServisPidboru(mockStorage, "fake_path.json");
    }

    @AfterEach
    void tearDown() {
        // Повертаємо консоль у звичайний режим після кожного тесту
        System.setOut(standardOut);
    }

    // --- ТЕСТИ ---

    @Test
    @DisplayName("Конструктор: завантажує дані та заповнює початковий список")
    void testConstructor() {
        verify(mockStorage).zavantazhyty("fake_path.json");
        assertEquals(3, servis.getOstanniZnaydeni().size(), "Має завантажити всі 3 вклади");
    }

    @Test
    @DisplayName("otrymatyUnikalniNazvyBankiv: повертає унікальні імена, відсортовані")
    void testOtrymatyUnikalniNazvyBankiv() {
        // У нас є 2 "PrivatBank" і 1 "Monobank"
        List<String> banks = servis.otrymatyUnikalniNazvyBankiv();

        assertEquals(2, banks.size(), "Має бути 2 унікальні банки");
        assertEquals("Monobank", banks.get(0));
        assertEquals("PrivatBank", banks.get(1));
    }

    @Test
    @DisplayName("pidibraty: фільтрація за банком")
    void testPidibraty_BankFilter() {
        // Шукаємо тільки PrivatBank
        List<Vklad> res = servis.pidibraty("PrivatBank", 1000, Valyuta.UAH, false, false, false, 0);

        assertEquals(1, res.size(), "Тільки 1 вклад UAH у Приваті");
        assertEquals("PrivatBank", res.get(0).getNazvaBanku());
    }

    @Test
    @DisplayName("pidibraty: фільтрація за критеріями (Гнучкість + Термін)")
    void testPidibraty_Criteria() {
        // Шукаємо: будь-який банк, UAH, з поповненням (true), термін 12 міс
        List<Vklad> res = servis.pidibraty(null, 1000, Valyuta.UAH, true, false, false, 12);

        assertEquals(1, res.size());
        assertEquals("Super", res.get(0).getNazvaVkladu()); // Це v1
    }

    @Test
    @DisplayName("pidibraty: повертає порожній список, якщо нічого не знайдено")
    void testPidibraty_NotFound() {
        // Шукаємо EUR (якого немає)
        List<Vklad> res = servis.pidibraty(null, 1000, Valyuta.EUR, false, false, false, 0);
        assertTrue(res.isEmpty());
    }

    @Test
    @DisplayName("otrymatyVsi: скидає фільтри і повертає все")
    void testOtrymatyVsi() {
        // Спочатку відфільтруємо, щоб список зменшився
        servis.pidibraty(null, 1000, Valyuta.USD, false, false, false, 0);
        assertEquals(1, servis.getOstanniZnaydeni().size());

        // Тепер "Отримати всі"
        List<Vklad> res = servis.otrymatyVsi();
        assertEquals(3, res.size());
        assertEquals(0, servis.getOstanniaSuma(), "Сума має скинутися на 0");
    }

    @Test
    @DisplayName("sortuvatyZaVygodoyu: сортування за MAX ставкою (без суми)")
    void testSortuvaty_NoSum() {
        // Робимо "Отримати всі" -> сума = 0
        servis.otrymatyVsi();

        // В базі: v1 (10%), v2 (15%), v3 (2.0%)
        // Очікуємо: v2, v1, v3
        servis.sortuvatyZaVygodoyu();
        List<Vklad> sorted = servis.getOstanniZnaydeni();

        assertEquals("Monobank", sorted.get(0).getNazvaBanku(), "15% має бути першим");
        assertEquals("PrivatBank", sorted.get(1).getNazvaBanku(), "10% має бути другим (UAH)");
        assertEquals(Valyuta.USD, sorted.get(2).getValyuta(), "2% має бути останнім");
    }

    @Test
    @DisplayName("sortuvatyZaVygodoyu: сортування за ставкою для КОНКРЕТНОЇ суми")
    void testSortuvaty_WithSum() {
        // Робимо підбір для суми 1000 грн (тільки UAH)
        // v1 дає 10%, v2 дає 15%
        servis.pidibraty(null, 1000, Valyuta.UAH, false, false, false, 0);

        servis.sortuvatyZaVygodoyu();
        List<Vklad> sorted = servis.getOstanniZnaydeni();

        assertEquals("Monobank", sorted.get(0).getNazvaBanku()); // 15%
        assertEquals("PrivatBank", sorted.get(1).getNazvaBanku()); // 10%
    }

    @Test
    @DisplayName("rozrakhuvatyPrybutok: коректна математика")
    void testRozrakhuvatyPrybutok() {
        Vklad v = mock(Vklad.class);
        when(v.getStavkaDlyaSumy(10000)).thenReturn(12.0); // 12% річних
        when(v.getTerminMisyatsiv()).thenReturn(12); // 1 рік

        // Формула: (10000 * 12 * 12) / 1200 = 1200 грн
        double income = servis.rozrakhuvatyPrybutok(v, 10000);
        assertEquals(1200.0, income, 0.01);
    }

    @Test
    @DisplayName("pokazatyDetali: перевірка виводу та меж списку")
    void testPokazatyDetali() {
        // 1. Спочатку робимо пошук, щоб заповнити список
        servis.otrymatyVsi(); // у списку 3 елементи

        // 2. Тест: Валідний номер (наприклад, 1)


        servis.pokazatyDetali(1);
        String output = outputStreamCaptor.toString();

        // Перевіряємо, чи є ключові слова у виводі
        assertTrue(output.contains("ДЕТАЛЬНА КАРТКА"), "Має бути заголовок");
        assertTrue(output.contains("БАНК:"), "Має бути поле Банк");
        assertTrue(output.contains("PrivatBank"), "Має вивести назву першого банку"); // Бо перший у списку (без сортування)

        // 3. Тест: Невалідний номер (0 або > розміру)
        outputStreamCaptor.reset(); // чистимо буфер
        servis.pokazatyDetali(999);
        assertTrue(outputStreamCaptor.toString().contains("Невірний номер"), "Має повідомити про помилку");

        outputStreamCaptor.reset();
        servis.pokazatyDetali(0);
        assertTrue(outputStreamCaptor.toString().contains("Невірний номер"));
    }

    @Test
    @DisplayName("pokazatyDetali: помилка, якщо список порожній")
    void testPokazatyDetali_EmptyList() {
        // Робимо пошук, який нічого не знайде
        servis.pidibraty(null, 1000, Valyuta.EUR, false, false, false, 0);

        servis.pokazatyDetali(1);
        assertTrue(outputStreamCaptor.toString().contains("Спочатку зробіть пошук"),
                "Має попередити, що список порожній");
    }
}