package net.fijma;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestParser {

    @Test
    public void testFactor() {
        test("just a try", "11", "11");
        test("factor <- (expression)", "(12)", "12");
    }

    @Test
    public void testTerm() {
        test("term <- factor", "13", "13");
        test("term <- factor+term", "14+15", "Plus(14,15)");
    }

    @Test
    public void testExpression() {
        test("expression <- term", "10", "10");
        test("expression <- term*expression", "10*20", "Asterisk(10,20)");
    }

    @Test
    public void testNested() {
        test("some nested stuff", "10+(11*2)*(21+(22*234))", "Plus(10,Asterisk(Asterisk(11,2),Plus(21,Asterisk(22,234))))");
    }

    @Test
    public void testNegative() {
        test("unbalanced", "(10 theRest()", null);
        test("not even looks like it", "class blah;", null);
    }

    public void test(String reason, String input, String expected) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        try (Scanner t = Scanner.create(bais)) {
            final Optional<Expression> expression = Parser.parse(t);
            assertThat(reason, expression.isPresent(), is(expected != null));
            if (expected != null) {
                if (expression.isEmpty()) {
                    throw new AssertionError(reason);
                }
                System.out.println(expression.get());
                assertThat(reason, expression.get().toString(), is(expected));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
