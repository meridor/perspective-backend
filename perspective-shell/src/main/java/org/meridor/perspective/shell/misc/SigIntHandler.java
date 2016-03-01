package org.meridor.perspective.shell.misc;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * This class is loaded by Class.forName only if legacy signals API is present in current JDK
 * @see @see <a href="http://stackoverflow.com/questions/3250280/ignore-sigint-in-java></a>
 */
public final class SigIntHandler implements SignalHandler {
    
    static {
        Signal.handle(new Signal("INT"), new SigIntHandler());
    }

    @Override
    public void handle(Signal signal) {
        //Right now we simply ignore this signal to avoid shell closing
        //Later this can be changed to canceling user input on current line.
    }
    
}
