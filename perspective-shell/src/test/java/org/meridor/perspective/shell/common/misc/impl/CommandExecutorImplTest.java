package org.meridor.perspective.shell.common.misc.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.shell.common.misc.CommandExecutor;
import org.meridor.perspective.shell.interactive.TestLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/command-executer-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class CommandExecutorImplTest {

    @Autowired
    private TestLogger logger;
    
    @Autowired
    private MockShell mockShell;
    
    @Autowired
    private CommandExecutor commandExecutor;
    
    @Test
    public void testExecute() {
        commandExecutor.execute("some command");
        assertThat(mockShell.getCommands(), contains("some command"));
        assertThat(logger.getOkMessages(), hasSize(1));
    }

    

}