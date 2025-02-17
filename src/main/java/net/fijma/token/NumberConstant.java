package net.fijma.token;

public class NumberConstant extends Token {

    private final String value;

    public NumberConstant(String value) {
        this.value = value;
    }

    public String value() { return value; }

    @Override
    public String toString() {
        return "Number(%s)".formatted(value);
    }

    @Override
    public String format() { return value; }
}
