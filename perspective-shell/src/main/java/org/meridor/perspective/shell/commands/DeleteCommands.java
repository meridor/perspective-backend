package org.meridor.perspective.shell.commands;

import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.InstancesRepository;
import org.meridor.perspective.shell.request.FindImagesRequest;
import org.meridor.perspective.shell.request.FindInstancesRequest;
import org.meridor.perspective.shell.request.RequestProvider;
import org.meridor.perspective.shell.result.FindImagesResult;
import org.meridor.perspective.shell.result.FindInstancesResult;
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

    @Autowired
    private RequestProvider requestProvider;

    @CliCommand(value = "delete instances", help = "Completely delete (terminate) instances")
    public void deleteInstances(
            @CliOption(key = "", mandatory = true, help = "Comma separated instances names or patterns to match against instance name") String names,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        FindInstancesRequest findInstancesRequest = requestProvider.get(FindInstancesRequest.class).withNames(names).withClouds(cloud);
        validateConfirmExecuteShowStatus(
                findInstancesRequest,
                r -> instancesRepository.findInstances(findInstancesRequest),
                instances -> String.format("Going to delete %d instances.", instances.size()),
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
                instancesRepository::deleteInstances
        );
    }
    
    @CliCommand(value = "delete images", help = "Delete images")
    public void set(
            @CliOption(key = "", mandatory = true, help = "Comma separated instances names or patterns to match against instance name") String patterns,
            @CliOption(key = "cloud", help = "Cloud type") String cloud
    ) {
        FindImagesRequest findImagesRequest = requestProvider.get(FindImagesRequest.class).withNames(patterns).withClouds(cloud);
        validateConfirmExecuteShowStatus(
                findImagesRequest,
                r -> imagesRepository.findImages(r),
                images -> String.format("Going to delete %d images.", images.size()),
                images -> new String[]{"Name", "Cloud", "State", "Last modified"},
                images -> images.stream()
                        .map(i -> new String[]{i.getName(), i.getCloudType().name().toLowerCase(), i.getState(), i.getLastUpdated()})
                        .collect(Collectors.toList()),
                (r, images) -> images.stream().map(FindImagesResult::getId).collect(Collectors.toSet()),
                imagesRepository::deleteImages
        );
    }


}
