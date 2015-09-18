package org.meridor.perspective.shell.validator;

public enum Field {

    PROJECTS,
    CLOUDS,

    NETWORK_IDS,
    NETWORK_NAMES,

    FLAVOR_IDS,
    FLAVOR_NAMES,
    
    INSTANCE_STATES,
    INSTANCE_NAMES,
    INSTANCE_IDS,
    
    IMAGE_IDS,
    IMAGE_NAMES,
    IMAGE_STATES;

    public static boolean contains(String name) {
        for (Field c : values()) {
            if (c.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
}
