package com.deposits.service;

import com.deposits.model.*;
import com.deposits.storage.SkhovyshcheVkladiv;
// 1. Додаємо імпорти для Log4j2
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ServisPidboru {
    // 2. Створюємо логер
    private static final Logger logger = LogManager.getLogger(ServisPidboru.class);

    private KatalogVkladiv katalog;

    // --- ПАМ'ЯТЬ СЕРВІСУ ---
    private List<Vklad> ostanniZnaydeni = new ArrayList<>();
    private double ostanniaSuma = 0;

    public ServisPidboru(SkhovyshcheVkladiv skhovyshche, String shlyakh) {
        try {
            this.katalog = skhovyshche.zavantazhyty(shlyakh);

            // Логуємо успішний старт
            logger.log(Level.INFO, "Сервіс запущено. Завантажено вкладів: " + katalog.otrymatyVsiVklady().size());

            this.ostanniZnaydeni.addAll(katalog.otrymatyVsiVklady());
        } catch (Exception e) {
            // Логуємо критичну помилку (піде на пошту, якщо налаштовано SMTP)
            logger.log(Level.FATAL, "Критична помилка! Не вдалося завантажити базу даних із файлу: " + shlyakh, e);
            throw e; // Прокидаємо помилку далі, щоб програма зупинилася
        }
    }

    public List<String> otrymatyUnikalniNazvyBankiv() {
        return katalog.otrymatyVsiVklady().stream()
                .map(Vklad::getNazvaBanku)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Vklad> pidibraty(String obranijBank, double suma, Valyuta val, boolean popovn, boolean znyattya, boolean kap, int termin) {
        // Логуємо запит користувача (інформаційно)
        logger.log(Level.INFO, String.format("Пошук: Банк='%s', Сума=%.2f, Валюта=%s", obranijBank, suma, val));

        this.ostanniaSuma = suma;
        List<Vklad> rezultat = new ArrayList<>();

        for (Vklad v : katalog.otrymatyVsiVklady()) {
            if (obranijBank != null && !v.getNazvaBanku().equalsIgnoreCase(obranijBank)) continue;
            if (v.getValyuta() != val) continue;
            if (v.getStavkaDlyaSumy(suma) == 0.0) continue;
            if (popovn && !v.chyMozhnaPopovnyuvaty()) continue;
            if (znyattya && !v.chyMozhnaZnyatyDostrokovo()) continue;
            if (kap && !v.isYeKapitalizatsiya()) continue;
            if (termin > 0 && v.getTerminMisyatsiv() != termin) continue;

            rezultat.add(v);
        }
        this.ostanniZnaydeni = rezultat;

        logger.log(Level.INFO, "Знайдено варіантів: " + rezultat.size());
        return rezultat;
    }

    public List<Vklad> otrymatyVsi() {
        this.ostanniaSuma = 0;
        this.ostanniZnaydeni = new ArrayList<>(katalog.otrymatyVsiVklady());
        logger.log(Level.INFO, "Користувач запросив повний список вкладів.");
        return this.ostanniZnaydeni;
    }

    public void sortuvatyZaVygodoyu() {
        logger.log(Level.INFO, "Виконується сортування списку...");
        if (ostanniaSuma > 0) {
            ostanniZnaydeni.sort(Comparator.comparingDouble(v -> -v.getStavkaDlyaSumy(ostanniaSuma)));
        } else {
            ostanniZnaydeni.sort(Comparator.comparingDouble(v -> -v.getMaxStavka()));
        }
    }

    public double rozrakhuvatyPrybutok(Vklad v, double suma) {
        double stavka = v.getStavkaDlyaSumy(suma);
        return (suma * stavka * v.getTerminMisyatsiv()) / (12 * 100);
    }

    public void pokazatyDetali(int nomer) {
        if (ostanniZnaydeni.isEmpty()) {
            System.out.println("Спочатку зробіть пошук (пункт 1) або перегляньте всі (пункт 2)!");
            logger.log(Level.WARN, "Спроба перегляду деталей у порожньому списку.");
            return;
        }

        if (nomer < 1 || nomer > ostanniZnaydeni.size()) {
            System.out.println("Невірний номер.");
            // Це помилка користувача, логуємо як ERROR або WARN
            logger.log(Level.ERROR, "Невірний номер вкладу: " + nomer + ". Всього у списку: " + ostanniZnaydeni.size());
            return;
        }

        Vklad v = ostanniZnaydeni.get(nomer - 1);

        // Можна залогувати, яку саме картку відкрив користувач
        logger.log(Level.DEBUG, "Перегляд деталей вкладу: " + v.getNazvaVkladu());

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

    public List<Vklad> getOstanniZnaydeni() { return ostanniZnaydeni; }
    public double getOstanniaSuma() { return ostanniaSuma; }
}