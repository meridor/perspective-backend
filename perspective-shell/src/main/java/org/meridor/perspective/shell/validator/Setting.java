package org.meridor.perspective.shell.validator;

public enum Setting {
    
    //Contains all supported shell settings
    API_URL,
    LOG_LEVEL,
    PAGE_SIZE;

    public static boolean contains(String name) {
        for (Setting c : values()) {
            if (c.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
