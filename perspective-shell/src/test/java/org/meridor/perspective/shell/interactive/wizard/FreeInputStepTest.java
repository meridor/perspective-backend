package org.meridor.perspective.shell.interactive.wizard;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.shell.interactive.TestLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.shell.common.repository.impl.ConsoleUtils.mockConsoleReader;
import static org.meridor.perspective.shell.interactive.wizard.MockFreeInputStep.TEST_MESSAGE;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class FreeInputStepTest {
    
    @Autowired
    private MockFreeInputStep mockFreeInputStep;
    
    @Autowired
    private TestLogger logger;
    
    @Test
    public void testRun() throws Exception {
        mockFreeInputStep.setConsoleReader(mockConsoleReader("nan\n-1\n2\n"));
        assertThat(mockFreeInputStep.run(), is(true));
        assertThat(mockFreeInputStep.getAnswer(), equalTo("2"));
        assertThat(mockFreeInputStep.getAnswerField(), equalTo(2));
        assertThat(logger.getWarnMessages(), hasSize(2));
        assertThat(logger.getOkMessages(), contains(TEST_MESSAGE));
    }

    @Test
    public void testExit() throws Exception {
        mockFreeInputStep.setConsoleReader(mockConsoleReader("q\n"));
        assertThat(mockFreeInputStep.run(), is(false));
    }

    @Test
    public void testExitAfterInvalidAnswer() throws Exception {
        mockFreeInputStep.setConsoleReader(mockConsoleReader("n\nq\n"));
        assertThat(mockFreeInputStep.run(), is(false));
    }

}