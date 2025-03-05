package net.fijma;

import net.fijma.token.EndOfProgram;

public class Main {

    static boolean isTTY = System.console() != null;

    private static void prompt() {
        if (isTTY) {
            System.out.print("> ");
            System.out.flush();
        }
    }

    public static void main(String[] args) {

        prompt();
        try (Scanner scanner = Scanner.create(isTTY, System.in)) {
            while (true) {
                final var token = scanner.next();
                if (token instanceof EndOfProgram) break;
                System.out.print(token + " ");
                if (token instanceof EndOfProgram) break;
                if (token.isEOL()) {
                    System.out.println();
                    prompt();
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}