package org.meridor.perspective.shell.wizard.impl;

import org.meridor.perspective.shell.wizard.Step;
import org.meridor.perspective.shell.wizard.Wizard;
import org.meridor.perspective.shell.wizard.WizardScreen;

import java.util.*;

public abstract class BaseWizard implements Wizard {
    
    private final Set<String> answers = new HashSet<>(); 
    
    private WizardScreen currentScreen;

    public BaseWizard(WizardScreen firstScreen) {
        this.currentScreen = firstScreen;
    }

    @Override
    public boolean run() {
        for (WizardScreen wizardScreen : this) {
            Step currentStep = wizardScreen.getStep();
            if (!currentStep.run()) {
                return false;
            }
            String answer = currentStep.getAnswer();
            answers.add(answer);
        }
        return true;
    }

    protected Set<String> getAnswers() {
        return answers;
    }

    @Override
    public boolean hasNext() {
        return (currentScreen != null) && currentScreen.getNextScreen().isPresent();
    }

    @Override
    public WizardScreen next() {
        if (currentScreen == null) {
            throw new IllegalArgumentException("Current screen can't be null");
        }
        Optional<WizardScreen> nextScreen = currentScreen.getNextScreen();
        if (!nextScreen.isPresent()) {
            throw new NoSuchElementException("Last wizard screen reached");
        }
        currentScreen = nextScreen.get();
        return currentScreen;
    }

    @Override
    public Iterator<WizardScreen> iterator() {
        return this;
    }
}
