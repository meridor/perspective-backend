package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.backend.storage.ProjectsAware;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.worker.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;

import static org.meridor.perspective.worker.fetcher.impl.SchedulerUtils.delayToLimit;

@Component
public class ProjectModificationListener extends LastModificationListener<Project> {

    private final ProjectsAware projectsAware;

    private final Config config;
    
    @Autowired
    public ProjectModificationListener(ProjectsAware projectsAware, Config config) {
        this.projectsAware = projectsAware;
        this.config = config;
    }

    @PostConstruct
    public void init() {
        showInfo();
        projectsAware.addProjectListener(this);
    }

    @Override
    protected int getLongTimeAgoLimit() {
        return delayToLimit(config.getProjectsFetchDelay());
    }

    @Override
    protected String getId(Project project) {
        return project.getId();
    }

    @Override
    protected String getCloudId(Project project) {
        return project.getCloudId();
    }

    @Override
    protected Instant getLastModifiedInstant(Project project) {
        return project.getTimestamp().toInstant();
    }
}
