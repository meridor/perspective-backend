package org.meridor.perspective.shell.validator;

public enum Setting {
    
    //Contains all supported shell settings
    ALWAYS_SAY_YES,
    API_URL,
    DISABLE_PROJECTS_CACHE,
    LOG_LEVEL,
    PAGE_SIZE,
    TABLE_WIDTH;

    public static boolean contains(String name) {
        for (Setting c : values()) {
            if (c.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
