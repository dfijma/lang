package net.fijma.parsetree;

import net.fijma.token.Token;

public class ParseResult<T> {

    final T value;
    final boolean isError;
    final String error;
    final Token token;

    ParseResult(T value,Token token, boolean isError, String error) {
        this.value = value;
        this.token = token;
        this.isError = isError;
        this.error = error;
    }

    public static <T> ParseResult<T> success(T result) {
        return new ParseResult<T>(result, null, false, null);
    }

    public static <T> ParseResult<T> error(Token token, String error) {
        return new ParseResult<T>(null, token, true, error);
    }

    public static <T> ParseResult<T> genericError(Token token) {
        return new ParseResult<T>(null, token, true, null);
    }

    public T value() { return value; }
    public String error() { return error; }
    public Token token() { return token; }

    public boolean isError() { return isError; }
    public boolean isGenericError() { return isError() && error == null; }
    public boolean isSuccess() { return !isError(); }

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
