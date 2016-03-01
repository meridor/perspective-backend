package org.meridor.perspective.shell;

import org.springframework.shell.Bootstrap;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        registerSigIntHandler();
        Bootstrap.main(args);
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
