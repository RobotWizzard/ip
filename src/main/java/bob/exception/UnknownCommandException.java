package bob.exception;

public class UnknownCommandException extends BobException {
    public UnknownCommandException() {
        super("WHOA! Not sure what you're aiming for,\n" +
                "but I can't help with that one.");
    }
}
