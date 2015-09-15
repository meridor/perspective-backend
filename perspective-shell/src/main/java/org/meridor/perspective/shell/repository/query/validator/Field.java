package org.meridor.perspective.shell.repository.query.validator;

public enum Field {

    PROJECT,
    FLAVOR,
    IMAGE,
    NETWORK,
    INSTANCE_STATE,
    CLOUD,
    NAME;

    public static boolean contains(String name) {
        for (Field c : values()) {
            if (c.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
}
