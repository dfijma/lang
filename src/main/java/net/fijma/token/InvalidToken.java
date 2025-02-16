package net.fijma.token;

public class InvalidToken extends Token {

    private final String value;

    public InvalidToken(int line, int column, String value) {
        super(line, column);
        this.value = value;
    }

    public String value() { return value; }

    @Override
    public String toString() {
        return "InvalidToken(%s)".formatted(value) + super.toString();
    }
}
