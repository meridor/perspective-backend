package org.meridor.perspective.shell.query;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.shell.repository.ProjectsRepository;
import org.meridor.perspective.shell.validator.annotation.Filter;
import org.meridor.perspective.shell.validator.annotation.SupportedCloud;
import org.meridor.perspective.shell.validator.annotation.SupportedInstanceState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.meridor.perspective.shell.repository.impl.TextUtils.oneOfMatches;
import static org.meridor.perspective.shell.repository.impl.TextUtils.parseEnumeration;
import static org.meridor.perspective.shell.validator.Field.*;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ShowInstancesQuery implements Query<Predicate<Instance>> {

    private Set<String> ids;

    @Filter(INSTANCE_NAMES)
    private Set<String> names;
    
    @Filter(FLAVOR_NAMES)
    private Set<String> flavors;
    
    @Filter(IMAGE_NAMES)
    private Set<String> images;
    
    @SupportedInstanceState
    @Filter(INSTANCE_STATES)
    private Set<String> states;
    
    @SupportedCloud
    @Filter(CLOUDS)
    private Set<String> clouds;

    @Filter(PROJECTS)
    private String projects;

    @Autowired
    private ProjectsRepository projectsRepository;

    @Autowired
    private QueryProvider queryProvider;

    public ShowInstancesQuery withIds(String ids) {
        this.ids = parseEnumeration(ids);
        return this;
    }
    
    public ShowInstancesQuery withNames(String names) {
        this.names = parseEnumeration(names);
        return this;
    }
    
    public ShowInstancesQuery withClouds(String clouds) {
        this.clouds = parseEnumeration(clouds);
        return this;
    }
    
    public ShowInstancesQuery withProjectNames(String projects) {
        this.projects = projects;
        return this;
    }
    
    public ShowInstancesQuery withImages(String images) {
        this.images = parseEnumeration(images);
        return this;
    }
    
    public ShowInstancesQuery withFlavors(String flavors) {
        this.flavors = parseEnumeration(flavors);
        return this;
    }
    
    public ShowInstancesQuery withStates(String states) {
        this.states = parseEnumeration(states);
        return this;
    }

    @Override
    public Predicate<Instance> getPayload() {
        return getInstancePredicate(
                Optional.ofNullable(ids),
                Optional.ofNullable(names),
                Optional.ofNullable(flavors),
                Optional.ofNullable(images),
                Optional.ofNullable(states),
                Optional.ofNullable(clouds),
                Optional.ofNullable(projects)
        );
    }

    private Predicate<Instance> getInstancePredicate(
            Optional<Set<String>> ids,
            Optional<Set<String>> names,
            Optional<Set<String>> flavors,
            Optional<Set<String>> images,
            Optional<Set<String>> states,
            Optional<Set<String>> clouds,
            Optional<String> projects
    ) {
        return instance ->
                ( !ids.isPresent() || idMatches(instance, ids.get()) ) &&
                ( !names.isPresent() || nameMatches(instance, names.get()) ) &&
                ( !flavors.isPresent() || flavorMatches(instance, flavors.get()) ) &&
                ( !images.isPresent() || imageMatches(instance, images.get()) ) &&
                ( !states.isPresent() || stateMatches(instance, states.get()) ) &&
                ( !clouds.isPresent() || cloudMatches(instance, clouds.get()) ) &&
                ( !projects.isPresent() || projectMatches(projects.get(), instance.getProjectId()));
    }

    private static boolean idMatches(Instance instance, Set<String> ids) {
        return oneOfMatches(instance.getId(), ids);
    }
    
    private static boolean nameMatches(Instance instance, Set<String> names) {
        return oneOfMatches(instance.getName(), names);
    }
    
    private static boolean flavorMatches(Instance instance, Set<String> flavors) {
        return instance.getFlavor() != null && oneOfMatches(instance.getFlavor().getName(), flavors);
    }
    
    private static boolean imageMatches(Instance instance, Set<String> images) {
        return instance.getImage() != null && oneOfMatches(instance.getImage().getName(), images);
    }
    
    private static boolean stateMatches(Instance instance, Set<String> states) {
        return oneOfMatches(instance.getState().value().toLowerCase(), states);
    }
    
    private static boolean cloudMatches(Instance instance, Set<String> clouds) {
        return oneOfMatches(instance.getCloudType().value().toLowerCase(), clouds);
    }
    
    private boolean projectMatches(String projects, String projectIdFromInstance) {
        return projectsRepository
                .showProjects(queryProvider.get(ShowProjectsQuery.class).withNames(projects))
                .stream().filter(
                        p -> p.getId().equals(projectIdFromInstance)
                ).count() > 0;
    }


}
