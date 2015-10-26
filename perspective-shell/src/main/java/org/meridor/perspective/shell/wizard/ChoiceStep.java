package org.meridor.perspective.shell.wizard;

import org.meridor.perspective.shell.misc.TableRenderer;
import org.meridor.perspective.shell.repository.impl.SettingsStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.misc.LoggingUtils.warn;
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
        printPossibleChoices(choicesMap);
        String answer = waitForAnswer();
        while (!validateAnswer(choicesMap, answer)) {
            warn(String.format("Answer should be one of [%d..%d]. Please try again or type q to quit:", 1, choicesMap.size()));
            answer = waitForAnswer();
        }
        if (isExitKey(answer)) {
            return false;
        }
        this.answer = answer;
        return true;
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
