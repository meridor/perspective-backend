package org.meridor.perspective.shell.commands;

public enum CommandArgument {
    NAME,
    PROJECT,
    INSTANCES,
    IMAGE,
    FLAVOR,
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
