package net.fijma.parsetree;

import net.fijma.Memory;
import net.fijma.value.ErrorValue;
import net.fijma.value.IntValue;
import net.fijma.value.Ok;
import net.fijma.value.Value;

public class LetStatement extends Statement {

    final Identifier identifier;
    final Expression expression;

    public LetStatement(Identifier identifier, Expression expression) {
        this.identifier = identifier;
        this.expression = expression;
    }

    @Override
    public Value eval(Memory memory) {
        final var expressionValue = expression.eval(memory);
        if (expressionValue instanceof  ErrorValue) return expressionValue;
        if (expressionValue instanceof IntValue intValue) {
            memory.set(identifier.name, intValue);
            return new Ok();
        }
        return new ErrorValue("unknwon expression type: ".formatted(expressionValue.getClass()));
    }

    @Override
    public String toString() {
        return "Let(" + identifier.toString() + "," + expression.toString() + ")";
    }
}
