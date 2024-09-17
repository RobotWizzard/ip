package bob;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Storage {
    private File file;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("ddMMuuuuHHmm");

    public Storage(String filepath) {
        file = new File(filepath);
    }

    private void createFile() throws IOException {
        file.getParentFile().mkdirs();
        file.createNewFile();
    }

    private Task decode(String encodedString) {
        Task task = null;
        int n;
        String desc;

        switch(encodedString.charAt(0)) {
        case 'T':
            // T<isDone><desc>
            task = new Todo(encodedString.substring(2));
            break;
        case 'D':
            // D<isDone><len(desc)#4><desc><by>
            n = Integer.parseInt(encodedString.substring(2, 6));
            desc = encodedString.substring(6, 6 + n);
            String by = encodedString.substring(6 + n);
            LocalDateTime parsedBy = LocalDateTime.from(DATE_TIME_FORMATTER.parse(by));
            task = new Deadline(desc, parsedBy);
            break;
        case 'E':
            // E<isDone><len(desc)#4><desc><len(from)#4><from><to>
            n = Integer.parseInt(encodedString.substring(2, 6));
            int curr = 6 + n;
            desc = encodedString.substring(6, curr);
            n = Integer.parseInt(encodedString.substring(curr, curr + 4));

            curr += 4;
            String from = encodedString.substring(curr, curr + n);
            String to = encodedString.substring(curr + n);

            LocalDateTime parsedFrom = LocalDateTime.from(DATE_TIME_FORMATTER.parse(from));
            LocalDateTime parsedTo = LocalDateTime.from(DATE_TIME_FORMATTER.parse(to));

            task = new Event(desc, parsedFrom, parsedTo);
            break;
        default:
            // TODO: throw error
            break;
        }

        if (encodedString.charAt(1) == '1') {
            task.mark();
        }

        return task;
    }

    private String encode(Task task) {
        StringBuilder str = new StringBuilder();

        if (task instanceof Todo) {
            // Bob.Todo: T<isDone><desc>
            str.append("T");
            str.append(task.isDone ? 1 : 0);
            str.append(task.description);
        } else if (task instanceof Deadline deadline) {
            // Bob.Deadline: D<isDone><len(desc)#4><desc><by>
            str.append("D");
            str.append(task.isDone ? 1 : 0);
            str.append(String.format("%04d", deadline.description.length()));
            str.append(deadline.description);

            String formattedBy = deadline.by.format(DATE_TIME_FORMATTER);
            str.append(formattedBy);
        } else if (task instanceof Event event) {
            // Bob.Event: E<isDone><len(desc)#4><desc><len(from)#4><from><to>
            str.append("E");
            str.append(task.isDone ? 1 : 0);
            str.append(String.format("%04d", event.description.length()));
            str.append(event.description);

            String formattedFrom = event.from.format(DATE_TIME_FORMATTER);
            str.append(String.format("%04d", formattedFrom.length()));
            str.append(formattedFrom);

            String formattedTo = event.to.format(DATE_TIME_FORMATTER);
            str.append(formattedTo);
        }

        str.append('\n');
        return str.toString();
    }

    public List<Task> load() throws IOException {
        if (!file.exists()) {
            createFile();
        }

        List<Task> list = new ArrayList<>();
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        while (scanner.hasNext()) {
            try {
                list.add(decode(scanner.nextLine()));
            } catch (RuntimeException e) {
                throw new FileCorruptedException();
            }
        }

        return list;
    }

    public void save(List<Task> tasksList) throws IOException {
        if (!file.exists()) {
            createFile();
        }

        FileWriter fw = new FileWriter(file, false);
        for (Task task : tasksList) {
            fw.write(encode(task));
        }
        fw.close();
    }
}
