package org.meridor.perspective.shell.interactive.commands;

import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.shell.common.misc.CommandExecutor;
import org.meridor.perspective.shell.common.misc.OperationSupportChecker;
import org.meridor.perspective.shell.common.repository.ImagesRepository;
import org.meridor.perspective.shell.common.repository.InstancesRepository;
import org.meridor.perspective.shell.common.repository.ProjectsRepository;
import org.meridor.perspective.shell.common.repository.impl.TextUtils;
import org.meridor.perspective.shell.common.request.FindFlavorsRequest;
import org.meridor.perspective.shell.common.request.FindImagesRequest;
import org.meridor.perspective.shell.common.request.FindInstancesRequest;
import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.result.FindFlavorsResult;
import org.meridor.perspective.shell.common.result.FindImagesResult;
import org.meridor.perspective.shell.common.result.FindInstancesResult;
import org.meridor.perspective.shell.interactive.wizard.instances.rebuild.RebuildInstancesWizard;
import org.meridor.perspective.shell.interactive.wizard.instances.resize.ResizeInstancesWizard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.meridor.perspective.config.OperationType.*;

@Component
public class ModifyCommands extends BaseCommands {

    private static final String NAMES_HELP = "Comma separated instances names or patterns to match against instance name";

    private static final Function<List<FindInstancesResult>, String[]> INSTANCE_CONFIRMATION_COLUMNS_PROVIDER = instances ->
            new String[]{"Name", "Project", "Image", "Flavor", "Network", "State", "Last modified"};

    private static final Function<List<FindInstancesResult>, List<String[]>> INSTANCE_CONFIRMATION_ROWS_PROVIDER = instances ->
            instances.stream()
                    .map(i -> new String[]{
                            i.getName(),
                            i.getProjectName(),
                            i.getImageName(),
                            i.getFlavorName(),
                            i.getAddresses(),
                            i.getState(),
                            i.getLastUpdated()}
                    )
                    .collect(Collectors.toList());

    private static final BiFunction<FindInstancesRequest, List<FindInstancesResult>, Set<String>> INSTANCES_TASK_DATA_PROVIDER =
            (r, instances) -> instances.stream().map(FindInstancesResult::getId).collect(Collectors.toSet());

    @Autowired
    private InstancesRepository instancesRepository;

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private ProjectsRepository projectsRepository;

    @Autowired
    private RequestProvider requestProvider;

    @Autowired
    private ResizeInstancesWizard resizeInstancesWizard;

    @Autowired
    private RebuildInstancesWizard rebuildInstancesWizard;

    @Autowired
    private CommandExecutor commandExecutor;

    @Autowired
    private OperationSupportChecker operationSupportChecker;

