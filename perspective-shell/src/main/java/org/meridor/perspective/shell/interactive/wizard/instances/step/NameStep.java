package org.meridor.perspective.shell.interactive.wizard.instances.step;

import org.meridor.perspective.shell.common.repository.impl.Placeholder;
import org.meridor.perspective.shell.common.validator.annotation.Required;
import org.meridor.perspective.shell.common.validator.annotation.SupportedSymbols;
import org.meridor.perspective.shell.interactive.wizard.FreeInputStep;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.getPlaceholder;

@Component("instanceNameStep")
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
        return String.format("Specify instance name. Any %s occurrence will be replaced by instance number:", getPlaceholder(Placeholder.NUMBER));
    }
}
