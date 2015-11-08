package org.meridor.perspective.shell.wizard;

import org.springframework.stereotype.Component;

import java.util.Map;

import static org.meridor.perspective.shell.repository.impl.TextUtils.isExitKey;
import static org.meridor.perspective.shell.repository.impl.TextUtils.isPositiveInt;

@Component
public abstract class SingleChoiceStep extends BaseChoiceStep {
    
    @Override
    protected String getValueToSave(Map<Integer, String> choicesMap, String answer) {
        return choicesMap.get(Integer.parseUnsignedInt(answer));
    }

    @Override
    protected String getIncorrectChoiceMessage(Map<Integer, String> choicesMap) {
        return String.format("Answer should be one of [%d..%d]. Please try again or type q to quit:", 1, choicesMap.size());
    }

    @Override
    protected boolean validateAnswer(Map<Integer, String> choicesMap, String answer) {
        return isExitKey(answer) || isPositiveInt(answer) && choicesMap.containsKey(Integer.parseUnsignedInt(answer));
    }
    
}
