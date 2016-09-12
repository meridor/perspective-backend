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
public class MultipleChoicesStepTest {

    @Autowired
    private MockMultipleChoicesStep mockMultipleChoicesStep;

    @Test
    public void testRun() throws Exception {
        mockMultipleChoicesStep.setPossibleChoices(Arrays.asList("one", "two", "three", "four", "five"));
        mockMultipleChoicesStep.setConsoleReader(mockConsoleReader("n\n4-6\n1,3-4\n"));
        assertThat(mockMultipleChoicesStep.run(), is(true));
        assertThat(mockMultipleChoicesStep.getAnswer(), equalTo("^one$, ^three$, ^four$"));
    }

}