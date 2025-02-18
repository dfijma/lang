package net.fijma.parsetree;

import net.fijma.value.IntValue;
import net.fijma.value.Value;

public class IntConstant extends Expression {
    int value;

    public IntConstant(int value) {
        this.value = value;
    }

    public int value() { return value; }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public Value eval() {
        return new IntValue(value);
    }
}
