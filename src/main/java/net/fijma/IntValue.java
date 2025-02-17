package net.fijma;

public class IntValue extends Expression {

    int value;

    public IntValue(int value) {
        this.value = value;
    }

    public int value() { return value; }

    @Override
    public Value eval() { return this; }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
