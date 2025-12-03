package com.deposits.ui.commands;

import com.deposits.model.Valyuta;
import com.deposits.model.Vklad;
import com.deposits.service.ServisPidboru;
import com.deposits.ui.Command;
import java.util.List;
import java.util.Scanner;

public class ZnaytyVkladCommand implements Command {
    private final ServisPidboru servis;
    private final Scanner scanner;

    public ZnaytyVkladCommand(ServisPidboru servis) {
        this.servis = servis;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void execute() {
        System.out.println("\n--- МАЙСТЕР ПІДБОРУ ВКЛАДУ ---");

        // --- НОВЕ: Вибір банку ---
        System.out.println("Оберіть банк:");
        System.out.println("0. Неважливо (Всі банки)");

        // Отримуємо список банків із сервісу
        List<String> banky = servis.otrymatyUnikalniNazvyBankiv();

        // Виводимо їх пронумерованим списком
        for (int i = 0; i < banky.size(); i++) {
            System.out.println((i + 1) + ". " + banky.get(i));
        }

        System.out.print("> Ваший вибір: ");
        int vybirBanku = scanner.nextInt();

        // Визначаємо назву обраного банку
        String obranijBank = null; // null означає "всі банки"
        if (vybirBanku > 0 && vybirBanku <= banky.size()) {
            obranijBank = banky.get(vybirBanku - 1); // -1 бо індекс з 0
            System.out.println("Обрано: " + obranijBank);
        } else {
            System.out.println("Обрано: Всі банки");
        }
        // -------------------------

        System.out.print("1. Ваша сума вкладу: ");
        double suma = scanner.nextDouble();

        System.out.print("2. Валюта (1-UAH, 2-USD, 3-EUR): ");
        int v = scanner.nextInt();
        Valyuta val = (v == 2) ? Valyuta.USD : (v == 3 ? Valyuta.EUR : Valyuta.UAH);

        System.out.print("3. Бажаний термін у місяцях (3, 6, 12... або 0 якщо неважливо): ");
        int termin = scanner.nextInt();

        System.out.print("4. Чи важлива капіталізація? (1-Так, 0-Ні): ");
        boolean kap = scanner.nextInt() == 1;

        System.out.print("5. Чи потрібно поповнення? (1-Так, 0-Ні): ");
        boolean popovn = scanner.nextInt() == 1;

        System.out.print("6. Чи потрібне дострокове зняття? (1-Так, 0-Ні): ");
        boolean znyattya = scanner.nextInt() == 1;

        // Передаємо obranijBank у метод підбору
        List<Vklad> result = servis.pidibraty(obranijBank, suma, val, popovn, znyattya, kap, termin);

        if (result.isEmpty()) {
            System.out.println("На жаль, нічого не знайдено за вашими критеріями.");
            return;
        }

        System.out.println("\n--- ЗНАЙДЕНО: " + result.size() + " ---");
        for (int i = 0; i < result.size(); i++) {
            Vklad vk = result.get(i);
            double stavka = vk.getStavkaDlyaSumy(suma);
            double prybutok = servis.rozrakhuvatyPrybutok(vk, suma);

            System.out.printf("%d. %s | Ставка: %.2f%% | Прибуток: %.2f %s\n",
                    (i + 1), vk, stavka, prybutok, vk.getValyuta());
        }
        System.out.println("-----------------------------------");
    }

    @Override
    public String getName() { return "Знайти вклад (Помічник)"; }
}