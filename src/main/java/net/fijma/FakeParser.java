package net.fijma;

import net.fijma.parsetree.Identifier;
import net.fijma.parsetree.Unit;
import net.fijma.token.EndOfProgram;
import net.fijma.token.Token;
import net.fijma.token.Word;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FakeParser {

    Thread thread;
    private final Scanner scanner;

    public FakeParser(Scanner scanner) {
        this.scanner = scanner;
    }

    public static class Kick {
        public static Kick INSTANCE = new Kick();
    }

    BlockingQueue<Kick> requests = new ArrayBlockingQueue<>(1);
    BlockingQueue<Step> responses = new ArrayBlockingQueue<>(1);

    public static abstract class Step {}

    public static class ResultStep extends Step {
        private final Unit unit;
        public ResultStep(Unit unit) {
            this.unit = unit;
        }
        public Unit getUnit() { return unit; }
    }

    public static class ExceptionStep extends Step {
        private final IOException exception;
        public ExceptionStep(IOException exception) {
            this.exception = exception;
        }
        IOException get() { return exception; }
    }

    public static class ContinueStep extends Step {}

    Step run() {

        List<List<Token>> result = new ArrayList<>();

        while (true) {

            try {
                final var line = scanner.next();

                boolean endOfProgram = line.stream().anyMatch(token -> token instanceof EndOfProgram);
                boolean error = line.stream().anyMatch(token -> token instanceof Word word && word.value().equals("error"));
                result.add(line);

                // FIXME: 'Step' and 'ParseResult' can probably by joined into a single class, also prevent double boxing like below
                if (endOfProgram || error) {
                    if (error) {
                        return new ResultStep(new Unit(endOfProgram, new Word(0, 0, "test"), "some specific error"));
                    } else {
                        return new ResultStep(new Unit(endOfProgram, List.of(new Identifier("aa"))));
                    }
                }

            } catch (IOException e) {
                return new ExceptionStep(e);
            }

        }
    }

    void start() {
        thread = new Thread(() -> {

            List<List<Token>> result = new ArrayList<>();

            while (true) {
                try {
                    Kick ignored  = requests.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                Step step;
                try {
                    final var line = scanner.next();

                    boolean endOfUnit = line.stream().anyMatch(token -> token instanceof Word word && word.value().equals("quit"));
                    boolean endOfProgram = line.stream().anyMatch(token -> token instanceof EndOfProgram);
                    boolean error = line.stream().anyMatch(token -> token instanceof Word word && word.value().equals("error"));
                    result.add(line);

                    // FIXME: 'Step' and 'ParseResult' can probably by joined into a single class, also prevent double boxing like below
                    if (endOfUnit || endOfProgram || error) {
                        if (error) {
                            step = new ResultStep(new Unit(endOfProgram, new Word(0, 0, "test"), "some specific error"));
                        } else {
                            step = new ResultStep(new Unit(endOfProgram, List.of(new Identifier("aa"))));
                        }
                    } else {
                        step = new ContinueStep();
                    }

                } catch (IOException e) {
                    step = new ExceptionStep(e);
                }

                try {
                    responses.put(step);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                if (! (step instanceof ContinueStep)) break;

            }
        });

        thread.start();
    }

    public Step step() throws InterruptedException {
        requests.put(Kick.INSTANCE);
        return responses.take();
    }

    void join() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
