package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.shell.validator.NumberRelation;
import org.meridor.perspective.shell.validator.annotation.RelativeToNumber;
import org.meridor.perspective.shell.wizard.FreeInputStep;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.meridor.perspective.shell.repository.impl.TextUtils.isPositiveInt;

@Component
public class StartNumberStep extends FreeInputStep {
    
    @RelativeToNumber(relation = NumberRelation.GREATER_THAN, number = 0)
    private Integer from = 0;
    
    @Override
    protected void saveAnswerToFields(String answer) {
        if (isPositiveInt(answer)) {
            from = Integer.valueOf(answer);
        }
    }

    @Override
    public Optional<String> getDefaultAnswer() {
        return Optional.of("1");
    }

    @Override
    public String getMessage() {
        return "Select instance start number ([$default_answer]):";
    }
}
