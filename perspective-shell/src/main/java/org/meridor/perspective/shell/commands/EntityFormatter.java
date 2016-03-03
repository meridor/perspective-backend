package org.meridor.perspective.shell.commands;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Network;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.request.QueryProvider;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.meridor.perspective.shell.repository.impl.TextUtils.*;

@Component
public class EntityFormatter {

    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private QueryProvider queryProvider;

    private Map<String, Project> getProjects(String cloud) {
        return Collections.emptyMap();
//        return projectsRepository.findProjects(
//                queryProvider.get(ShowProjectsRequest.class)
//                        .withClouds(cloud)
//        ).stream().collect(Collectors.toMap(Project::getId, Function.identity()));
    }
    
    private Map<String, Project> getProjects() {
        return getProjects(null);
    }

    public List<String[]> formatNewInstances(List<Instance> instances) {
        Map<String, Project> projectsMap = getProjects();
        return instances.stream()
                .map(i -> new String[]{
                        i.getName(),
                        projectsMap.containsKey(i.getProjectId()) ?
                                projectsMap.get(i.getProjectId()).getName() : DASH,
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
        Map<String, Project> projectsMap = getProjects();
        return images.stream().map(i -> {
            Optional<String> realProjectId = projectsMap.keySet().stream()
                    .filter(id -> i.getProjectIds().contains(id))
                    .findFirst();
            return new String[]{
                    i.getName(),
                    //TODO: insert instance name
                     realProjectId.isPresent() ?
                            projectsMap.get(realProjectId.get()).getName() : DASH
            };
        }).collect(Collectors.toList());
    }
    
}
