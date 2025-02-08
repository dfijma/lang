package net.fijma.token;

import java.util.HashMap;
import java.util.Map;

public class Symbol extends Token {

    public enum SymbolType { ParOpen, ParClose, CurlyOpen, CurlyClose, SquareOpen, SquareClose, At, Comma, Dot, Larger, Smaller, Exclamation, Plus, Minus, Becomes, Colon, Is, Equals, ReallyEquals, SemiColon }
    public final SymbolType type;

    public Symbol(SymbolType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
