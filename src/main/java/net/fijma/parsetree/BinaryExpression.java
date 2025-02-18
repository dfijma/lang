package net.fijma.parsetree;

import net.fijma.value.Error;
import net.fijma.value.IntValue;
import net.fijma.value.Value;
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
    public Value eval() {
        Value leftVal = left.eval();
        Value rightVal = right.eval();

        if (leftVal instanceof IntValue leftIntVal && rightVal instanceof IntValue rightIntVal) {

            return switch (operator.type) {
                case Symbol.SymbolType.Plus -> new IntValue(leftIntVal.value() + rightIntVal.value());
                case Symbol.SymbolType.Minus -> new IntValue(leftIntVal.value() - rightIntVal.value());
                case Symbol.SymbolType.Asterisk -> new IntValue(leftIntVal.value() * rightIntVal.value());
                case Symbol.SymbolType.Slash -> {
                    if (rightIntVal.value() != 0) {
                        yield new IntValue(leftIntVal.value() / rightIntVal.value());
                    } else {
                        yield new Error("division by zero");
                    }
                }
                default -> {
                    throw new IllegalStateException("bug: unsupported operator escaped from parser: " + operator.type);
                }
            };
        }

        return new Error("cannot evaluate " + leftVal + " and " + rightVal);
    }

    @Override
    public String toString() {
        return operator + "(" + left.toString() + "," + right.toString() + ")";
    }
}
