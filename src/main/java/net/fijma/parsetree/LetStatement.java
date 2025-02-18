package net.fijma.parsetree;

public class LetStatement extends Statement {

    final Identifier identifier;
    final Expression expression;

    public LetStatement(Identifier identifier, Expression expression) {
        this.identifier = identifier;
        this.expression = expression;
    }

    @Override
    public void execute() {
        System.out.println("trace: let " + identifier.toString() + " = " + expression.eval());
    }

    @Override
    public String toString() {
        return "Let(" + identifier.toString() + "," + expression.toString() + ")";
    }
}
