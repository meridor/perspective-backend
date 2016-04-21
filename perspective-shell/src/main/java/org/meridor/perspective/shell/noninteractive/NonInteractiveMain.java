package org.meridor.perspective.shell.noninteractive;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;
import org.meridor.perspective.shell.noninteractive.commands.Command;
import org.meridor.perspective.shell.noninteractive.commands.QueryCommand;
import org.meridor.perspective.shell.noninteractive.commands.UsageCommand;
import org.meridor.perspective.shell.noninteractive.commands.VersionCommand;

public class NonInteractiveMain {
    
    private final String[] args;
    private final CmdLineParser parser;
    
    @Option(name = "-v", aliases = {"--version", "-version"}, handler = BooleanOptionHandler.class, usage = "Show version")
    private boolean showVersion;
    
    @Option(name = "-h", aliases = {"--help", "-help"}, handler = BooleanOptionHandler.class, usage = "Show usage")
    private boolean showHelp;

    @Option(name = "-q", aliases = {"--query", "-query"}, usage = "Execute SQL query non-interactive")
    private String sql;

    public NonInteractiveMain(String[] args) {
        this.args = args;
        this.parser = new CmdLineParser(this);
    }

    public void start() {
        try {
            parser.parseArgument(args);
            Command command = getCommand();
            command.run();
            System.exit(0);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            usageCommand().run();
            System.exit(1);
        }
    }
    
    private Command getCommand() {
        if (showVersion) {
            return versionCommand();
        } else if (showHelp) {
            return usageCommand();
        } else if (sql != null) {
            return queryCommand(sql);
        }
        throw new UnsupportedOperationException("Unknown operation. This is probably a bug.");
    }
    
    private Command versionCommand(){
        return new VersionCommand();
    }
    
    private Command usageCommand() {
        return new UsageCommand(parser);
    }
    
    private static Command queryCommand(String sql) {
        return new QueryCommand(sql);
    }

}
