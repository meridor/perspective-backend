package org.meridor.perspective.shell.interactive.wizard;

import org.meridor.perspective.shell.common.misc.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public abstract class BaseWizard implements Wizard {
    
    private final Map<Class<? extends Step>, String> answers = new HashMap<>(); 
    
    private WizardScreen currentScreen;
    
    private boolean isFirstScreen = true;
    
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

    private Map<Class<? extends Step>, String> getAnswers() {
        return answers;
    }
    
    protected Optional<String> getAnswer(Class<? extends Step> cls) {
        return Optional.ofNullable(getAnswers().get(cls));
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

    public abstract String getCommand();

}
