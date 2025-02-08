package net.fijma;

public class StringFragment extends Token {
    private final String value;

    public StringFragment(String value) {
        this.value = value;
    }

    public String value() { return value; }

    @Override
    public String toString() {
        return "StringFragment(%s)".formatted(value);
    }
}
