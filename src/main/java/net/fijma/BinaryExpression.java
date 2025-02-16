package net.fijma;

import net.fijma.token.Symbol;

public class BinaryExpression extends Expression {

    private final Expression left;
    private final Expression right;
    private final Symbol operator;

    public BinaryExpression(Expression left, Symbol operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public String toString() {
        return operator + "(" + left.toString() + "," + right.toString() + ")";
    }
}
