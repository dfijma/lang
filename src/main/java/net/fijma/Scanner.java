package net.fijma;

import net.fijma.token.*;
import net.fijma.token.NumberConstant;

import java.io.IOException;
import java.io.InputStream;

public class Scanner implements AutoCloseable{

    private final InputReader scanner;
    private final StringBuilder sb = new StringBuilder();
    private Token current;

    private Scanner(InputReader scanner) {
        this.scanner = scanner;
        this.current = null;
    }

    public static Scanner create(InputStream is) throws IOException {
        final var res = new Scanner(InputReader.create(is));
        res.skip();
        return res;
    }

    public Token current() {
        return current;
    }

    private boolean deferredSkip = false;

    private void setDeferredSkip() {
        if (deferredSkip) throw new IllegalStateException("Deferred skip already set");
        deferredSkip = true;
    }

    public void skip() throws IOException {
        if (deferredSkip) {
            scanner.skip();
            deferredSkip = false;
        }
        if (!(current instanceof EndOfProgram)) {

            while (scanner.current() != '\n' && Character.isWhitespace(scanner.current())) {
                scanner.skip();
            }

            if (scanner.current() == '\n') {
                setDeferredSkip();
                current = new NewLine();
                return;
            }

            sb.setLength(0);

            if (Character.isDigit(scanner.current()) /* || scanner.current() == '.' */) {
                int dots = 0;
                do {
                    sb.append(scanner.current());
                    if (scanner.current() == '.') {
                        dots++;
                    }
                    scanner.skip();
                } while (Character.isDigit(scanner.current())|| scanner.current() == '.');

                final String value = sb.toString();
                if (dots == 0) {
                    current = new NumberConstant(value);
                } else if (dots == 1 && value.lastIndexOf('.') != value.length()-1) {
                    current = new NumberConstant(value);
                } else {
                    current = new InvalidToken("invalid number: %s".formatted(value));
                }
                return;
            }

            if (scanner.current() == '\0') {
                current = new EndOfProgram();
                return;
            }

            final var symbol = parseSymbol();
            if (symbol != null) {
                current = symbol;
                return;
            }

            if (scanner.current() == ':') {
                scanner.skip();
                if (scanner.current() == '=') {
                    scanner.skip();
                    current = new Symbol(Symbol.SymbolType.Becomes);
                } else {
                    current = new Symbol(Symbol.SymbolType.Colon);
                }
                return;
            }

            if (scanner.current() == '"' || scanner.current() == '\'') {
                final char openChar = scanner.current();
                scanner.skip();
                boolean escape = false;
                while (escape || (scanner.current() != openChar) && scanner.current() != '\n' && scanner.current() != '\0') {
                    escape = !escape && scanner.current() == '\\';
                    sb.append(scanner.current());
                    scanner.skip();
                }
                if (scanner.current() == openChar) {
                    scanner.skip();
                    current = new StringConstant(sb.toString());
                    return;
                } else if (scanner.current() == '\n'|| scanner.current() == '\0') {
                    current = new InvalidToken("non-terminated string fragment: %s".formatted(sb.toString()));
                    return;
                }
            }

            if (scanner.current() == '_' || (!Character.isDigit(scanner.current()) && Character.isLetterOrDigit(scanner.current()))) {
                do {
                    sb.append(scanner.current());
                    scanner.skip();
                } while (scanner.current() == '_' || Character.isLetterOrDigit(scanner.current()));

                current = new Word(sb.toString());
                return;
            }

            current = new InvalidToken(Character.toString(scanner.current()));
            scanner.skip();
        }
    }

    private Symbol parseSymbol() throws IOException {
        final var c = scanner.current();

        final var s = switch (c) {
            case '(' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.LeftParenthesis); }
            case ')' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.RightParenthesis); }
            case '{' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.LeftCurlyBracket); }
            case '}' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.RightCurlyBracket); }
            case '[' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.LeftSquareBracket); }
            case ']' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.RightSquareBracket); }
            case '.' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.Dot); }
            case '*' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.Asterisk); }
            case '/' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.Slash); }
            case '@' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.At); }
            case '>' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.GreaterThan); }
            case '<' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.SmallerThan); }
            case '!' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.Exclamation); }
            case ',' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.Comma); }
            case '+' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.Plus); }
            case '-' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.Minus); }
            case ';' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.Semicolon); }
            case '|' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.Pipe); }
            case '&' -> { scanner.skip(); yield new Symbol(Symbol.SymbolType.Ampersand); }
            case ':' -> { // ":", ":=" and "=" are all valid, also like this: "<<", ">>" "?:", "<=", ">", "!=", "++", "--"
                scanner.skip();
                final var c2 = scanner.current();
                yield switch (c2) {
                    case '=' -> {
                        scanner.skip();
                        yield new Symbol(Symbol.SymbolType.Becomes);
                    }
                    default -> new Symbol(Symbol.SymbolType.Colon);
                };
            }
            case '=' -> { // "=", "==" and "===" are all valid (like <<=, >>=)
                scanner.skip();
                final var c2 = scanner.current();
                yield switch (c2) {
                    case '=' -> {
                        scanner.skip();
                        final var c3 = scanner.current();
                        yield switch (c3) {
                            case '=' -> {
                                scanner.skip();
                                yield new Symbol(Symbol.SymbolType.EqualsEqualsEquals);
                            }
                            default -> new Symbol(Symbol.SymbolType.EqualsEquals);
                        };
                    }
                    default -> new Symbol(Symbol.SymbolType.Equals);
                };            }
            default -> null;
        };

        return s;

    }

    @Override
    public void close() throws Exception {
        scanner.close();
    }
}