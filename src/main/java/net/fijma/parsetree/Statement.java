package net.fijma.parsetree;

import net.fijma.Memory;
import net.fijma.value.Value;

public abstract class Statement {

    public abstract Value eval(Memory memory);
}
