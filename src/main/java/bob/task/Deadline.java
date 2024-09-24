package bob.task;

import bob.util.DateTime;

import java.time.LocalDateTime;

public class Deadline extends Task {
    private final LocalDateTime by;

    public Deadline(String description, LocalDateTime by) {
        super(description);
        this.by = by;
    }

    public LocalDateTime getBy() {
        return this.by;
    }

    @Override
    public String toString() {
        String formattedBy = DateTime.format(by);
        return "[D]" + super.toString() + " (by: " + formattedBy + ")";
    }
}