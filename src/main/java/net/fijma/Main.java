package net.fijma;

import gnu.getopt.Getopt;
import net.fijma.parsetree.Expression;
import net.fijma.parsetree.Statement;
import net.fijma.parsetree.Unit;
import net.fijma.token.EndOfProgram;
import net.fijma.value.ErrorValue;
import net.fijma.value.Value;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main {

    static boolean isTTY = System.console() != null;
    static boolean forceTTY = false;
    static boolean scanOnly  = false;
    static String filename = null;

    public static void main(String[] args) {
        getOpts(args);
        parse(filename);
    }

    private static void prompt() {
        if (isTTY) {
            System.out.print("> ");
            System.out.flush();
        }
    }

    private static void getOpts(String[] args) {
        final Getopt g = new Getopt("lang", args, "st");
        int c;
        while ( (c = g.getopt()) != -1) {
            switch(c) {
                case 's':
                    scanOnly = true;
                    break;
                case 't':
                    forceTTY = true;
                    break;
                case '?':
                    System.exit(2);
                    break;
                default:
                    System.out.print("bug: getopt() returned " + c + "\n");
                    System.exit(1);
            }
        }

        int f = g.getOptind();
        filename = null;
        if (f < args.length) {
            filename = args[f];
        }

        isTTY = (isTTY && filename == null) || forceTTY;
    }

    public static void parse(String filename) {
        prompt();
        try (InputStream is = (filename != null ? new FileInputStream(filename) : System.in); Scanner scanner = Scanner.create(isTTY, is)) {
            if (scanOnly) {
                while (true) {
                    final var token = scanner.next();
                    System.out.print(token + " ");
                    if (token instanceof EndOfProgram) break;
                    if (token.isEOL()) {
                        System.out.println();
                        prompt();
                    }
                }
            } else {
                final Parser parser = Parser.create(scanner, isTTY);
                if (isTTY) {
                    parseUnits(parser);
                } else {
                    parseProgram(parser);
                }
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
                parser.skipToEndOfLine();
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
            System.out.printf("syntax error (line %d, column %d): %s%n", result.token().line(), result.token().column(), result.errorMessage());
            System.exit(1);
        }
    }

}