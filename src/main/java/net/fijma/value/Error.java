package net.fijma.value;

public class Error extends Value {

    private final String message;

    public Error(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "error: " + message;
    }
}
