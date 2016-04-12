package org.meridor.perspective.shell;

import org.meridor.perspective.shell.commands.noninteractive.NonInteractiveMain;
import org.springframework.shell.Bootstrap;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        registerSigIntHandler();
        if (args.length > 0) {
            new NonInteractiveMain(args).start();
        } else {
            Bootstrap.main(args);
        }
    }
    
    private static void registerSigIntHandler() {
        if (signalsSupported()) {
            try {
                Class.forName("org.meridor.perspective.shell.misc.SigIntHandler");
            } catch (ClassNotFoundException e) {
                System.out.println("Failed to register SIGINT handler. Pressing Ctrl+C may not work as expected.");
                e.printStackTrace();
            }
        }
    }
    
    private static boolean signalsSupported() {
        try {
            Class.forName("sun.misc.SignalHandler");
            Class.forName("sun.misc.Signal");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
}
