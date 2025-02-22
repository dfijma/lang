package net.fijma.parsetree;

import net.fijma.Memory;
import net.fijma.value.ErrorValue;
import net.fijma.value.Value;

public class Identifier extends Expression {

    String name;

    public Identifier(String name) {
        this.name = name;
    }

    public String name() { return name; }

    @Override
    public Value eval(Memory memory) {
        final var intValue = memory.get(name);
        if (intValue == null) return new ErrorValue("%s undefined".formatted(name));
        return intValue;
    }

    @Override
    public String toString() {
        return "Identifier(%s)".formatted(name);
    }

}
