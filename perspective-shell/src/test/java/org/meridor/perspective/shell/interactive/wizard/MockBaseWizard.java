package org.meridor.perspective.shell.interactive.wizard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.meridor.perspective.shell.common.repository.impl.ConsoleUtils.mockConsoleReader;

@Component
public class MockBaseWizard extends BaseWizard {
    
    @Autowired
    private MockSingleChoiceStep mockSingleChoiceStep;
    
    @Autowired
    private MockYesNoStep mockYesNoStep;
    
    private boolean allStepsPass = true;
    
    @Override
    protected WizardScreen getFirstScreen() {
        return new FirstScreen();
    }

    @Override
    @SuppressWarnings("all")
    protected String getCommand() {
        return String.format(
                "%s %s",
                getAnswer(MockSingleChoiceStep.class).get(),
                getAnswer(MockYesNoStep.class).get()
        );
    }

    public void setAllStepsPass(boolean allStepsPass) {
        this.allStepsPass = allStepsPass;
    }

    private class FirstScreen implements WizardScreen {

        FirstScreen() {
            try {
                mockSingleChoiceStep.setPossibleChoices(Arrays.asList("one", "two"));
                mockSingleChoiceStep.setConsoleReader(mockConsoleReader("2\n"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
            return mockSingleChoiceStep;
        }

        @Override
        public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
            return Optional.of(new SecondScreen());
        }
    }
    
    private class SecondScreen implements WizardScreen {

        SecondScreen() {
            try {
                mockYesNoStep.setConsoleReader(mockConsoleReader("n\n"));
                mockYesNoStep.setAnyAnswerIsCorrect(allStepsPass);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        public Step getStep(Map<Class<? extends Step>, String> previousAnswers) {
            return mockYesNoStep;
        }

        @Override
        public Optional<WizardScreen> getNextScreen(Map<Class<? extends Step>, String> previousAnswers) {
            return Optional.empty();
        }
    }
}
