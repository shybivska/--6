package com.deposits.model;

/**
 * Клас-помічник, який описує одну "сходинку" в драбинці відсоткових ставок.
 */
public class RivenStavky {
    private double vidSumy; // Нижня межа
    private double doSumy;  // Верхня межа
    private double stavka;  // Відсоток

    // --- ГЕТЕРИ (Тепер тут порядок, без дублікатів) ---
    public double getVidSumy() {
        return vidSumy;
    }

    public double getDoSumy() {
        return doSumy;
    }

    public double getStavka() {
        return stavka;
    }

    /**
     * Метод-перевірка. Він визначає, чи потрапляє сума клієнта в цей діапазон.
     * @param suma Сума, яку хоче вкласти клієнт.
     * @return true, якщо сума підходить для цієї ставки.
     */
    public boolean chyPidkhodyt(double suma) {
        return suma >= vidSumy && suma <= doSumy;
    }

    public double getMinSuma() { return vidSumy; }
}