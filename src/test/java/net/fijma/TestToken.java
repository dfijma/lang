package net.fijma;

import net.fijma.token.Symbol;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestToken {

    @Test
    public void test() {
        Symbol s = new Symbol(Symbol.SymbolType.ParOpen);
        assertThat("ParOpen", is(s.toString()));
    }

    @Test
    public void test2() {
        Symbol s = new Symbol(Symbol.SymbolType.Becomes);
        assertThat("Becomes", is(s.toString()));
    }

}
