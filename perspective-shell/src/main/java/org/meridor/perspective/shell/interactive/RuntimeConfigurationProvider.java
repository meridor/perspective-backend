package org.meridor.perspective.shell.interactive;

import org.meridor.perspective.common.events.EventBus;
import org.meridor.perspective.shell.common.events.ShellStartedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.Shell;
import org.springframework.shell.event.ShellStatus;
import org.springframework.shell.event.ShellStatusListener;
import org.springframework.shell.support.logging.HandlerUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.meridor.perspective.shell.interactive.PathUtils.getConfigurationFilePath;

@Component
public class RuntimeConfigurationProvider implements ShellStatusListener {

    private static final Logger LOG = HandlerUtils.getLogger(RuntimeConfigurationProvider.class);
    
    private static final Logger COMMAND_LOG = HandlerUtils.getLogger(org.meridor.perspective.shell.common.misc.Logger.class);
    
    @Autowired
    private Shell shell;
    
    @Autowired
    private EventBus eventBus;
    
    @PostConstruct
    public void init() {
        shell.addShellStatusListener(this);
    }

    @Override
    public void onShellStatusChange(ShellStatus oldStatus, ShellStatus newStatus) {
        if (newStatus.getStatus().equals(ShellStatus.Status.STARTED)) {
            Path rcFilePath = getRCFilePath();
            if (Files.exists(rcFilePath)) {
                Level logLevel = COMMAND_LOG.getLevel();
                COMMAND_LOG.setLevel(Level.OFF);
                LOG.info(String.format("Loading shell configuration from file %s", rcFilePath.toAbsolutePath()));
                try (
                        InputStream inputStream = Files.newInputStream(rcFilePath);
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
                ) {
                    String line;
                    int lineNumber = 1;
                    while ( (line = bufferedReader.readLine()) != null) {
                        if (!line.trim().isEmpty()) {
                            LOG.fine(String.format("Executing command %s", line));
                            boolean success = shell.executeCommand(line).isSuccess();
                            if (!success) {
                                LOG.severe(String.format("Failed to execute line %s: %s", lineNumber, line));
                                break;
                            }
                        }
                        lineNumber++;
                    }

                } catch (Exception e) {
                    LOG.severe(String.format("Failed to load shell configuration because of exception: %s", e));
                }
                COMMAND_LOG.setLevel(logLevel);
            } else {
                LOG.fine(String.format("Shell configuration file [%s] does not exist", rcFilePath.toAbsolutePath()));
            }
            eventBus.fire(new ShellStartedEvent());
        }
    }

    private Path getRCFilePath() {
        return getConfigurationFilePath("rc");
    }
}
