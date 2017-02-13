package org.meridor.perspective.worker.operation;

import org.meridor.perspective.backend.storage.ImagesAware;
import org.meridor.perspective.backend.storage.InstancesAware;
import org.meridor.perspective.backend.storage.ProjectsAware;
import org.meridor.perspective.beans.*;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.BiFunction;

import static org.meridor.perspective.events.EventFactory.now;
import static org.meridor.perspective.worker.misc.impl.ValueUtils.getProjectName;

@Component
public final class OperationUtils {

    private final ProjectsAware projectsAware;

    private final InstancesAware instancesAware;

    private final ImagesAware imagesAware;

    private final IdGenerator idGenerator;

    @Autowired
    public OperationUtils(ProjectsAware projectsAware, InstancesAware instancesAware, ImagesAware imagesAware, IdGenerator idGenerator) {
        this.projectsAware = projectsAware;
        this.instancesAware = instancesAware;
        this.imagesAware = imagesAware;
        this.idGenerator = idGenerator;
    }

    public Map<String, Project> getProjectsFetchMap(Set<String> ids) {
        Map<String, Project> ret = new HashMap<>();
        ids.forEach(id -> {
            Optional<Project> projectCandidate = projectsAware.getProject(id);
            if (projectCandidate.isPresent()) {
                Project project = projectCandidate.get();
                String region = project.getMetadata().get(MetadataKey.REGION);
                ret.put(region, project);
            }
        });
        return ret;
    }

    public Map<String, Set<String>> getImagesFetchMap(Set<String> ids) {
        Map<String, Set<String>> ret = new HashMap<>();
        ids.forEach(id -> {
            Optional<Image> imageCandidate = imagesAware.getImage(id);
            if (imageCandidate.isPresent()) {
                Image image = imageCandidate.get();
                String realId = image.getRealId();
                if (realId != null) {
                    String region = image.getMetadata().get(MetadataKey.REGION);
                    ret.putIfAbsent(region, new HashSet<>());
                    ret.get(region).add(realId);
                }
            }
        });
        return ret;
    }

    public Map<String, Set<String>> getInstancesFetchMap(Set<String> ids) {
        Map<String, Set<String>> ret = new HashMap<>();
        ids.forEach(id -> {
            Optional<Instance> instanceCandidate = instancesAware.getInstance(id);
            if (instanceCandidate.isPresent()) {
                Instance instance = instanceCandidate.get();
                String realId = instance.getRealId();
                if (realId != null) {
                    String region = instance.getMetadata().get(MetadataKey.REGION);
                    ret.putIfAbsent(region, new HashSet<>());
                    ret.get(region).add(realId);
                }
            }
        });
        return ret;
    }

    public Project createProject(Cloud cloud, String region) {
        String projectId = idGenerator.getProjectId(cloud, region);
        Project project = new Project();
        project.setId(projectId);
        project.setName(getProjectName(cloud, region));
        project.setTimestamp(now().minusHours(1));

        MetadataMap metadata = new MetadataMap();
        metadata.put(MetadataKey.REGION, region);

        project.setMetadata(metadata);
        return project;
    }

    public <T> T fromProjectAndUserName(Cloud cloud, BiFunction<String, String, T> action) {
        final String DELIMITER = ":";
        String[] identity = cloud.getIdentity().split(DELIMITER);
        Assert.isTrue(identity.length == 2, "Identity should be in format project:username");
        String projectName = identity[0];
        String userName = identity[1];
        return action.apply(projectName, userName);
    }
    
}
