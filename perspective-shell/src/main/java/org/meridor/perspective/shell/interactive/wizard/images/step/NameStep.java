package org.meridor.perspective.shell.interactive.wizard.images.step;

import org.meridor.perspective.shell.common.repository.impl.Placeholder;
import org.meridor.perspective.shell.common.validator.annotation.Required;
import org.meridor.perspective.shell.common.validator.annotation.SupportedSymbols;
import org.meridor.perspective.shell.interactive.wizard.FreeInputStep;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.getPlaceholder;

@Component("imageNameStep")
public class NameStep extends FreeInputStep {
    
    @Required
    @SupportedSymbols
    private String name;
    
    @Override
    protected void saveAnswerToFields(String answer) {
        this.name = answer;
    }

    @Override
    public String getMessage() {
        return String.format("Specify image name. Any %s occurrence will be replaced by instance name, any %s occurrence will be replaced by current date:", getPlaceholder(Placeholder.NAME), getPlaceholder(Placeholder.DATE));
    }

    @Override
    public Optional<String> getDefaultAnswer() {
        return Optional.of(String.format("%s-%s", getPlaceholder(Placeholder.NAME), getPlaceholder(Placeholder.DATE)));
    }
}
