package net.fijma.token;

public class StringConstant extends Token {
    private final String value;

    public StringConstant(int line, int column, String value) {
        super(line, column);
        this.value = value;
    }

    public String value() { return value; }

    @Override
    public String toString() {
        return "StringConstant(%s)".formatted(value) + super.toString();
    }
}
