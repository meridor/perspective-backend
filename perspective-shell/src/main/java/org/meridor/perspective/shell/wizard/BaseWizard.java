package org.meridor.perspective.shell.wizard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.JLineShellComponent;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public abstract class BaseWizard implements Wizard {
    
    private final Map<Class<? extends Step>, String> answers = new HashMap<>(); 
    
    private WizardScreen currentScreen;

    @Autowired
    private JLineShellComponent jLineShellComponent;

    @PostConstruct
    public void init() {
        this.currentScreen = getFirstScreen();
    }

    @Override
    public boolean runSteps() {
        for (WizardScreen wizardScreen : this) {
            Step currentStep = wizardScreen.getStep();
            if (!currentStep.run()) {
                return false;
            }
            String answer = currentStep.getAnswer();
            Class<? extends Step> stepClass = currentStep.getClass();
            answers.put(stepClass, answer);
        }
        return true;
    }

    protected abstract WizardScreen getFirstScreen();
    
    protected Map<Class<? extends Step>, String> getAnswers() {
        return answers;
    }

    @Override
    public boolean hasNext() {
        return (currentScreen != null) && currentScreen.getNextScreen(getAnswers()).isPresent();
    }

    @Override
    public WizardScreen next() {
        if (currentScreen == null) {
            throw new IllegalArgumentException("Current screen can't be null");
        }
        Optional<WizardScreen> nextScreen = currentScreen.getNextScreen(getAnswers());
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
    
    protected abstract String getCommand();
    
    @Override
    public void runCommand() {
        String command = getCommand();
        jLineShellComponent.executeCommand(command);
    }

}
