package org.meridor.perspective.shell.noninteractive.commands;

import org.kohsuke.args4j.CmdLineParser;

public class UsageCommand implements Command {

    private final CmdLineParser parser;

    public UsageCommand(CmdLineParser parser) {
        this.parser = parser;
    }

    @Override
    public void run() {
        System.err.println("perspective [options...]");
        parser.printUsage(System.err);
    }
}
