package com.deposits.service;

import com.deposits.model.*;
import com.deposits.storage.SkhovyshcheVkladiv;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ServisPidboru {
    private KatalogVkladiv katalog;

    // --- ПАМ'ЯТЬ СЕРВІСУ ---
    // Ми зберігаємо результат останнього пошуку, щоб команди "Сортувати" (3)
    // та "Оформити" (4) могли працювати з цим списком, не питаючи все заново.
    private List<Vklad> ostanniZnaydeni = new ArrayList<>();

    // Ми запам'ятовуємо суму, яку ввів клієнт. Це потрібно для правильного сортування,
    // бо ставка залежить від суми.
    private double ostanniaSuma = 0;

    public ServisPidboru(SkhovyshcheVkladiv skhovyshche, String shlyakh) {
        this.katalog = skhovyshche.zavantazhyty(shlyakh);
        // Спочатку "останні знайдені" - це просто всі вклади
        this.ostanniZnaydeni.addAll(katalog.otrymatyVsiVklady());
    }

    // --- НОВИЙ МЕТОД: Отримати список унікальних банків ---
    public List<String> otrymatyUnikalniNazvyBankiv() {
        return katalog.otrymatyVsiVklady().stream()
                .map(Vklad::getNazvaBanku) // Беремо тільки назви
                .distinct()                // Прибираємо дублікати (щоб "Приват" був 1 раз)
                .sorted()                  // Сортуємо за алфавітом
                .collect(Collectors.toList());
    }

    /**
     * Головний метод пошуку (Фільтр).
     * Він оновлює "пам'ять" сервісу (ostanniZnaydeni та ostanniaSuma).
     */
    // --- ОНОВЛЕНИЙ МЕТОД ПІДБОРУ (додано String obranijBank) ---
    public List<Vklad> pidibraty(String obranijBank, double suma, Valyuta val, boolean popovn, boolean znyattya, boolean kap, int termin) {
        this.ostanniaSuma = suma;
        List<Vklad> rezultat = new ArrayList<>();

        for (Vklad v : katalog.otrymatyVsiVklady()) {

            // 0. Фільтр Банку (НОВЕ)
            // Якщо обраний банк не null (користувач обрав конкретний)
            // І назва банку вкладу не співпадає -> пропускаємо.
            if (obranijBank != null && !v.getNazvaBanku().equalsIgnoreCase(obranijBank)) {
                continue;
            }

            // 1. Валюта
            if (v.getValyuta() != val) continue;
            // 2. Ставка
            if (v.getStavkaDlyaSumy(suma) == 0.0) continue;
            // 3. Гнучкість
            if (popovn && !v.chyMozhnaPopovnyuvaty()) continue;
            if (znyattya && !v.chyMozhnaZnyatyDostrokovo()) continue;
            // 4. Капіталізація
            if (kap && !v.isYeKapitalizatsiya()) continue;
            // 5. Термін
            if (termin > 0 && v.getTerminMisyatsiv() != termin) continue;

            rezultat.add(v);
        }
        this.ostanniZnaydeni = rezultat;
        return rezultat;
    }

    public List<Vklad> otrymatyVsi() {
        this.ostanniaSuma = 0; // Для всіх вкладів конкретна сума не важлива
        this.ostanniZnaydeni = new ArrayList<>(katalog.otrymatyVsiVklady());
        return this.ostanniZnaydeni;
    }

    /**
     * Сортує той список, який ЗАРАЗ знаходиться в пам'яті (ostanniZnaydeni).
     */
    public void sortuvatyZaVygodoyu() {
        if (ostanniaSuma > 0) {
            // Якщо ми знаємо суму клієнта, сортуємо за реальною ставкою для цієї суми
            ostanniZnaydeni.sort(Comparator.comparingDouble(v -> -v.getStavkaDlyaSumy(ostanniaSuma)));
        } else {
            // Якщо це просто список "Всі вклади", сортуємо за максимально можливою ставкою (рекламною)
            ostanniZnaydeni.sort(Comparator.comparingDouble(v -> -v.getMaxStavka()));
        }
    }

    public double rozrakhuvatyPrybutok(Vklad v, double suma) {
        double stavka = v.getStavkaDlyaSumy(suma);
        // Формула простих відсотків: (Сума * % * Місяці) / 1200
        return (suma * stavka * v.getTerminMisyatsiv()) / (12 * 100);
    }

    // ... інші методи ...

    // --- НОВИЙ МЕТОД ДЛЯ ПУНКТУ 4 ---
    public void pokazatyDetali(int nomer) {
        if (ostanniZnaydeni.isEmpty()) {
            System.out.println("Спочатку зробіть пошук (пункт 1) або перегляньте всі (пункт 2)!");
            return;
        }

        if (nomer < 1 || nomer > ostanniZnaydeni.size()) {
            System.out.println("Невірний номер.");
            return;
        }

        Vklad v = ostanniZnaydeni.get(nomer - 1);

        System.out.println("\n==========================================");
        System.out.println("          ДЕТАЛЬНА КАРТКА ВКЛАДУ");
        System.out.println("==========================================");
        System.out.println("БАНК:       " + v.getNazvaBanku());
        System.out.println("НАЗВА:      " + v.getNazvaVkladu());
        System.out.println("ВАЛЮТА:     " + v.getValyuta());
        System.out.println("ТЕРМІН:     " + v.getTerminMisyatsiv() + " міс.");
        System.out.println("------------------------------------------");
        System.out.println("ОПИС:       " + v.getOpys());
        System.out.println("------------------------------------------");
        System.out.println("УМОВИ ГНУЧКОСТІ:");
        System.out.println(" > Поповнення: " + (v.chyMozhnaPopovnyuvaty() ? "✅ ТАК" : "❌ НІ"));
        System.out.println(" > Зняття:     " + (v.chyMozhnaZnyatyDostrokovo() ? "✅ ТАК" : "❌ НІ"));
        System.out.println(" > Капіталізація: " + (v.isYeKapitalizatsiya() ? "✅ ТАК" : "❌ НІ"));
        System.out.println("------------------------------------------");
        System.out.println("ТАБЛИЦЯ ПРОЦЕНТНИХ СТАВОК:");
        if (v.getRivniStavok() != null) {
            for (RivenStavky r : v.getRivniStavok()) {
                System.out.printf(" > Від %10.2f до %12.2f %s:  %.2f%%\n",
                        r.getVidSumy(), r.getDoSumy(), v.getValyuta(), r.getStavka());
            }
        }
        System.out.println("==========================================\n");
    }

    // Геттери для команд
    public List<Vklad> getOstanniZnaydeni() { return ostanniZnaydeni; }
    public double getOstanniaSuma() { return ostanniaSuma; }
}