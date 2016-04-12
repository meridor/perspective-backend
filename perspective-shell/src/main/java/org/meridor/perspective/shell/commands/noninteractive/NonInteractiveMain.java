package org.meridor.perspective.shell.commands.noninteractive;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.BooleanOptionHandler;

import static org.meridor.perspective.shell.repository.impl.TextUtils.getVersion;

public class NonInteractiveMain {
    
    private final String[] args;
    private final CmdLineParser parser;
    
    @Option(name = "-v", aliases = {"--version", "-version"}, handler = BooleanOptionHandler.class, usage = "Show version")
    private boolean showVersion;
    
    @Option(name = "-h", aliases = {"--help", "-help"}, handler = BooleanOptionHandler.class, usage = "Show usage")
    private boolean showHelp;

    public NonInteractiveMain(String[] args) {
        this.args = args;
        this.parser = new CmdLineParser(this);
    }

    public void start() {
        try {
            parser.parseArgument(args);
            selectAction();
            System.exit(0);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            showUsage();
            System.exit(1);
        }
    }
    
    private void selectAction() {
        if (showVersion) {
            showVersion();
        } else if (showHelp) {
            showUsage();
        }
    }
    
    private void showVersion(){
        System.out.println(getVersion());
    }
    
    private void showUsage() {
        System.err.println("perspective [options...]");
        parser.printUsage(System.err);
    }
}
