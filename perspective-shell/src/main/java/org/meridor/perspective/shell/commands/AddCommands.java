package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.query.AddImagesQuery;
import org.meridor.perspective.shell.query.AddInstancesQuery;
import org.meridor.perspective.shell.query.QueryProvider;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.repository.impl.TextUtils;
import org.meridor.perspective.shell.wizard.images.AddImagesWizard;
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
    
    @Autowired
    private AddImagesWizard addImagesWizard;

    @Autowired
    private QueryProvider queryProvider;

    @CliCommand(value = "add instances", help = "Add one or more instances to project")
    public void addInstance(
            @CliOption(key = "name", help = "Instance name") String name,
            @CliOption(key = "project", help = "Name of the project to launch instance in") String project,
            @CliOption(key = "flavor", help = "Instance flavor") String flavor,
            @CliOption(key = "image", help = "Instance image") String image,
            @CliOption(key = "network", help = "Name of the network to use") String network,
            @CliOption(key = "range", help = "A range of numbers to launch instances with") String range,
            @CliOption(key = "count", help = "How many instances to launch") Integer count,
            @CliOption(key = "options", help = "Various instance options") String options
    ) {
        if (name != null) {
            AddInstancesQuery addInstancesQuery = queryProvider.get(AddInstancesQuery.class)
                    .withName(name)
                    .withProject(project)
                    .withFlavor(flavor)
                    .withImage(image)
                    .withNetwork(network)
                    .withOptions(options);
            addInstancesQuery = (count != null) ?
                    addInstancesQuery.withCount(count) :
                    addInstancesQuery.withRange(range);
            
            validateConfirmExecuteShowStatus(
                    addInstancesQuery,
                    instances -> String.format("Going to add %d instances:", instances.size()),
                    instances -> new String[]{"Name", "Image", "Flavor", "More"},
                    instances -> instances.stream().map(TextUtils::newInstanceToRow).collect(Collectors.toList()),
                    instancesRepository::addInstances
            );
        } else if (addInstancesWizard.runSteps()) {
            addInstancesWizard.runCommand();
        }
        
    }
    
    @CliCommand(value = "add images", help = "Add one or more images to project")
    public void addImage(
            @CliOption(key = "instances", help = "Comma separeted instance names or patterns to match against instance name") String instanceNames,
            @CliOption(key = "name", help = "Image name") String imageName
    ) {
        //TODO: implement adding image from file
        if (instanceNames != null) {
            AddImagesQuery addImagesQuery = queryProvider.get(AddImagesQuery.class).withInstanceNames(instanceNames).withName(imageName);
            validateConfirmExecuteShowStatus(
                    addImagesQuery,
                    images -> String.format("Going to add %d images.", images.size()),
                    images -> new String[]{"Name", "State", "Last modified"},
                    images -> images.stream().map(TextUtils::imageToRow).collect(Collectors.toList()),
                    imagesRepository::addImages
            );
        } else if (addImagesWizard.runSteps()) {
            addImagesWizard.runCommand();
        }
    }
    
}
