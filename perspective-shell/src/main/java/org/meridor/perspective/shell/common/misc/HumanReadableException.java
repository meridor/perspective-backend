package org.meridor.perspective.shell.common.misc;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HumanReadableException extends RuntimeException {

    public HumanReadableException(String message) {
        super(message);
    }

    public HumanReadableException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {

        Throwable cause = getCause();
        if (cause == null) {
            return ": " + getMessage();
        }
        List<Throwable> list = stackToList(this, new ArrayList<>());
        Optional<HumanReadableException> humanReadableExceptionCandidate = findHumanReadableException(list);
        if (humanReadableExceptionCandidate.isPresent()) {
            return humanReadableExceptionCandidate.get().toString();
        }

        if (findConnectionError(list)) {
            return ": failed to connect to the API";
        }

        Optional<String> messageCandidate = findMessage(list);
        if (messageCandidate.isPresent()) {
            return ":" + messageCandidate.get();
        }
        return ": unknown error";
    }

    private boolean findConnectionError(List<Throwable> list) {
        return list.stream()
                .filter(e -> e instanceof SocketException || e instanceof SocketTimeoutException)
                .findAny()
                .isPresent();
    }
    
    private Optional<String> findMessage(List<Throwable> list) {
        return list.stream()
                .filter(t -> t.getMessage() != null && !t.getMessage().isEmpty())
                .map(Throwable::getMessage)
                .findFirst();
    }
    
    private Optional<HumanReadableException> findHumanReadableException(List<Throwable> list) {
        List<HumanReadableException> humanReadableExceptions = list.stream()
                .filter(e -> e instanceof HumanReadableException)
                .map(HumanReadableException.class::cast)
                .collect(Collectors.toList());
        return humanReadableExceptions.isEmpty() ?
                Optional.empty() :
                Optional.ofNullable(humanReadableExceptions.get(humanReadableExceptions.size() - 1));
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
