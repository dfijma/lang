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
        prompt(true);
    }

    private static void prompt(boolean done) {
        if (isTTY) {
            System.out.print(done ? "> " : "| ");
            System.out.flush();
        }
    }

    public static void main(String[] args) {
        prompt();
        try (Scanner scanner = Scanner.create(isTTY, System.in)) {
            FakeParser parser = new FakeParser(scanner);
            if (isTTY) {
                parseUnitsAsync(parser);
            } else {
                parseProgram(parser);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseUnitsAsync(FakeParser parser) throws IOException {
        Memory memory = new Memory();
        while (true) {
            final FakeParser.Step step = parseUnitAsync(parser, memory);
            if (step == null || (step instanceof FakeParser.ResultStep resultStep && resultStep.getUnit().isLast())) break;
            prompt(true);
        }
    }

    private static FakeParser.Step parseUnitAsync(FakeParser parser, Memory memory) throws IOException {
        parser.start();
        FakeParser.Step step = null;
        while (true) {
            try {
                step = parser.step();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            if (step instanceof FakeParser.ContinueStep) {
                prompt(false);
            } else {
                parser.join();
                if (step instanceof FakeParser.ResultStep resultStep) {
                    final Unit unit = resultStep.getUnit();
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
                } else if (step instanceof FakeParser.ExceptionStep es) {
                    throw es.get();
                }
                break;
            }
        }
        return step;
    }

    private static void parseProgram(FakeParser parser) throws IOException {
        Memory memory = new Memory();
        final var result = parser.run();
        switch (result) {
            case FakeParser.ExceptionStep exceptionStep -> throw exceptionStep.get();
            case FakeParser.ResultStep resultStep -> {
                final var unit = resultStep.getUnit();
                if (!unit.isError()) {
                    for (Statement statement : unit.value()) {
                        final Value value = statement.eval(memory);
                        if (statement instanceof Expression || value instanceof ErrorValue) {
                            System.out.println(value);
                        }
                    }
                } else {
                    System.out.println("syntax error (line %d, column %d): %s".formatted(unit.token().line(), unit.token().column(), unit.errorMessage()));
                    System.exit(1);
                }
            }
            default -> throw new IllegalStateException("bug: parseProgramFake should continue to EndOfProgram but got " + result);
        }

    }


}
