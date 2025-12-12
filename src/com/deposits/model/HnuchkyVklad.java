package com.deposits.model;

import com.deposits.main.Main;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Клас-модель для Гнучкого Вкладу.
 * * Цей клас описує тип депозиту, який надає клієнту максимальну свободу:
 * гроші можна вільно класти та знімати. Прикладом може бути вклад "Слава Героям"
 * або "Приват-вклад".
 * * Він успадковує (extends) усі загальні поля (назву, валюту, ставку)
 * від абстрактного батьківського класу Vklad.
 */
public class HnuchkyVklad extends Vklad {
    private static final Logger logger = LogManager.getLogger(HnuchkyVklad.class);

    /**
     * Унікальне поле саме для цього типу вкладу.
     * Деякі спеціальні вклади мають обмеження зверху (наприклад, не більше 500 000 грн).
     * * Бібліотека GSON автоматично знайде поле "maksSuma" у файлі deposits.json
     * і запише його значення сюди.
     */
    private double maksSuma;

    // Геттер для отримання ліміту суми
    public double getMaksSuma() {
        return maksSuma;
    }

    /**
     * Реалізація абстрактного методу батька.
     * Чи можна поповнювати цей вклад?
     * * @return true - завжди ТАК, бо це суть гнучкого вкладу.
     */
    @Override
    public boolean chyMozhnaPopovnyuvaty() {
        logger.log(Level.INFO,"return true");
        return true;
    }

    /**
     * Реалізація абстрактного методу батька.
     * Чи можна зняти гроші достроково?
     * * @return true - завжди ТАК. Гнучкі вклади дозволяють забирати гроші
     * (повністю або частково) без штрафів і закриття рахунку.
     */
    @Override
    public boolean chyMozhnaZnyatyDostrokovo() {
        return true;
    }
}