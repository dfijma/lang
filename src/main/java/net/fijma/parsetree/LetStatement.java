package net.fijma.parsetree;

import net.fijma.Memory;
import net.fijma.value.ErrorValue;
import net.fijma.value.IntValue;

public class LetStatement extends Statement {

    final Identifier identifier;
    final Expression expression;

    public LetStatement(Identifier identifier, Expression expression) {
        this.identifier = identifier;
        this.expression = expression;
    }

    @Override
    public void execute(Memory memory) {
        final var expressionValue = expression.eval(memory);
        if (expressionValue instanceof  ErrorValue) {
            throw new IllegalArgumentException(expressionValue.toString());
        }
        if (expressionValue instanceof IntValue intValue) {
            memory.set(identifier.name, intValue);
            return;
        }
        throw new IllegalArgumentException("unknwon expression type: ".formatted(expressionValue.getClass()));
    }

    @Override
    public String toString() {
        return "Let(" + identifier.toString() + "," + expression.toString() + ")";
    }
}
