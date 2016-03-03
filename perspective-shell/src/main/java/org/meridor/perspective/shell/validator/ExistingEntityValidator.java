package org.meridor.perspective.shell.validator;

import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.request.*;
import org.meridor.perspective.shell.result.*;
import org.meridor.perspective.shell.validator.annotation.ExistingEntity;
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
    private QueryProvider queryProvider;
    
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
        return projectsRepository.findProjects(queryProvider.get(FindProjectsRequest.class).withNames(name));
    }
    
    private List<FindKeypairsResult> getKeypairsByName(String name) {
        return projectsRepository
                .findKeypairs(
                        queryProvider.get(FindKeypairsRequest.class)
                                .withNames(name)
                                .withProjects(projectName)
                );
    }
    
    private Collection<FindFlavorsResult> getFlavorsByName(String name) {
        return projectsRepository
                .findFlavors(
                        queryProvider.get(FindFlavorsRequest.class)
                        .withNames(name)
                        .withProjects(projectName)
                );
    }
    
    private Collection<FindNetworksResult> getNetworksByName(String name) {
        return projectsRepository.findNetworks(
                queryProvider.get(FindNetworksRequest.class)
                        .withNames(name)
                        .withProjects(projectName)
        );
    }
    
    private Collection<FindImagesResult> getImagesByName(String name) {
        return imagesRepository.findImages(
                queryProvider.get(FindImagesRequest.class)
                    .withNames(name)
                    .withProjectNames(projectName)
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
