package net.fijma.token;

import java.util.HashSet;
import java.util.Set;

public class Word extends Token{

    private final String value;

    public Word(int line, int column, String value) {
        super(line, column);
        this.value = value;
    }

    public String value() { return value; }

    private static final Set<String> reserved;

    static {
        reserved = new HashSet<>();
        reserved.add("let");
        reserved.add("var");
    }

    public boolean isReserved() {
        return reserved.contains(value);
    }

    @Override
    public String toString() {
        return "Word(%s)".formatted(value) + super.toString();
    }
}
