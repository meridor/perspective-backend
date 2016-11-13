package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.backend.storage.ProjectsAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;

import static org.meridor.perspective.worker.fetcher.impl.SchedulerUtils.delayToLimit;

@Component
public class ProjectModificationListener extends LastModificationListener<Project> {

    private final ProjectsAware projectsAware;
    
    @Value("${perspective.fetch.delay.projects}")
    private int projectsFetchDelay;
    
    @Autowired
    public ProjectModificationListener(ProjectsAware projectsAware) {
        this.projectsAware = projectsAware;
    }

    @PostConstruct
    public void init() {
        showInfo();
        projectsAware.addProjectListener(this);
    }

    @Override
    protected int getLongTimeAgoLimit() {
        return delayToLimit(projectsFetchDelay);
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
