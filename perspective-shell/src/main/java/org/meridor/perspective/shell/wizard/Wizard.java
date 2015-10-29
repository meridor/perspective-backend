package org.meridor.perspective.shell.wizard;

import java.util.Iterator;

public interface Wizard extends Iterator<WizardScreen>, Iterable<WizardScreen> {
    
    boolean runSteps();
    
    void runCommand();
    
}
