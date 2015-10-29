package org.meridor.perspective.shell.wizard.instances.step;

import org.meridor.perspective.shell.repository.impl.Placeholder;
import org.meridor.perspective.shell.validator.annotation.SupportedName;
import org.meridor.perspective.shell.wizard.FreeInputStep;
import org.springframework.stereotype.Component;

import static org.meridor.perspective.shell.repository.impl.TextUtils.getPlaceholder;

@Component
public class NameStep extends FreeInputStep {
    
    @SupportedName
    private String name;
    
    @Override
    protected void saveAnswerToFields(String answer) {
        this.name = answer;
    }

    @Override
    public String getMessage() {
        return String.format("Specify instance name. Use %s marker to inject instance number to name.", getPlaceholder(Placeholder.NUMBER));
    }
}
