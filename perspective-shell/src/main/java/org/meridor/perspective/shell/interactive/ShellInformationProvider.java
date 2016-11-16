package org.meridor.perspective.shell.interactive;

import org.meridor.perspective.common.events.EventBus;
import org.meridor.perspective.common.events.EventListener;
import org.meridor.perspective.shell.common.events.PromptChangedEvent;
import org.meridor.perspective.shell.common.repository.FiltersAware;
import org.meridor.perspective.shell.common.repository.MailRepository;
import org.meridor.perspective.shell.common.repository.impl.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.BannerProvider;
import org.springframework.shell.plugin.HistoryFileNameProvider;
import org.springframework.shell.plugin.PromptProvider;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.meridor.perspective.shell.interactive.PathUtils.getConfigurationDirectoryPath;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ShellInformationProvider implements BannerProvider, HistoryFileNameProvider, PromptProvider, EventListener<PromptChangedEvent> {

    private String prompt;
    
    private final EventBus eventBus;

    private final FiltersAware filtersAware;
    
    private final MailRepository mailRepository;

    @Autowired
    public ShellInformationProvider(EventBus eventBus, MailRepository mailRepository, FiltersAware filtersAware) {
        this.eventBus = eventBus;
        this.mailRepository = mailRepository;
        this.filtersAware = filtersAware;
    }
    
    @PostConstruct
    public void init() {
        refreshPrompt();
        eventBus.addListener(PromptChangedEvent.class, this);
    }
    
    @Override
    public String getBanner() {
        return "";
    }

    @Override
    public String getVersion() {
        return TextUtils.getVersion();
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
        return prompt;
    }

    @Override
    public void onEvent(PromptChangedEvent event) {
        refreshPrompt();
    }

    private void refreshPrompt() {
        this.prompt = createPrompt();
    }
    
    private String createPrompt() {
        StringBuilder sb = new StringBuilder("perspective");
        if (!filtersAware.getFilters(false).isEmpty()) {
            sb.append("[*]");
        }
        if (mailRepository.getLetters().size() > 0) {
            sb.append(String.format("[%d]", mailRepository.getLetters().size()));
        }
        sb.append(">");
        return sb.toString();
    }
    
}
