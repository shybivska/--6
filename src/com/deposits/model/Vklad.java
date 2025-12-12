package com.deposits.model;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Абстрактний клас "Вклад" (Deposit).
 */
public abstract class Vklad {
    // 1. Ініціалізація логера
    private static final Logger logger = LogManager.getLogger(Vklad.class);

    protected String nazvaBanku;
    protected String nazvaVkladu;
    protected String opys;
    protected Valyuta valyuta;
    protected int terminMisyatsiv;
    protected boolean yeKapitalizatsiya;

    protected List<RivenStavky> rivniStavok;

    protected boolean mozhnaPopovnyuvaty;
    protected boolean mozhnaZnyatyDostrokovo;

    public abstract boolean chyMozhnaPopovnyuvaty();
    public abstract boolean chyMozhnaZnyatyDostrokovo();

    /**
     * Розумний метод для визначення ставки.
     */
    public double getStavkaDlyaSumy(double suma) {
        // DEBUG: Корисно для відлагодження, щоб бачити процес розрахунку
        logger.log(Level.DEBUG, String.format("Спроба знайти ставку для: %s '%s', сума: %.2f", nazvaBanku, nazvaVkladu, suma));

        if (rivniStavok == null || rivniStavok.isEmpty()) {
            // ERROR: Це серйозна помилка даних. Вклад без ставок не має сенсу.
            // Це повідомлення полетить на E-mail, якщо налаштовано SMTP.
            logger.log(Level.ERROR, String.format("КРИТИЧНО: У вкладу '%s' (%s) відсутня таблиця ставок (rivniStavok is empty)!", nazvaVkladu, nazvaBanku));
            return 0.0;
        }

        for (RivenStavky riven : rivniStavok) {
            if (riven.chyPidkhodyt(suma)) {
                // DEBUG: Знайшли ставку
                logger.log(Level.DEBUG, "Ставку знайдено: " + riven.getStavka() + "%");
                return riven.getStavka();
            }
        }

        // WARN: Це не помилка програми, але бізнес-попередження.
        // Сума клієнта не підійшла під жодні умови (можливо, сума замала).
        logger.log(Level.WARN, String.format("Увага: Сума %.2f не відповідає умовам вкладу '%s' (Банк: %s)", suma, nazvaVkladu, nazvaBanku));

        return 0.0;
    }

    /**
     * Метод для сортування "Найвигідніші".
     */
    public double getMaxStavka() {
        if (rivniStavok == null || rivniStavok.isEmpty()) {
            // Тут достатньо WARN, бо це може викликатися масово при сортуванні
            logger.log(Level.WARN, "Спроба отримати MaxStavka для некоректного вкладу: " + nazvaVkladu);
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

    public boolean isYeKapitalizatsiya() {
        return yeKapitalizatsiya;
    }

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

    @Override
    public String toString() {
        return String.format("%s \"%s\" (%s, %d міс.)", nazvaBanku, nazvaVkladu, valyuta, terminMisyatsiv);
    }
}