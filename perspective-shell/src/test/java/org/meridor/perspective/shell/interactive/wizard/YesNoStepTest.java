package org.meridor.perspective.shell.interactive.wizard;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.shell.common.repository.impl.TextUtils;
import org.meridor.perspective.shell.interactive.TestLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.shell.common.repository.impl.ConsoleUtils.mockConsoleReader;
import static org.meridor.perspective.shell.common.repository.impl.TextUtils.NO;
import static org.meridor.perspective.shell.common.repository.impl.TextUtils.QUIT;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class YesNoStepTest {

    @Autowired
    private TestLogger logger;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testQuit() throws Exception {
        MockYesNoStep step = applicationContext.getBean(MockYesNoStep.class);
        step.setConsoleReader(mockConsoleReader(QUIT + "\n"));
        assertThat(step.run(), is(false));
    }

    @Test
    public void testRun() throws Exception {
        testRun(false, false);
    }

    @Test
    public void testRunProceedAnyway() throws Exception {
        testRun(true, true);
    }

    private void testRun(boolean shouldProceedAnyway, boolean result) throws Exception {
        MockYesNoStep step = applicationContext.getBean(MockYesNoStep.class);
        step.setConsoleReader(mockConsoleReader(TextUtils.DASH + "\n" + NO + "\n"));
        step.setMessage("test-message");
        step.setDefaultAnswer(NO);
        step.setShouldProceedAnyway(shouldProceedAnyway);
        assertThat(step.run(), is(result));
        assertThat(logger.getWarnMessages(), hasSize(1));
        assertThat(step.getAnswer(), equalTo(NO));
    }

}