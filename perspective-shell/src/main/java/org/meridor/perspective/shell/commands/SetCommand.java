package org.meridor.perspective.shell.commands;

import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class SetCommand implements CommandMarker {

    @CliAvailabilityIndicator({"set clouds"})
    public boolean isAvailable() {
        return true;
    }


    @CliCommand(value = "set clouds", help = "List available projects")
    public String listProjects(
            @CliOption(key = "", help = "A list of space separated cloud names") String clouds
    ) {
        System.out.printf("Settings clouds = %s", clouds);
        return "";
    }

}
