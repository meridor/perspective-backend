package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.misc.impl.ValueUtils;
import org.meridor.perspective.worker.operation.OperationUtils;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.openstack4j.model.compute.AbsoluteLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;

@Component
public class ListProjectsOperation implements SupplyingOperation<Project> {

    private static final Logger LOG = LoggerFactory.getLogger(ListProjectsOperation.class);

    private final ApiProvider apiProvider;

    private final OperationUtils operationUtils;

    @Autowired
    public ListProjectsOperation(ApiProvider apiProvider, OperationUtils operationUtils) {
        this.apiProvider = apiProvider;
        this.operationUtils = operationUtils;
    }

    @Override
    public boolean perform(Cloud cloud, Consumer<Project> consumer) {
        try {
            apiProvider.forEachComputeRegion(cloud, (region, api) -> {
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
            apiProvider.forEachComputeRegion(cloud, (region, api) -> {
                if (fetchMap.containsKey(region)) {
                    Project project = processProject(cloud, region, api);
                    consumer.accept(project);
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

    private Project processProject(Cloud cloud, String region, Api api) {
        Project project = operationUtils.createProject(cloud, region);
        addFlavors(project, api);
        addNetworks(project, api);
        addKeyPairs(project, api);
        addAvailabilityZones(project, cloud, region, api);
        addQuota(project, cloud, region, api);
        return project;
    }

    private void addFlavors(Project project, Api api) {
        for (org.openstack4j.model.compute.Flavor flavor : api.listFlavors()) {
            Flavor flavorToAdd = new org.meridor.perspective.beans.Flavor();
            flavorToAdd.setId(flavor.getId());
            flavorToAdd.setName(flavor.getName());
            flavorToAdd.setVcpus(flavor.getVcpus());
            flavorToAdd.setRam(flavor.getRam());
            flavorToAdd.setRootDisk(flavor.getDisk());
            flavorToAdd.setEphemeralDisk(flavor.getEphemeral());
            flavorToAdd.setHasSwap(flavor.getSwap() > 0); //Probably need to change to number instead
            flavorToAdd.setIsPublic(true);
            project.getFlavors().add(flavorToAdd);
        }
    }

    private void addNetworks(Project project, Api api) {
        for (org.openstack4j.model.network.Network network : api.listNetworks()) {
            Network networkToAdd = new org.meridor.perspective.beans.Network();
            networkToAdd.setId(network.getId());
            networkToAdd.setName(network.getName());
            networkToAdd.setIsShared(network.isShared());
            networkToAdd.setState(network.getStatus().name());
            for (org.openstack4j.model.network.Subnet subnet : network.getNeutronSubnets()) {
                networkToAdd.getSubnets().add(processSubnet(subnet));
            }
            project.getNetworks().add(networkToAdd);
        }
    }

    private Subnet processSubnet(org.openstack4j.model.network.Subnet subnet) {
        Subnet ret = new Subnet();
        ret.setId(subnet.getId());
        ret.setCidr(subnet.getCidr());
        ret.setGateway(subnet.getGateway());
        ret.setIsDHCPEnabled(subnet.isDHCPEnabled());
        ret.setName(subnet.getName());
        int ipVersion = subnet.getIpVersion() != null ? subnet.getIpVersion().getVersion() : 4;
        ret.setProtocolVersion(ipVersion);
        return ret;
    }

    private void addAvailabilityZones(Project project, Cloud cloud, String region, Api api) {
        try {
            for (org.openstack4j.model.compute.ext.AvailabilityZone az : api.listAvailabilityZones()) {
                AvailabilityZone availabilityZone = new AvailabilityZone();
                availabilityZone.setName(az.getZoneName());
                project.getAvailabilityZones().add(availabilityZone);
            }
        } catch (Exception e) {
            LOG.debug("Failed to fetch availability zone information for cloud = {}, region = {}", cloud.getName(), region);
        }
    }

    private void addKeyPairs(Project project, Api api) {
        for (org.openstack4j.model.compute.Keypair keyPair : api.listKeypairs()) {
            Keypair keypair = new Keypair();
            keypair.setName(keyPair.getName());
            keypair.setFingerprint(keyPair.getFingerprint());
            keypair.setPublicKey(keyPair.getPublicKey());
            project.getKeypairs().add(keypair);
        }
    }

    private void addQuota(Project project, Cloud cloud, String region, Api api) {
        Quota quota = new Quota();
        try {
            AbsoluteLimit limits = api.getQuota();
            quota.setInstances(ValueUtils.formatQuota(limits.getTotalInstancesUsed(), limits.getMaxTotalInstances()));
            quota.setVcpus(ValueUtils.formatQuota(limits.getTotalCoresUsed(), limits.getMaxTotalCores()));
            quota.setRam(ValueUtils.formatQuota(limits.getTotalRAMUsed(), limits.getMaxTotalRAMSize()));
            quota.setIps(ValueUtils.formatQuota(limits.getTotalFloatingIpsUsed(), limits.getMaxTotalFloatingIps()));
            quota.setSecurityGroups(ValueUtils.formatQuota(limits.getTotalSecurityGroupsUsed(), limits.getMaxSecurityGroups()));
            quota.setVolumes(ValueUtils.formatQuota(limits.getMaxTotalVolumes(), limits.getMaxTotalVolumeGigabytes()));
            quota.setKeypairs(ValueUtils.formatQuota(limits.getTotalKeyPairsUsed(), limits.getMaxTotalKeypairs()));
        } catch (Exception e) {
            LOG.debug("Failed to fetch quota information for cloud = {}, region = {}", cloud.getName(), region);
        }
        project.setQuota(quota);
    }

}
