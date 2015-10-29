package org.meridor.perspective.shell.wizard;

import java.util.Map;
import java.util.Optional;

public interface WizardScreen {
    
    Step getStep();
    
    Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers);
    
}
