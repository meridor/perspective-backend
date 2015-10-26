package org.meridor.perspective.shell.wizard.instances;

import org.meridor.perspective.shell.validator.NumberRelation;
import org.meridor.perspective.shell.validator.annotation.RelativeToNumericField;
import org.meridor.perspective.shell.wizard.FreeInputStep;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.meridor.perspective.shell.repository.impl.TextUtils.isPositiveInt;

@Component
public class EndNumberStep extends FreeInputStep {
    
    private final Integer from;
    
    @RelativeToNumericField(relation = NumberRelation.GREATER_THAN, field = "from")
    private Integer to = 0;

    public EndNumberStep(Integer from) {
        this.from = from;
    }

    @Override
    protected void saveAnswerToFields(String answer) {
        if (isPositiveInt(answer)) {
            to = Integer.valueOf(answer);
        }
    }

    @Override
    public Optional<String> getDefaultAnswer() {
        return Optional.of("1");
    }

    @Override
    public String getMessage() {
        return "Select instance end number.";
    }
}
