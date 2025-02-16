package net.fijma;

import net.fijma.token.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Parser {

    private final Scanner scanner;

    private Parser(Scanner scanner) {
        this.scanner = scanner;
    }

    static Optional<List<Expression>> parse(Scanner scanner) throws IOException {
        final var parser = new Parser(scanner);
        return parser.parseProgram();
    }

    // expression_list <- <NL>* expression [; or <NL> or both] [expression_list]
    private Optional<List<Expression>> parseExpressionList() throws IOException {

        var startingNewLine = parseToken(NewLine.class);
        while (startingNewLine.isPresent()) {
            startingNewLine = parseToken(NewLine.class);
        }

        final Optional<Expression> expression = parseExpression();
        if (expression.isEmpty()) return Optional.empty();

        final var semicolon = parseSymbol(Symbol.SymbolType.Semicolon);
        final var newline = parseToken(NewLine.class);
        final var endOfProgram = parseToken(EndOfProgram.class);
        if (semicolon.isEmpty() && newline.isEmpty() && endOfProgram.isEmpty()) return Optional.empty();

        final Optional<List<Expression>> expressions = parseExpressionList();
        if (expressions.isEmpty()) {
            final List<Expression> result = new LinkedList<>();
            result.add(expression.get());
            return Optional.of(result);
        } else {
            final List<Expression> result = expressions.get();
            result.addFirst(expression.get());
            return Optional.of(result);
        }
    }

    private Optional<List<Expression>> parseProgram() throws IOException {
        final Optional<List<Expression>> expressionList = parseExpressionList();
        if (expressionList.isEmpty()) return Optional.empty();

        final var endOfProgram = parseEndOfProgram();
        if (endOfProgram.isEmpty()) return Optional.empty();

        return expressionList;
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

