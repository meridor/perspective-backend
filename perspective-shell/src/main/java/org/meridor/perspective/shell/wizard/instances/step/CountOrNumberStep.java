package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.shell.repository.impl.TextUtils;
import org.meridor.perspective.shell.wizard.YesNoStep;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CountOrNumberStep extends YesNoStep {

    public CountOrNumberStep() {
        super(true);
    }

    @Override
    public String getMessage() {
        return "Do you want to specify exact start and end instances number?";
    }

    @Override
    public Optional<String> getDefaultAnswer() {
        return Optional.of(TextUtils.NO);
    }
}
