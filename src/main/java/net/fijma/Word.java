package net.fijma;

public class Word extends Token{

    private final String value;

    public Word(String value) {
        this.value = value;
    }

    public String value() { return value; }

    @Override
    public String toString() {
        return "Word(%s)".formatted(value);
    }
}
