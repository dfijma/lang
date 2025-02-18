package net.fijma.value;

public class IntValue extends Value {

    int value;

    public IntValue(int value) {
        this.value = value;
    }

    public int value() { return value; }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
