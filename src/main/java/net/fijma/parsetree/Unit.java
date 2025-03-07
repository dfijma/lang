package net.fijma.parsetree;

import net.fijma.token.Token;

import java.util.List;

public class Unit extends ParseResult<List<Statement>> {

    private final boolean last;

    public Unit(boolean last, List<Statement> statements) {
        super(statements, null, false,null);
        this.last = last;
    }

    public Unit(boolean last, Token token, String error) {
        super(null, token, true, error);
        this.last = last;
    }

    public boolean isLast() { return last; }

    @Override
    public String toString() {
        return value + "(last=" + last + ")";
    }

}
