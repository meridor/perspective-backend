package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.repository.query.LaunchInstancesQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.meridor.perspective.shell.repository.impl.TextUtils.*;

@Component
public class AddCommand implements CommandMarker {
    
    @Autowired
    private InstancesRepository instancesRepository;
    
    @CliCommand(value = "add instances", help = "Add one or more instances to project")
    public void addInstance(
            @CliOption(key = "name", mandatory = true, help = "Instance name") String name,
            @CliOption(key = "project", help = "Name of the project to launch instance in") String project,
            @CliOption(key = "flavor", help = "Instance flavor") String flavor,
            @CliOption(key = "flavor", help = "Instance flavor") String image,
            @CliOption(key = "network", help = "Name of ID of the network to use") String network,
            @CliOption(key = "count", help = "How many instances to launch") Integer count,
            @CliOption(key = "options", help = "Various instance options") String options
    ) {
        LaunchInstancesQuery launchInstancesQuery = new LaunchInstancesQuery(name, project, flavor, image, network, count, options);
        Set<String> validationErrors = launchInstancesQuery.validate();
        if (!validationErrors.isEmpty()) {
            error(joinLines(validationErrors));
        }
        Set<String> launchErrors = instancesRepository.launchInstances(launchInstancesQuery);
        if (!launchErrors.isEmpty()) {
            error(joinLines(launchErrors));
        }
        ok();
    }
    
}
