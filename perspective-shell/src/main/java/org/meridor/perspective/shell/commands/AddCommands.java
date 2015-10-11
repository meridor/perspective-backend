package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.query.AddImagesQuery;
import org.meridor.perspective.shell.query.AddInstancesQuery;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class AddCommands extends BaseCommands {
    
    @Autowired
    private InstancesRepository instancesRepository;
    
    @Autowired
    private ImagesRepository imagesRepository;
    
    @CliCommand(value = "add instances", help = "Add one or more instances to project")
    public void addInstance(
            @CliOption(key = "name", mandatory = true, help = "Instance name") String name,
            @CliOption(key = "project", help = "Name of the project to launch instance in") String project,
            @CliOption(key = "flavor", help = "Instance flavor") String flavor,
            @CliOption(key = "image", help = "Instance image") String image,
            @CliOption(key = "network", help = "Name of ID of the network to use") String network,
            @CliOption(key = "from", help = "Instance name start number") Integer from,
            @CliOption(key = "to", help = "Instance name end number") Integer to,
            @CliOption(key = "count", help = "How many instances to launch") Integer count,
            @CliOption(key = "options", help = "Various instance options") String options
    ) {
        AddInstancesQuery addInstancesQuery = (count != null) ?
                new AddInstancesQuery(name, project, flavor, image, network, count, options) :
                new AddInstancesQuery(name, project, flavor, image, network, from, to, options);
        validateExecuteShowStatus(addInstancesQuery, instancesRepository::addInstances);
    }
    
    @CliCommand(value = "add images", help = "Add one or more images to project")
    public void addImage(
            @CliOption(key = "", mandatory = true, help = "Instance IDs, names or patterns to match against instance name or ID") String names,
            @CliOption(key = "name", mandatory = true, help = "Image name") String imageName
    ) {
        //TODO: implement adding image from file
        AddImagesQuery addImagesQuery = new AddImagesQuery(imageName, names, instancesRepository);
        validateExecuteShowStatus(addImagesQuery, imagesRepository::addImages);
    }
    
}
