package org.meridor.perspective.shell.interactive;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandResult;
import org.springframework.shell.core.Shell;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
@ContextConfiguration(locations = "/META-INF/spring/commands-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class CommandsTest {

    @Parameterized.Parameters(name = "Command \"{0}\" should work")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"show settings"},
                {"show settings --all"},
                {"show filters"},
                {"show filters --all"},
                {"set projects = test"},
                {"set page_size = 20"},
                {"unset --filters"},
                {"show projects"},
                {"show networks"},
                {"show flavors"},
                {"show keypairs"},
                {"show instances"},
                {"show images"},
                {"delete instances test-instance"},
                {"delete images test-image"},
                {"reboot test-instance"},
                {"reboot test-instance --hard"},
                {"start test-instance"},
                {"shutdown test-instance"},
                {"pause test-instance"},
                {"suspend test-instance"},
                {"resume test-instance"},
                {"resize --project test-project --flavor test-flavor --instances test-instance"},
                {"rebuild --project test-project --image test-image --instances test-instance"},
                {"select 1 + 1"},
                {"explain select 1 + 1"},
        });
    }

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private Shell shell;

    @Autowired
    private TestLogger testLogger;

    private final String command;

    public CommandsTest(String command) {
        this.command = command;
    }

    @Test
    public void testExecuteCommand() throws Throwable {
        CommandResult commandResult = shell.executeCommand(command);
        Throwable exception = commandResult.getException();
        if (exception != null) {
            exception.printStackTrace();
            fail("Command threw an exception");
        }
        assertThat(commandResult.isSuccess(), is(true));
        assertThat(testLogger.getOkMessages().size(), is(greaterThan(0)));
        assertThat(testLogger.getWarnMessages(), is(empty()));
        assertThat(testLogger.getErrorMessages(), is(empty()));
    }
}