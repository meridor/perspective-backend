package org.meridor.perspective.shell.common.validator;

public enum Setting {
    
    //Contains all supported shell settings
    ALWAYS_SAY_YES,
    API_URL,
    DATE_FORMAT,
    INSTANCE_SUFFIXES,
    PAGE_SIZE,
    PAGING_MODE,
    SHOW_BOTTOM_TABLE_HEADER,
    SHOW_QUERY_STATS,
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
