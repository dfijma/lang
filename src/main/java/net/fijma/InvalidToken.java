package net.fijma;

public class InvalidToken extends Token {

    private final String value;

    public InvalidToken(String value) {
        this.value = value;
    }

    public String value() { return value; }

    @Override
    public String toString() {
        return "InvalidToken(%s)".formatted(value);
    }
}
