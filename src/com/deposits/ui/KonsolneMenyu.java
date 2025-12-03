package com.deposits.ui;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Клас "Консольне Меню" (Invoker у паттерні Command).
 * * Це єдине місце в програмі, яке керує загальним потоком виконання (головний цикл).
 * Воно зберігає набір доступних команд і запускає їх за вибором користувача.
 */
public class KonsolneMenyu {

    /**
     * "Словник" команд.
     * Ключ (String) — це цифра, яку вводить користувач ("1", "2").
     * Значення (Command) — це об'єкт команди, який треба виконати.
     * * Використовуємо LinkedHashMap, щоб пункти меню виводилися
     * в тому порядку, в якому ми їх додали (1, 2, 3...), а не в перемішаному.
     */
    private final Map<String, Command> menuItems = new LinkedHashMap<>();

    // Інструмент для зчитування введення з клавіатури
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Метод реєстрації команд ("Програмування кнопок").
     * Цей метод викликається в Main.java, щоб наповнити меню функціоналом.
     * @param key Клавіша (напр. "1").
     * @param command Об'єкт команди (напр. new ZnaytyVkladCommand(...)).
     */
    public void addCommand(String key, Command command) {
        menuItems.put(key, command);
    }

    /**
     * Головний метод запуску.
     * Тут програма "живе".
     */
    public void run() {
        System.out.println("=== БАНКІВСЬКИЙ ПОМІЧНИК ===");

        // Нескінченний цикл. Програма не зупиниться, доки не буде викликана команда VykhidCommand.
        while (true) {
            System.out.println("\nОберіть дію:");

            // Проходимось по всіх записаних командах і друкуємо їхні назви
            for (Map.Entry<String, Command> entry : menuItems.entrySet()) {
                // entry.getKey() -> "1"
                // entry.getValue().getName() -> "Знайти вклад"
                System.out.println(entry.getKey() + ". " + entry.getValue().getName());
            }

            System.out.print("> ");

            // Зчитуємо вибір користувача
            String choice = scanner.next();

            // Перевіряємо, чи є така команда в нашому списку
            if (menuItems.containsKey(choice)) {
                // !!! СУТЬ ПАТЕРНУ COMMAND !!!
                // Ми беремо об'єкт команди і кажемо йому: "Виконуй!".
                // Меню не знає, що саме там відбудеться (пошук, друк, вихід) — це знає сама команда.
                menuItems.get(choice).execute();
            } else {
                System.out.println("Невірна команда.");
            }
        }
    }
}