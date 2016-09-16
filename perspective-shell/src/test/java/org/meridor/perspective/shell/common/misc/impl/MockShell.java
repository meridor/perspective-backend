package org.meridor.perspective.shell.common.misc.impl;

import org.springframework.shell.core.CommandResult;
import org.springframework.shell.core.ExitShellRequest;
import org.springframework.shell.core.Shell;
import org.springframework.shell.event.ShellStatus;
import org.springframework.shell.event.ShellStatusListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Component
public class MockShell implements Shell {
    
    private List<String> commands = new ArrayList<>();
    
    @Override
    public void promptLoop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExitShellRequest getExitShellRequest() {
        return ExitShellRequest.NORMAL_EXIT;
    }

    @Override
    public CommandResult executeCommand(String command) {
        commands.add(command);
        return new CommandResult(true);
    }

    public List<String> getCommands() {
        return commands;
    }

    @Override
    public void setDevelopmentMode(boolean developmentMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flash(Level level, String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDevelopmentMode() {
        return true;
    }

    @Override
    public void setPromptPath(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPromptPath(String s, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getHome() {
        return new File(System.getProperty("user.dir"));
    }

    @Override
    public String getShellPrompt() {
        return "test";
    }

    @Override
    public void addShellStatusListener(ShellStatusListener shellStatusListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeShellStatusListener(ShellStatusListener shellStatusListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ShellStatus getShellStatus() {
        throw new UnsupportedOperationException();
    }
}
