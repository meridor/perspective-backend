package org.meridor.perspective.shell.interactive.wizard;

import org.junit.Before;
import org.junit.Test;
import org.meridor.perspective.shell.common.repository.impl.ConsoleUtils;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.shell.common.repository.impl.TextUtils.enumerateValues;

public abstract class AbstractWizardTest<T extends Wizard> {

    private final List<String> answers;

    private final boolean result;

    private final String command;

    protected AbstractWizardTest(List<String> answers, boolean result, String command) {
        this.answers = new ArrayList<>(answers);
        this.result = result;
        this.command = command;
    }

    protected abstract T getWizard();

    @Before
    public void injectMockConsoleReader() {
        beforeInjectingAnswers();
        T wizard = getWizard();
        for (WizardScreen wizardScreen : wizard) {
            beforeInjectingAnswer(wizardScreen.getClass());
            try {
                String answer = answers.remove(0);
                Step step = wizardScreen.getStep(new AnswersStorage());
                if (step instanceof AbstractStep) {
                    ((AbstractStep) step).setConsoleReader(ConsoleUtils.mockConsoleReader(answer + "\n"));
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(
                        "Failed to mock answer for screen %s. This is a bug in test.",
                        wizardScreen.getClass().getSimpleName()
                ), e);
            }
        }
        if (!answers.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "%d answers were not used to test this wizard: %s. This is a bug in test.",
                    answers.size(),
                    enumerateValues(answers)
            ));
        }
    }

    protected void beforeInjectingAnswers() {
        // By default we do nothing...
    }
    
    protected void beforeInjectingAnswer(Class<? extends WizardScreen> cls) {
        // Do nothing...
    }
    
    @Test
    public void testWizard() {
        T wizard = getWizard();
        assertThat(wizard.runSteps(), is(result));
        assertThat(wizard.getCommand(), equalTo(command));
    }
}
