package net.fijma;

import net.fijma.token.EndOfProgram;
import net.fijma.token.NewLine;

public class Main {

    static boolean first;
    static boolean any;
    static boolean isTTY = System.console() != null;

    private static void prompt() {
        if (isTTY) {
            System.out.print("> ");
            System.out.flush();
        }
        first = true;
        any = false;
    }

    public static void main(String[] args) {

        prompt();
        try (Scanner t = Scanner.create(System.in)) {

            boolean done = false;

            while (!done) {
                switch (t.current()) {
                    case EndOfProgram ignored -> done = true;
                    case NewLine ignored -> {
                        if (any) {
                            System.out.print("\n");
                        }
                        prompt();
                    }
                    default -> {
                        if (!first) {
                            System.out.print(" ");
                        }
                        first = false;
                        System.out.print(t.current());
                        any = true;
                    }
                }

                t.skip();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}