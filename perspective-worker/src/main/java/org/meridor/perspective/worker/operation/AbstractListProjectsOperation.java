package org.meridor.perspective.worker.operation;

import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;

@Component
public abstract class AbstractListProjectsOperation<R, A> implements SupplyingOperation<Project> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractListProjectsOperation.class);

    @Autowired
    private OperationUtils operationUtils;

    @Override
    public boolean perform(Cloud cloud, Consumer<Project> consumer) {
        try {
            getRegionsAware().forEachRegion(cloud, (region, api) -> {
                try {
                    Project project = processProject(cloud, region, api);
                    LOG.info("Fetched project {} for cloud = {}, region = {}", project.getName(), cloud.getName(), region);
                    consumer.accept(project);
                } catch (Exception e) {
                    LOG.error(String.format("Failed to fetch project for cloud = %s, region = %s", cloud.getName(), region), e);
                }
            });
            return true;
        } catch (Exception e) {
            LOG.error("Failed to fetch projects for cloud = " + cloud.getName(), e);
            return false;
        }
    }

    @Override
    public boolean perform(Cloud cloud, Set<String> ids, Consumer<Project> consumer) {
        try {
            Map<String, Project> fetchMap = operationUtils.getProjectsFetchMap(ids);
            getRegionsAware().forEachRegion(cloud, (region, api) -> {
                String regionName = getRegionsAware().getRegionName(region);
                if (fetchMap.containsKey(regionName)) {
                    Project project = processProject(cloud, region, api);
                    consumer.accept(project);
                    LOG.info("Fetched project {} for cloud = {}, region = {}", project.getName(), cloud.getName(), region);
                }
            });
            return true;
        } catch (Exception e) {
            LOG.error(String.format(
                    "Failed to fetch projects with ids = %s for cloud = %s",
                    ids,
                    cloud.getName()
            ), e);
            return false;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_PROJECTS};
    }

    protected abstract Project processProject(Cloud cloud, R region, A api);

    protected abstract RegionsAware<R, A> getRegionsAware();

}
