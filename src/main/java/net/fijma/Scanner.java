package net.fijma;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Scanner implements AutoCloseable{

    private final InputStreamReader isr;
    private char currentChar;

    private Scanner(InputStream is) {
        this.isr = new InputStreamReader(is);
        this.currentChar = ' ';
    }

    public static Scanner scan(InputStream is) throws IOException {
        final var scanner = new Scanner(is);
        scanner.skip();
        return scanner;
    }

    public char current() {
        return currentChar;
    }

    public void skip() throws IOException {
        if (currentChar != 0) {
            int c = isr.read();
            if (c >= 0) {
                currentChar = (char) c;
            } else {
                currentChar = 0;
            }
        }
    }

    @Override
    public void close() throws Exception {
        isr.close();
    }
}
