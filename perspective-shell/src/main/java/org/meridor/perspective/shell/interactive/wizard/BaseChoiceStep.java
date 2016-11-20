package org.meridor.perspective.shell.interactive.wizard;

import org.meridor.perspective.shell.common.misc.Logger;
import org.meridor.perspective.shell.common.misc.Pager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.isExitKey;
import static org.meridor.perspective.shell.common.repository.impl.TextUtils.isSkipKey;

@Component
public abstract class BaseChoiceStep<T> extends AbstractStep {

    @Autowired
    private Logger logger;
    
    @Autowired
    private Pager pager;

    @Autowired
    private AnswersStorage answersStorage;

    private String answer;

    @Override
    public boolean run() {
        answer = null;
        printMessageWithDefaultAnswer();
        ChoicesStorage<T> choicesStorage = new ChoicesStorage<>(getAnswerProvider(), getPossibleChoices(answersStorage));
        Map<Integer, String> answersMap = choicesStorage.getAnswersMap();
        Optional<Boolean> returnValue = processZeroOrOneAnswer(choicesStorage);
        if (returnValue.isPresent()) {
            return returnValue.get();
        }
        printPossibleChoices(answersMap);
        logger.ok(getPrompt());
        Optional<String> answerCandidate = processAnswer();
        if (!answerCandidate.isPresent()) {
            return false;
        }
        if (isSkipKey(answerCandidate.get())) {
            if (answerRequired()) {
                logger.warn("You can not skip this step.");
            } else {
                return true;
            }
        }
        while (!validateAnswer(answersMap, answerCandidate.get())) {
            logger.warn(getIncorrectChoiceMessage(answersMap));
            answerCandidate = processAnswer();
            if (!answerCandidate.isPresent()) {
                return false;
            }
        }
        String answer = answerCandidate.get();
        this.answer = getAnswerToSave(choicesStorage, answer);
        saveAdditionalData(answersStorage, choicesStorage, answer);
        return true;
    }

    private Optional<String> processAnswer() {
        String answer = waitForAnswer();
        if (isExitKey(answer)) {
            return Optional.empty();
        }
        return Optional.of(answer);
    }

    protected abstract List<T> getPossibleChoices(AnswersStorage previousAnswers);

    protected Function<T, String> getAnswerProvider() {
        return String::valueOf;
    }
    
    protected abstract String getPrompt();

    protected abstract String getAnswerToSave(ChoicesStorage<T> choicesStorage, String answer);

    // Used to save more data about previous steps e.g. save Project 
    // bean selected during previous steps 
    protected void saveAdditionalData(AnswersStorage answersStorage, ChoicesStorage<T> choicesStorage, String answer) {
        // Does nothing by default
    }
    
    protected abstract String getIncorrectChoiceMessage(Map<Integer, String> choicesMap);
    
    protected abstract boolean validateAnswer(Map<Integer, String> choicesMap, String answer);

    private void printPossibleChoices(Map<Integer, String> possibleChoices) {
        List<String[]> choicesRows = possibleChoices.keySet().stream()
                .map(k -> new String[]{k.toString(), possibleChoices.get(k)})
                .collect(Collectors.toList());
        pager.page(new String[]{"Number", "Name"}, choicesRows);
    }

    private Optional<Boolean> processZeroOrOneAnswer(ChoicesStorage<T> choicesStorage) {
        Map<Integer, String> answersMap = choicesStorage.getAnswersMap();
        if (answersMap.size() == 0) {
            if (answerRequired()) {
                logger.error("We're sorry but no possible answers exist. Exiting.");
                return Optional.of(false);
            } else {
                logger.warn("Skipping this step because no possible answers exist.");
                return Optional.of(true);
            }
        }
        if (answerRequired() && answersMap.size() == 1) {
            Integer singleKey = answersMap.keySet().toArray(new Integer[answersMap.keySet().size()])[0];
            String singleAnswer = answersMap.get(singleKey);
            logger.ok(String.format("Automatically selecting the only possible answer: %s", singleAnswer));
            saveAdditionalData(answersStorage, choicesStorage, String.valueOf(singleKey));
            this.answer = getAnswerToSave(choicesStorage, String.valueOf(singleKey));
            return Optional.of(true);
        }
        return Optional.empty();
    }

    @Override
    public String getAnswer() {
        return answer;
    }

}
