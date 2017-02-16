package org.meridor.perspective.googlecloud;

import com.google.cloud.compute.MachineTypeId;
import com.google.cloud.compute.Region;
import org.meridor.perspective.beans.*;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.operation.AbstractListProjectsOperation;
import org.meridor.perspective.worker.operation.OperationUtils;
import org.meridor.perspective.worker.operation.RegionsAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.toIntExact;
import static org.meridor.perspective.googlecloud.IdUtils.*;
import static org.meridor.perspective.worker.misc.impl.ValueUtils.formatQuota;

@Component
public class ListProjectsOperation extends AbstractListProjectsOperation<Region, Api> {

    private static final Logger LOG = LoggerFactory.getLogger(ListProjectsOperation.class);

    private final ApiProvider apiProvider;

    private final OperationUtils operationUtils;

    @Autowired
    public ListProjectsOperation(ApiProvider apiProvider, OperationUtils operationUtils) {
        this.apiProvider = apiProvider;
        this.operationUtils = operationUtils;
    }

    @Override
    protected Project processProject(Cloud cloud, Region region, Api api) {
        Project project = operationUtils.createProject(cloud, region.getRegionId().getRegion());
        addFlavors(project, api);
        addNetworks(project, cloud, region, api);
        addKeyPairs(project, api);
        addAvailabilityZones(project, cloud, region);
        addQuota(project, region);
        return project;
    }

    @Override
    protected RegionsAware<Region, Api> getRegionsAware() {
        return apiProvider;
    }

    private void addFlavors(Project project, Api api) {
        api.listFlavors().forEach(mt -> {
            Flavor flavor = new Flavor();
            MachineTypeId machineTypeId = mt.getMachineTypeId();
            flavor.setId(machineTypeIdToString(machineTypeId));
            flavor.setName(machineTypeId.getType());
            flavor.setVcpus(mt.getCpus());
            flavor.setRam(mt.getMemoryMb());
            flavor.setRootDisk(toIntExact(mt.getMaximumPersistentDisksSizeGb()));
            flavor.setEphemeralDisk(0);
            flavor.setHasSwap(false);
            flavor.setIsPublic(true);
            project.getFlavors().add(flavor);
        });
    }

    private void addNetworks(Project project, Cloud cloud, Region region, Api api) {
        try {
            Map<String, Set<Subnet>> subnetworkMap = new HashMap<>();
            api.listSubnetworks().forEach(sn -> {
                String networkId = networkIdToString(sn.getNetwork());
                subnetworkMap.putIfAbsent(networkId, new HashSet<>());
                Subnet subnet = new Subnet();
                subnet.setId(subnetworkIdToString(sn.getSubnetworkId()));
                subnet.setName(sn.getDescription());
                subnet.setCidr(sn.getIpRange());
                subnet.setGateway(sn.getGatewayAddress());
            });

            api.listNetworks().stream()
                    .map(n -> {
                        String networkId = networkIdToString(n.getNetworkId());
                        Network network = new Network();
                        network.setId(networkId);
                        network.setName(n.getDescription());
                        network.setIsShared(true);
                        network.setState("ACTIVE");
                        network.getSubnets().addAll(
                                subnetworkMap.getOrDefault(networkId, Collections.emptySet())
                        );
                        return network;
                    })
                    .forEach(n -> project.getNetworks().add(n));
        } catch (Exception e) {
            LOG.debug("Failed to fetch networks information for cloud = {}, region = {}", cloud.getName(), getRegionsAware().getRegionName(region));
        }
    }

    private void addAvailabilityZones(Project project, Cloud cloud, Region region) {
        try {
            List<AvailabilityZone> availabilityZones = region.getZones().stream()
                    .map(z -> new AvailabilityZone() {
                        {
                            setName(z.getZone());
                        }
                    })
                    .collect(Collectors.toList());
            project.getAvailabilityZones().addAll(availabilityZones);
        } catch (Exception e) {
            LOG.debug("Failed to fetch availability zone information for cloud = {}, region = {}", cloud.getName(), region);
        }
    }

    private void addKeyPairs(Project project, Api api) {
        project.getKeypairs().addAll(api.listKeypairs());
    }

    private void addQuota(Project project, Region region) {
        Quota quota = new Quota();
        region.getQuotas().forEach(q -> {
            int usage = Double.valueOf(q.getUsage()).intValue();
            int limit = Double.valueOf(q.getLimit()).intValue();
            String metric = q.getMetric();
            String value = formatQuota(usage, limit);
            //TODO: save metric to quota fields!
        });
        project.setQuota(quota);
    }

}