    @CliCommand(value = "reboot", help = "Reboot instances")
    public void rebootInstances(
            @CliOption(key = "", mandatory = true, help = NAMES_HELP) String names,
            @CliOption(key = "hard", unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether to hard reboot instance") boolean hard
    ) {
        if (hard) {
            hardRebootInstances(names);
        } else {
            executeSimpleModificationCommand(
                    names,
                    size -> String.format("Going to reboot %d instances.", size),
                    instancesRepository::rebootInstances,
                    REBOOT_INSTANCE
            );
        }
    }

    @CliCommand(value = "hard-reboot", help = "Hard reboot instances")
    public void hardRebootInstances(
            @CliOption(key = "", mandatory = true, help = NAMES_HELP) String names
    ) {
        executeSimpleModificationCommand(
                names,
                size -> String.format("Going to hard reboot %d instances.", size),
                instancesRepository::hardRebootInstances,
                HARD_REBOOT_INSTANCE
        );
    }

    @CliCommand(value = "start", help = "Start previously stopped instances (use \"add instances\" to launch new ones)")
    public void startInstances(
            @CliOption(key = "", mandatory = true, help = NAMES_HELP) String names
    ) {
        executeSimpleModificationCommand(
                names,
                size -> String.format("Going to start %d instances.", size),
                instancesRepository::startInstances,
                START_INSTANCE
        );
    }

    @CliCommand(value = "shutdown", help = "Shutdown instances")
    public void shutdownInstances(
            @CliOption(key = "", mandatory = true, help = NAMES_HELP) String names
    ) {
        executeSimpleModificationCommand(
                names,
                size -> String.format("Going to shutdown %d instances.", size),
                instancesRepository::shutdownInstances,
                SHUTDOWN_INSTANCE
        );
    }

    @CliCommand(value = "pause", help = "Pause instances")
    public void pauseInstances(
            @CliOption(key = "", mandatory = true, help = NAMES_HELP) String names
    ) {
        executeSimpleModificationCommand(
                names,
                size -> String.format("Going to pause %d instances.", size),
                instancesRepository::pauseInstances,
                PAUSE_INSTANCE
        );
    }

    @CliCommand(value = "suspend", help = "Suspend instances")
    public void suspendInstances(
            @CliOption(key = "", mandatory = true, help = NAMES_HELP) String names
    ) {
        executeSimpleModificationCommand(
                names,
                size -> String.format("Going to suspend %d instances.", size),
                instancesRepository::suspendInstances,
                SUSPEND_INSTANCE
        );
    }

    @CliCommand(value = "resume", help = "Resume instances")
    public void resumeInstances(
            @CliOption(key = "", mandatory = true, help = NAMES_HELP) String names
    ) {
        executeSimpleModificationCommand(
                names,
                size -> String.format("Going to resume %d instances.", size),
                instancesRepository::resumeInstances,
                RESUME_INSTANCE
        );
    }

    @CliCommand(value = "resize", help = "Resize instances")
    public void resizeInstances(
            @CliOption(key = "project", help = "Name of the project to resize instances in") String projectName,
            @CliOption(key = "instances", help = NAMES_HELP) String instanceNames,
            @CliOption(key = "flavor", help = "Flavor name") String flavorName
    ) {
        if (projectName != null && instanceNames != null && flavorName != null) {
            FindInstancesRequest findInstancesRequest = requestProvider.get(FindInstancesRequest.class)
                    .withProjects(projectName)
                    .withNames(instanceNames);
            FindFlavorsRequest findFlavorsRequest = requestProvider.get(FindFlavorsRequest.class)
                    .withProjects(projectName)
                    .withNames(flavorName);
            validateConfirmExecuteShowStatus(
                    findFlavorsRequest,
                    request -> {
                        List<FindFlavorsResult> flavors = projectsRepository.findFlavors(request);
                        List<FindInstancesResult> instances = instancesRepository.findInstances(findInstancesRequest);
                        return flavors.size() > 0 ?
                                new ResizingInstances(flavors.get(0), instances) :
                                new ResizingInstances();
                    },
                    resizingInstances -> String.format("Going to resize %d instances.", resizingInstances.getInstances().size()),
                    resizingInstances -> new String[]{"Name", "Project", "Current flavor", "New flavor"},
                    resizingInstances -> resizingInstances.getInstances().stream()
                            .map(ri -> new String[]{
                                    ri.getName(),
                                    ri.getProjectName(),
                                    ri.getFlavorName(),
                                    resizingInstances.getFlavor().getName()
                            })
                            .collect(Collectors.toList()),
                    (r, resizingInstances) -> new TaskData(
                            resizingInstances.getFlavor().getId(),
                            resizingInstances.getInstances().stream().map(FindInstancesResult::getId).collect(Collectors.toSet())
                    ),
                    taskData -> instancesRepository.resizeInstances(taskData.getNewId(), taskData.getIds())

            );

        } else if (resizeInstancesWizard.runSteps()) {
            commandExecutor.execute(resizeInstancesWizard.getCommand());
        }
    }

    @CliCommand(value = "rebuild", help = "Rebuild instances")
    public void rebuildInstances(
            @CliOption(key = "project", help = "Name of the project to rebuild instances in") String projectName,
            @CliOption(key = "instances", help = NAMES_HELP) String instanceNames,
            @CliOption(key = "image", help = "Image name") String imageName
    ) {
        if (projectName != null && instanceNames != null && imageName != null) {
            FindInstancesRequest findInstancesRequest = requestProvider.get(FindInstancesRequest.class)
                    .withProjects(projectName)
                    .withNames(instanceNames);
            FindImagesRequest findImagesRequest = requestProvider.get(FindImagesRequest.class)
                    .withProjects(projectName)
                    .withNames(imageName);
            validateConfirmExecuteShowStatus(
                    findImagesRequest,
                    request -> {
                        List<FindImagesResult> images = imagesRepository.findImages(findImagesRequest);
                        List<FindInstancesResult> instances = instancesRepository.findInstances(findInstancesRequest);
                        return images.size() > 0 ?
                                new RebuildingInstances(images.get(0), instances) :
                                new RebuildingInstances();
                    },
                    rebuildingInstances -> String.format("Going to rebuild %d instances.", rebuildingInstances.getInstances().size()),
                    rebuildingInstances -> new String[]{"Name", "Project", "Current image", "New image"},
                    rebuildingInstances -> rebuildingInstances.getInstances().stream()
                            .map(ri -> new String[]{
                                    ri.getName(),
                                    ri.getProjectName(),
                                    ri.getImageName(),
                                    rebuildingInstances.getImage().getName()
                            })
                            .collect(Collectors.toList()),
                    (r, rebuildingInstances) -> new TaskData(
                            rebuildingInstances.getImage().getId(),
                            rebuildingInstances.getInstances().stream().map(FindInstancesResult::getId).collect(Collectors.toSet())
                    ),
                    taskData -> instancesRepository.rebuildInstances(taskData.getNewId(), taskData.getIds())

            );
        } else if (rebuildInstancesWizard.runSteps()) {
            commandExecutor.execute(rebuildInstancesWizard.getCommand());
        }
    }

    private void executeSimpleModificationCommand(
            String names,
            Function<Integer, String> confirmationMessageProvider,
            Function<Set<String>, Set<String>> action,
            OperationType operationType
    ) {
        FindInstancesRequest findInstancesRequest = requestProvider.get(FindInstancesRequest.class).withNames(names);
        validateConfirmExecuteShowStatus(
                findInstancesRequest,
                r -> findAndFilterInstances(r, operationType),
                instances -> confirmationMessageProvider.apply(instances.size()),
                INSTANCE_CONFIRMATION_COLUMNS_PROVIDER,
                INSTANCE_CONFIRMATION_ROWS_PROVIDER,
                INSTANCES_TASK_DATA_PROVIDER,
                action
        );
    }

    private List<FindInstancesResult> findAndFilterInstances(FindInstancesRequest r, OperationType operationType) {
        Set<FindInstancesResult> instances = new HashSet<>(instancesRepository.findInstances(r));
        Collection<FindInstancesResult> matchedInstances = operationSupportChecker.filter(
                instances,
                FindInstancesResult::getCloudType,
                operationType
        );
        instances.removeAll(matchedInstances);
        if (instances.size() > 0) {
            String instanceNames = TextUtils.enumerateValues(
                    instances.stream()
                            .map(FindInstancesResult::getName)
                            .collect(Collectors.toList())
            ); //We may probably want to limit total count of shown instance names
            warn(String.format(
                    "This operation is not supported for the following %d matched instances: %s",
                    instances.size(),
                    instanceNames
            ));
        }
        return new ArrayList<>(matchedInstances);
    }

    private static class RebuildingInstances {

        private final List<FindInstancesResult> instances = new ArrayList<>();

        private final FindImagesResult image;

        private RebuildingInstances(FindImagesResult image, List<FindInstancesResult> instances) {
            this.instances.addAll(instances);
            this.image = image;
        }

        RebuildingInstances() {
            this(null, Collections.emptyList());
        }

        public List<FindInstancesResult> getInstances() {
            return instances;
        }

        public FindImagesResult getImage() {
            Assert.isTrue(image != null, "Image for rebuild operation can't be null");
            return image;
        }
    }

    private static class ResizingInstances {
        private final List<FindInstancesResult> instances = new ArrayList<>();
        private final FindFlavorsResult flavor;

        private ResizingInstances(FindFlavorsResult flavor, List<FindInstancesResult> instances) {
            this.instances.addAll(instances);
            this.flavor = flavor;
        }

        ResizingInstances() {
            this(null, Collections.emptyList());
        }

        public List<FindInstancesResult> getInstances() {
            return instances;
        }

        public FindFlavorsResult getFlavor() {
            Assert.isTrue(flavor != null, "Flavor for resize operation can't be null");
            return flavor;
        }
    }

    private static class TaskData {
        private final String newId;
        private final Collection<String> ids;

        TaskData(String newId, Collection<String> ids) {
            this.newId = newId;
            this.ids = ids;
        }

        Collection<String> getIds() {
            return ids;
        }

        String getNewId() {
            return newId;
        }
    }

}
