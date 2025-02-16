package net.fijma;

import net.fijma.token.EndOfProgram;
import net.fijma.token.NewLine;

import java.util.List;
import java.util.Optional;

public class ParserMain {

    public static void main(String[] args) {

        try (Scanner t = Scanner.create(System.in)) {
            final Optional<List<Expression>> result = Parser.parse(t);
            if (result.isPresent()) {
                for (Expression expr : result.get()) {
                    System.out.println(expr);
                }
            } else {
                System.out.println("syntax error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
