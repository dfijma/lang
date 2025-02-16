package net.fijma;

import net.fijma.token.EndOfProgram;
import net.fijma.token.NewLine;

public class Main {

    static boolean first;
    static boolean isTTY = System.console() != null;

    private static void prompt() {
        if (isTTY) {
            System.out.print("> ");
            System.out.flush();
        }
        first = true;
    }

    public static void main(String[] args) {

        prompt();
        try (Scanner t = Scanner.create(System.in)) {

            boolean done = false;

            while (!done) {
                if (!first) {
                    System.out.print(" ");
                }
                switch (t.current()) {
                    case EndOfProgram ignored -> {
                        System.out.print("EOF(" + t.current().line() + ":" + t.current().column() + ")" + "\n");
                        done = true;
                    }
                    case NewLine ignored -> {
                        System.out.print("EOL(" + t.current().line() + ":" + t.current().column() + ")" + "\n");
                        prompt();
                    }
                    default -> {
                        first = false;
                        System.out.print(t.current());
                        System.out.print("(" + t.current().line() + ":" + t.current().column() + ")");
                    }
                }

                t.skip();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}