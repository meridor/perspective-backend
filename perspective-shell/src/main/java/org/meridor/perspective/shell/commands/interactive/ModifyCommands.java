package org.meridor.perspective.shell.commands.interactive;

import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.request.FindInstancesRequest;
import org.meridor.perspective.shell.request.RequestProvider;
import org.meridor.perspective.shell.result.FindInstancesResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ModifyCommands extends BaseCommands {

    @Autowired
    private InstancesRepository instancesRepository;

    @Autowired
    private RequestProvider requestProvider;
    
    @CliCommand(value = "reboot", help = "Reboot instances")
    public void rebootInstances(
            @CliOption(key = "", mandatory = true, help = "Comma separated instances names or patterns to match against instance name") String names,
            @CliOption(key = "cloud", help = "Cloud types") String cloud,
            @CliOption(key = "hard", unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether to hard reboot instance") boolean hard
    ) {
        FindInstancesRequest findInstancesRequest = requestProvider.get(FindInstancesRequest.class).withNames(names).withClouds(cloud);
        validateConfirmExecuteShowStatus(
                findInstancesRequest,
                r -> instancesRepository.findInstances(r),
                instances -> hard ?
                        String.format("Going to hard reboot %d instances.", instances.size()):
                        String.format("Going to reboot %d instances.", instances.size()),
                instances -> new String[]{"Name", "Project", "Image", "Flavor", "Network", "State", "Last modified"},
                instances -> instances.stream()
                        .map(i -> new String[]{
                                i.getName(),
                                i.getProjectName(),
                                i.getImageName(),
                                i.getFlavorName(),
                                i.getAddresses(),
                                i.getState(),
                                i.getLastUpdated()}
                        )
                        .collect(Collectors.toList()),
                (r, instances) -> instances.stream().map(FindInstancesResult::getId).collect(Collectors.toSet()),
                hard ? instancesRepository::hardRebootInstances : instancesRepository::rebootInstances
        );
    }


}
