package net.fijma.token;

public abstract class Token {
    private final int line;
    private final int column;

    public Token(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int line() { return line; }
    public int column() { return column; }

    @Override
    public String toString() {
        return "";
    }
}
