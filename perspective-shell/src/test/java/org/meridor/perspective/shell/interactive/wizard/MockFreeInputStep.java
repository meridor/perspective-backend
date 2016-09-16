package org.meridor.perspective.shell.interactive.wizard;

import org.meridor.perspective.beans.BooleanRelation;
import org.meridor.perspective.shell.common.validator.annotation.RelativeToNumber;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.isPositiveInt;

@Component
public class MockFreeInputStep extends FreeInputStep {

    public static final String TEST_MESSAGE = "test-message";
    
    @RelativeToNumber(relation = BooleanRelation.GREATER_THAN, number = 0)
    private Integer answerField = 0;

    @Override
    public String getMessage() {
        return TEST_MESSAGE;
    }

    @Override
    protected void saveAnswerToFields(String answer) {
        if (isPositiveInt(answer)) {
            answerField = Integer.valueOf(answer);
        }
    }

    public Integer getAnswerField() {
        return answerField;
    }
}
