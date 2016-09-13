package org.meridor.perspective.shell.interactive.wizard;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class BaseWizardTest {

    @Autowired
    private MockBaseWizard mockBaseWizard;
    
    @Test
    public void testRunSteps() {
        assertThat(mockBaseWizard.runSteps(), is(true));
        assertThat(mockBaseWizard.getCommand(), equalTo("^two$ n"));
    }
    
    @Test
    public void testFailingStep() {
        mockBaseWizard.setAllStepsPass(false);
        assertThat(mockBaseWizard.runSteps(), is(false));
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testNoNextScreen() {
        assertThat(mockBaseWizard.hasNext(), is(true));
        mockBaseWizard.next();
        assertThat(mockBaseWizard.hasNext(), is(true));
        mockBaseWizard.next();
        assertThat(mockBaseWizard.hasNext(), is(false));
        mockBaseWizard.next();
    }
    
}