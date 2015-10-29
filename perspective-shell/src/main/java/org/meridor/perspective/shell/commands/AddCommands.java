package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.query.AddImagesQuery;
import org.meridor.perspective.shell.query.AddInstancesQuery;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.repository.impl.TextUtils;
import org.meridor.perspective.shell.wizard.instances.AddInstancesWizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AddCommands extends BaseCommands {
    
    @Autowired
    private InstancesRepository instancesRepository;
    
    @Autowired
    private ImagesRepository imagesRepository;
    
    @Autowired
    private AddInstancesWizard addInstancesWizard;
    
    @CliCommand(value = "add instances", help = "Add one or more instances to project")
    public void addInstance(
            @CliOption(key = "name", help = "Instance name") String name,
            @CliOption(key = "project", help = "Name of the project to launch instance in") String project,
            @CliOption(key = "flavor", help = "Instance flavor") String flavor,
            @CliOption(key = "image", help = "Instance image") String image,
            @CliOption(key = "network", help = "Name of ID of the network to use") String network,
            @CliOption(key = "from", help = "Instance name start number") Integer from,
            @CliOption(key = "to", help = "Instance name end number") Integer to,
            @CliOption(key = "count", help = "How many instances to launch") Integer count,
            @CliOption(key = "options", help = "Various instance options") String options
    ) {
        if (name != null) {
            AddInstancesQuery addInstancesQuery = (count != null) ?
                    new AddInstancesQuery(name, project, flavor, image, network, count, options) :
                    new AddInstancesQuery(name, project, flavor, image, network, from, to, options);
            validateConfirmExecuteShowStatus(
                    addInstancesQuery,
                    instances -> String.format("Going to add %d instances.", instances.size()),
                    instances -> new String[]{"Name", "Image", "Flavor", "State", "Last modified"},
                    instances -> instances.stream().map(TextUtils::instanceToRow).collect(Collectors.toList()),
                    instancesRepository::addInstances
            );
        } else if (addInstancesWizard.runSteps()) {
            addInstancesWizard.runCommand();
        }
        
    }
    
    @CliCommand(value = "add images", help = "Add one or more images to project")
    public void addImage(
            @CliOption(key = "", mandatory = true, help = "Instance IDs, names or patterns to match against instance name or ID") String names,
            @CliOption(key = "name", mandatory = true, help = "Image name") String imageName
    ) {
        //TODO: implement adding image from file
        AddImagesQuery addImagesQuery = new AddImagesQuery(imageName, names, instancesRepository);
        validateConfirmExecuteShowStatus(
                addImagesQuery,
                images -> String.format("Going to add %d images.", images.size()),
                images -> new String[]{"Name", "State", "Last modified"},
                images -> images.stream().map(TextUtils::imageToRow).collect(Collectors.toList()),
                imagesRepository::addImages
        );
    }
    
}
