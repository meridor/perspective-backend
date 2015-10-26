package org.meridor.perspective.shell.validator;

import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Network;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.shell.query.ShowFlavorsQuery;
import org.meridor.perspective.shell.query.ShowImagesQuery;
import org.meridor.perspective.shell.query.ShowNetworksQuery;
import org.meridor.perspective.shell.query.ShowProjectsQuery;
import org.meridor.perspective.shell.repository.ImagesRepository;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.validator.annotation.ExistingEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

@Component
public class ExistingEntityValidator implements Validator {
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private ImagesRepository imagesRepository;
    
    @Override
    public boolean validate(Object instance, Annotation annotation, Object value) {
        if (value == null) {
            return true;
        }
        ExistingEntity ann = ExistingEntity.class.cast(annotation);
        String entityId = value.toString();
        int maxCount = ann.maxCount();
        int minCount = ann.minCount();
        Collection<?> entities = getEntitiesList(ann.value(), entityId);
        return entities.size() >= minCount && entities.size() <= maxCount;
    }
    
    private Collection<Project> getProjectsById(String id) {
        return projectsRepository.showProjects(new ShowProjectsQuery(id));
    }
    
    private Collection<Flavor> getFlavorsById(String id) {
        return projectsRepository.showFlavors(null, null, new ShowFlavorsQuery(id));
    }
    
    private Collection<Network> getNetworksById(String id) {
        return projectsRepository.showNetworks(null, null, new ShowNetworksQuery(id));
    }
    
    private Collection<Image> getImagesById(String id) {
        return imagesRepository.showImages(new ShowImagesQuery(null, id, null));
    }
    
    private Collection<?> getEntitiesList(Entity entity, String entityId) {
        switch (entity) {
            case PROJECT: return getProjectsById(entityId);
            case FLAVOR: return getFlavorsById(entityId);
            case NETWORK: return getNetworksById(entityId);
            case IMAGE: return getImagesById(entityId);
        }
        return Collections.emptyList();
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
            return String.format("At least %d %s with ID or name = %s should exist", minCount, entity, entityId);
        }
        return String.format("No more than %d %s with ID or name = %s should exist", maxCount, entity, entityId);
    }
}
