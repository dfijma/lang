package net.fijma;

import net.fijma.token.Token;

import java.util.List;
import java.util.Optional;

public class Unit extends ParseResult<List<Expression>> {

    private final boolean last;

    public Unit(boolean last, List<Expression> expressions) {
        super(expressions, null, null);
        this.last = last;
    }

    public Unit(boolean last, Token token, String error) {
        super(null, token, error);
        this.last = last;
    }

    public boolean isLast() { return last; }

    @Override
    public String toString() {
        return value + "(last=" + last + ")";
    }

}
