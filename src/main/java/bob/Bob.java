package bob;

import bob.command.Command;
import bob.exception.BobException;

import java.io.IOException;

/**
 * The main class of BobBot.
 * BobBot is a chatbot that helps users manage tasks.
 */
public class Bob {
    private final Storage storage;
    private final Ui ui;
    private final Parser parser;
    private TaskList tasks;
    private String commandType = "";
    private boolean isExit = false;

    /**
     * Constructs a Bob instance that stores data at the given filePath.
     *
     * @param filePath where this instance of BobBot stores its data
     */
    public Bob(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
        parser = new Parser();
        try {
            tasks = new TaskList(storage.load());
        } catch (BobException e) {
            ui.printError(e.getMessage());
            tasks = new TaskList();
        }
        ui.printGreeting();
    }

    public void run() {
        while (!isExit) {
            try {
                String input = ui.readInput();
                Command c = parser.parse(input);
                c.execute(tasks, ui, storage);
                isExit = c.isExit();
            } catch (BobException e) {
                ui.printError(e.getMessage());
            }
        }
    }

    /**
     * Cleanup function when this Bob instance exits.
     */
    public void exit() {
        try {
            storage.save(tasks);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new Bob("data/Bob.txt").run();

    }

    /**
     * Generates a response for the user's chat message.
     */
    public String getResponse(String input) {
        try {
            Command c = parser.parse(input);
            c.execute(tasks, ui, storage);
            commandType = c.getClass().getSimpleName();
            isExit = c.isExit();
        } catch (BobException e) {
            ui.printError(e.getMessage());
            commandType = "Error";
        }
        return ui.getLastMessage();
    }

    /**
     * Returns the last message of this Bob instance.
     */
    public String getLastMessage() {
        return ui.getLastMessage();
    }

    /**
     * Returns the command type of the last command as a string.
     *
     * @return the command type as a String
     */
    public String getCommandType() {
        return commandType;
    }

    /**
     * Checks if the previous executed command triggers a program exit.
     *
     * @return true if the command triggers a program exit, false otherwise
     */
    public boolean isExit() {
        return isExit;
    }
}
