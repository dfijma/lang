package net.fijma.parsetree;

import net.fijma.Memory;

public abstract class Statement {

    public abstract void execute(Memory memory);
}
