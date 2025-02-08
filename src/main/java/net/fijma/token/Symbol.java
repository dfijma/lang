package net.fijma.token;

import java.util.HashMap;
import java.util.Map;

public class Symbol extends Token {

    public enum SymbolType { ParOpen, ParClose, Plus, Minus, Becomes, Colon, SemiColon }
    public final SymbolType type;

    private static final Map<Character, SymbolType> singleSymbols;

    static {
        singleSymbols = new HashMap<>();
        singleSymbols.put('(', SymbolType.ParOpen);
        singleSymbols.put(')', SymbolType.ParClose);
        singleSymbols.put(';', SymbolType.SemiColon);
    }

    public Symbol(SymbolType type) {
        this.type = type;
    }

    public static Symbol fromSingleChar(char c) {
        final var t = singleSymbols.get(c);
        if (t != null) {
            return new Symbol(t);
        }
        return null;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
