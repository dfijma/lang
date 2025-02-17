package net.fijma;

public class ParseResult<T> {

    final T value;
    final String error;

    ParseResult(T value, String error) {
        this.value = value;
        this.error = error;
    }

    public static <T> ParseResult<T> success(T result) {
        return new ParseResult<T>(result, null);
    }

    public static <T> ParseResult<T> error(String error) {
        return new ParseResult<T>(null, error);
    }

    public static <T> ParseResult<T> genericError() {
        return new ParseResult<T>(null, null);
    }

    public T value() { return value; }
    public String error() { return error; }
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
