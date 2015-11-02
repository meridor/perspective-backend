package org.meridor.perspective.shell.wizard;

import java.util.Map;
import java.util.Optional;

public interface WizardScreen {
    
    Step getStep(Map<Class<? extends Step>, String> previousAnswers);
    
    Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers);
    
}
