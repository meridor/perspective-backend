package org.meridor.perspective.shell.interactive.wizard;

import org.meridor.perspective.shell.common.misc.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.AbstractShell;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public abstract class BaseWizard implements Wizard {
    
    private final Map<Class<? extends Step>, String> answers = new HashMap<>(); 
    
    private WizardScreen currentScreen;
    
    private boolean isFirstScreen = true;
    
    @Autowired
    private AbstractShell shell;

    @Autowired
    private Logger logger;
    
    @Override
    public boolean runSteps() {
        for (WizardScreen wizardScreen : this) {
            Step currentStep = wizardScreen.getStep(getAnswers());
            if (!currentStep.run()) {
                return false;
            }
            Optional<String> answer = Optional.ofNullable(currentStep.getAnswer());
            if (answer.isPresent()) {
                Class<? extends Step> stepClass = currentStep.getClass();
                getAnswers().put(stepClass, answer.get());
            }
        }
        return true;
    }

    protected abstract WizardScreen getFirstScreen();

    protected Map<Class<? extends Step>, String> getAnswers() {
        return answers;
    }

    @Override
    public boolean hasNext() {
        return
                ( (currentScreen != null) && currentScreen.getNextScreen(getAnswers()).isPresent() ) ||
                (isFirstScreen && getFirstScreen() != null);
    }

    @Override
    public WizardScreen next() {
        if (currentScreen == null) {
            currentScreen = getFirstScreen();
            isFirstScreen = false;
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
        reset();
        return this;
    }

    private void reset() {
        this.currentScreen = null;
        isFirstScreen = true;
        getAnswers().clear();
    }

    protected abstract String getCommand();

    @Override
    public void runCommand() {
        String command = getCommand();
        logger.ok(String.format("Executing command: %s", command));
        shell.executeCommand(command);
    }

}
