package net.fijma.token;

public class Number extends Token {

    private final String value;

    public Number(String value) {
        this.value = value;
    }

    public String value() { return value; }

    @Override
    public String toString() {
        return "Number(%s)".formatted(value);
    }
}
