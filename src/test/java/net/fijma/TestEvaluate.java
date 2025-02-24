package net.fijma;

import net.fijma.parsetree.Expression;
import net.fijma.parsetree.Unit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestEvaluate {

    @Test
    public void testEvaluate() {
        testUnit("trivial", "1+1", "2");
        testUnit("a little less", "(1+ 2) * ( 5 + 6)", "33");
    }

    public void testUnit(String reason, String input, String expected) {
        final ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        try (Scanner scanner = Scanner.create(bais)) {
            final Parser parser = new Parser(scanner);
            Unit unit = parser.parseUnit();
            System.out.println(unit);
            final var first = unit.value().get(0);
            Memory memory = new Memory();
            if (first instanceof Expression expression) {
                final var value = expression.eval(memory).toString();
                assertThat(value, is(expected));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
