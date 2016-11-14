package org.meridor.perspective.shell.interactive.wizard;

import java.util.Optional;

public interface WizardScreen {

    Step getStep(AnswersStorage previousAnswers);

    Optional<WizardScreen> getNextScreen(AnswersStorage previousAnswers);
    
}
