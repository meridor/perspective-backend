package org.meridor.perspective.shell.interactive.wizard;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.*;

@Component
public abstract class MultipleChoicesStep extends BaseChoiceStep {
    
    @Override
    protected String getValueToSave(Map<Integer, String> choicesMap, String answer) {
        Set<Integer> range = parseRange(answer);
        return enumerateValues(
                range.stream()
                .map(i -> getAsExactMatch(choicesMap.get(i)))
                .collect(Collectors.toList())
        );
    }

    @Override
    protected String getIncorrectChoiceMessage(Map<Integer, String> choicesMap) {
        final Integer MAX_NUMBER = choicesMap.size();
        Assert.isTrue(MAX_NUMBER >= 2); // Single answer is automatically selected
        String example = (MAX_NUMBER >= 5) ?
                String.format("1,%d-%d", MAX_NUMBER - 2, MAX_NUMBER) :
                "1,2";
        return String.format("Answer should be a range of numbers from [%d..%d], e.g. \"%s\". Please try again or type q to quit:", 1, MAX_NUMBER, example);
    }

    @Override
    protected boolean validateAnswer(Map<Integer, String> choicesMap, String answer) {
        if (isExitKey(answer) || !isRange(answer)) {
            return false;
        }
        Set<Integer> range = parseRange(answer);
        for (Integer key : range) {
            if (!choicesMap.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected String getPrompt() {
        return answerRequired() ?
                "Type one or more numbers corresponding to your choice or q to exit:" :
                "Type one or more numbers corresponding to your choice, s to skip or q to exit:";
    }
}
