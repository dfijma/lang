package net.fijma.token;

public class Word extends Token{

    private final String value;

    public Word(int line, int column, String value) {
        super(line, column);
        this.value = value;
    }

    public String value() { return value; }

    @Override
    public String toString() {
        return "Word(%s)".formatted(value) + super.toString();
    }
}
