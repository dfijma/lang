package net.fijma.token;

public abstract class Token {

    private final int line;
    private final int column;
    private boolean EOL = false;

    public Token(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int line() { return line; }
    public int column() { return column; }

    public Token setEOL(boolean EOL) {
        this.EOL = EOL;
        return this;
    }

    public boolean isEOL() { return EOL; }

    @Override
    public String toString() {
        return EOL ? "(EOL)" : "";
    }

    public String format() { return toString(); }
}
