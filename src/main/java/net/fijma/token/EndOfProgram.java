package net.fijma.token;

public class EndOfProgram extends Token {

    public EndOfProgram(int line, int column) {
        super(line, column);
    }

    @Override
    public String toString() {
        return "EndOfProgram" + super.toString();
    }
}
