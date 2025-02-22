package net.fijma.token;

public class Kill extends Token {

    public Kill(int line, int column) {
        super(line, column);
    }

    @Override
    public String toString() {
        return "Kill" + super.toString();
    }
}
