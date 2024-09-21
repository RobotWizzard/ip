package bob;

import bob.command.Command;
import bob.exception.UnknownCommandException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Parser {
    private static final String ARGUMENT_PREFIX = "/";
    private static HashMap<String, Class<? extends Command>> commandTable;

    public Parser() {
        this.loadCommands();
    }

    private void loadCommands() {
        // Create a new HashMap
        commandTable = new HashMap<>();

        // Get all classes from the bob.command package
        Set<Class<?>> allClasses = ClassGetter.getClassesFromPackage(Command.class.getPackageName());

        // Iterate through the classes in the bob.command package
        for (Class<?> clazz : allClasses) {
            // If the class does not inherit bob.command.Command, ignore it
            if (!Command.class.isAssignableFrom(clazz)) {
                continue;
            }

            // Get the public static "COMMAND" field from the class. If it is not declared, ignore the class
            String command;
            try {
                command = (String) clazz.getDeclaredField("COMMAND").get(null);
            } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
                continue;
            }

            // Map the value of "COMMAND" in the class with the class itself, and put it in the commandTable
            commandTable.put(command, clazz.asSubclass(Command.class));
        }
    }

    private Class<? extends Command> getCommand(String command) {
        Class<? extends Command> clazz = commandTable.get(command);
        if (clazz == null) {
            throw new UnknownCommandException();
        }

        return clazz;
    }

    public Command parse(String string) {
        Map<String, String> tokenizedString = tokenize(string);
        Class<? extends Command> commandClazz = getCommand(tokenizedString.get("CMD"));

        try {
            return commandClazz.getConstructor(Map.class).newInstance(tokenizedString);
        } catch (NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException e) {
            throw new UnknownCommandException();
        }
    }

    public static Map<String, String> tokenize(String string) {
        // Create a new Map<String, String>
        Map<String, String> map = new HashMap<>();

        // Map the first word with key "CMD"
        String[] t = string.split(" ", 2);
        map.put("CMD", t[0]);

        // If there are no arguments, return the map
        if (t.length == 1) {
            return map;
        }

        // delimiter matches any word that begins with ARGUMENT_PREFIX, given it is followed by whitespace
        String delimiter = String.format("((^| )%s[^\\s]+( |$))", ARGUMENT_PREFIX);

        // Split the string by each argument, including the delimiter
        String[] arguments = t[1].split(String.format("(?<=%1$s)|(?=%1$s)", delimiter));

        // Iterate through each argument and its value
        for (int i = 0; i < arguments.length; i++) {
            String s = arguments[i].strip();
            if (s.startsWith(ARGUMENT_PREFIX)) {
                // Map the argument name to its value if it exists, otherwise map to ""
                String s1 = i == arguments.length - 1 ? "" : arguments[++i].strip();
                map.put(s.substring(1), s1);
            } else {
                // If the argument does not have a name, map the value with key ""
                map.put("", s);
            }
        }

        return map;
    }
}
