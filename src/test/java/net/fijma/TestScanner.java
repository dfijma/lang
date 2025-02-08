
package net.fijma;

import net.fijma.token.EndOfProgram;
import net.fijma.token.Token;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestScanner {

    @Test
    public void testRegular() {
        test("some identifiers split on two lines", "een twee\r\ndrie vier", List.of("Word(een)", "Word(twee)", "NewLine", "Word(drie)", "Word(vier)", "EndOfProgram"));
    }

    @Test
    void testZero() {
        test("zero byte is the end", "x\0y", List.of("Word(x)", "EndOfProgram"));
    }

    @Test
    void testNumber() {
        test("a regular float", "123.010", List.of("Number(123.010)", "EndOfProgram"));
        test("zero can be omitted", ".123", List.of("Number(.123)", "EndOfProgram"));
        test("zero can be omitted", "0.123", List.of("Number(0.123)", "EndOfProgram"));
        test("but not at the end", "123.", List.of("InvalidToken(invalid number: 123.)", "EndOfProgram"));
        test("a regular integer", "456", List.of("Number(456)", "EndOfProgram"));
        test("cannot have two decimal dots", "1.23.", List.of("InvalidToken(invalid number: 1.23.)", "EndOfProgram"));
    }

    @Test
    void testNewLine() {
        test("a regular newline", "\n", List.of("NewLine", "EndOfProgram"));
        test("the CR is optional", "\r\n", List.of("NewLine", "EndOfProgram"));
        test("a lot CR's are ignored", "\r\r\n\r", List.of("NewLine", "EndOfProgram"));
        test("a lot of newline", "\nxx\n\nyy\n", List.of("NewLine", "Word(xx)", "NewLine", "NewLine", "Word(yy)", "NewLine", "EndOfProgram"));
        test("a lot of newlines with some optional CR's", "\r\nxx\n\r\nyy\n\r", List.of("NewLine", "Word(xx)", "NewLine", "NewLine", "Word(yy)", "NewLine", "EndOfProgram"));
    }

    @Test
    void testWords() {
        test("two words","  begin end  ", List.of("Word(begin)", "Word(end)", "EndOfProgram"));
        test("words can start with a _", "_1aaa", List.of("Word(_1aaa)", "EndOfProgram"));
        test("words cannot start with a digit", "1a_22aa", List.of("Number(1)", "Word(a_22aa)", "EndOfProgram"));
        test("words looking to start with a number are actually tokenized as a number and word, the parser will have to fix it", "112.0a_22aa", List.of("Number(112.0)", "Word(a_22aa)", "EndOfProgram"));
    }

    @Test
    void testOperators() {
        test("a double char operator (becomes)", "x:=y", List.of("Word(x)", "Becomes", "Word(y)", "EndOfProgram"));
        test("a double char operator (equals)", "x==y", List.of("Word(x)", "Equals", "Word(y)", "EndOfProgram"));
        test("javascript sucks", "x===y", List.of("Word(x)", "ReallyEquals", "Word(y)", "EndOfProgram"));
        test("javascript sucks (part 2)", "x== =y", List.of("Word(x)", "Equals", "Is", "Word(y)", "EndOfProgram"));
        test("double char operators cannot contain whitespace", "x: =y", List.of("Word(x)", "Colon", "Is", "Word(y)", "EndOfProgram"));
        test("some regular sequence of words and punctuation", "x(y)", List.of("Word(x)", "ParOpen", "Word(y)", "ParClose", "EndOfProgram"));
        test("brackets", "(){}[]", List.of("ParOpen", "ParClose", "CurlyOpen", "CurlyClose", "SquareOpen", "SquareClose", "EndOfProgram"));
        // test("dot", ".", List.of("Dot", "EndOfProgram"));
    }

    @Test
    void testStrings() {
        test("the empty string", "\"\"", List.of("StringConstant()", "EndOfProgram"));
        test("some regular string", "\"abc def\"", List.of("StringConstant(abc def)", "EndOfProgram"));
        test("string with escaping", "\"a\\b\\\\c\\nd\\ef\"", List.of("StringConstant(a\\b\\\\c\\nd\\ef)", "EndOfProgram"));
        test("non-terminated string (end of line)", "\"a\n", List.of("InvalidToken(non-terminated string fragment: a)", "NewLine", "EndOfProgram"));
        test("non-terminated string (end of file)", "\"a", List.of("InvalidToken(non-terminated string fragment: a)", "EndOfProgram"));
        test("tokenizer can recover after non-terminated string", "\"a\nabc", List.of("InvalidToken(non-terminated string fragment: a)", "NewLine", "Word(abc)", "EndOfProgram"));
        test("empty non terminated (end of line)", "\"\n", List.of("InvalidToken(non-terminated string fragment: )", "NewLine", "EndOfProgram"));
        test("empty non terminated (end of file)", "\"", List.of("InvalidToken(non-terminated string fragment: )", "EndOfProgram"));
        test("empty non terminated (expliciet end of file)", "\"\0", List.of("InvalidToken(non-terminated string fragment: )", "EndOfProgram"));
    }

    public void test(String reason, String input, List<String> expected) {

        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

        try (Scanner t = Scanner.create(bais)) {
            List<String> actual = new ArrayList<>();
            Token token;
            do {
                token = t.current();
                actual.add(token.toString());
                t.skip();
            } while (! (token instanceof EndOfProgram));

            assertThat(reason, actual, is(expected));

            t.skip();
            assertThat( "EndOfProgram", is(t.current().toString()));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
