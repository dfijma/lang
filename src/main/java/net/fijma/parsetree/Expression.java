package net.fijma.parsetree;

import net.fijma.value.Value;

public abstract class Expression extends Statement {

    public abstract Value eval();

    @Override
    public void execute() {
        eval();
    }

}
