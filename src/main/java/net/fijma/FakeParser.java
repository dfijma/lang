package net.fijma;

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

    public static class Step {

        private final IOException e;
        private final boolean cont;
        private final String error;
        private final String result;
        private final boolean last;

        private Step(String s, boolean cont, String error, boolean last, IOException e) {
            this.result = s;
            this.cont = cont;
            this.error = error;
            this.last = last;
            this.e = e;
        }

        static Step fromException(IOException e) {
            return new Step(null, false, null, false, e);
        }

        static Step withResult(String result, boolean last) {
            return new Step(result, false, null, last,null);
        }

        static Step fromError(String error, boolean last) {
            return new Step(null, false, error, last,null);
        }

        static Step cont() {
            return new Step(null, true, null, false, null);
        }

        public boolean isException() {
            return e != null;
        }

        public IOException exception() {
            return e;
        }

        public boolean isError() {
            return error != null;
        }

        public boolean isCont() {
            return cont;
        }

        public String result() {
            return result;
        }

        public String error() {
            return error;
        }

        public boolean isLast() {
            return last;
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

                    if (endOfUnit || endOfProgram || error) {
                        if (error) {
                            step = Step.fromError("oeps!", endOfProgram);
                        } else {
                            step = Step.withResult(result.toString(), endOfProgram);
                        }
                    } else {
                        step = Step.cont();
                    }

                } catch (IOException e) {
                    step = Step.fromException(e);
                }

                try {
                    responses.put(step);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                if (!step.isCont()) break;

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
