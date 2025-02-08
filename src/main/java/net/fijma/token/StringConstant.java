package net.fijma.token;

public class StringConstant extends Token {
    private final String value;

    public StringConstant(String value) {
        this.value = value;
    }

    public String value() { return value; }

    @Override
    public String toString() {
        return "StringConstant(%s)".formatted(value);
    }
}
