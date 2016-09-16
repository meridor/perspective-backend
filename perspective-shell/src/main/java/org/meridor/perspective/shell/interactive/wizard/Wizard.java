package org.meridor.perspective.shell.interactive.wizard;

import java.util.Iterator;

public interface Wizard extends Iterator<WizardScreen>, Iterable<WizardScreen> {
    
    boolean runSteps();
    
    String getCommand();
    
}
