package net.fijma;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
            final Optional<List<Expression>> expressionList = Parser.parse(t);
            assertThat(reason, expressionList.isPresent(), is(expected != null));
            if (expected != null) {
                if (expressionList.isEmpty()) {
                    throw new AssertionError(reason);
                }
                System.out.println(expressionList.get());
                assertThat(reason, expressionList.get().getLast().toString(), is(expected));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testList() {
        test("additional newlines ignored", "\n\n10;\n\n\n14\n\n\n", "14");
        test("full ; \n", "10;\n11;\n", "11");
        test("terminated full ; \n", "15;\n", "15");
        test("first expression not terminated", "10 12;\n", null);
        test("optional \n", "10;12", "12");
        test("optional '", "10\n13", "13");
        test("end of program terminates", "16\013", "16");
        test("terminated by just end of program", "10;13\0", "13");
    }
}
