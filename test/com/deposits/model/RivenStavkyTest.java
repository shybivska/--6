package com.deposits.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class RivenStavkyTest {

    private RivenStavky rivenStavky;

    @BeforeEach
    void setUp() {
        rivenStavky = new RivenStavky();
    }

    @Test
    @DisplayName("Перевірка геттерів: мають повертати встановлені значення")
    void testGetters() throws NoSuchFieldException, IllegalAccessException {
        // GIVEN
        double expectedVid = 1000.0;
        double expectedDo = 5000.0;
        double expectedStavka = 12.5;

        // Встановлюємо дані через рефлексію
        setData(rivenStavky, expectedVid, expectedDo, expectedStavka);

        // THEN
        assertEquals(expectedVid, rivenStavky.getVidSumy(), "getVidSumy не працює");
        assertEquals(expectedDo, rivenStavky.getDoSumy(), "getDoSumy не працює");
        assertEquals(expectedStavka, rivenStavky.getStavka(), "getStavka не працює");

        // Перевірка дублюючого геттера
        assertEquals(expectedVid, rivenStavky.getMinSuma(), "getMinSuma має повертати те ж саме, що і getVidSumy");
    }

    @Test
    @DisplayName("Логіка: сума всередині діапазону -> TRUE")
    void testChyPidkhodyt_InsideRange() throws NoSuchFieldException, IllegalAccessException {
        // Діапазон 1000 - 5000
        setData(rivenStavky, 1000.0, 5000.0, 10.0);

        // 2500 входить в діапазон
        assertTrue(rivenStavky.chyPidkhodyt(2500.0));
    }

    @Test
    @DisplayName("Логіка: граничні значення (межі включно) -> TRUE")
    void testChyPidkhodyt_Boundaries() throws NoSuchFieldException, IllegalAccessException {
        // Діапазон 1000 - 5000
        setData(rivenStavky, 1000.0, 5000.0, 10.0);

        // Нижня межа
        assertTrue(rivenStavky.chyPidkhodyt(1000.0), "Нижня межа має включатися (>=)");

        // Верхня межа
        assertTrue(rivenStavky.chyPidkhodyt(5000.0), "Верхня межа має включатися (<=)");
    }

    @Test
    @DisplayName("Логіка: сума поза діапазоном -> FALSE")
    void testChyPidkhodyt_OutsideRange() throws NoSuchFieldException, IllegalAccessException {
        // Діапазон 1000 - 5000
        setData(rivenStavky, 1000.0, 5000.0, 10.0);

        // Менше мінімуму (999.99)
        assertFalse(rivenStavky.chyPidkhodyt(999.99), "Сума менша за мінімум має бути false");

        // Більше максимуму (5000.01)
        assertFalse(rivenStavky.chyPidkhodyt(5000.01), "Сума більша за максимум має бути false");
    }

    // --- Додатковий просунутий варіант тесту (Parameterized Test) ---
    // Це дозволяє перевірити багато варіантів одним методом
    @ParameterizedTest(name = "Сума {0} для діапазону 100-200 має бути {1}")
    @CsvSource({
            "150.0, true",   // Всередині
            "100.0, true",   // На межі (нижній)
            "200.0, true",   // На межі (верхній)
            "99.9,  false",  // За межами (знизу)
            "200.1, false"   // За межами (зверху)
    })
    void testChyPidkhodyt_Parameterized(double inputSum, boolean expectedResult)
            throws NoSuchFieldException, IllegalAccessException {

        setData(rivenStavky, 100.0, 200.0, 15.0);

        assertEquals(expectedResult, rivenStavky.chyPidkhodyt(inputSum));
    }


    // --- Helper method для заповнення приватних полів ---
    private void setData(RivenStavky target, double vid, double doSum, double stavka)
            throws NoSuchFieldException, IllegalAccessException {

        setField(target, "vidSumy", vid);
        setField(target, "doSumy", doSum);
        setField(target, "stavka", stavka);
    }

    private void setField(Object target, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}