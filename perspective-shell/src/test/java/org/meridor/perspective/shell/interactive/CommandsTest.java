package org.meridor.perspective.shell.interactive;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.shell.common.validator.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandResult;
import org.springframework.shell.core.Shell;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.meridor.perspective.config.CloudType.MOCK;

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
                {"show projects --name test-project"},
                {"show projects test-project"},
                {"show networks"},
                {"show networks --name test-network"},
                {"show networks test-network"},
                {"show flavors"},
                {"show flavors --name test-flavor"},
                {"show flavors test-flavor"},
                {"show keypairs"},
                {"show keypairs --name test-keypair"},
                {"show keypairs test-keypair"},
                {"show instances"},
                {"show instances --name test-instance"},
                {"show instances test-instance"},
                {"show images"},
                {"show images --name test-image"},
                {"show images test-image"},
                {"show mail"},
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
                {"rename instances --instances test-instance --name new-name"},
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

    @Autowired
    private TestRepository testRepository;

    private final String command;

    public CommandsTest(String command) {
        this.command = command;
    }

    @Before
    public void before() {
        // All operations are supported
        Arrays.stream(OperationType.values())
                .forEach(ot -> testRepository.addSupportedOperations(MOCK, Collections.singleton(ot)));
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