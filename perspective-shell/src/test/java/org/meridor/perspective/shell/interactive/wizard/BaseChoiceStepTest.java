package org.meridor.perspective.shell.interactive.wizard;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.shell.interactive.TestLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.shell.common.repository.impl.ConsoleUtils.mockConsoleReader;
import static org.meridor.perspective.shell.interactive.wizard.MockBaseChoiceStep.TEST_MESSAGE;
import static org.meridor.perspective.shell.interactive.wizard.MockBaseChoiceStep.TEST_PROMPT;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class BaseChoiceStepTest {

    @Autowired
    private TestLogger logger;

    @Autowired
    private MockBaseChoiceStep mockBaseChoiceStep;

    @Test
    public void testSimpleRun() throws Exception {
        mockBaseChoiceStep.setConsoleReader(mockConsoleReader("s\nn\ny\n"));
        mockBaseChoiceStep.setValidAnswers(Collections.singletonList("y"));
        mockBaseChoiceStep.setValueToSave("test");
        mockBaseChoiceStep.setAnswerRequired(true);
        mockBaseChoiceStep.setPossibleChoices(Arrays.asList("one", "two"));
        assertThat(mockBaseChoiceStep.run(), is(true));
        assertThat(mockBaseChoiceStep.getAnswer(), equalTo("test"));
        assertThat(logger.getWarnMessages(), hasSize(3));
        assertThat(logger.getOkMessages(), hasSize(3));
        assertThat(logger.getOkMessages().get(0), equalTo(TEST_MESSAGE));
        String choicesTable = logger.getOkMessages().get(1);
        assertThat(choicesTable.contains("one"), is(true));
        assertThat(choicesTable.contains("two"), is(true));
        assertThat(logger.getOkMessages().get(2), equalTo(TEST_PROMPT));
    }

    @Test
    public void testZeroChoicesAnswerRequired() throws Exception {
        mockBaseChoiceStep.setConsoleReader(mockConsoleReader("y\n"));
        mockBaseChoiceStep.setPossibleChoices(Collections.emptyList());
        mockBaseChoiceStep.setAnswerRequired(true);
        assertThat(mockBaseChoiceStep.run(), is(false));
        assertThat(logger.getErrorMessages(), hasSize(1));
    }

    @Test
    public void testZeroChoicesAnswerOptional() throws Exception {
        mockBaseChoiceStep.setConsoleReader(mockConsoleReader("y\n"));
        mockBaseChoiceStep.setPossibleChoices(Collections.emptyList());
        mockBaseChoiceStep.setAnswerRequired(false);
        assertThat(mockBaseChoiceStep.run(), is(true));
        assertThat(logger.getWarnMessages(), hasSize(1));
    }

    @Test
    public void testSingleChoiceAnswerRequired() throws Exception {
        mockBaseChoiceStep.setConsoleReader(mockConsoleReader("y\n"));
        mockBaseChoiceStep.setPossibleChoices(Collections.singletonList("single"));
        mockBaseChoiceStep.setAnswerRequired(true);
        assertThat(mockBaseChoiceStep.run(), is(true));
        assertThat(mockBaseChoiceStep.getAnswer(), equalTo("single"));
        assertThat(logger.getOkMessages(), hasSize(2));
    }

    @Test
    public void testExit() throws Exception {
        mockBaseChoiceStep.setConsoleReader(mockConsoleReader("q\n"));
        mockBaseChoiceStep.setPossibleChoices(Arrays.asList("one", "two"));
        assertThat(mockBaseChoiceStep.run(), is(false));
    }

    @Test
    public void testExitAfterInvalidAnswer() throws Exception {
        mockBaseChoiceStep.setConsoleReader(mockConsoleReader("n\nq\n"));
        mockBaseChoiceStep.setPossibleChoices(Arrays.asList("one", "two"));
        mockBaseChoiceStep.setValidAnswers(Arrays.asList("1", "2"));
        mockBaseChoiceStep.setAnswerRequired(true);
        assertThat(mockBaseChoiceStep.run(), is(false));
    }

    @Test
    public void testSkip() throws Exception {
        mockBaseChoiceStep.setConsoleReader(mockConsoleReader("s\n"));
        mockBaseChoiceStep.setPossibleChoices(Arrays.asList("one", "two"));
        mockBaseChoiceStep.setAnswerRequired(false);
        assertThat(mockBaseChoiceStep.run(), is(true));

    }

}