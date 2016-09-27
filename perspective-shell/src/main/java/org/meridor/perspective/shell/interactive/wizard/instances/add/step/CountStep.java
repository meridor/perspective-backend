package org.meridor.perspective.shell.interactive.wizard.instances.add.step;

import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.shell.common.validator.annotation.RelativeToNumber;
import org.meridor.perspective.shell.interactive.wizard.FreeInputStep;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.isPositiveInt;

@Component("addInstancesCountStep")
public class CountStep extends FreeInputStep {
    
    @RelativeToNumber(relation = BooleanRelation.GREATER_THAN, number = 0)
    private Integer count = 0;
    
    @Override
    protected void saveAnswerToFields(String answer) {
        if (isPositiveInt(answer)) {
            count = Integer.valueOf(answer);
        }
    }

    @Override
    public Optional<String> getDefaultAnswer() {
        return Optional.of("1");
    }

    @Override
    public String getMessage() {
        return "Select instances count ([$defaultAnswer]):";
    }
}
