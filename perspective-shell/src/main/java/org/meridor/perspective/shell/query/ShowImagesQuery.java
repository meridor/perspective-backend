package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Image;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.validator.annotation.Filter;
import org.meridor.perspective.shell.validator.annotation.SupportedCloud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.validator.Field.CLOUDS;
import static org.meridor.perspective.shell.validator.Field.IMAGE_NAMES;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ShowImagesQuery implements Query<Predicate<Image>> {
    
    private Set<String> ids;
    
    @Filter(IMAGE_NAMES)
    private Set<String> names;
    
    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> clouds;

    private String projects;
    
    @Autowired
    private ProjectsRepository projectsRepository;
    
    @Autowired
    private QueryProvider queryProvider;

    public ShowImagesQuery withIds(String imageIds) {
        this.ids = parseEnumeration(imageIds);
        return this;
    }
    
    public ShowImagesQuery withNames(String names) {
        this.names = parseEnumeration(names);
        return this;
    }

    public ShowImagesQuery withCloudNames(String cloudNames) {
        this.clouds = parseEnumeration(cloudNames);
        return this;
    }
    
    public ShowImagesQuery withProjectNames(String projectNames) {
        this.projects = projectNames;
        return this;
    }

    @Override
    public Predicate<Image> getPayload() {
        return getImagePredicate(
                Optional.ofNullable(ids),
                Optional.ofNullable(names),
                Optional.ofNullable(clouds),
                Optional.ofNullable(projects)
        );
    }

    private Predicate<Image> getImagePredicate(
            Optional<Set<String>> ids,
            Optional<Set<String>> names,
            Optional<Set<String>> clouds,
            Optional<String> projects
            
    ) {
        return image -> 
                ( !ids.isPresent() || ids.get().contains(image.getId()) ) &&
                ( !names.isPresent() || names.get().contains(image.getName()) ) &&
                ( !clouds.isPresent() || clouds.get().contains(image.getCloudType().value().toLowerCase())) &&
                ( !projects.isPresent() || projectMatches(projects.get(), image.getProjectId()));
    }
    
    private boolean projectMatches(String projects, String projectIdFromImage) {
        return projectsRepository
                .showProjects(queryProvider.get(ShowProjectsQuery.class).withNames(projects))
                .stream().filter(
                        p -> 
                                p.getId().equals(projectIdFromImage) ||
                                p.getParentId() != null && p.getParentId().equals(projectIdFromImage)
                ).count() > 0;
    }

}
