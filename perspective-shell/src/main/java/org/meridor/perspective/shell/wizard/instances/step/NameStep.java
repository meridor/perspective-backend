package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.shell.repository.impl.Placeholder;
import org.meridor.perspective.shell.validator.annotation.Required;
import org.meridor.perspective.shell.validator.annotation.SupportedSymbols;
import org.meridor.perspective.shell.wizard.FreeInputStep;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.shell.repository.impl.TextUtils.getPlaceholder;

@Component("instanceNameStep")
public class NameStep extends FreeInputStep {
    
    @Required
    @SupportedSymbols("a-zA-Z0-9-_$")
    private String name;
    
    @Override
    protected void saveAnswerToFields(String answer) {
        this.name = answer;
    }

    @Override
    public String getMessage() {
        return String.format("Specify instance name. Any %s occurence will be replaced by instance number:", getPlaceholder(Placeholder.NUMBER));
    }
}
