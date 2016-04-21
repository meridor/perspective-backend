package org.meridor.perspective.shell.interactive.wizard;

import org.junit.Test;
import org.meridor.perspective.shell.interactive.commands.CommandArgument;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CommandBuilderTest {
    
    private static final String COMMAND = "command";
    
    @Test
    public void testAddArgumentWithValue() {
        CommandBuilder commandBuilder = new CommandBuilder(COMMAND);
        commandBuilder.addArgument(CommandArgument.NAME, "value");
        assertThat(commandBuilder.getCommand(), equalTo("command --name value"));
    }
    
    @Test
    public void testAddArgumentWithAssignment() {
        CommandBuilder commandBuilder = new CommandBuilder(COMMAND);
        Map<String, Set<String>> data = new HashMap<>();
        data.put("key", new LinkedHashSet<>(Arrays.asList(new String[]{"value1", "value2"})));
        commandBuilder.addArgument(CommandArgument.NAME, data);
        assertThat(commandBuilder.getCommand(), equalTo("command --name key=value1,value2"));
    }
    
    @Test
    public void testAddArgumentWithoutValue() {
        CommandBuilder commandBuilder = new CommandBuilder(COMMAND);
        commandBuilder.addArgument(CommandArgument.NAME);
        assertThat(commandBuilder.getCommand(), equalTo("command --name"));
    }
    
}
