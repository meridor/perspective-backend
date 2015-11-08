package org.meridor.perspective.shell.wizard;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Set;

import static org.meridor.perspective.shell.repository.impl.TextUtils.*;

@Component
public abstract class MultipleChoicesStep extends BaseChoiceStep {
    
    @Override
    protected String getValueToSave(Map<Integer, String> choicesMap, String answer) {
        return null;
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
            if (choicesMap.containsKey(key)) {
                return false;
            }
        }
        return true;
    }
    
}
