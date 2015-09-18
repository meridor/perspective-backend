package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.query.ModifyInstancesQuery;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class ModifyCommands extends BaseCommands {

    @Autowired
    private InstancesRepository instancesRepository;

    @CliCommand(value = "reboot", help = "Reboot instances")
    public void rebootInstances(
            @CliOption(key = "", mandatory = true, help = "Space separated instances names, ID or patterns to match against instance name") String names,
            @CliOption(key = "cloud", help = "Cloud types") String cloud,
            @CliOption(key = "hard", help = "Whether to hard reboot instance") boolean hard
    ) {
        ModifyInstancesQuery modifyInstancesQuery = new ModifyInstancesQuery(names, cloud, instancesRepository);
        validateExecuteShowStatus(
                modifyInstancesQuery,
                hard ? instancesRepository::hardRebootInstances : instancesRepository::rebootInstances
        );
    }


}
