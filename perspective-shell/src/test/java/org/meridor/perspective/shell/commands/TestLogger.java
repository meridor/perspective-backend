package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.misc.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestLogger implements Logger {
    
    private List<String> okMessages = new ArrayList<>();
    
    private List<String> warnMessages = new ArrayList<>();
    
    private List<String> errorMessages = new ArrayList<>();
    
    @Override
    public void ok() {
        ok("OK");
    }

    @Override
    public void ok(String message) {
        this.okMessages.add(message);
    }

    @Override
    public void warn(String message) {
        this.warnMessages.add(message);
    }

    @Override
    public void error(String message) {
        this.errorMessages.add(message);
    }

    public List<String> getOkMessages() {
        return okMessages;
    }

    public List<String> getWarnMessages() {
        return warnMessages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
