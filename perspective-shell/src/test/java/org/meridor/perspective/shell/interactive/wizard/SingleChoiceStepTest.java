package org.meridor.perspective.shell.interactive.wizard;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.shell.common.repository.impl.ConsoleUtils.mockConsoleReader;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class SingleChoiceStepTest {

    @Autowired
    private MockSingleChoiceStep mockSingleChoiceStep;

    @Test
    public void testRun() throws Exception {
        mockSingleChoiceStep.setPossibleChoices(Arrays.asList("one", "two"));
        mockSingleChoiceStep.setConsoleReader(mockConsoleReader("n\n2\n"));
        assertThat(mockSingleChoiceStep.run(), is(true));
        assertThat(mockSingleChoiceStep.getAnswer(), equalTo("^two$"));
    }

    @Test
    public void testExit() throws Exception {
        mockSingleChoiceStep.setPossibleChoices(Arrays.asList("one", "two"));
        mockSingleChoiceStep.setConsoleReader(mockConsoleReader("q\n"));
        assertThat(mockSingleChoiceStep.run(), is(false));
    }

}