package org.meridor.perspective.shell.interactive.commands;

public enum CommandArgument {
    NAME,
    PROJECT,
    INSTANCES,
    IMAGE,
    FLAVOR,
    KEYPAIR,
    NETWORK,
    RANGE,
    COUNT,
    OPTIONS,
    COMMAND;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
