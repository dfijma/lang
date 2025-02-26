package net.fijma;

import net.fijma.parsetree.Expression;
import net.fijma.parsetree.Statement;
import net.fijma.parsetree.Unit;
import net.fijma.value.ErrorValue;
import net.fijma.value.Value;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
        try (InputStream is = (args.length > 0 ? new FileInputStream(args[0]) : System.in); Scanner scanner = Scanner.create(isTTY, is)) {
            final Parser parser = Parser.create(scanner, isTTY);
            if (isTTY) {
                parseUnits(parser);
            } else {
                parseProgram(parser);
            }
        } catch (Exception e) {
            System.err.printf("fatal error: %s%n", e.getMessage());
            System.exit(1);
        }
    }

    private static void parseUnits(Parser parser) throws IOException {
        Memory memory = new Memory();
        while (true) {
            final Unit unit = parser.parseUnit();
            if (unit.isError()) {
                System.out.printf("syntax error (position %d): %s%n", unit.token().column(), unit.errorMessage());
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
            parser.skipPseudoToken();
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
            System.out.printf("syntax error (line %d, column %d): %s%n", result.token().line(), result.token().column(), result.errorMessage());
            System.exit(1);
        }
    }


}
