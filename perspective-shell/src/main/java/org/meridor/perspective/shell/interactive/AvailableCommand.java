package org.meridor.perspective.shell.interactive;

public enum AvailableCommand {

    //Project commands
    SHOW_PROJECTS,

    //Instance commands
    SHOW_INSTANCES,
    SHOW_CONSOLE,
    ADD_INSTANCE,
    REBOOT_INSTANCE,
    DELETE_INSTANCE,

    //Image commands
    SHOW_IMAGES,
    ADD_IMAGE,
    DELETE_IMAGE,
    
    //Flavor commands
    SHOW_FLAVORS,
    
    //Network commands
    SHOW_NETWORKS,
    
    //Keypair commands
    SHOW_KEYPAIRS,
    
    //SQL commands
    SELECT,
    EXPLAIN,
    
    //Misc commands
    SET,
    UNSET,
    SHOW_FILTERS,
    SHOW_SETTINGS
}
