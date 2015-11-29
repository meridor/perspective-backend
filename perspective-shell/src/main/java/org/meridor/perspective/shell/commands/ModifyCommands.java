package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.query.ModifyInstancesQuery;
import org.meridor.perspective.shell.query.QueryProvider;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class ModifyCommands extends BaseCommands {

    @Autowired
    private InstancesRepository instancesRepository;

    @Autowired
    private QueryProvider queryProvider;
    
    @Autowired
    private EntityFormatter entityFormatter;

    @CliCommand(value = "reboot", help = "Reboot instances")
    public void rebootInstances(
            @CliOption(key = "", mandatory = true, help = "Space separated instances names, ID or patterns to match against instance name") String names,
            @CliOption(key = "cloud", help = "Cloud types") String cloud,
            @CliOption(key = "hard", unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether to hard reboot instance") boolean hard
    ) {
        ModifyInstancesQuery modifyInstancesQuery = queryProvider.get(ModifyInstancesQuery.class)
                .withNames(names)
                .withClouds(cloud);
        validateConfirmExecuteShowStatus(
                modifyInstancesQuery,
                instances -> hard ?
                        String.format("Going to hard reboot %d instances.", instances.size()):
                        String.format("Going to reboot %d instances.", instances.size()),
                instances -> new String[]{"Name", "Project", "Image", "Flavor", "State", "Last modified"},
                instances -> entityFormatter.formatInstances(instances, cloud),
                hard ? instancesRepository::hardRebootInstances : instancesRepository::rebootInstances
        );
    }


}
