package net.fijma.token;

public class EndOfLine extends Token {

    public EndOfLine(int line, int column) {
        super(line, column);
    }

    @Override
    public String toString() {
        return "EndOfLine" + super.toString();
    }
}
