package net.fijma;

import net.fijma.parsetree.Expression;
import net.fijma.parsetree.Statement;
import net.fijma.parsetree.Unit;
import net.fijma.value.ErrorValue;
import net.fijma.value.Value;

import java.io.IOException;

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
        try (Scanner scanner = Scanner.create(isTTY, System.in)) {
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
        Memory memory = new Memory();
        while (true) {
            final Unit unit = parser.parseUnit();
            if (unit.isError()) {
                System.out.println("syntax error (position %d): %s".formatted(unit.token().column(), unit.errorMessage()));
            } else if (!unit.value().isEmpty()) {
                System.out.println(unit.value());
                for (Statement statement : unit.value()) {
                    final Value value = statement.eval(memory);
                    if (statement instanceof Expression || value instanceof ErrorValue) {
                        System.out.println(value);
                    }
                }
            }
            if (unit.isLast()) break;
            prompt();
        }
    }

    private static void parseProgram(Parser parser) throws IOException {
        Memory memory = new Memory();
        final Unit result = parser.parseProgram();
        if (!result.isError()) {
            for (Statement statement : result.value()) {
                final Value value = statement.eval(memory);
                if (statement instanceof Expression || value instanceof ErrorValue) {
                    System.out.println(value);
                }
            }
        } else {
            System.out.println("syntax error (line %d, column %d): %s".formatted(result.token().line(), result.token().column(), result.errorMessage()));
            System.exit(1);
        }
    }


}
