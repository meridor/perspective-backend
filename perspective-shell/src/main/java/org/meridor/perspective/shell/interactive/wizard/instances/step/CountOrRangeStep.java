package org.meridor.perspective.shell.interactive.wizard.instances.step;

import org.meridor.perspective.shell.common.repository.impl.TextUtils;
import org.meridor.perspective.shell.interactive.wizard.YesNoStep;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CountOrRangeStep extends YesNoStep {

    @Override
    public String getMessage() {
        return "Do you want to specify a range of instance numbers?";
    }

    @Override
    public Optional<String> getDefaultAnswer() {
        return Optional.of(TextUtils.NO);
    }

    @Override
    protected boolean anyAnswerIsCorrect() {
        return true;
    }
}
