package com.deposits.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class StrokovyVkladTest {

    private StrokovyVklad strokovyVklad;

    @BeforeEach
    void setUp() {
        strokovyVklad = new StrokovyVklad();
    }

    // --- БЛОК ТЕСТІВ: Поповнення (mozhnaPopovnyuvaty) ---

    @Test
    @DisplayName("Поповнення: має повернути TRUE, якщо в налаштуваннях дозволено")
    void testChyMozhnaPopovnyuvaty_True() throws NoSuchFieldException, IllegalAccessException {
        // GIVEN
        setParentField(strokovyVklad, "mozhnaPopovnyuvaty", true);

        // WHEN
        boolean result = strokovyVklad.chyMozhnaPopovnyuvaty();

        // THEN
        assertTrue(result, "Метод повинен повертати true, відповідно до поля");
    }

    @Test
    @DisplayName("Поповнення: має повернути FALSE, якщо в налаштуваннях заборонено")
    void testChyMozhnaPopovnyuvaty_False() throws NoSuchFieldException, IllegalAccessException {
        // GIVEN
        setParentField(strokovyVklad, "mozhnaPopovnyuvaty", false);

        // WHEN
        boolean result = strokovyVklad.chyMozhnaPopovnyuvaty();

        // THEN
        assertFalse(result, "Метод повинен повертати false, відповідно до поля");
    }

    // --- БЛОК ТЕСТІВ: Дострокове зняття (mozhnaZnyatyDostrokovo) ---

    @Test
    @DisplayName("Зняття: має повернути TRUE, якщо в налаштуваннях дозволено")
    void testChyMozhnaZnyatyDostrokovo_True() throws NoSuchFieldException, IllegalAccessException {
        // GIVEN
        setParentField(strokovyVklad, "mozhnaZnyatyDostrokovo", true);

        // WHEN
        boolean result = strokovyVklad.chyMozhnaZnyatyDostrokovo();

        // THEN
        assertTrue(result, "Метод повинен повертати true, відповідно до поля");
    }

    @Test
    @DisplayName("Зняття: має повернути FALSE, якщо в налаштуваннях заборонено")
    void testChyMozhnaZnyatyDostrokovo_False() throws NoSuchFieldException, IllegalAccessException {
        // GIVEN
        setParentField(strokovyVklad, "mozhnaZnyatyDostrokovo", false);

        // WHEN
        boolean result = strokovyVklad.chyMozhnaZnyatyDostrokovo();

        // THEN
        assertFalse(result, "Метод повинен повертати false, відповідно до поля");
    }

    // --- Загальні перевірки ---

    @Test
    @DisplayName("Перевірка спадкування: Строковий вклад є Вкладом")
    void testInheritance() {
        assertTrue(strokovyVklad instanceof Vklad);
    }

    // --- Допоміжний метод (Helper) ---
    // Встановлює значення приватних/protected полів у батьківському класі Vklad
    private void setParentField(Object targetObject, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {

        // Отримуємо доступ до класу-батька (Vklad), бо поля оголошені саме там
        Class<?> superClass = targetObject.getClass().getSuperclass();

        Field field = superClass.getDeclaredField(fieldName);
        field.setAccessible(true); // Ламаємо інкапсуляцію для тесту
        field.set(targetObject, value);
    }
}