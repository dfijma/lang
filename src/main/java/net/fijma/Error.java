package net.fijma;

public class Error extends Value {

    private final String message;

    public Error(String message) {
        this.message = message;
    }

    @Override
    public Value eval() {
        return this;
    }

    @Override
    public String toString() {
        return "error: " + message;
    }
}
