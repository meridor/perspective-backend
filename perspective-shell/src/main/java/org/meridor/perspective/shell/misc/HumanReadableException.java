package org.meridor.perspective.shell.misc;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class HumanReadableException extends RuntimeException {

    public HumanReadableException(String message) {
        super(message);
    }

    public HumanReadableException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        return ": " + getString();
    }
    
    private String getString() {
        Throwable cause = getCause();
        if (cause == null) {
            return getMessage();
        }
        List<Throwable> list = stackToList(this, new ArrayList<>());
        if (findConnectionError(list)) {
            return "failed to connect to the API";
        }
        return "unknown error";
    }
    
    private boolean findConnectionError(List<Throwable> list) {
        return list.stream()
                .filter(e -> e instanceof SocketException || e instanceof SocketTimeoutException)
                .findAny()
                .isPresent();
    }
    
    private List<Throwable> stackToList(Throwable currentException, List<Throwable> previousExceptions) {
        Throwable cause = currentException.getCause();
        if (cause != null) {
            previousExceptions.add(cause);
            return stackToList(cause, previousExceptions);
        }
        return previousExceptions;
    }
    
}
