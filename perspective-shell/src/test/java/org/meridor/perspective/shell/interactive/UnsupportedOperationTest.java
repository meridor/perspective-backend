package org.meridor.perspective.shell.interactive;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandResult;
import org.springframework.shell.core.Shell;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class UnsupportedOperationTest {

    @Autowired
    private Shell shell;

    @Autowired
    private TestLogger testLogger;

    @Test
    public void testUnsupportedOperation() {
        /* 
            No operations are added to TestRepository, so test-instance should
            be filtered out and an error should be shown that nothing is selected.
         */
        CommandResult commandResult = shell.executeCommand("reboot test-instance");
        assertThat(commandResult.isSuccess(), is(true));
        assertThat(testLogger.getOkMessages(), is(empty()));
        assertThat(testLogger.getWarnMessages(), hasSize(1));
        assertThat(testLogger.getErrorMessages(), hasSize(1));
    }

}
