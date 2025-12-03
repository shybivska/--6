package com.deposits.model;
import java.util.List;

/**
 * Абстрактний клас "Вклад" (Deposit).
 * Це батьківський клас для всіх типів депозитів (Строковий, Гнучкий, Ощадний).
 * Він не може бути створений сам по собі (new Vklad() - помилка),
 * але він містить спільні поля та логіку для своїх нащадків.
 */
public abstract class Vklad {
    // Основна інформація про вклад (заповнюється автоматично з JSON бібліотекою GSON)
    protected String nazvaBanku;      // Назва банку (напр., "ПриватБанк")
    protected String nazvaVkladu;     // Назва продукту (напр., "Слава Героям")
    protected String opys;            // Маркетинговий опис для клієнта
    protected Valyuta valyuta;        // Валюта вкладу (enum: UAH, USD, EUR)
    protected int terminMisyatsiv;    // Тривалість вкладу (напр., 12 місяців)
    protected boolean yeKapitalizatsiya; // Чи додаються відсотки до вкладу (true) чи виплачуються (false)

    /**
     * Найважливіше поле: "Драбинка ставок".
     * Банки часто дають різний відсоток залежно від суми (більше грошей = вищий відсоток).
     * Це список об'єктів, де кожен об'єкт описує діапазон (від-до) і ставку.
     */
    protected List<RivenStavky> rivniStavok;

    // Поля, які визначають гнучкість вкладу.
    // Використовуються нащадками або напряму, якщо логіка проста.
    protected boolean mozhnaPopovnyuvaty;
    protected boolean mozhnaZnyatyDostrokovo;

    // --- Абстрактні методи ---
    // Кожен клас-нащадок (StrokovyVklad і т.д.) ЗОБОВ'ЯЗАНИЙ реалізувати ці методи по-своєму.
    public abstract boolean chyMozhnaPopovnyuvaty();

    public abstract boolean chyMozhnaZnyatyDostrokovo();

    /**
     * Розумний метод для визначення ставки.
     * Він перебирає "драбинку" (rivniStavok) і шукає, яка ставка відповідає сумі клієнта.
     *
     * @param suma Сума, яку клієнт хоче вкласти.
     * @return Знайдена відсоткова ставка (або 0.0, якщо сума замала).
     */
    public double getStavkaDlyaSumy(double suma) {
        if (rivniStavok != null) {
            for (RivenStavky riven : rivniStavok) {
                // Питаємо у кожного рівня: "Ця сума підходить тобі?"
                if (riven.chyPidkhodyt(suma)) return riven.getStavka();
            }
        }
        return 0.0; // Нічого не знайшли
    }

    /**
     * Метод для сортування "Найвигідніші".
     * Знаходить максимальну можливу ставку серед усіх рівнів цього вкладу,
     * щоб показати клієнту "до 16% річних".
     */
    public double getMaxStavka() {
        if (rivniStavok == null || rivniStavok.isEmpty()) {
            return 0.0;
        }
        double max = 0;
        for (RivenStavky r : rivniStavok) {
            if (r.getStavka() > max) {
                max = r.getStavka();
            }
        }
        return max;
    }

    public List<RivenStavky> getRivniStavok() {
        return rivniStavok;
    }

    // Геттер для перевірки капіталізації (використовується при фільтрації)
    public boolean isYeKapitalizatsiya() {
        return yeKapitalizatsiya;
    }

    // --- Стандартні геттери (методи для отримання значень полів) ---
    public String getNazvaBanku() {
        return nazvaBanku;
    }

    public String getNazvaVkladu() {
        return nazvaVkladu;
    }

    public Valyuta getValyuta() {
        return valyuta;
    }

    public int getTerminMisyatsiv() {
        return terminMisyatsiv;
    }

    public String getOpys() {
        return opys;
    }

    /**
     * Перевизначений метод toString.
     * Відповідає за те, як об'єкт буде виглядати, якщо його просто роздрукувати (System.out.println).
     * Повертає гарний рядок: ПриватБанк "Стандарт" (UAH, 12 міс.)
     */
    @Override
    public String toString() {
        return String.format("%s \"%s\" (%s, %d міс.)", nazvaBanku, nazvaVkladu, valyuta, terminMisyatsiv);
    }
}