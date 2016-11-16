package org.meridor.perspective.shell.interactive.wizard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public abstract class BaseWizard implements Wizard {

    @Autowired
    private AnswersStorage answers;

    private WizardScreen currentScreen;
    
    private boolean isFirstScreen = true;
    
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
                getAnswers().putAnswer(stepClass, answer.get());
            }
        }
        return true;
    }

    protected abstract WizardScreen getFirstScreen();

    private AnswersStorage getAnswers() {
        return answers;
    }
    
    protected Optional<String> getAnswer(Class<? extends Step> cls) {
        return Optional.ofNullable(getAnswers().getAnswer(cls));
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
