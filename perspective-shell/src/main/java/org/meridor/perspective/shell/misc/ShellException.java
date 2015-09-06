package org.meridor.perspective.shell.misc;

import java.net.URISyntaxException;

public class ShellException extends RuntimeException {
    public ShellException(String message, URISyntaxException cause) {
        super(message, cause);
    }
}
