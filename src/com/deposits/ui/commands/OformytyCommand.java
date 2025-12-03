package com.deposits.ui.commands;

import com.deposits.model.Vklad;
import com.deposits.service.ServisPidboru;
import com.deposits.storage.HeneratorZvitiv;
import com.deposits.ui.Command;
import java.util.Scanner;

/**
 * Конкретна команда (Concrete Command).
 * Відповідає за пункт меню "Оформити заявку".
 * Її завдання: дізнатися, ЯКИЙ саме вклад обрав клієнт, ХТО цей клієнт,
 * і наказати генератору звітів створити файл.
 */
public class OformytyCommand implements Command {

    // Посилання на "Мозок" (Receiver), щоб взяти список знайдених вкладів
    private final ServisPidboru servis;

    // Посилання на "Принтер", який вміє створювати .txt файли
    private final HeneratorZvitiv henerator;

    // Інструмент для читання введення з клавіатури
    private final Scanner scanner;

    public OformytyCommand(ServisPidboru servis) {
        this.servis = servis;
        this.henerator = new HeneratorZvitiv(); // Створюємо генератор
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void execute() {
        // 1. Отримуємо список вкладів, з якими ми працювали в останній раз.
        // Це може бути результат пошуку (пункт 1) або сортування (пункт 3).
        var list = servis.getOstanniZnaydeni();

        // Захист: якщо клієнт одразу натиснув "Оформити", не зробивши пошук.
        if (list.isEmpty()) {
            System.out.println("Спочатку знайдіть вклади (Пункт 1)!");
            return;
        }

        // 2. Виводимо цей список на екран ще раз.
        // Це важливо для зручності (UX): клієнт бачить номери (1, 2, 3) прямо перед вибором.
        System.out.println("\n--- ОБЕРІТЬ ВКЛАД ДЛЯ ОФОРМЛЕННЯ ---");

        // Отримуємо суму, яку клієнт вводив при пошуку (якщо вводив)
        double currentSuma = servis.getOstanniaSuma();

        for (int i = 0; i < list.size(); i++) {
            // (i + 1) — це візуальний номер (для людей, починається з 1)
            System.out.println((i + 1) + ". " + list.get(i).getNazvaBanku() + " \"" + list.get(i).getNazvaVkladu() + "\"");
        }
        System.out.println("------------------------------------");

        // 3. Запитуємо вибір користувача
        System.out.print("Введіть номер (1-" + list.size() + "): ");
        int nomer = scanner.nextInt();

        // Валідація: перевіряємо, чи існує такий номер у списку
        if (nomer < 1 || nomer > list.size()) {
            System.out.println("Невірний номер.");
            return;
        }

        // Отримуємо об'єкт Вкладу зі списку.
        // nomer - 1 — це перетворення людського номера (1) в індекс масиву (0).
        Vklad vklad = list.get(nomer - 1);

        // 4. Логіка уточнення суми.
        // Якщо currentSuma <= 0, це означає, що клієнт не користувався "Пошуком" (пункт 1),
        // а, наприклад, просто натиснув "Показати всі" (пункт 2).
        // У такому разі ми не знаємо суму, тому мусимо спитати її зараз.
        double sumaToUse = currentSuma;
        if (sumaToUse <= 0) {
            System.out.print("Введіть суму вкладу: ");
            sumaToUse = scanner.nextDouble();
        } else {
            // Якщо сума відома, просто підтверджуємо її
            System.out.println("Сума вкладу: " + sumaToUse + " " + vklad.getValyuta());
        }

        // 5. Запитуємо ПІБ
        System.out.print("Введіть ваше ПІБ: ");
        scanner.nextLine(); // "З'їдаємо" символ ентера, що залишився після nextDouble() (фікс багу сканера)
        String pib = scanner.nextLine(); // Читаємо повний рядок імені

        // 6. Фінальні розрахунки перед записом
        // Нам треба знати точний прибуток і ставку саме для цієї суми
        double prybutok = servis.rozrakhuvatyPrybutok(vklad, sumaToUse);
        double stavka = vklad.getStavkaDlyaSumy(sumaToUse);

        // 7. Делегуємо збереження файлу спеціальному класу
        henerator.zberegtyDogovir(vklad, sumaToUse, stavka, prybutok, pib);
    }

    @Override
    public String getName() {
        return "Оформити заявку (Договір)";
    }
}