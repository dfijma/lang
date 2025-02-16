package net.fijma;

import net.fijma.token.*;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class Parser {

    private final Scanner scanner;

    private Parser(Scanner scanner) {
        this.scanner = scanner;
    }

    static Optional<Expression> parse(Scanner scanner) throws IOException {
        final var parser = new Parser(scanner);
        return parser.parseProgram();
    }

    private Optional<Expression> parseProgram() throws IOException {
        final Optional<Expression> expression = parseExpression();
        if (expression.isEmpty()) return Optional.empty();

        final var ignored = parseNewLine();

        final var endOfProgram = parseEndOfProgram();
        if (endOfProgram.isEmpty()) return Optional.empty();

        return expression;
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

