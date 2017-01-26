package org.meridor.perspective.shell.interactive.impl;

import org.meridor.perspective.shell.common.misc.Logger;
import org.meridor.perspective.shell.interactive.CommandExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.Shell;
import org.springframework.stereotype.Component;

@Component
public class CommandExecutorImpl implements CommandExecutor {

    @Autowired
    private Shell shell;

    @Autowired
    private Logger logger;

    @Override
    public void execute(String command) {
        logger.ok(String.format("Executing command: %s", command));
        shell.executeCommand(command);
    }
}
