package net.fijma;

import net.fijma.parsetree.*;
import net.fijma.token.*;

import java.io.IOException;
import java.util.*;

public class Parser {

    private final Scanner scanner;
    private final TokenLine tokenLine = new TokenLine();
    private final boolean interactive;

    private class TokenLine {

        private  List<Token> tokens;
        int currentToken = 0;
        boolean previousTokenLastOfLine = true;

        public Unit next(boolean prompt) throws IOException {
            do {
                if (prompt && interactive) { System.out.print("| "); }
                tokens = scanner.next();
                final var kill = tokens.stream().anyMatch(token -> token instanceof Kill);
                if (kill) {
                    final var endOfProgram = tokens.stream().anyMatch(token -> token instanceof EndOfProgram);
                    return new Unit(endOfProgram, new ArrayList<>());
                }
                if (interactive) {
                    tokens.addLast(new InvalidToken(-42, -42, "pseudo token to indicate the yet unknown next token")); // FIXME: ugly -42 by design
                }
                currentToken = 0;
                previousTokenLastOfLine = true;
            } while (tokens.isEmpty());
            return null;
        }

        public Token currentToken() {
            return tokens.get(currentToken);
        }

        public boolean previousTokenLastOfLine() {
            return previousTokenLastOfLine  || (currentToken() instanceof InvalidToken ); // yah!
        }

        void skip() throws IOException {
            previousTokenLastOfLine = false;
            currentToken++;
            if (currentToken >= tokens.size()) {
               next(true);
            }
        }
    }

    private Parser(Scanner scanner, boolean interactive) {
        this.scanner = scanner;
        this.interactive = interactive;
    }

    static Parser create(Scanner scanner) throws IOException {
        return create(scanner, false);
    }

    static Parser create(Scanner scanner, boolean interactive) throws IOException {
        final var result = new Parser(scanner, interactive);
        result.tokenLine.next(false);
        return result;
    }

    public Unit parseUnit() throws IOException {

        final List<Statement> result = new ArrayList<>();

        String error = null;
        Token errorToken = null;

        if (tokenLine.currentToken() instanceof EndOfProgram) {
            return new Unit(tokenLine.currentToken() instanceof EndOfProgram, result);
        }

        while (tokenLine.currentToken() != null) {

            final var statementOrExpression = parseStatementOrExpression();
            if (statementOrExpression.isError()) {
                errorToken = statementOrExpression.token();
                error = statementOrExpression.error();
                break;
            }

            final var semicolon = parseSymbol(Symbol.SymbolType.Semicolon);
            if (semicolon.isSuccess() || (tokenLine.previousTokenLastOfLine() || tokenLine.currentToken() instanceof EndOfProgram)) {
                result.addLast(statementOrExpression.value());
                if (tokenLine.previousTokenLastOfLine()  || tokenLine.currentToken() instanceof EndOfProgram) {
                    return new Unit(tokenLine.currentToken() instanceof EndOfProgram, result);
                }
            } else {
                errorToken = semicolon.token();
                error = semicolon.error();
                break;
            }

        }

        if (errorToken == null || error == null) { throw new IllegalStateException("bug?"); }

        return new Unit(tokenLine.currentToken() instanceof EndOfProgram, errorToken, error);
    }

    public void skipPseudoToken() throws IOException {
        if (tokenLine.currentToken() instanceof InvalidToken) { tokenLine.skip(); }
    }

    public Unit  parseProgram() throws IOException {
        List<Statement> result = new LinkedList<>();
        while (true) {
            final Unit unit = parseUnit();
            if (!unit.isError()) {
                result.addAll(unit.value());
            } else {
                return unit;
            }
            if (unit.isLast()) break;
        }
        return new Unit(true, result);
    }

    private ParseResult<? extends Statement> parseStatementOrExpression() throws IOException {
        final var statement = parseStatement();
        if (statement.isSuccess() || (statement.isError() && !statement.isGenericError())) { return statement;}
        return parseExpression();
    }

    private ParseResult<Statement> parseStatement() throws IOException {
        final ParseResult<Statement> let = parseLetStatement();
        if (let.isSuccess() || (let.isError() && !let.isGenericError())) { return let;}
        return ParseResult.genericError(tokenLine.currentToken());
    }

