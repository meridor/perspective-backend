package org.meridor.perspective.shell.interactive.wizard;

import org.junit.Before;
import org.junit.Test;
import org.meridor.perspective.shell.common.repository.impl.ConsoleUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
        T wizard = getWizard();
        for (WizardScreen wizardScreen : wizard) {
            try {
                String answer = answers.remove(0);
                Step step = wizardScreen.getStep(Collections.emptyMap());
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
    }

    @Test
    public void testWizard() {
        T wizard = getWizard();
        assertThat(wizard.runSteps(), is(result));
        assertThat(wizard.getCommand(), equalTo(command));
    }
}
