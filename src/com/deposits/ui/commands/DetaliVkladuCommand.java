package com.deposits.ui.commands;

import com.deposits.service.ServisPidboru;
import com.deposits.ui.Command;
import java.util.Scanner;

public class DetaliVkladuCommand implements Command {
    private final ServisPidboru servis;
    private final Scanner scanner;

    public DetaliVkladuCommand(ServisPidboru servis) {
        this.servis = servis;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void execute() {
        // 1. Перевіряємо, чи є список
        var list = servis.getOstanniZnaydeni();
        if (list.isEmpty()) {
            System.out.println("Список порожній. Спочатку знайдіть вклади (Пункт 1) або покажіть всі (Пункт 2).");
            return;
        }

        // 2. Показуємо короткий список, щоб користувач згадав номери
        System.out.println("\n--- ОБЕРІТЬ ВКЛАД ДЛЯ ДЕТАЛЕЙ ---");
        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + ". " + list.get(i).getNazvaBanku() + " \"" + list.get(i).getNazvaVkladu() + "\"");
        }
        System.out.println("---------------------------------");

        // 3. Питаємо номер
        System.out.print("Введіть номер (1-" + list.size() + "): ");
        int nomer = scanner.nextInt();

        // 4. Викликаємо сервіс
        servis.pokazatyDetali(nomer);
    }

    @Override
    public String getName() {
        return "Переглянути детальні умови вкладу";
    }
}