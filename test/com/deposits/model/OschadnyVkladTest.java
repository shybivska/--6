package com.deposits.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class OschadnyVkladTest {

    private OschadnyVklad oschadnyVklad;

    @BeforeEach
    void setUp() {
        oschadnyVklad = new OschadnyVklad();
    }

    @Test
    @DisplayName("Перевірка поповнення: завжди дозволено (true)")
    void testChyMozhnaPopovnyuvaty() {
        // Ощадний вклад завжди створений для накопичення, тому true
        assertTrue(oschadnyVklad.chyMozhnaPopovnyuvaty(),
                "Метод повинен повертати true, бо це суть ощадного вкладу");
    }

    @Test
    @DisplayName("Перевірка зняття: має повертати TRUE, якщо це дозволено у налаштуваннях (JSON)")
    void testChyMozhnaZnyatyDostrokovo_True() throws NoSuchFieldException, IllegalAccessException {
        // GIVEN
        // Встановлюємо значення батьківського поля mozhnaZnyatyDostrokovo = true
        setParentField(oschadnyVklad, "mozhnaZnyatyDostrokovo", true);

        // WHEN
        boolean result = oschadnyVklad.chyMozhnaZnyatyDostrokovo();

        // THEN
        assertTrue(result, "Має повертати true, бо ми встановили поле в true");
    }

    @Test
    @DisplayName("Перевірка зняття: має повертати FALSE, якщо це заборонено у налаштуваннях (JSON)")
    void testChyMozhnaZnyatyDostrokovo_False() throws NoSuchFieldException, IllegalAccessException {
        // GIVEN
        // Встановлюємо значення батьківського поля mozhnaZnyatyDostrokovo = false
        setParentField(oschadnyVklad, "mozhnaZnyatyDostrokovo", false);

        // WHEN
        boolean result = oschadnyVklad.chyMozhnaZnyatyDostrokovo();

        // THEN
        assertFalse(result, "Має повертати false, бо ми встановили поле в false");
    }

    @Test
    @DisplayName("Перевірка ієрархії: об'єкт є нащадком Vklad")
    void testInheritance() {
        assertTrue(oschadnyVklad instanceof Vklad);
    }

    // --- Допоміжний метод для рефлексії ---
    // Це допомагає уникнути дублювання коду, бо поле знаходиться у БАТЬКІВСЬКОМУ класі
    private void setParentField(Object targetObject, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {

        // Отримуємо клас батька (Vklad), бо саме там оголошено поле
        Class<?> superClass = targetObject.getClass().getSuperclass();

        // Знаходимо поле
        Field field = superClass.getDeclaredField(fieldName);

        // Відкриваємо доступ (на випадок, якщо воно private)
        field.setAccessible(true);

        // Записуємо значення
        field.set(targetObject, value);
    }
}