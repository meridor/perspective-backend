package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.query.DeleteImagesQuery;
import org.meridor.perspective.shell.query.ModifyInstancesQuery;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.repository.impl.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DeleteCommands extends BaseCommands {

    @Autowired
    private InstancesRepository instancesRepository;
    
    @Autowired
    private ImagesRepository imagesRepository;
    
    @CliCommand(value = "delete instances", help = "Completely delete (terminate) instances")
    public void deleteInstances(
            @CliOption(key = "", mandatory = true, help = "Space separated instances names, ID or patterns to match against instance name") String names,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        ModifyInstancesQuery modifyInstancesQuery = new ModifyInstancesQuery(names, cloud, instancesRepository);
        validateConfirmExecuteShowStatus(
                modifyInstancesQuery,
                instances -> String.format("Going to delete %d instances.", instances.size()),
                instances -> new String[]{"Name", "Image", "Flavor", "State", "Last modified"},
                instances -> instances.stream().map(TextUtils::instanceToRow).collect(Collectors.toList()),
                instancesRepository::deleteInstances
        );
    }
    
    @CliCommand(value = "delete images", help = "Delete images")
    public void set(
            @CliOption(key = "", mandatory = true, help = "Space separated instances names, ID or patterns to match against instance name") String patterns,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        DeleteImagesQuery deleteImagesQuery = new DeleteImagesQuery(patterns, cloud, imagesRepository);
        validateConfirmExecuteShowStatus(
                deleteImagesQuery,
                images -> String.format("Going to delete %d images.", images.size()),
                images -> new String[]{"Name", "State", "Last modified"},
                images -> images.stream().map(TextUtils::imageToRow).collect(Collectors.toList()),
                imagesRepository::deleteImages
        );
    }


}
