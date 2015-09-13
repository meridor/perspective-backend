package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.misc.TableRenderer;
import org.meridor.perspective.shell.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.*;

@Component
public class SetCommands implements CommandMarker {

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private TableRenderer tableRenderer;

    @CliCommand(value = "set", help = "Set filter or option")
    public void set(
            @CliOption(key = "", mandatory = true, help = "Data to set") String data
    ) {
        Set<String> errors = settingsRepository.set(data);
        if (errors.isEmpty()) {
            ok();
        } else {
            error(joinLines(errors));
        }
    }
    
    @CliCommand(value = "unset", help = "Unset filter or option")
    public void unset(
            @CliOption(key = "", mandatory = true, help = "Data to unset") String data
    ) {
        Set<String> errors = settingsRepository.unset(data);
        if (errors.isEmpty()) {
            ok();
        } else {
            error(joinLines(errors));
        }
    }

    //TODO: add --all flag to show all possible filters and settings (not only explicitly set)
    @CliCommand(value = "show filters", help = "Show enabled filters")
    public String showFilters() {
        List<String[]> rows = settingsRepository.showFilters()
                .entrySet().stream()
                .map(e -> new String[]{e.getKey(), e.getValue()})
                .collect(Collectors.toList());
        return !rows.isEmpty() ? tableRenderer.render(new String[]{"Filter Name", "Value"}, rows) : nothingToShow();
    }
    
    @CliCommand(value = "show settings", help = "Show enabled settings")
    public String showSettings() {
        List<String[]> rows = settingsRepository.showSettings()
                .entrySet().stream()
                .map(e -> new String[]{e.getKey(), e.getValue()})
                .collect(Collectors.toList());
        return !rows.isEmpty() ? tableRenderer.render(new String[]{"Setting", "Value"}, rows) : nothingToShow();
    }
    
}
