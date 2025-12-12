package com.deposits.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

// Імпортуємо статичні методи для тверджень (asserts) та моків
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KatalogVkladivTest {

    private KatalogVkladiv katalog;

    @BeforeEach
    void setUp() {
        // Ініціалізуємо "чистий" каталог перед кожним тестом
        katalog = new KatalogVkladiv();
    }

    @Test
    @DisplayName("Перевірка ініціалізації: список має бути порожнім, але не null")
    void testInitialization() {
        // WHEN
        List<Vklad> result = katalog.otrymatyVsiVklady();

        // THEN
        assertNotNull(result, "Список вкладів не повинен бути null після створення");
        assertTrue(result.isEmpty(), "Спочатку список вкладів має бути порожнім");
    }

    @Test
    @DisplayName("Перевірка додавання одного вкладу (Mock)")
    void testDodatyVklad() {
        // GIVEN
        // Створюємо мок-об'єкт (імітацію) абстрактного класу Vklad
        Vklad mockVklad = mock(Vklad.class);

        // WHEN
        katalog.dodatyVklad(mockVklad);
        List<Vklad> result = katalog.otrymatyVsiVklady();

        // THEN
        assertEquals(1, result.size(), "Розмір списку має бути 1");
        assertEquals(mockVklad, result.get(0), "У списку має бути саме той об'єкт, який ми додали");
    }

    @Test
    @DisplayName("Перевірка додавання кількох вкладів: порядок та кількість")
    void testDodatyMultipleVklady() {
        // GIVEN
        Vklad vklad1 = mock(Vklad.class);
        Vklad vklad2 = mock(Vklad.class);
        Vklad vklad3 = mock(Vklad.class);

        // WHEN
        katalog.dodatyVklad(vklad1);
        katalog.dodatyVklad(vklad2);
        katalog.dodatyVklad(vklad3);

        List<Vklad> result = katalog.otrymatyVsiVklady();

        // THEN
        assertEquals(3, result.size(), "Має бути збережено 3 вклади");
        assertTrue(result.contains(vklad1));
        assertTrue(result.contains(vklad2));
        assertTrue(result.contains(vklad3));

        // Перевіряємо порядок (ArrayList зберігає порядок вставки)
        assertEquals(vklad1, result.get(0));
        assertEquals(vklad3, result.get(2));
    }

    @Test
    @DisplayName("Перевірка цілісності даних: каталог повертає посилання на той самий список")
    void testReferenceIntegrity() {
        // Цей тест перевіряє, чи повертає метод посилання на внутрішній об'єкт,
        // і чи зміни у повернутому списку впливають на каталог (так працює Java за замовчуванням)

        // WHEN
        List<Vklad> listRef1 = katalog.otrymatyVsiVklady();
        List<Vklad> listRef2 = katalog.otrymatyVsiVklady();

        // THEN
        assertSame(listRef1, listRef2, "Метод має повертати посилання на один і той самий об'єкт списку");
    }
}