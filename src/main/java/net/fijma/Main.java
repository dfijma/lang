package net.fijma;

import java.util.logging.Logger;

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
        try (Tokenizer t = Tokenizer.create(System.in)) {

            boolean done = false;

            while (!done) {
                switch (t.current()) {
                    case EndOfProgram ignored -> {
                        done = true;
                    }
                    case NewLine newLine -> {
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