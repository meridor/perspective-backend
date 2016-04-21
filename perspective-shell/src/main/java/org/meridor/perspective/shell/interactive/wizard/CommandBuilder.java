package org.meridor.perspective.shell.interactive.wizard;

import org.meridor.perspective.shell.interactive.commands.CommandArgument;

import java.util.Map;
import java.util.Set;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.createAssignment;
import static org.meridor.perspective.shell.common.repository.impl.TextUtils.quoteIfNeeded;

public class CommandBuilder {
    
    private StringBuilder command;
    
    public CommandBuilder(String command) {
        this.command = new StringBuilder(command);
    }
    
    public void addArgument(CommandArgument commandArgument, String value) {
        command.append(String.format(" --%s %s", commandArgument, quoteIfNeeded(value)));
    }
    
    public void addArgument(CommandArgument commandArgument) {
        command.append(String.format(" --%s", commandArgument));
    }
    
    public void addArgument(CommandArgument commandArgument, Map<String, Set<String>> values) {
        command.append(String.format(" --%s %s", commandArgument, quoteIfNeeded(createAssignment(values))));
    }
    
    public String getCommand() {
        return command.toString();
    }
    
}
