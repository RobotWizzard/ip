package bob.task;

import bob.Bob;

import java.time.LocalDateTime;

public class Deadline extends Task {
    protected LocalDateTime by;

    public Deadline(String description, LocalDateTime by) {
        super(description);
        this.by = by;
    }

    public LocalDateTime getBy() {
        return this.by;
    }

    @Override
    public String toString() {
        String formattedBy = Bob.formatDateTime(by);
        return "[D]" + super.toString() + " (by: " + formattedBy + ")";
    }
}