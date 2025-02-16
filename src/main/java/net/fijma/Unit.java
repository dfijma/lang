package net.fijma;

import java.util.List;
import java.util.Optional;

public class Unit {

    private final boolean last;
    private final Optional<List<Expression>> expressions;

    public Unit(boolean last, Optional<List<Expression>> expressions) {
        this.last = last;
        this.expressions = expressions;
    }

    public Optional<List<Expression>> expressions() {
        return expressions;
    }

    public boolean isLast() {
        return last;
    }

    @Override
    public String toString() {
        return expressions.toString() + "(last=" + last + ")";
    }

}
