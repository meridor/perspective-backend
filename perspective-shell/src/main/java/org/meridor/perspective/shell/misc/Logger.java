package org.meridor.perspective.shell.misc;

public interface Logger {

    void ok();
    
    void ok(String message);
    
    void warn(String message);
    
    void error(String message);
    
}
