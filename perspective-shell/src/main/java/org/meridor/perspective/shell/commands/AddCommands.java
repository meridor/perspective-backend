package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.repository.query.AddImagesQuery;
import org.meridor.perspective.shell.repository.query.AddInstancesQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.meridor.perspective.shell.repository.impl.TextUtils.*;

@Component
public class AddCommands implements CommandMarker {
    
    @Autowired
    private InstancesRepository instancesRepository;
    
    @Autowired
    private ImagesRepository imagesRepository;
    
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
        AddInstancesQuery addInstancesQuery = new AddInstancesQuery(name, project, flavor, image, network, count, options);
        Set<String> validationErrors = addInstancesQuery.validate();
        if (!validationErrors.isEmpty()) {
            error(joinLines(validationErrors));
        }
        Set<String> addErrors = instancesRepository.addInstances(addInstancesQuery);
        if (!addErrors.isEmpty()) {
            error(joinLines(addErrors));
        }
        ok();
    }
    
    @CliCommand(value = "add images", help = "Add one or more images to project")
    public void addImage(
            @CliOption(key = "", mandatory = true, help = "Instance IDs, names or patterns to match against instance name or ID") String names,
            @CliOption(key = "name", mandatory = true, help = "Image name") String imageName
    ) {
        //TODO: implement adding image from file
        AddImagesQuery addImagesQuery = new AddImagesQuery(imageName, names, instancesRepository);
        Set<String> validationErrors = addImagesQuery.validate();
        if (!validationErrors.isEmpty()) {
            error(joinLines(validationErrors));
        }
        Set<String> addErrors = imagesRepository.addImages(addImagesQuery);
        if (!addErrors.isEmpty()) {
            error(joinLines(addErrors));
        }
        ok();
    }
    
}
