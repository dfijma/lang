package net.fijma;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputReader implements AutoCloseable {

    private final InputStreamReader isr;
    private char currentChar;

    private int line;
    private int column;
    private boolean atStart = true;

    private InputReader(InputStream is) {
        this.isr = new InputStreamReader(is);
        this.line = 0;
        this.column = 0;
    }

    public static InputReader create(InputStream is) throws IOException {
        final var inputReader = new InputReader(is);
        inputReader.read();
        return inputReader;
    }

    public int line() { return line; }
    public int column() { return column; }

    public char current() {
        return currentChar;
    }

    public void skip() throws IOException {
        if (currentChar != 0) {
            read();
        }
    }

    @Override
    public void close() throws Exception {
        isr.close();
    }

    private void read() throws IOException {
        int c = isr.read();
        if (atStart) { line++; column = 0; atStart = false; }
        column += 1;
        if (c >= 0) {
            currentChar = (char) c;
            if (c == '\n') {
                atStart = true;
            }
        } else {
            currentChar = 0;
        }
    }

}
