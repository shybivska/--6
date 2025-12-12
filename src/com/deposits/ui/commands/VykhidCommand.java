package com.deposits.ui.commands;

import com.deposits.ui.Command;
// 1. Імпорти Log4j2
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

public class VykhidCommand implements Command {

    // 2. Ініціалізація логера
    private static final Logger logger = LogManager.getLogger(VykhidCommand.class);

    // Це наша "кнопка виходу". Вона приймає число (код виходу).
    private final Consumer<Integer> systemExit;

    /**
     * Конструктор без параметрів.
     */
    public VykhidCommand() {
        this.systemExit = System::exit;
    }

    /**
     * Конструктор для ТЕСТІВ.
     */
    public VykhidCommand(Consumer<Integer> mockExit) {
        this.systemExit = mockExit;
    }

    @Override
    public void execute() {
        System.out.println("До побачення!");

        // Логуємо подію завершення роботи
        // Це важливо для аудиту: ми знатимемо, що програма не "впала", а її закрили штатно.
        logger.log(Level.INFO, "Користувач обрав пункт меню 'Вихід'. Завершення роботи програми...");

        // Викликаємо функцію виходу
        systemExit.accept(0);
    }

    @Override
    public String getName() {
        return "Вихід";
    }
}