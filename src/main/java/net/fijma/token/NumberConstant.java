package net.fijma.token;

public class NumberConstant extends Token {

    private final String value;

    public NumberConstant(int line, int column, String value) {
        super(line, column);
        this.value = value;
    }

    public String value() { return value; }

    @Override
    public String toString() {
        return "Number(%s)".formatted(value) + super.toString();
    }
}
