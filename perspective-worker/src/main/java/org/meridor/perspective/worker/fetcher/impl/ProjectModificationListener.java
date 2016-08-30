package org.meridor.perspective.worker.fetcher.impl;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;

@Component
public class ProjectModificationListener extends LastModificationListener<Project> {
    
    @Autowired
    private ProjectsAware projectsAware;
    
    @Value("${perspective.fetch.delay.projects}")
    private int projectsFetchDelay;
    
    @PostConstruct
    public void init() {
        projectsAware.addProjectListener(this);
    }

    @Override
    protected int getLongTimeAgoLimit() {
        return SchedulerUtils.delayToLimit(projectsFetchDelay);
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
