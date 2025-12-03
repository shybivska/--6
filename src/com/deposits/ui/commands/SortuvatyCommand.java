package com.deposits.ui.commands;

import com.deposits.model.Vklad;
import com.deposits.service.ServisPidboru;
import com.deposits.ui.Command;

/**
 * Конкретна команда (Concrete Command), що відповідає за пункт меню "3. Відсортувати результати".
 * Вона не реалізує алгоритм сортування сама (це робота Сервісу),
 * але вона керує процесом: перевіряє, чи є що сортувати, запускає сортування і виводить результат.
 */
public class SortuvatyCommand implements Command {

    // Посилання на Сервіс (Receiver), де зберігається поточний список вкладів (ostanniZnaydeni)
    private final ServisPidboru servis;

    public SortuvatyCommand(ServisPidboru servis) {
        this.servis = servis;
    }

    @Override
    public void execute() {
        // 1. Валідація (Захист від порожнього списку).
        // Якщо клієнт щойно запустив програму і одразу натиснув "3", сортувати нічого.
        if (servis.getOstanniZnaydeni().isEmpty()) {
            System.out.println("Список порожній. Спочатку виконайте пошук (пункт 1).");
            return;
        }

        // 2. Виклик бізнес-логіки.
        // Ми наказуємо сервісу переставити елементи в його внутрішньому списку місцями
        // (від найвигіднішого до найменш вигідного).
        servis.sortuvatyZaVygodoyu();
        System.out.println("\nСписок відсортовано за найвищою ставкою!");

        // 3. Відображення результату.
        // Ми одразу друкуємо новий список, щоб клієнт бачив зміни.
        System.out.println("--- ВІДСОРТОВАНІ РЕЗУЛЬТАТИ ---");

        // Дістаємо з пам'яті сервісу суму, яку клієнт вводив при пошуку.
        // Це потрібно, щоб показати точний прибуток у гривнях.
        double suma = servis.getOstanniaSuma();

        var list = servis.getOstanniZnaydeni();
        for (int i = 0; i < list.size(); i++) {
            Vklad vk = list.get(i);

            // 4. Розумний вивід:
            // Якщо ми знаємо суму (suma > 0) — показуємо конкретний прибуток і ставку.
            // Якщо не знаємо (напр., клієнт просто дивився "Всі вклади") — показуємо загальну інфо.
            if (suma > 0) {
                double stavka = vk.getStavkaDlyaSumy(suma);
                double prybutok = servis.rozrakhuvatyPrybutok(vk, suma);
                System.out.printf("%d. %s | Ставка: %.2f%% | Прибуток: %.2f %s\n",
                        (i + 1), vk, stavka, prybutok, vk.getValyuta());
            } else {
                System.out.println((i + 1) + ". " + vk);
            }
        }
        System.out.println("-----------------------------------");
    }

    // Назва пункту в меню
    @Override
    public String getName() {
        return "Відсортувати результати";
    }
}