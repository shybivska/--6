package com.deposits.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VkladTest {

    // --- 1. Створюємо тестову реалізацію абстрактного класу ---
    // Це дозволяє нам створити об'єкт і протестувати методи батька.
    private static class VkladImpl extends Vklad {
        @Override
        public boolean chyMozhnaPopovnyuvaty() { return false; }
        @Override
        public boolean chyMozhnaZnyatyDostrokovo() { return false; }
    }

    private VkladImpl vklad;

    @BeforeEach
    void setUp() {
        vklad = new VkladImpl();
    }

    // ==========================================
    // БЛОК 1: Тестування складної логіки
    // ==========================================

    @Test
    @DisplayName("getStavkaDlyaSumy: повертає коректну ставку для суми")
    void testGetStavkaDlyaSumy_Found() throws NoSuchFieldException, IllegalAccessException {
        // GIVEN
        // Створюємо мок рівня ставки, який "прийме" нашу суму
        RivenStavky matchingLevel = mock(RivenStavky.class);
        when(matchingLevel.chyPidkhodyt(1000.0)).thenReturn(true);
        when(matchingLevel.getStavka()).thenReturn(12.5);

        // Інший рівень, який не підходить
        RivenStavky nonMatchingLevel = mock(RivenStavky.class);
        when(nonMatchingLevel.chyPidkhodyt(1000.0)).thenReturn(false);

        List<RivenStavky> levels = new ArrayList<>();
        levels.add(nonMatchingLevel);
        levels.add(matchingLevel); // Наш цільовий рівень

        setField(vklad, "rivniStavok", levels);

        // WHEN
        double result = vklad.getStavkaDlyaSumy(1000.0);

        // THEN
        assertEquals(12.5, result, "Має повернути ставку з рівня, який підійшов");
    }

    @Test
    @DisplayName("getStavkaDlyaSumy: повертає 0.0, якщо список null або порожній")
    void testGetStavkaDlyaSumy_Empty() throws NoSuchFieldException, IllegalAccessException {
        // 1. Null
        setField(vklad, "rivniStavok", null);
        assertEquals(0.0, vklad.getStavkaDlyaSumy(500));

        // 2. Empty
        setField(vklad, "rivniStavok", Collections.emptyList());
        assertEquals(0.0, vklad.getStavkaDlyaSumy(500));
    }

    @Test
    @DisplayName("getStavkaDlyaSumy: повертає 0.0, якщо сума нікуди не підходить")
    void testGetStavkaDlyaSumy_NoMatch() throws NoSuchFieldException, IllegalAccessException {
        // GIVEN
        RivenStavky level = mock(RivenStavky.class);
        when(level.chyPidkhodyt(anyDouble())).thenReturn(false);

        setField(vklad, "rivniStavok", List.of(level));

        // WHEN
        double result = vklad.getStavkaDlyaSumy(999999.0);

        // THEN
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("getMaxStavka: знаходить максимальне число у списку")
    void testGetMaxStavka() throws NoSuchFieldException, IllegalAccessException {
        // GIVEN
        RivenStavky r1 = mock(RivenStavky.class);
        when(r1.getStavka()).thenReturn(5.0);

        RivenStavky r2 = mock(RivenStavky.class);
        when(r2.getStavka()).thenReturn(20.0); // MAX

        RivenStavky r3 = mock(RivenStavky.class);
        when(r3.getStavka()).thenReturn(10.0);

        setField(vklad, "rivniStavok", List.of(r1, r2, r3));

        // WHEN
        double max = vklad.getMaxStavka();

        // THEN
        assertEquals(20.0, max);
    }

    @Test
    @DisplayName("getMaxStavka: повертає 0.0 для порожнього списку")
    void testGetMaxStavka_Empty() throws NoSuchFieldException, IllegalAccessException {
        setField(vklad, "rivniStavok", new ArrayList<>());
        assertEquals(0.0, vklad.getMaxStavka());
    }

    // ==========================================
    // БЛОК 2: Тестування геттерів (Getters)
    // ==========================================

    @Test
    @DisplayName("getNazvaBanku: повертає коректне значення")
    void testGetNazvaBanku() throws NoSuchFieldException, IllegalAccessException {
        setField(vklad, "nazvaBanku", "Monobank");
        assertEquals("Monobank", vklad.getNazvaBanku());
    }

    @Test
    @DisplayName("getNazvaVkladu: повертає коректне значення")
    void testGetNazvaVkladu() throws NoSuchFieldException, IllegalAccessException {
        setField(vklad, "nazvaVkladu", "Iron Bank");
        assertEquals("Iron Bank", vklad.getNazvaVkladu());
    }

    @Test
    @DisplayName("getOpys: повертає коректне значення")
    void testGetOpys() throws NoSuchFieldException, IllegalAccessException {
        setField(vklad, "opys", "Найкращий вклад");
        assertEquals("Найкращий вклад", vklad.getOpys());
    }

    @Test
    @DisplayName("getValyuta: повертає коректне значення")
    void testGetValyuta() throws NoSuchFieldException, IllegalAccessException {
        // Припускаємо, що у тебе є enum Valyuta.UAH
        // Якщо ні, заміни на null або існуючий enum
        Valyuta expected = Valyuta.UAH;
        setField(vklad, "valyuta", expected);

        assertEquals(expected, vklad.getValyuta());
    }

    @Test
    @DisplayName("getTerminMisyatsiv: повертає коректне значення")
    void testGetTerminMisyatsiv() throws NoSuchFieldException, IllegalAccessException {
        setField(vklad, "terminMisyatsiv", 12);
        assertEquals(12, vklad.getTerminMisyatsiv());
    }

    @Test
    @DisplayName("isYeKapitalizatsiya: повертає true/false")
    void testIsYeKapitalizatsiya() throws NoSuchFieldException, IllegalAccessException {
        setField(vklad, "yeKapitalizatsiya", true);
        assertTrue(vklad.isYeKapitalizatsiya());

        setField(vklad, "yeKapitalizatsiya", false);
        assertFalse(vklad.isYeKapitalizatsiya());
    }

    @Test
    @DisplayName("getRivniStavok: повертає список об'єктів")
    void testGetRivniStavok() throws NoSuchFieldException, IllegalAccessException {
        List<RivenStavky> list = new ArrayList<>();
        setField(vklad, "rivniStavok", list);

        assertSame(list, vklad.getRivniStavok());
    }

    // ==========================================
    // БЛОК 3: Тестування toString
    // ==========================================

    @Test
    @DisplayName("toString: формує рядок у правильному форматі")
    void testToString() throws NoSuchFieldException, IllegalAccessException {
        // GIVEN
        setField(vklad, "nazvaBanku", "Privat");
        setField(vklad, "nazvaVkladu", "Super");
        setField(vklad, "valyuta", Valyuta.USD);
        setField(vklad, "terminMisyatsiv", 6);

        // WHEN
        String result = vklad.toString();

        // THEN
        // Очікуємо: Privat "Super" (USD, 6 міс.)
        String expected = "Privat \"Super\" (USD, 6 міс.)";
        assertEquals(expected, result);
    }

    // ==========================================
    // Helper Method (Рефлексія)
    // ==========================================
    private void setField(Object target, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {

        // Оскільки ми працюємо з VkladImpl, а поля у Vklad (superclass)
        Class<?> clazz = target.getClass().getSuperclass();

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}