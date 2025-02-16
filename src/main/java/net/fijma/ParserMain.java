package net.fijma;

import net.fijma.token.EndOfProgram;
import net.fijma.token.NewLine;

public class ParserMain {

    public static void main(String[] args) {

        try (Scanner t = Scanner.create(System.in)) {
            final var result = Parser.parse(t);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
