package net.fijma;

public class LiteralExpression extends Expression {

    int value;

    public LiteralExpression(int value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
