package com.deposits.ui.commands;

import com.deposits.model.Vklad;
import com.deposits.service.ServisPidboru;
import com.deposits.ui.Command;
import java.util.List;

/**
 * Клас команди для виведення ПОВНОГО списку вкладів.
 * Відповідає за пункт меню №2.
 * Реалізує патерн Command.
 */
public class PokazatyVsiCommand implements Command {

    // Посилання на "Мозок" (Receiver), звідки ми візьмемо дані
    private final ServisPidboru servis;

    // Конструктор приймає готовий сервіс з Main.java
    public PokazatyVsiCommand(ServisPidboru servis) {
        this.servis = servis;
    }

    @Override
    public void execute() {
        System.out.println("\n--- ВСІ ПРОПОЗИЦІЇ БАНКІВ ---");

        // 1. Звертаємось до сервісу і просимо ВСІ вклади (без фільтрів)
        // Метод otrymatyVsi() повертає копію повного каталогу.
        List<Vklad> vsi = servis.otrymatyVsi();

        // 2. Перевірка на порожнечу (раптом файл deposits.json пустий)
        if (vsi.isEmpty()) {
            System.out.println("(Список порожній)");
            return;
        }

        // 3. Виводимо список у циклі з нумерацією
        for (int i = 0; i < vsi.size(); i++) {
            // (i + 1) потрібно, щоб нумерація починалася з 1, а не з 0.
            // vsi.get(i) автоматично викликає метод toString() у класі Vklad,
            // тому ми бачимо гарний текст "ПриватБанк 'Стандарт'..."
            System.out.println((i + 1) + ". " + vsi.get(i));
        }
        System.out.println("-----------------------------");
    }

    // Цей текст відображається в головному меню
    @Override
    public String getName() {
        return "Переглянути всі пропозиції";
    }
}