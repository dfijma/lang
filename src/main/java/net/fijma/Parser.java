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

        String error;
        Token token;

        while (true) {

            if (scanner.current() instanceof NewLine || scanner.current() instanceof EndOfProgram) {
                return new Unit(scanner.current() instanceof EndOfProgram, result);
            }

            token = scanner.current();
            final var expression = parseExpression();
            if (expression.isError()) {
                error = expression.error();
                break;
            }

            token = scanner.current();
            final var semicolon = parseSymbol(Symbol.SymbolType.Semicolon);
            if (semicolon.isSuccess() || (scanner.current() instanceof NewLine || scanner.current() instanceof EndOfProgram)) {
                result.addLast(expression.value());
            } else {
                error = semicolon.error();
                break;
            }

        }

        while (!(scanner.current() instanceof NewLine || scanner.current() instanceof EndOfProgram)) {
            scanner.skip();
        }
        return new Unit(scanner.current() instanceof EndOfProgram, token, error);
    }

    public Unit parseProgram() throws IOException {
        List<Expression> result = new LinkedList<>();
        while (true) {
            final Unit unit = parseUnit();
            if (!unit.isError()) {
                result.addAll(unit.value());
            } else {
                return unit;
            }
            if (unit.isLast()) break;
            scanner.skip();
        }
        return new Unit(true, result);
    }

    // expression <- term [ additive-operator expression]
    private ParseResult<Expression> parseExpression() throws IOException {

        final ParseResult<Expression> term = parseTerm();
        if (term.isError()) { return term; }

        final ParseResult<Symbol> operator = parseSymbol(Set.of(Symbol.SymbolType.Plus, Symbol.SymbolType.Minus));
        if (operator.isError()) { return term; }

        final ParseResult<Expression> expression = parseExpression();
        if (expression.isSuccess()) {
            return ParseResult.success(new BinaryExpression(term.value(), operator.value(), expression.value()));
        } else {
            return expression;
        }
    }

    // term <- factor [ multiplicative-operator term]
    private ParseResult<Expression> parseTerm() throws IOException {

        final ParseResult<Expression> factor = parseFactor();
        if (factor.isError()) { return factor; }

        final ParseResult<Symbol> operator = parseSymbol(Set.of(Symbol.SymbolType.Asterisk, Symbol.SymbolType.Slash));
        if (operator.isError()) { return factor; }

        final ParseResult<Expression> term = parseTerm();
        if (term.isSuccess()) {
            return ParseResult.success(new BinaryExpression(factor.value, operator.value(), term.value));
        } else {
            return term;
        }
    }

    // factor <- number | ( expression)
    private ParseResult<Expression> parseFactor() throws IOException {

        final ParseResult<Expression> number = parseNumber();
        if (number.isSuccess()) return number;

        if (parseSymbol(Symbol.SymbolType.LeftParenthesis).isSuccess()) {
            final ParseResult<Expression> expression = parseExpression();
            if (expression.isError()) { return expression; }

            if (parseSymbol(Symbol.SymbolType.RightParenthesis).isSuccess()) {
                return expression;
            }
            return ParseResult.error(scanner.current(), "missing )");
        }

        if (!number.isGenericError()) return number;
        return ParseResult.error(scanner.current(), "invalid expression");
    }

    private ParseResult<Symbol> parseSymbol(Set<Symbol.SymbolType> expectedSymbols) throws IOException {
        return switch (scanner.current()) {
            case Symbol symbol when expectedSymbols.contains(symbol.type) -> {
                scanner.skip();
                yield ParseResult.success(symbol);
            }
            default -> ParseResult.error(scanner.current(), "unexpected token: '" + scanner.current().format() + "'");
        };
    }

    private ParseResult<Symbol> parseSymbol(Symbol.SymbolType expectedSymbol) throws IOException {
        return parseSymbol(Set.of(expectedSymbol));
    }

    private ParseResult<Expression> parseNumber() throws IOException {
        final var minus = parseSymbol(Symbol.SymbolType.Minus);

        final var token = scanner.current();
        if (token instanceof NumberConstant number) {
            scanner.skip();
            if (number.value().contains(".")) {
                return ParseResult.error(token, "floating point number not (yet) supported");
            };
            try {
                return ParseResult.success(new IntValue((minus.isSuccess() ? -1 : 1) * Integer.parseInt(number.value())));
            } catch (NumberFormatException e) {
                return ParseResult.error(token, "invalid number (number too big?)");
            }
        }
        return ParseResult.genericError(token);
    }

}