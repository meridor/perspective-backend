package org.meridor.perspective.shell.interactive.commands;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Network;
import org.meridor.perspective.shell.common.repository.InstancesRepository;
import org.meridor.perspective.shell.common.repository.ProjectsRepository;
import org.meridor.perspective.shell.common.request.FindInstancesRequest;
import org.meridor.perspective.shell.common.request.FindProjectsRequest;
import org.meridor.perspective.shell.common.request.RequestProvider;
import org.meridor.perspective.shell.common.result.FindInstancesResult;
import org.meridor.perspective.shell.common.result.FindProjectsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.common.repository.impl.TextUtils.*;

@Component
public class EntityFormatter {

    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private InstancesRepository instancesRepository;
    
    @Autowired
    private RequestProvider requestProvider;

    private Map<String, String> getProjects(Collection<String> projectIds) {
        FindProjectsRequest findProjectsRequest = requestProvider.get(FindProjectsRequest.class);
        if (projectIds != null && !projectIds.isEmpty()) {
            findProjectsRequest = findProjectsRequest.withIds(enumerateValues(projectIds));
        }
        return projectsRepository.findProjects(findProjectsRequest).stream()
                .collect(Collectors.toMap(
                    FindProjectsResult::getId,
                    FindProjectsResult::getName
                ));
    }
    
    private Map<String, String> getInstances(Collection<String> instanceIds) {
        FindInstancesRequest findInstancesRequest = requestProvider.get(FindInstancesRequest.class);
        if (instanceIds != null && !instanceIds.isEmpty()) {
            findInstancesRequest = findInstancesRequest.withIds(enumerateValues(instanceIds));
        }
        return instancesRepository.findInstances(findInstancesRequest).stream()
                .collect(Collectors.toMap(
                    FindInstancesResult::getId,
                    FindInstancesResult::getName
                ));
    }
    
    public List<String[]> formatNewInstances(List<Instance> instances) {
        Set<String> projectIds = instances.stream()
                .map(Instance::getProjectId)
                .collect(Collectors.toSet());
        Map<String, String> projectsMap = getProjects(projectIds);
        return instances.stream()
                .map(i -> new String[]{
                        i.getName(),
                        projectsMap.containsKey(i.getProjectId()) ?
                                projectsMap.get(i.getProjectId()) : DASH,
                        (i.getImage() != null) ? i.getImage().getName() : DASH,
                        (i.getFlavor() != null) ? i.getFlavor().getName() : DASH,
                        formatInstanceAdditionalProperties(i)
                }).collect(Collectors.toList());
    }

    private static String formatInstanceAdditionalProperties(Instance instance) {
        List<String> additionalProperties = new ArrayList<>();
        if (instance.getMetadata() != null) {
            additionalProperties.add(String.format("Metadata: %s", instance.getMetadata().toString()));
        }
        if (instance.getNetworks() != null) {
            additionalProperties.add(String.format("Networks: %s", enumerateValues(
                    instance.getNetworks().stream()
                            .map(Network::getName)
                            .collect(Collectors.toList()))
            ));
        }
        if (instance.getKeypair() != null) {
            additionalProperties.add(String.format("Keypair: %s", instance.getKeypair().getName()));
        }
        return additionalProperties.isEmpty() ? DASH : joinLines(additionalProperties);
    }


    public List<String[]> formatNewImages(List<Image> images) {
        Set<String> projectIds = images.stream()
                .flatMap(i -> i.getProjectIds().stream())
                .collect(Collectors.toSet());
        Map<String, String> projectsMap = getProjects(projectIds);

        Set<String> instanceIds = images.stream()
                .map(Image::getInstanceId)
                .collect(Collectors.toSet());
        Map<String, String> instancesMap = getInstances(instanceIds);
        return images.stream().map(i -> {
            Optional<String> projectId = projectsMap.keySet().stream()
                    .filter(id -> i.getProjectIds().contains(id))
                    .findFirst();
            String instanceId = i.getInstanceId();
            return new String[]{
                    i.getName(),
                    instancesMap.containsKey(instanceId) ?
                            instancesMap.get(instanceId) : DASH,
                     projectId.isPresent() ?
                            projectsMap.get(projectId.get()) : DASH
            };
        }).collect(Collectors.toList());
    }
    
}
