package org.meridor.perspective.openstack;

import com.google.common.base.Optional;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.neutron.v2.features.NetworkApi;
import org.jclouds.openstack.neutron.v2.features.SubnetApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.Flavor;
import org.jclouds.openstack.nova.v2_0.domain.KeyPair;
import org.jclouds.openstack.nova.v2_0.extensions.AvailabilityZoneApi;
import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.meridor.perspective.beans.*;
import org.meridor.perspective.beans.AvailabilityZone;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.worker.misc.IdGenerator;
import org.meridor.perspective.worker.operation.SupplyingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.function.Consumer;

import static org.meridor.perspective.config.OperationType.LIST_PROJECTS;

@Component
public class ListProjectsOperation implements SupplyingOperation<Project> {

    private static final Logger LOG = LoggerFactory.getLogger(ListProjectsOperation.class);

    @Autowired
    private OpenstackApiProvider apiProvider;
    
    @Autowired
    private IdGenerator idGenerator;

    @Override
    public boolean perform(Cloud cloud, Consumer<Project> consumer) {
        try (NovaApi novaApi = apiProvider.getNovaApi(cloud); NeutronApi neutronApi = apiProvider.getNeutronApi(cloud)) {
            for (String region : novaApi.getConfiguredRegions()) {
                try {
                    Project project = createProject(cloud, region);

                    FlavorApi flavorApi = novaApi.getFlavorApi(region);
                    addFlavors(project, flavorApi);

                    NetworkApi networkApi = neutronApi.getNetworkApi(region);
                    SubnetApi subnetApi = neutronApi.getSubnetApi(region);
                    addNetworks(project, networkApi, subnetApi);

                    Optional<KeyPairApi> keyPairApi = novaApi.getKeyPairApi(region);
                    if (keyPairApi.isPresent()) {
                        addKeyPairs(keyPairApi.get(), project);
                    }

                    Optional<AvailabilityZoneApi> availabilityZoneApi = novaApi.getAvailabilityZoneApi(region);
                    if (availabilityZoneApi.isPresent()) {
                        addAvailabilityZones(availabilityZoneApi.get(), project, cloud, region);
                    }

                    LOG.info("Fetched project {} for cloud = {}, region = {}", project.getName(), cloud.getName(), region);
                    consumer.accept(project);
                } catch (Exception e) {
                    LOG.error(String.format("Failed to fetch project for cloud = %s, region = %s", cloud.getName(), region), e);
                }
            }
            return true;
        } catch (IOException e) {
            LOG.error("Failed to fetch projects for cloud = " + cloud.getName(), e);
            return false;
        }
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{LIST_PROJECTS};
    }

    private Project createProject(Cloud cloud, String region) {
        String projectId = idGenerator.getProjectId(cloud, region);
        Project project = new Project();
        project.setId(projectId);
        project.setName(getProjectName(cloud, region));
        project.setTimestamp(ZonedDateTime.now());

        MetadataMap metadata = new MetadataMap();
        metadata.put(MetadataKey.REGION, region);
        
        project.setMetadata(metadata);
        return project;
    }

    private String getProjectName(Cloud cloud, String region) {
        return String.format("%s_%s", cloud.getName(), region);
    }

    private void addFlavors(Project project, FlavorApi flavorApi) {
        for (Flavor flavor : flavorApi.listInDetail().concat()) {
            org.meridor.perspective.beans.Flavor flavorToAdd = new org.meridor.perspective.beans.Flavor();
            flavorToAdd.setId(flavor.getId());
            flavorToAdd.setName(flavor.getName());
            flavorToAdd.setVcpus(flavor.getVcpus());
            flavorToAdd.setRam(flavor.getRam());
            flavorToAdd.setRootDisk(flavor.getDisk());
            Optional<Integer> ephemeralDisk = flavor.getEphemeral();
            flavorToAdd.setEphemeralDisk(ephemeralDisk.isPresent() ? ephemeralDisk.get() : 0);
            flavorToAdd.setHasSwap(flavor.getSwap().isPresent());
            flavorToAdd.setIsPublic(true);
            project.getFlavors().add(flavorToAdd);
        }
    }

    private void addNetworks(Project project, NetworkApi networkApi, SubnetApi subnetApi) {
        for (org.jclouds.openstack.neutron.v2.domain.Network network : networkApi.list().concat()) {
            org.meridor.perspective.beans.Network networkToAdd = new org.meridor.perspective.beans.Network();
            networkToAdd.setId(network.getId());
            networkToAdd.setName(network.getName());
            networkToAdd.setIsShared(network.getShared());
            networkToAdd.setState(network.getStatus().name());
            for (String subnetId : network.getSubnets()) {
                org.jclouds.openstack.neutron.v2.domain.Subnet subnet = subnetApi.get(subnetId);
                networkToAdd.getSubnets().add(processSubnet(subnet));
            }
            project.getNetworks().add(networkToAdd);
        }
    }

    private Subnet processSubnet(org.jclouds.openstack.neutron.v2.domain.Subnet subnet) {
        Subnet ret = new Subnet();
        ret.setId(subnet.getId());
        Cidr cidr = parseCidr(subnet.getCidr());
        ret.setCidr(cidr);
        ret.setGateway(subnet.getGatewayIp());
        ret.setIsDHCPEnabled(subnet.getEnableDhcp());
        ret.setName(subnet.getName());
        int ipVersion = subnet.getIpVersion() != null ? subnet.getIpVersion() : 4;
        ret.setProtocolVersion(ipVersion);
        return ret;
    }
    
    private static Cidr parseCidr(String cidrString) {
        //TODO: replace this with https://github.com/abedra/orchard/pull/7 when it gets merged
        final String CIDR_DELIMITER = "/";
        String[] pieces = cidrString.split(CIDR_DELIMITER);
        Cidr cidr = new Cidr();
        if (pieces.length == 2) {
            cidr.setAddress(pieces[0]);
            cidr.setPrefixSize(Integer.valueOf(pieces[1]));
        } else {
            cidr.setAddress("255.255.255.255");
            cidr.setPrefixSize(32);
        }
        return cidr;
    }
    
    private void addAvailabilityZones(AvailabilityZoneApi availabilityZoneApi, Project project, Cloud cloud, String region) {
        try {
            for (org.jclouds.openstack.nova.v2_0.domain.regionscoped.AvailabilityZone az : availabilityZoneApi.listAvailabilityZones()) {
                AvailabilityZone availabilityZone = new AvailabilityZone();
                availabilityZone.setName(az.getName());
                project.getAvailabilityZones().add(availabilityZone);
            }
        } catch (Exception e) {
            LOG.debug("Failed to fetch availability zone information for cloud = {}, region = {}", cloud.getName(), region);
        }
    }
    
    private void addKeyPairs(KeyPairApi keyPairApi, Project project) {
        for (KeyPair keyPair : keyPairApi.list()) {
            Keypair keypair = new Keypair();
            keypair.setName(keyPair.getName());
            keypair.setFingerprint(keyPair.getFingerprint());
            project.getKeypairs().add(keypair);
        }
    }

}
