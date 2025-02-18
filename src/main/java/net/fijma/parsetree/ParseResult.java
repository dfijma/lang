package net.fijma.parsetree;

import net.fijma.token.Token;

public class ParseResult<T> {

    final T value;
    final String error;
    final Token token;

    ParseResult(T value,Token token, String error) {
        this.value = value;
        this.token = token;
        this.error = error;
    }

    public static <T> ParseResult<T> success(T result) {
        return new ParseResult<T>(result, null, null);
    }

    public static <T> ParseResult<T> error(Token token, String error) {
        return new ParseResult<T>(null, token, error);
    }

    public static <T> ParseResult<T> genericError(Token token) {
        return new ParseResult<T>(null, token, null);
    }

    public T value() { return value; }
    public String error() { return error; }
    public Token token() { return token; }

    public boolean isError() { return value == null; }
    public boolean isGenericError() { return value == null  && error == null; }
    public boolean isSuccess() { return value != null; }

    public String errorMessage() {
        if (isError()) {
            if (isGenericError()) {
                return "Unknown error";
            } else {
                return error;
            }
        } else {
            throw new IllegalStateException("not an error?");
        }
    }
}
