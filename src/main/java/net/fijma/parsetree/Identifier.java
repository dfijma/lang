package net.fijma.parsetree;

import net.fijma.value.Error;
import net.fijma.value.Value;

public class Identifier extends Expression {

    String name;

    public Identifier(String name) {
        this.name = name;
    }

    public String name() { return name; }

    @Override
    public Value eval() { return new Error("identifier evaluation not yet implemented"); }

    @Override
    public String toString() {
        return "Identifier(%s)".formatted(name);
    }

}
