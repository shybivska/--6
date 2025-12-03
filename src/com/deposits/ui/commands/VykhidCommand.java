package com.deposits.ui.commands;
import com.deposits.ui.Command;

/**
 * Конкретна команда (Concrete Command) для завершення роботи.
 * Відповідає за пункт меню "0. Вихід".
 */
public class VykhidCommand implements Command {

    /**
     * Цей метод викликається, коли користувач хоче закрити програму.
     */
    @Override
    public void execute() {
        System.out.println("До побачення!");

        // System.exit(0) — це стандартний спосіб зупинити Java-програму.
        // Цифра "0" (status code) повідомляє операційній системі,
        // що програма завершилася нормально (успішно), без помилок.
        System.exit(0);
    }

    @Override
    public String getName() {
        return "Вихід";
    }
}