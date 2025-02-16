package net.fijma;

import net.fijma.token.*;

import java.io.IOException;
import java.util.*;

public class Parser {

    private final Scanner scanner;

    private Parser(Scanner scanner) {
        this.scanner = scanner;
    }

    static Parser create(Scanner scanner)  {
        return new Parser(scanner);
    }

    public Unit parseUnit() throws IOException {

        final List<Expression> result = new ArrayList<>();

        while (true) {

            if (scanner.current() instanceof NewLine || scanner.current() instanceof EndOfProgram) {
                return new Unit(scanner.current() instanceof EndOfProgram, Optional.of(result));
            }

            final var expression = parseExpression();
            if (expression.isEmpty()) break;

            final var semicolon = parseSymbol(Symbol.SymbolType.Semicolon);
            if (semicolon.isPresent() || (scanner.current() instanceof NewLine || scanner.current() instanceof EndOfProgram)) {
                result.addLast(expression.get());
            } else break;

        }

        while (!(scanner.current() instanceof NewLine || scanner.current() instanceof EndOfProgram)) {
            scanner.skip();
        }
        return new Unit(scanner.current() instanceof EndOfProgram, Optional.empty());
    }

    public Optional<List<Expression>> parseProgram() throws IOException {
        List<Expression> result = new LinkedList<>();
        while (true) {
            final Unit unit = parseUnit();
            if (unit.expressions().isPresent()) {
                result.addAll(unit.expressions().get());
            } else {
                return Optional.empty();
            }
            if (unit.isLast()) break;
            scanner.skip();
        }
        return Optional.of(result);
    }

    // expression <- term [ additive-operator expression]
    private Optional<Expression> parseExpression() throws IOException {

        final Optional<Expression> term = parseTerm();
        if (term.isEmpty()) { return Optional.empty(); }

        final Optional<Symbol> operator = parseSymbol(Set.of(Symbol.SymbolType.Plus, Symbol.SymbolType.Minus));
        if (operator.isEmpty()) { return term; }

        final Optional<Expression> expression = parseExpression();
        if (expression.isPresent()) {
            return Optional.of(new BinaryExpression(term.get(), operator.get(), expression.get()));
        } else {
            return Optional.empty();
        }
    }


    // term <- factor [ multiplicative-operator term]
    private Optional<Expression> parseTerm() throws IOException {

        final Optional<Expression> factor = parseFactor();
        if (factor.isEmpty()) { return Optional.empty(); }

        final Optional<Symbol> operator = parseSymbol(Set.of(Symbol.SymbolType.Asterisk, Symbol.SymbolType.Slash));
        if (operator.isEmpty()) { return factor; }

        final Optional<Expression> term = parseTerm();
        if (term.isPresent()) {
            return Optional.of(new BinaryExpression(factor.get(), operator.get(), term.get()));
        } else {
            return Optional.empty();
        }
    }

    // factor <- number | ( expression)
    private Optional<Expression> parseFactor() throws IOException {

        final Optional<Expression> number = parseNumber();
        if (number.isPresent()) return number;

        if (parseSymbol(Symbol.SymbolType.LeftParenthesis).isPresent()) {
            final Optional<Expression> expression = parseExpression();
            if (parseSymbol(Symbol.SymbolType.RightParenthesis).isPresent()) {
                return expression;
            }
            return Optional.empty();
        }

        return Optional.empty();
    }

    private Optional<Symbol> parseSymbol(Set<Symbol.SymbolType> expectedSymbols) throws IOException {
        return switch (scanner.current()) {
            case Symbol symbol when expectedSymbols.contains(symbol.type) -> {
                scanner.skip();
                yield Optional.of(symbol);
            }
            default -> Optional.empty();
        };
    }

    private Optional<Token> parseNewLine() throws IOException {
        final var current = scanner.current();
        return switch (current) {
            case NewLine ignored -> {
                scanner.skip();
                yield Optional.of(current);
            }
            default -> Optional.empty();
        };
    }

    private Optional<Token> parseEndOfProgram() throws IOException {
        final var current = scanner.current();
        return switch (current) {
            case EndOfProgram ignored -> Optional.of(current);
            default -> Optional.empty();
        };
    }

    private <T extends Token> Optional<Token> parseToken(Class<T> tokenClass) throws IOException {
        final var current = scanner.current();
        if (tokenClass.isInstance(current)) {
            scanner.skip();
            return Optional.of(current);
        }
        return Optional.empty();
    }


    private Optional<Symbol> parseSymbol(Symbol.SymbolType expectedSymbol) throws IOException {
        return parseSymbol(Set.of(expectedSymbol));
    }

    private Optional<Expression> parseNumber() throws IOException {
        final var token = scanner.current();
        if (token instanceof NumberConstant number) {
            scanner.skip();
            return Optional.of(new LiteralExpression(Integer.parseInt(number.value())));
        }
        return Optional.empty();
    }


}

