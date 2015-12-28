package org.meridor.perspective.shell.misc;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.BannerProvider;
import org.springframework.shell.plugin.HistoryFileNameProvider;
import org.springframework.shell.plugin.PromptProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.meridor.perspective.shell.misc.PathUtils.getConfigurationDirectoryPath;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ShellInformationProvider implements BannerProvider, HistoryFileNameProvider, PromptProvider, PromptManager {
    
    private static final String DEFAULT_PROMPT = "perspective>";
    
    private Optional<String> prompt = Optional.empty();
    
    @Override
    public String getBanner() {
        return "";
    }

    @Override
    public String getVersion() {
        Optional<String> version = Optional.ofNullable(getClass().getPackage().getImplementationVersion());
        return version.isPresent() ? version.get() : "devel";
    }

    @Override
    public String getWelcomeMessage() {
        return "Welcome to Perspective shell. Type \"help\" for the list of available commands.";
    }

    @Override
    public String getProviderName() {
        return "Perspective Shell";
    }

    @Override
    public String getHistoryFileName() {
        final String LOG_FILE_NAME = "perspective-history.log";
        Path configurationDirectoryPath = getConfigurationDirectoryPath();
        if (!Files.exists(configurationDirectoryPath)) {
            try {
                Files.createDirectory(configurationDirectoryPath);
            } catch (IOException e) {
                return LOG_FILE_NAME;
            }
        }
        if (!Files.isDirectory(configurationDirectoryPath)) {
            return LOG_FILE_NAME;
        }
        return configurationDirectoryPath.resolve(LOG_FILE_NAME).toString();
    }

    @Override
    public String getPrompt() {
        return prompt.isPresent() ? prompt.get() : DEFAULT_PROMPT;
    }

    @Override
    public void setPrompt(String prompt) {
        this.prompt = Optional.ofNullable(prompt);
    }

    @Override
    public void resetPrompt() {
        this.prompt = Optional.empty();
    }
}
