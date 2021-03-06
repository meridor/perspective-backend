package org.meridor.perspective.shell.interactive.wizard;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MockBaseChoiceStep extends BaseChoiceStep<String> {

    public static final String TEST_MESSAGE = "test-message";
    public static final String TEST_PROMPT = "test-prompt";
    public static final String TEST_INCORRECT_CHOICE_MESSAGE = "incorrect-choice";

    private boolean answerRequired;

    private List<String> validAnswers;

    private String valueToSave;

    private List<String> possibleChoices;

    private String additionalData;

    @Override
    public String getMessage() {
        return TEST_MESSAGE;
    }

    @Override
    protected List<String> getPossibleChoices(AnswersStorage previousAnswers) {
        return possibleChoices;
    }

    public void setPossibleChoices(List<String> possibleChoices) {
        this.possibleChoices = possibleChoices;
    }

    @Override
    protected String getPrompt() {
        return TEST_PROMPT;
    }

    @Override
    protected String getAnswerToSave(ChoicesStorage<String> choicesStorage, String answer) {
        return valueToSave;
    }

    public void setValueToSave(String valueToSave) {
        this.valueToSave = valueToSave;
    }

    @Override
    protected String getIncorrectChoiceMessage(Map<Integer, String> choicesMap) {
        return TEST_INCORRECT_CHOICE_MESSAGE;
    }

    @Override
    public boolean answerRequired() {
        return answerRequired;
    }

    public void setAnswerRequired(boolean answerRequired) {
        this.answerRequired = answerRequired;
    }

    @Override
    protected boolean validateAnswer(Map<Integer, String> choicesMap, String answer) {
        return validAnswers.contains(answer);
    }

    public void setValidAnswers(List<String> validAnswers) {
        this.validAnswers = validAnswers;
    }

    @Override
    protected void saveAdditionalData(AnswersStorage answersStorage, ChoicesStorage<String> choicesStorage, String answer) {
        additionalData = "additional_data";
    }

    public String getAdditionalData() {
        return additionalData;
    }
}
