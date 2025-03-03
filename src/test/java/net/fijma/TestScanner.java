
package net.fijma;

import net.fijma.token.EndOfProgram;
import net.fijma.token.Token;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestScanner {

    @Test
    public void testRegular() {
        test("trivial", "een\n", List.of("Word(een)", "EndOfLine", "EndOfProgram"));
        test("trivial", "een  \n", List.of("Word(een)", "EndOfLine", "EndOfProgram"));
        test("trivial (EOF)", "een", List.of("Word(een)", "EndOfProgram"));
        test("some identifiers split on two lines", "een twee\r\ndrie vier\r\n", List.of("Word(een)", "Word(twee)", "EndOfLine", "Word(drie)", "Word(vier)", "EndOfLine", "EndOfProgram"));
        test("some identifiers split on two lines (NO EOL)", "een twee\r\ndrie vier", List.of("Word(een)", "Word(twee)", "EndOfLine", "Word(drie)", "Word(vier)", "EndOfProgram"));
    }

    @Test
    void testZero() {
        test("zero byte is the end", "x\0y", List.of("Word(x)", "EndOfProgram"));
    }

    @Test
    void testNumber() {
        test("a regular float", "123.010", List.of("Number(123.010)", "EndOfProgram"));
        /* FIXME: numbers could start with "." without conflicting with a "." operator
        test("zero can be omitted", ".123", List.of("Number(.123)", "EndOfProgram"));
         */
        test("zero can be omitted", "0.123", List.of("Number(0.123)", "EndOfProgram"));
        test("but not at the end", "123.", List.of("InvalidToken(invalid number: 123.)", "EndOfProgram"));
        test("a regular integer", "456", List.of("Number(456)", "EndOfProgram"));
        test("cannot have two decimal dots", "1.23.", List.of("InvalidToken(invalid number: 1.23.)", "EndOfProgram"));
    }

    @Test
    void testNewLine() {
        test("a regular newline", "\n", List.of( "EndOfLine", "EndOfProgram"));
        test("the CR is optional", "\r\n", List.of("EndOfLine", "EndOfProgram"));
        test("a lot CR's are ignored", "\r\r\n\r", List.of("EndOfLine", "EndOfProgram"));
        test("a lot of newline", "\nxx\n\nyy", List.of("EndOfLine", "Word(xx)", "EndOfLine", "EndOfLine", "Word(yy)", "EndOfProgram"));
        test("a lot of newlines with some optional CR's", "\r\nxx\n\r\nyy\n\r", List.of("EndOfLine", "Word(xx)", "EndOfLine", "EndOfLine", "Word(yy)", "EndOfLine", "EndOfProgram"));
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
        test("javascript sucks", "x===y", List.of("Word(x)", "EqualsEqualsEquals", "Word(y)", "EndOfProgram"));
        test("javascript sucks (part 2)", "x== =y", List.of("Word(x)", "EqualsEquals", "Equals", "Word(y)", "EndOfProgram"));
        test("double char operators cannot contain whitespace", "x: =y", List.of("Word(x)", "Colon", "Equals", "Word(y)", "EndOfProgram"));
        test("some regular sequence of words and punctuation", "x(y)", List.of("Word(x)", "LeftParenthesis", "Word(y)", "RightParenthesis", "EndOfProgram"));
        test("brackets", "(){}[]", List.of("LeftParenthesis", "RightParenthesis", "LeftCurlyBracket", "RightCurlyBracket", "LeftSquareBracket", "RightSquareBracket", "EndOfProgram"));
        test("dot", ".", List.of("Dot", "EndOfProgram"));
    }

    @Test
    void testStrings() {
        test("the empty string", "\"\"", List.of("StringConstant()", "EndOfProgram"));
        test("some regular string", "\"abc def\"", List.of("StringConstant(abc def)", "EndOfProgram"));
        test("string with escaping", "\"a\\b\\\\c\\nd\\ef\"", List.of("StringConstant(a\\b\\\\c\\nd\\ef)", "EndOfProgram"));
        test("non-terminated string (end of line)", "\"a\n", List.of("InvalidToken(non-terminated string fragment: a)", "EndOfLine", "EndOfProgram"));
        test("non-terminated string (end of file)", "\"a", List.of("InvalidToken(non-terminated string fragment: a)", "EndOfProgram"));
        test("tokenizer can recover after non-terminated string", "\"a\nabc", List.of("InvalidToken(non-terminated string fragment: a)", "EndOfLine", "Word(abc)", "EndOfProgram"));
        test("empty non terminated (end of line)", "\"\n", List.of("InvalidToken(non-terminated string fragment: )", "EndOfLine", "EndOfProgram"));
        test("empty non terminated (end of file)", "\"", List.of("InvalidToken(non-terminated string fragment: )", "EndOfProgram"));
        test("empty non terminated (explicit end of file)", "\"\0", List.of("InvalidToken(non-terminated string fragment: )", "EndOfProgram"));
        test("no need to escape single quote in double quoted string", "\"a'bc\"", List.of("StringConstant(a'bc)", "EndOfProgram"));
    }

    @Test
    void testSingleQuotedString() {
        test("some regular string (single quoted)", "'abc def'", List.of("StringConstant(abc def)", "EndOfProgram"));
        test("empty non terminated (single quoted, end of line)", "'\n", List.of("InvalidToken(non-terminated string fragment: )", "EndOfLine", "EndOfProgram"));
        test("empty non terminated (single quoted, end of file)", "'", List.of("InvalidToken(non-terminated string fragment: )", "EndOfProgram"));
        test("empty non terminated (single quored explicit end of file)", "'\0", List.of("InvalidToken(non-terminated string fragment: )", "EndOfProgram"));
        test("no need to escape double quote in single quoted string", "'a\"bc'", List.of("StringConstant(a\"bc)", "EndOfProgram"));
    }

    public void test(String reason, String input, List<String> expected) {

        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

        try (Scanner t = Scanner.create(bais)) {
            final List<String> actual = new ArrayList<>();
            while (true) {
                Token token = t.next();
                actual.add(token.toString());
                if (token instanceof EndOfProgram) break;
            }
            assertThat(reason, actual, is(expected));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}