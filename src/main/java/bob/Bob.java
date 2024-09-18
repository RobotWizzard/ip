package bob;

import bob.command.Command;
import bob.exception.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

public class Bob {
    private static final DateTimeFormatter INPUT_FORMATTER =
            new DateTimeFormatterBuilder()
                    .append(DateTimeFormatter.ofPattern("d[d]/M[M][/uuuu][ HHmm]"))
                    .parseDefaulting(ChronoField.YEAR, LocalDate.now().getYear())
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .toFormatter();
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("'{'dd-MMM-uuuu HHmm'}'");
    private static final Storage STORAGE = new Storage("data/Bob.txt");
    private static final Ui UI = new Ui();
    private static TaskList tasks;

    public static LocalDateTime parseDateTime(String string) {
        switch (string) {
        case "now":
            return LocalDateTime.now();
        case "tmr":
        case "tomorrow":
            return LocalDateTime.now().plusDays(1);
        default:
            try {
                return LocalDateTime.from(INPUT_FORMATTER.parse(string));
            } catch (DateTimeParseException e) {
                throw new InvalidDateTimeException();
            }
        }
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return OUTPUT_FORMATTER.format(dateTime);
    }

    public static void main(String[] args) {
        UI.printGreeting();
        bob.command.Command.loadCommands();

        try {
            tasks = STORAGE.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (BobException e) {
            UI.printError(e.getMessage());
            tasks = new TaskList();
        }

        boolean isExit = false;
        while (!isExit) {
            String[] input = UI.readInput().split(" ", 2);
            String argument = input.length == 1 ? "" : input[1];
            try {
                Command command = Command.of(input[0]);
                command.execute(tasks, UI, STORAGE, argument);
                isExit = command.isExit();
            } catch (BobException e) {
                UI.printError(e.getMessage());
            }
        }

        System.exit(0);
    }
}
