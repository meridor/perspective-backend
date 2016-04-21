package org.meridor.perspective.shell.common.validator;

import org.meridor.perspective.shell.common.repository.ImagesRepository;
import org.meridor.perspective.shell.common.repository.ProjectsRepository;
import org.meridor.perspective.shell.common.request.*;
import org.meridor.perspective.shell.common.result.*;
import org.meridor.perspective.shell.common.validator.annotation.ExistingEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class ExistingEntityValidator implements Validator {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private RequestProvider requestProvider;
    
    private String projectName;

    @Override
    public boolean validate(Object instance, Annotation annotation, Object value) {
        if (value == null) {
            return true;
        }
        ExistingEntity ann = ExistingEntity.class.cast(annotation);
        String entityId = value.toString();
        int maxCount = ann.maxCount();
        int minCount = ann.minCount();
        this.projectName = getProjectName(instance, ann.projectField()); 
        Collection<?> entities = getEntitiesList(ann.value(), entityId);
        return entities.size() >= minCount && entities.size() <= maxCount;
    }
    
    private Collection<FindProjectsResult> getProjectsByName(String name) {
        return projectsRepository.findProjects(requestProvider.get(FindProjectsRequest.class).withNames(name));
    }
    
    private List<FindKeypairsResult> getKeypairsByName(String name) {
        return projectsRepository
                .findKeypairs(
                        requestProvider.get(FindKeypairsRequest.class)
                                .withNames(name)
                                .withProjects(projectName)
                );
    }
    
    private Collection<FindFlavorsResult> getFlavorsByName(String name) {
        return projectsRepository
                .findFlavors(
                        requestProvider.get(FindFlavorsRequest.class)
                        .withNames(name)
                        .withProjects(projectName)
                );
    }
    
    private Collection<FindNetworksResult> getNetworksByName(String name) {
        return projectsRepository.findNetworks(
                requestProvider.get(FindNetworksRequest.class)
                        .withNames(name)
                        .withProjects(projectName)
        );
    }
    
    private Collection<FindImagesResult> getImagesByName(String name) {
        return imagesRepository.findImages(
                requestProvider.get(FindImagesRequest.class)
                    .withNames(name)
                    .withProjects(projectName)
        );
    }
    
    private Collection<?> getEntitiesList(Entity entity, String entityId) {
        switch (entity) {
            case PROJECT: return getProjectsByName(entityId);
            case FLAVOR: return getFlavorsByName(entityId);
            case NETWORK: return getNetworksByName(entityId);
            case IMAGE: return getImagesByName(entityId);
            case KEYPAIR: return getKeypairsByName(entityId);
        }
        return Collections.emptyList();
    }
    
    private String getProjectName(Object instance, String projectFieldName) {
        if (projectFieldName.isEmpty()) {
            return null;
        }
        try {
            java.lang.reflect.Field projectField = instance.getClass().getDeclaredField(projectFieldName);
            projectField.setAccessible(true);
            String projectName = projectField.get(instance).toString();
            return (projectName == null || projectName.isEmpty()) ? null : projectName;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Class<? extends Annotation> getAnnotationClass() {
        return ExistingEntity.class;
    }

    @Override
    public String getMessage(Annotation annotation, String fieldName, Object value) {
        ExistingEntity ann = ExistingEntity.class.cast(annotation);
        String entityId = value.toString();
        int maxCount = ann.maxCount();
        int minCount = ann.minCount();
        Collection<?> entities = getEntitiesList(ann.value(), entityId);
        String entity = ann.value().name().toLowerCase();
        if (entities.size() < minCount) {
            return String.format("At least %d %s with name = %s should exist", minCount, entity, entityId);
        }
        return String.format("No more than %d %s with name = %s should exist", maxCount, entity, entityId);
    }
}
