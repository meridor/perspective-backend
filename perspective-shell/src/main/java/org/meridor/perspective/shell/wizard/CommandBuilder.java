package org.meridor.perspective.shell.wizard;

import org.meridor.perspective.shell.commands.CommandArgument;

import java.util.Map;
import java.util.Set;

import static org.meridor.perspective.shell.repository.impl.TextUtils.createAssignment;

public class CommandBuilder {
    
    private StringBuilder command;
    
    public CommandBuilder(String command) {
        this.command = new StringBuilder(command);
    }
    
    public void addArgument(CommandArgument commandArgument, String value) {
        command.append(String.format(" --%s=%s", commandArgument, value));
    }
    
    public void addArgument(CommandArgument commandArgument) {
        command.append(String.format(" --%s", commandArgument));
    }
    
    public void addArgument(CommandArgument commandArgument, Map<String, Set<String>> values) {
        command.append(String.format(" --%s %s", commandArgument, createAssignment(values)));
    }
    
    public String getCommand() {
        return command.toString();
    }
    
}
