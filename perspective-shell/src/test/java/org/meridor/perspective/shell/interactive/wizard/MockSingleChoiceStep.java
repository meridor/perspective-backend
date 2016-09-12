package org.meridor.perspective.shell.interactive.wizard;

import jline.console.ConsoleReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class MockSingleChoiceStep extends SingleChoiceStep {

    private List<String> possibleChoices;

    private ConsoleReader consoleReader;

    @Override
    public String getMessage() {
        return "test-message";
    }

    public void setPossibleChoices(List<String> possibleChoices) {
        this.possibleChoices = possibleChoices;
    }

    @Override
    protected List<String> getPossibleChoices() {
        return possibleChoices;
    }

    @Override
    protected ConsoleReader getConsoleReader() throws IOException {
        return consoleReader;
    }

    public void setConsoleReader(ConsoleReader consoleReader) {
        this.consoleReader = consoleReader;
    }
}
