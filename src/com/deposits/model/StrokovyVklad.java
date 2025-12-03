package com.deposits.model;

/**
 * Клас-модель для Строкового Вкладу (Term Deposit).
 * Це класичний тип депозиту, де гроші кладуться на фіксований термін (3, 6, 12 місяців).
 */
public class StrokovyVklad extends Vklad {

    /**
     * Чи дозволено поповнювати цей вклад?
     *
     * @return значення змінної 'mozhnaPopovnyuvaty', яку GSON завантажив із файлу.
     * Наприклад:
     * - Для вкладу "Капітал" (Приват) у JSON написано false -> поверне false.
     * - Для вкладу "Мій захисник" (Ощад) у JSON написано true -> поверне true.
     */
    @Override
    public boolean chyMozhnaPopovnyuvaty() {
        return mozhnaPopovnyuvaty;
    }

    /**
     * Чи дозволено знімати гроші достроково?
     *
     * @return значення змінної 'mozhnaZnyatyDostrokovo' з файлу.
     * Зазвичай для строкових вкладів це false (банк тримає гроші до кінця терміну),
     * але ми залишаємо можливість, що якийсь банк це дозволить.
     */
    @Override
    public boolean chyMozhnaZnyatyDostrokovo() {
        return mozhnaZnyatyDostrokovo;
    }
}