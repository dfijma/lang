package net.fijma.token;

public class NewLine extends Token {

    public NewLine(int line, int column) {
        super(line, column);
    }

    @Override
    public String toString() {
        return "NewLine" + super.toString() + super.toString();
    }
}
