package net.fijma.token;

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

    public final SymbolType type;

    public Symbol(int line, int column, SymbolType type) {
        super(line, column);
        this.type = type;
    }

    @Override
    public String toString() {
        return type.toString() + super.toString();
    }
}
