package org.meridor.perspective.shell.interactive.wizard;

import jline.console.ConsoleReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class MockStep extends AbstractStep {
    
    private String message;
    
    private String answer;
    
    private String defaultAnswer;
    
    private boolean answerRequired;
    
    private ConsoleReader consoleReader;

    @Override
    public boolean run() {
        return true;
    }

    @Override
    public String getAnswer() {
        return answer;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Optional<String> getDefaultAnswer() {
        return Optional.ofNullable(defaultAnswer);
    }

    @Override
    public boolean answerRequired() {
        return answerRequired;
    }

    @Override
    protected ConsoleReader getConsoleReader() throws IOException {
        return consoleReader;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setDefaultAnswer(String defaultAnswer) {
        this.defaultAnswer = defaultAnswer;
    }

    public void setAnswerRequired(boolean answerRequired) {
        this.answerRequired = answerRequired;
    }

    public void setConsoleReader(ConsoleReader consoleReader) {
        this.consoleReader = consoleReader;
    }
}
