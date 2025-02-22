package net.fijma.value;

public class ErrorValue extends Value {

    private final String message;

    public ErrorValue(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "error: " + message;
    }
}
