package org.meridor.perspective.shell.interactive.wizard;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MockAbstractStep extends AbstractStep {

    private String message;

    private String defaultAnswer;

    @Override
    public boolean run() {
        return true;
    }

    @Override
    public String getAnswer() {
        return "anything";
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
        return true;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDefaultAnswer(String defaultAnswer) {
        this.defaultAnswer = defaultAnswer;
    }

}
