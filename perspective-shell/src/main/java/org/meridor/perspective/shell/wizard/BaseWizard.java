package org.meridor.perspective.shell.wizard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.AbstractShell;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.meridor.perspective.shell.misc.LoggingUtils.ok;

@Component
public abstract class BaseWizard implements Wizard {
    
    private final Map<Class<? extends Step>, String> answers = new HashMap<>(); 
    
    private WizardScreen currentScreen;

    @Autowired
    private AbstractShell shell;

    @Override
    public boolean runSteps() {
        reset();
        for (WizardScreen wizardScreen : this) {
            Step currentStep = wizardScreen.getStep(getAnswers());
            if (!currentStep.run()) {
                return false;
            }
            String answer = currentStep.getAnswer();
            Class<? extends Step> stepClass = currentStep.getClass();
            answers.put(stepClass, answer);
        }
        return true;
    }

    private void reset() {
        this.currentScreen = null;
    }

    protected abstract WizardScreen getFirstScreen();
    
    protected Map<Class<? extends Step>, String> getAnswers() {
        return answers;
    }

    @Override
    public boolean hasNext() {
        return
                ( (currentScreen != null) && currentScreen.getNextScreen(getAnswers()).isPresent() ) ||
                (getFirstScreen() != null);
    }

    @Override
    public WizardScreen next() {
        if (currentScreen == null) {
            currentScreen = getFirstScreen();
        } else {
            Optional<WizardScreen> nextScreen = currentScreen.getNextScreen(getAnswers());
            if (!nextScreen.isPresent()) {
                throw new NoSuchElementException("Last wizard screen reached");
            }
            currentScreen = nextScreen.get();
        }
        return currentScreen;
    }

    @Override
    public Iterator<WizardScreen> iterator() {
        return this;
    }
    
    protected abstract String getCommand();
    
    @Override
    public void runCommand() {
        String command = getCommand();
        ok(String.format("Executing command: %s", command));
        shell.executeCommand(command);
    }

}
