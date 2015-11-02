package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.shell.validator.NumberRelation;
import org.meridor.perspective.shell.validator.annotation.RelativeToNumericField;
import org.meridor.perspective.shell.wizard.FreeInputStep;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.meridor.perspective.shell.repository.impl.TextUtils.isPositiveInt;

@Component
public class EndNumberStep extends FreeInputStep {
    
    private Integer from = 0;
    
    @RelativeToNumericField(relation = NumberRelation.GREATER_THAN_EQUAL, field = "from")
    private Integer to = 0;

    @Override
    protected void saveAnswerToFields(String answer) {
        if (isPositiveInt(answer)) {
            to = Integer.valueOf(answer);
        }
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    @Override
    public Optional<String> getDefaultAnswer() {
        return Optional.of("1");
    }

    @Override
    public String getMessage() {
        return "Select instance end number ([$default_answer]):";
    }
}