    // expression <- term [ additive-operator expression] | let x = expression |
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

    private ParseResult<Statement> parseLetStatement() throws IOException {
        final var let = parseReservedWord("let");
        if (let.isError()) return ParseResult.genericError(let.token());

        final ParseResult<Expression> variable = parseIdentifier();
        if (variable.isError()) return ParseResult.error(variable.token(), "identifier expected");

        final var is = parseSymbol(Set.of(Symbol.SymbolType.Equals));
        if (is.isError()) return ParseResult.error(is.token(), is.error());

        final var expression = parseExpression();
        if (expression.isError()) return ParseResult.error(expression.token(), expression.error());

        return ParseResult.success(new LetStatement((Identifier)(variable.value()), expression.value()));
    }

    // term <- factor [ multiplicative-operator term]
    private ParseResult<Expression> parseTerm() throws IOException {

        final ParseResult<Expression> factor = parseFactor();
        if (factor.isError()) { return factor; }

        final ParseResult<Symbol> operator = parseSymbol(Set.of(Symbol.SymbolType.Asterisk, Symbol.SymbolType.Slash));
        if (operator.isError()) { return factor; }

        final ParseResult<Expression> term = parseTerm();
        if (term.isSuccess()) {
            return ParseResult.success(new BinaryExpression(factor.value(), operator.value(), term.value()));
        } else {
            return term;
        }
    }

    // factor <- number | identifier | ( expression)
    private ParseResult<Expression> parseFactor() throws IOException {

        final ParseResult<Expression> number = parseNumber();
        if (number.isSuccess()) return number;

        final ParseResult<Expression> identifier = parseIdentifier();
        if (identifier.isSuccess()) return identifier;

        if (parseSymbol(Symbol.SymbolType.LeftParenthesis).isSuccess()) {
            final ParseResult<Expression> expression = parseExpression();
            if (expression.isError()) { return expression; }

            if (parseSymbol(Symbol.SymbolType.RightParenthesis).isSuccess()) {
                return expression;
            }
            return ParseResult.error(tokenLine.currentToken(), "missing )");
        }

        if (!number.isGenericError()) return number;
        return ParseResult.error(tokenLine.currentToken(), "invalid expression");
    }

    private ParseResult<Symbol> parseSymbol(Set<Symbol.SymbolType> expectedSymbols) throws IOException  {
        final var token = tokenLine.currentToken();
        return switch (token) {
            case Symbol symbol when expectedSymbols.contains(symbol.type) -> {
                tokenLine.skip();
                yield ParseResult.success(symbol);
            }
            default -> ParseResult.error(token, "unexpected token: '" + token.format() + "'");
        };
    }

    private ParseResult<Symbol> parseSymbol(Symbol.SymbolType expectedSymbol) throws IOException  {
        return parseSymbol(Set.of(expectedSymbol));
    }

    private ParseResult<Word> parseReservedWord(String word) throws IOException {
        final var token = tokenLine.currentToken();
        return switch (token) {
            case Word w when w.value().equals(word) -> {
                tokenLine.skip();
                yield ParseResult.success(w);
            }
            default -> ParseResult.error(token, "unexpected token: '" + token.format() + "'");
        };
    }

    private ParseResult<Expression> parseNumber() throws IOException {
        final var minus = parseSymbol(Symbol.SymbolType.Minus);

        final var token = tokenLine.currentToken();
        if (token instanceof NumberConstant number) {
            tokenLine.skip();
            if (number.value().contains(".")) {
                return ParseResult.error(token, "floating point number not (yet) supported");
            }
            try {
                return ParseResult.success(new IntConstant((minus.isSuccess() ? -1 : 1) * Integer.parseInt(number.value())));
            } catch (NumberFormatException e) {
                return ParseResult.error(token, "invalid number (number too big?)");
            }
        }
        return ParseResult.genericError(token);
    }

    private ParseResult<Expression> parseIdentifier() throws IOException {
        final var token = tokenLine.currentToken();
        if (token instanceof Word word) {
            tokenLine.skip();
            if (word.isReserved()) {
                return ParseResult.error(token, "identifier expected");
            }
            return ParseResult.success(new Identifier(word.value()));
        }
        return ParseResult.genericError(token);
    }

}