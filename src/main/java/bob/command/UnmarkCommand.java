package bob.command;

import bob.Storage;
import bob.TaskList;
import bob.Ui;
import bob.exception.IncorrectArgumentException;
import bob.exception.MissingArgumentException;

public class UnmarkCommand extends Command {
    @Override
    public String getCommand() {
        return "unmark";
    }

    @Override
    public boolean isExit() {
        return false;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage, String argument) {
        if (argument.isBlank()) {
            throw new MissingArgumentException("index of the task that you want to unmark");
        }

        int i;
        try {
            i = Integer.parseInt(argument) - 1;
        } catch (NumberFormatException e) {
            throw new IncorrectArgumentException("an integer");
        }
        if (i < 0 || i >= tasks.size()) {
            throw new IncorrectArgumentException("a valid index");
        }

        tasks.get(i).unmark();
        ui.printWithFormat("OK, I've marked this task as not done yet:\n" +
                tasks.get(i));
    }
}
