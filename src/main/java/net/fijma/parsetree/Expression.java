package net.fijma.parsetree;

import net.fijma.Memory;
import net.fijma.value.Value;

public abstract class Expression extends Statement {

    public abstract Value eval(Memory memory);

    @Override
    public void execute(Memory memory) {
        eval(memory);
    }

}
