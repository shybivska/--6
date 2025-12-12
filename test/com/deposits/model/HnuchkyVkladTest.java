package com.deposits.model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class HnuchkyVkladTest {

    private HnuchkyVklad hnuchkyVklad;

    @BeforeEach
    void setUp() {
        // Ініціалізація об'єкта перед кожним тестом
        hnuchkyVklad = new HnuchkyVklad();
    }

    @Test
    @DisplayName("Перевірка getMaksSuma: має повертати значення, встановлене (як через GSON)")
    void testGetMaksSuma() throws NoSuchFieldException, IllegalAccessException {
        // GIVEN (Дано)
        double expectedMaksSuma = 500_000.0;

        // Імітуємо роботу GSON: встановлюємо значення приватного поля через рефлексію
        Field field = hnuchkyVklad.getClass().getDeclaredField("maksSuma");
        field.setAccessible(true); // Дозволяємо доступ до private поля
        field.set(hnuchkyVklad, expectedMaksSuma);

        // WHEN (Коли)
        double actualMaksSuma = hnuchkyVklad.getMaksSuma();

        // THEN (Тоді)
        assertEquals(expectedMaksSuma, actualMaksSuma, "Геттер має повертати коректну максимальну суму");
    }

    @Test
    @DisplayName("Перевірка chyMozhnaPopovnyuvaty: завжди має повертати true")
    void testChyMozhnaPopovnyuvaty() {
        // WHEN
        boolean result = hnuchkyVklad.chyMozhnaPopovnyuvaty();

        // THEN
        assertTrue(result, "Гнучкий вклад завжди повинен дозволяти поповнення");
    }

    @Test
    @DisplayName("Перевірка chyMozhnaZnyatyDostrokovo: завжди має повертати true")
    void testChyMozhnaZnyatyDostrokovo() {
        // WHEN
        boolean result = hnuchkyVklad.chyMozhnaZnyatyDostrokovo();

        // THEN
        assertTrue(result, "Гнучкий вклад завжди повинен дозволяти дострокове зняття");
    }

    @Test
    @DisplayName("Перевірка спадкування: об'єкт має бути нащадком Vklad")
    void testInheritance() {
        // Перевіряємо, що наш клас дійсно розширює функціонал Vklad
        assertTrue(hnuchkyVklad instanceof Vklad, "Клас має успадковуватись від Vklad");
    }
}