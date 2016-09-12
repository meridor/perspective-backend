package org.meridor.perspective.shell.interactive.wizard;

import jline.console.ConsoleReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.shell.interactive.TestLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.shell.common.repository.impl.ConsoleUtils.mockConsoleReader;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class AbstractStepTest {

    private static final String DEFAULT_ANSWER = "default-answer";

    @Autowired
    private TestLogger logger;

    @Autowired
    private MockStep mockStep;

    @Test
    public void testPrintMessageWithNoDefaultAnswer() {
        mockStep.setMessage("test-message");
        mockStep.printMessageWithDefaultAnswer();
        assertThat(logger.getOkMessages(), contains("test-message"));
    }

    @Test
    public void testPrintMessageWithDefaultAnswer() {
        mockStep.setMessage("test-message");
        mockStep.setDefaultAnswer(DEFAULT_ANSWER);
        mockStep.printMessageWithDefaultAnswer();
        assertThat(logger.getOkMessages(), contains("test-message [default-answer]"));
    }

    @Test
    public void testPrintMessageContainingPlaceholderWithDefaultAnswer() {
        mockStep.setMessage("test $defaultAnswer message");
        mockStep.setDefaultAnswer(DEFAULT_ANSWER);
        mockStep.printMessageWithDefaultAnswer();
        assertThat(logger.getOkMessages(), contains("test default-answer message"));
    }

    @Test
    public void testWaitForAnswer() throws Exception {
        mockStep.setConsoleReader(mockConsoleReader("test-answer\n"));
        String answer = mockStep.waitForAnswer();
        assertThat(answer, equalTo("test-answer"));
    }

    @Test
    public void testWaitForEmptyAnswer() throws Exception {
        mockStep.setConsoleReader(mockConsoleReader("\n"));
        mockStep.setDefaultAnswer(DEFAULT_ANSWER);
        String answer = mockStep.waitForAnswer();
        assertThat(answer, equalTo(DEFAULT_ANSWER));
    }

    @Test
    public void testWaitForAnswerError() throws Exception {
        ConsoleReader consoleReader = mockConsoleReader("test-answer\n");
        consoleReader.getInput().close(); //Trying to read closed stream
        mockStep.setConsoleReader(consoleReader);
        String answer = mockStep.waitForAnswer();
        assertThat(answer, equalTo(""));
    }

}