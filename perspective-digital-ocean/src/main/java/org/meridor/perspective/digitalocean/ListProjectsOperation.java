package org.meridor.perspective.digitalocean;

import com.myjeeva.digitalocean.pojo.Key;
import com.myjeeva.digitalocean.pojo.Size;
import org.meridor.perspective.beans.*;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;
import static org.meridor.perspective.events.EventFactory.now;
import static org.meridor.perspective.worker.misc.impl.ValueUtils.getProjectName;

@Component
public class ListProjectsOperation implements SupplyingOperation<Project> {

    private static final Logger LOG = LoggerFactory.getLogger(ListProjectsOperation.class);

    private final ApiProvider apiProvider;

    private final IdGenerator idGenerator;

    private final ProjectsAware projectsAware;

    @Autowired
    public ListProjectsOperation(IdGenerator idGenerator, ApiProvider apiProvider, ProjectsAware projectsAware) {
        this.idGenerator = idGenerator;
        this.apiProvider = apiProvider;
        this.projectsAware = projectsAware;
    }

    @Override
    public boolean perform(Cloud cloud, Consumer<Project> consumer) {
        try {
            apiProvider.forEachRegion(cloud, (region, api) -> {
                try {
                    Project project = processProject(cloud, region, api);
                    LOG.info("Fetched project {} for cloud = {}, region = {}", project.getName(), cloud.getName());
                    consumer.accept(project);
                } catch (Exception e) {
                    LOG.error(String.format("Failed to fetch project for cloud = %s", cloud.getName()), e);
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
            Map<String, Project> fetchMap = getFetchMap(ids);
            apiProvider.forEachRegion(cloud, (region, api) -> {
                try {
                    if (fetchMap.containsKey(region)) {
                        Project project = processProject(cloud, region, api);
                        consumer.accept(project);
                        LOG.info("Fetched project {} for cloud = {}, region = {}", project.getName(), cloud.getName(), region);
                    }
                } catch (Exception e) {
                    LOG.info("Failed to project for cloud = {}, region = {}", cloud.getName(), region);
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

    private Map<String, Project> getFetchMap(Set<String> ids) {
        Map<String, Project> ret = new HashMap<>();
        ids.forEach(id -> {
            Optional<Project> projectCandidate = projectsAware.getProject(id);
            if (projectCandidate.isPresent()) {
                Project project = projectCandidate.get();
                String region = project.getMetadata().get(MetadataKey.REGION);
                ret.put(region, project);
            }
        });
        return ret;
    }

    private Project processProject(Cloud cloud, String region, Api api) throws Exception {
        Project project = createProject(cloud, region);
        addFlavors(project, api);
        addKeyPairs(project, api);
        return project;
    }

    private Project createProject(Cloud cloud, String region) {
        String projectId = idGenerator.getProjectId(cloud);
        Project project = new Project();
        project.setId(projectId);
        project.setName(getProjectName(cloud, region));
        project.setTimestamp(now().minusHours(1));

        MetadataMap metadata = new MetadataMap();
        metadata.put(MetadataKey.REGION, region);
        project.setMetadata(metadata);
        return project;
    }

    private void addFlavors(Project project, Api api) throws Exception {
        for (Size size : api.listSizes()) {
            Flavor flavor = new Flavor();
            flavor.setId(size.getSlug());
            flavor.setVcpus(size.getVirutalCpuCount());
            flavor.setRam(size.getMemorySizeInMb());
            flavor.setRootDisk(size.getDiskSize());
            flavor.setEphemeralDisk(0);
            flavor.setHasSwap(false);
            flavor.setIsPublic(true);
            String flavorName = String.format(
                    "c%d-m%d-d%d-n%d-p%s",
                    size.getVirutalCpuCount(),
                    size.getMemorySizeInMb(),
                    size.getDiskSize(),
                    size.getTransfer(),
                    String.valueOf(size.getPriceMonthly())
            );
            flavor.setName(flavorName);
            project.getFlavors().add(flavor);
        }
    }
    
    private void addKeyPairs(Project project, Api api) throws Exception {
        for (Key key : api.listKeys()) {
            Keypair keypair = new Keypair();
            keypair.setName(key.getName());
            keypair.setPublicKey(key.getPublicKey());
            keypair.setFingerprint(key.getFingerprint());
            project.getKeypairs().add(keypair);
        }
    }

}
