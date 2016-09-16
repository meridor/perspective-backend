package org.meridor.perspective.shell.interactive.wizard;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MockMultipleChoicesStep extends MultipleChoicesStep {

    private List<String> possibleChoices;

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

}
