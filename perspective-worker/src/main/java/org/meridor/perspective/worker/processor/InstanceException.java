package org.meridor.perspective.worker.processor;

import org.meridor.perspective.beans.Instance;

public class InstanceException extends RuntimeException {

    private final Instance instance;

    public InstanceException(String message, Throwable cause, Instance instance) {
        super(message, cause);
        this.instance = instance;
    }

    public InstanceException(String message, Instance instance) {
        super(message);
        this.instance = instance;
    }

    public Instance getInstance() {
        return instance;
    }
}
