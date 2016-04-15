package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SetCommands extends BaseCommands {

    @Autowired
    private SettingsRepository settingsRepository;

    @CliCommand(value = "set", help = "Set filter or option")
    public void set(
            @CliOption(key = "", mandatory = true, help = "Data to set") String data
    ) {
        Set<String> errors = settingsRepository.set(data);
        okOrShowErrors(errors);
    }
    
    @CliCommand(value = "unset", help = "Unset filter or option")
    public void unset(
            @CliOption(key = "", mandatory = false, help = "Data to unset") String data,
            @CliOption(key = "filters", unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Show all available settings") boolean filters
            
    ) {
        Set<String> errors = new LinkedHashSet<>();
        if (filters) {
            settingsRepository.showFilters(false).keySet()
                    .forEach(k -> settingsRepository.unset(k));
        } else if (data == null) {
            errors.add("Please specify a filter or setting to unset");
        } else {
            errors.addAll(settingsRepository.unset(data));
        }
        okOrShowErrors(errors);
    }

    @CliCommand(value = "show filters", help = "Show enabled filters")
    public void showFilters(
            @CliOption(key = "all", unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Show all available settings") boolean all
    ) {
        List<String[]> rows = settingsRepository.showFilters(all)
                .entrySet().stream()
                .map(e -> new String[]{e.getKey(), e.getValue()})
                .collect(Collectors.toList());
        tableOrNothing(new String[]{"Filter Name", "Value"}, rows);
    }
    
    @CliCommand(value = "show settings", help = "Show enabled settings")
    public void showSettings(
            @CliOption(key = "all", unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Show all available settings") boolean all
    ) {
        List<String[]> rows = settingsRepository.showSettings(all)
                .entrySet().stream()
                .map(e -> new String[]{e.getKey(), e.getValue()})
                .collect(Collectors.toList());
        tableOrNothing(new String[]{"Setting", "Value"}, rows);
    }
    
}
