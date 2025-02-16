package net.fijma;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ParserMain {

    static boolean isTTY = System.console() != null;

    private static void prompt() {
        if (isTTY) {
            System.out.print("> ");
            System.out.flush();
        }
    }

    public static void main(String[] args) {
        prompt();
        try (Scanner scanner = Scanner.create(System.in)) {
            Parser parser = Parser.create(scanner);
            if (isTTY) {
                parseUnits(parser, scanner);
            } else {
                parseProgram(parser);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseUnits(Parser parser, Scanner t) throws IOException {
        while (true) {
            final Unit unit = parser.parseUnit();
            if (unit.expressions().isEmpty()) {
                System.out.println("syntax error");
            } else if (!unit.expressions().get().isEmpty()) {
                System.out.println(unit.expressions().get());
            }
            if (unit.isLast()) break;
            prompt();
            t.skip();
        }
    }

    private static void parseProgram(Parser parser) throws IOException {
        final Optional<List<Expression>> result = parser.parseProgram();
        if (result.isPresent()) {
            for (Expression expr : result.get()) {
                System.out.println(expr);
            }
        } else {
            System.out.println("syntax error");
            System.exit(1);
        }
    }
}
