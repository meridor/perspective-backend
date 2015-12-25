package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.query.DeleteImagesQuery;
import org.meridor.perspective.shell.query.ModifyInstancesQuery;
import org.meridor.perspective.shell.query.QueryProvider;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class DeleteCommands extends BaseCommands {

    @Autowired
    private InstancesRepository instancesRepository;
    
    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private QueryProvider queryProvider;

    @Autowired
    private EntityFormatter entityFormatter;

    @CliCommand(value = "delete instances", help = "Completely delete (terminate) instances")
    public void deleteInstances(
            @CliOption(key = "", mandatory = true, help = "Comma separated instances names or patterns to match against instance name") String names,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        ModifyInstancesQuery modifyInstancesQuery = queryProvider.get(ModifyInstancesQuery.class)
                .withNames(names)
                .withClouds(cloud);
        validateConfirmExecuteShowStatus(
                modifyInstancesQuery,
                instances -> String.format("Going to delete %d instances.", instances.size()),
                instances -> new String[]{"Name", "Project", "Image", "Flavor", "State", "Last modified"},
                instances -> entityFormatter.formatInstances(instances, cloud),
                instancesRepository::deleteInstances
        );
    }
    
    @CliCommand(value = "delete images", help = "Delete images")
    public void set(
            @CliOption(key = "", mandatory = true, help = "Comma separated instances names or patterns to match against instance name") String patterns,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        DeleteImagesQuery deleteImagesQuery = queryProvider.get(DeleteImagesQuery.class).withNames(patterns).withClouds(cloud);
        validateConfirmExecuteShowStatus(
                deleteImagesQuery,
                images -> String.format("Going to delete %d images.", images.size()),
                images -> new String[]{"Name", "Cloud", "State", "Last modified"},
                images -> entityFormatter.formatImages(images),
                imagesRepository::deleteImages
        );
    }


}
