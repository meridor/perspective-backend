package org.meridor.perspective.shell.wizard;

import org.meridor.perspective.shell.misc.TableRenderer;
import org.meridor.perspective.shell.repository.impl.SettingsStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.misc.LoggingUtils.*;
import static org.meridor.perspective.shell.repository.impl.TextUtils.*;

@Component
public abstract class ChoiceStep implements Step {

    private String answer;

    @Autowired
    private SettingsStorage settingsStorage;
    
    @Autowired
    private TableRenderer tableRenderer;

    @Override
    public boolean run() {
        printMessageWithDefaultAnswer();
        Map<Integer, String> choicesMap = getPossibleChoicesMap();
        if (choicesMap.size() == 0) {
            error("We're sorry but no possible answers exist. Exiting.");
            return false;
        }
        if (choicesMap.size() == 1) {
            Integer singleKey = choicesMap.keySet().toArray(new Integer[choicesMap.keySet().size()])[0];
            String singleAnswer = choicesMap.get(singleKey);
            ok(String.format("Automatically selecting the only possible answer: %s", singleAnswer));
            this.answer = singleAnswer;
            return true;
        }
        printPossibleChoices(choicesMap);
        Optional<String> answer = processAnswer();
        if (!answer.isPresent()) {
            return false;
        }
        while (!validateAnswer(choicesMap, answer.get())) {
            warn(String.format("Answer should be one of [%d..%d]. Please try again or type q to quit:", 1, choicesMap.size()));
            answer = processAnswer();
            if (!answer.isPresent()) {
                return false;
            }
        }
        this.answer = choicesMap.get(Integer.parseUnsignedInt(answer.get()));
        return true;
    }

    private Optional<String> processAnswer() {
        String answer = waitForAnswer();
        if (isExitKey(answer)) {
            return Optional.empty();
        }
        return Optional.of(answer);
    }


    private Map<Integer, String> getPossibleChoicesMap() {
        Map<Integer, String> choicesMap = new HashMap<>();
        List<String> possibleChoices = getPossibleChoices();
        for (int i = 1; i <= possibleChoices.size(); i++) {
            choicesMap.put(i, possibleChoices.get(i - 1));
        }
        return choicesMap;
    }
    
    private void printPossibleChoices(Map<Integer, String> possibleChoices) {
        List<String[]> choicesRows = possibleChoices.keySet().stream()
                .map(k -> new String[]{k.toString(), possibleChoices.get(k)})
                .collect(Collectors.toList());
        final Integer PAGE_SIZE = getPageSize(settingsStorage);
        page(preparePages(tableRenderer, PAGE_SIZE, new String[]{"Number", "Name"}, choicesRows));
    }
    
    @Override
    public String getAnswer() {
        return answer;
    }

    private boolean validateAnswer(Map<Integer, String> choicesMap, String answer) {
        return isExitKey(answer) || isPositiveInt(answer) && choicesMap.containsKey(Integer.parseUnsignedInt(answer));
    }
    
    protected abstract List<String> getPossibleChoices();
}
