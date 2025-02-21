package net.fijma;

import net.fijma.parsetree.Unit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Disabled
public class TestParser {


    /*

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
                Parser parser = Parser.create(t);
                final Unit result = parser.parseProgram();
                assertThat(reason, result.isError(), is(expected == null));
                if (expected != null) {
                    if (result.value().isEmpty()) {
                        throw new AssertionError(reason);
                    }
                    System.out.println(result.value());
                    assertThat(reason, result.value().getLast().toString(), is(expected));
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
    */
    @Test
    public void testUnit() {
        // testUnit("trivial", "1\0", "1");
        testUnit("trivial", "1\n\0", "1");
        // testUnit("aa", "\n1111  ;  2222\n\n3333\n\n4444", "4444");
        //testUnit("aa", "\n1111;2222\n\naaaa\n\n4444", "4444");
        //testUnit("aaa", "a\n33 22;", null);
    }

    @Test
    public void testLet() {
        testUnit("let y = 1 + 1", "let y = 1 + 1", "Let(Identifier(y),Plus(1,1))");
        testUnit("let with invalid expression", "let y = 1 + ", null);
    }

    public void testUnit(String reason, String input, String expected) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        try (Scanner t = Scanner.create(bais)) {
            final Parser parser = Parser.create(t);
            while (true) {
                final Unit unit = parser.parseUnit();
                System.out.println(unit);
                if (unit.isLast()) {
                    assertThat(reason, unit.isError(), is(expected == null));
                    if (expected != null) {
                        assertThat("last", unit.value().getLast().toString(), is(expected));
                    }
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
