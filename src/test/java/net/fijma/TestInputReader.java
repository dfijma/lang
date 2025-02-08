package net.fijma;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestInputReader {

    @Test
    public void testNormal() {
        test("test💩poop", "test💩poop");
    }

    @Test
    public void testZeroByte() {
        test("test💩poop\0should not appear", "test💩poop");
    }

    @Test
    public void testEmpty() {
        test("", "");
    }

    public void test(String input, String expected) {

        String inputWithZeroByte = input + "\0should_not_appear";
        ByteArrayInputStream bais = new ByteArrayInputStream(inputWithZeroByte.getBytes(StandardCharsets.UTF_8));

        try (InputReader s = InputReader.create(bais)) {
            StringBuilder sb = new StringBuilder();

            char c;
            while ( (c = s.current()) != 0) {
                sb.append(c);
                s.skip();
            }
            assertThat(sb.toString(), is(expected));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
