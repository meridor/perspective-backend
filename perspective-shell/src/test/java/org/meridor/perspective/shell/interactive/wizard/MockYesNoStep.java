package org.meridor.perspective.shell.interactive.wizard;

import jline.console.ConsoleReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class MockYesNoStep extends YesNoStep {

    private String message;

    private String defaultAnswer;

    private boolean anyAnswerIsCorrect;

    private ConsoleReader consoleReader;

    @Override
    protected boolean anyAnswerIsCorrect() {
        return anyAnswerIsCorrect;
    }

    public void setAnyAnswerIsCorrect(boolean anyAnswerIsCorrect) {
        this.anyAnswerIsCorrect = anyAnswerIsCorrect;
    }

    @Override
    protected ConsoleReader getConsoleReader() throws IOException {
        return consoleReader;
    }

    public void setConsoleReader(ConsoleReader consoleReader) {
        this.consoleReader = consoleReader;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public Optional<String> getDefaultAnswer() {
        return Optional.ofNullable(defaultAnswer);
    }

    public void setDefaultAnswer(String defaultAnswer) {
        this.defaultAnswer = defaultAnswer;
    }
}
