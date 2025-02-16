package net.fijma;

import net.fijma.token.EndOfProgram;
import net.fijma.token.NewLine;

import java.util.List;
import java.util.Optional;

public class ParserMain {

    static boolean isTTY = System.console() != null;

    private static void prompt() {
        System.out.print("> ");
        System.out.flush();
        //first = true;
        //any = false;
    }

    public static void main(String[] args) {
        if (isTTY) { interactive(args); } else { batch(args); }
    }

    public static void interactive(String[] args) {
        System.out.println("interactive");
        prompt();
        try (Scanner t = Scanner.create(System.in)) {
            Parser parse = Parser.create(t);
            while (true) {
                final Unit unit = parse.parseUnit();
                System.out.println("unit:" + unit);
                if (unit.isLast()) break;
                prompt();
                t.skip();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void batch(String[] args) {

        try (Scanner t = Scanner.create(System.in)) {
            final Optional<List<Expression>> result = Parser.parse(t);
            if (result.isPresent()) {
                for (Expression expr : result.get()) {
                    System.out.println(expr);
                }
            } else {
                System.out.println("syntax error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
