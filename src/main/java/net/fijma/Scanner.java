package net.fijma;

import net.fijma.token.*;
import net.fijma.token.NumberConstant;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Scanner implements AutoCloseable{

    private final InputReader scanner;
    private final StringBuilder sb = new StringBuilder();

    private Scanner(InputReader scanner) {
        this.scanner = scanner;
    }

    public static Scanner create(InputStream is) throws IOException {
        return new Scanner(InputReader.create(is));
    }

    boolean atEnd = false;

    public List<Token> next() throws IOException {
        if (atEnd) return null;

        List<Token> result = new ArrayList<>();
        Token token;
        while ( (token=nextToken()) != null) {
            result.add(token);
            if (token instanceof EndOfProgram) {
                atEnd = true;
                break;
            }
        }
        return result;
    }

    private void skipScanner() throws IOException {
        scanner.skip();
    }

    private boolean deferredSkip = false;

    private void setDeferredSkip() {
        if (deferredSkip) throw new IllegalStateException("Deferred skip already set");
       deferredSkip = true;
    }

    private Token nextToken() throws IOException {

        if (deferredSkip) {
            scanner.skip();
            deferredSkip = false;
        }

        final int line = scanner.line();
        final int column = scanner.column();

        while (scanner.current() != '\n' && Character.isWhitespace(scanner.current())) {
            skipScanner();
        }

        if (scanner.current() == '\0') {
            return new EndOfProgram(line, column);
        }

        if (scanner.current() == '\n') {
            setDeferredSkip();
            // skipScanner();
            return null;
        }

        sb.setLength(0);

        if (Character.isDigit(scanner.current()) /* || scanner.current() == '.' */) {
            int dots = 0;
            do {
                sb.append(scanner.current());
                if (scanner.current() == '.') {
                    dots++;
                }
                skipScanner();
            } while (Character.isDigit(scanner.current())|| scanner.current() == '.');

            final String value = sb.toString();
            if (dots == 0) {
                return new NumberConstant(line, column, value);
            } else if (dots == 1 && value.lastIndexOf('.') != value.length()-1) {
                return new NumberConstant(line, column, value);
            } else {
                return new InvalidToken(line, column, "invalid number: %s".formatted(value));
            }
        }

        final var symbol = parseSymbol(line, column);
        if (symbol != null) {
            return symbol;
        }

        if (scanner.current() == ':') {
            skipScanner();
            if (scanner.current() == '=') {
                skipScanner();
                return new Symbol(line, column, Symbol.SymbolType.Becomes);
            } else {
                return new Symbol(line, column, Symbol.SymbolType.Colon);
            }
        }

        if (scanner.current() == '"' || scanner.current() == '\'') {
            final char openChar = scanner.current();
            skipScanner();
            boolean escape = false;
            while (escape || (scanner.current() != openChar) && scanner.current() != '\n' && scanner.current() != '\0') {
                escape = !escape && scanner.current() == '\\';
                sb.append(scanner.current());
                skipScanner();
            }
            if (scanner.current() == openChar) {
                skipScanner();
                return new StringConstant(line, column, sb.toString());
            } else if (scanner.current() == '\n'|| scanner.current() == '\0') {
                return new InvalidToken(line, column, "non-terminated string fragment: %s".formatted(sb.toString()));
            }
        }

        if (scanner.current() == '_' || (!Character.isDigit(scanner.current()) && Character.isLetterOrDigit(scanner.current()))) {

            do {
                sb.append(scanner.current());
                skipScanner();
            } while (scanner.current() == '_' || Character.isLetterOrDigit(scanner.current()));

            return new Word(line, column, sb.toString());
        }

        return new InvalidToken(line, column, Character.toString(scanner.current()));

    }

    private Symbol parseSymbol(int line, int column) throws IOException {
        final var c = scanner.current();

        return switch (c) {
            case '(' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.LeftParenthesis); }
            case ')' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.RightParenthesis); }
            case '{' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.LeftCurlyBracket); }
            case '}' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.RightCurlyBracket); }
            case '[' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.LeftSquareBracket); }
            case ']' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.RightSquareBracket); }
            case '.' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.Dot); }
            case '*' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.Asterisk); }
            case '/' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.Slash); }
            case '@' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.At); }
            case '>' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.GreaterThan); }
            case '<' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.SmallerThan); }
            case '!' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.Exclamation); }
            case ',' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.Comma); }
            case '+' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.Plus); }
            case '-' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.Minus); }
            case ';' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.Semicolon); }
            case '|' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.Pipe); }
            case '&' -> { skipScanner(); yield new Symbol(line, column, Symbol.SymbolType.Ampersand); }
            case ':' -> { // ":", ":=" and "=" are all valid, also like this: "<<", ">>" "?:", "<=", ">", "!=", "++", "--"
                skipScanner();
                final var c2 = scanner.current();
                yield switch (c2) {
                    case '=' -> {
                        skipScanner();
                        yield new Symbol(line, column, Symbol.SymbolType.Becomes);
                    }
                    default -> new Symbol(line, column, Symbol.SymbolType.Colon);
                };
            }
            case '=' -> { // "=", "==" and "===" are all valid (like <<=, >>=)
                skipScanner();
                final var c2 = scanner.current();
                yield switch (c2) {
                    case '=' -> {
                        skipScanner();
                        final var c3 = scanner.current();
                        yield switch (c3) {
                            case '=' -> {
                                skipScanner();
                                yield new Symbol(line, column, Symbol.SymbolType.EqualsEqualsEquals);
                            }
                            default -> new Symbol(line, column, Symbol.SymbolType.EqualsEquals);
                        };
                    }
                    default -> new Symbol(line, column, Symbol.SymbolType.Equals);
                };            }
            default -> null;
        };
    }

    @Override
    public void close() throws Exception {
        scanner.close();
    }
}