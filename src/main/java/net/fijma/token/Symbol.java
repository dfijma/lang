package net.fijma.token;

import java.util.HashMap;
import java.util.Map;

public class Symbol extends Token {

    public enum SymbolType {
        LeftParenthesis,
        RightParenthesis,
        LeftCurlyBracket,
        RightCurlyBracket,
        LeftSquareBracket,
        RightSquareBracket,
        At,
        Comma,
        Dot,
        GreaterThan,
        SmallerThan,
        Exclamation,
        Plus,
        Minus,
        Becomes,
        Slash,
        Asterisk,
        Colon,
        Equals,
        EqualsEquals,
        EqualsEqualsEquals,
        Semicolon,
        Pipe,
        Ampersand
    }

    public static Map<SymbolType, String> symbols = new HashMap();

    static {
        symbols.put(SymbolType.LeftParenthesis, "(");
        symbols.put(SymbolType.RightParenthesis, ")");
        symbols.put(SymbolType.LeftCurlyBracket, "{");
        symbols.put(SymbolType.RightCurlyBracket, "}");
        symbols.put(SymbolType.LeftSquareBracket, "[");
        symbols.put(SymbolType.RightSquareBracket, "]");
        symbols.put(SymbolType.At, "@");
        symbols.put(SymbolType.Comma, ",");
        symbols.put(SymbolType.Dot, ".");
        symbols.put(SymbolType.GreaterThan, ">");
        symbols.put(SymbolType.SmallerThan, "<");
        symbols.put(SymbolType.Plus, "+");
        symbols.put(SymbolType.Minus, "-");
        symbols.put(SymbolType.Becomes, ":=");
        symbols.put(SymbolType.Slash, "/");
        symbols.put(SymbolType.Asterisk, "*");
        symbols.put(SymbolType.Colon, ":");
        symbols.put(SymbolType.Equals, "=");
        symbols.put(SymbolType.EqualsEquals, "==");
        symbols.put(SymbolType.EqualsEqualsEquals, "===");
        symbols.put(SymbolType.Semicolon, ";");
        symbols.put(SymbolType.Pipe, "|");
        symbols.put(SymbolType.Ampersand, "&");
    }

    public final SymbolType type;

    public Symbol(SymbolType type) {
        this.type = type;
    }

    public String format() {
        return symbols.getOrDefault(type, type.toString());
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
