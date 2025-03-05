package net.fijma;

import net.fijma.token.*;
import net.fijma.token.NumberConstant;

import java.io.IOException;
import java.io.InputStream;

public class Scanner implements AutoCloseable{

    private final InputReader reader;
    private final StringBuilder sb = new StringBuilder();
    private final boolean interactive;

    private Scanner(boolean interactive, InputReader reader) throws IOException {
        this.interactive = interactive;
        this.reader = reader;
    }

    public static Scanner create(boolean interactive, InputStream is) throws IOException {
        return new Scanner(interactive, InputReader.create(is));
    }

    public static Scanner create(InputStream is) throws IOException {
        return new Scanner(true, InputReader.create(is));
    }

    private void skipScanner() throws IOException {
        reader.skip();
    }

    private boolean deferredSkip = false;

    private void setDeferredSkip() {
        if (deferredSkip) throw new IllegalStateException("Deferred skip already set");
        deferredSkip = true;
    }

    public Token next() throws IOException {
        while (true) {
            final var token = nextToken();
            skipWhitespace();
            if (token != null) {
                return token.setEOL(reader.current() == '\n');
            }
        }
    }

    private void skipWhitespace() throws IOException {
        while (reader.current() != '\n' && (reader.current() != 11 || !interactive) && Character.isWhitespace(reader.current())) {
            skipScanner();
        }
    }

    private Token nextToken() throws IOException {

        if (deferredSkip) {
            reader.skip();
            deferredSkip = false;
        }

        final int line = reader.line();
        final int column = reader.column();

        skipWhitespace();

        if (reader.current() == '\0') {
            return new EndOfProgram(line, column);
        }

        if (reader.current() == '\n') {
            setDeferredSkip();
            return null;
        }

        sb.setLength(0);

        if (Character.isDigit(reader.current()) /* || scanner.current() == '.' */) {
            int dots = 0;
            do {
                sb.append(reader.current());
                if (reader.current() == '.') {
                    dots++;
                }
                skipScanner();
            } while (Character.isDigit(reader.current())|| reader.current() == '.');

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

        if (reader.current() == ':') {
            skipScanner();
            if (reader.current() == '=') {
                skipScanner();
                return new Symbol(line, column, Symbol.SymbolType.Becomes);
            } else {
                return new Symbol(line, column, Symbol.SymbolType.Colon);
            }
        }

        if (reader.current() == '"' || reader.current() == '\'') {
            final char openChar = reader.current();
            skipScanner();
            boolean escape = false;
            while (escape || (reader.current() != openChar) && reader.current() != '\n' && reader.current() != '\0') {
                escape = !escape && reader.current() == '\\';
                sb.append(reader.current());
                skipScanner();
            }
            if (reader.current() == openChar) {
                skipScanner();
                return new StringConstant(line, column, sb.toString());
            } else if (reader.current() == '\n'|| reader.current() == '\0') {
                return new InvalidToken(line, column, "non-terminated string fragment: %s".formatted(sb.toString()));
            }
        }

        if (reader.current() == '_' || (!Character.isDigit(reader.current()) && Character.isLetterOrDigit(reader.current()))) {

            do {
                sb.append(reader.current());
                skipScanner();
            } while (reader.current() == '_' || Character.isLetterOrDigit(reader.current()));

            return new Word(line, column, sb.toString());
        }

        return new InvalidToken(line, column, Character.toString(reader.current()));

    }

    private Symbol parseSymbol(int line, int column) throws IOException {
        final var c = reader.current();

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
                final var c2 = reader.current();
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
                final var c2 = reader.current();
                yield switch (c2) {
                    case '=' -> {
                        skipScanner();
                        final var c3 = reader.current();
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
        reader.close();
    }
}