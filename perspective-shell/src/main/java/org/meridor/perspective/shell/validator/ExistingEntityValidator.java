package org.meridor.perspective.shell.validator;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.shell.query.*;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.validator.annotation.ExistingEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

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
    
    private Collection<Project> getProjectsByName(String name) {
        return projectsRepository.showProjects(queryProvider.get(ShowProjectsQuery.class).withNames(name));
    }
    
    private Collection<Keypair> getKeypairsByName(String name) {
        return projectsRepository
                .showProjects(queryProvider.get(ShowProjectsQuery.class).withNames(projectName))
                .stream()
                .flatMap(p -> p.getKeypairs().stream())
                .filter(k -> k.getName().equals(name))
                .collect(Collectors.toList());
    }
    
    private Collection<Flavor> getFlavorsByName(String name) {
        return projectsRepository
                .showFlavors(projectName, null, queryProvider.get(ShowFlavorsQuery.class).withNames(name))
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
    
    private Collection<Network> getNetworksByName(String name) {
        return projectsRepository.showNetworks(projectName, null, queryProvider.get(ShowNetworksQuery.class).withNames(name))
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
    
    private Collection<Image> getImagesByName(String name) {
        return imagesRepository.showImages(
                queryProvider.get(ShowImagesQuery.class)
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
