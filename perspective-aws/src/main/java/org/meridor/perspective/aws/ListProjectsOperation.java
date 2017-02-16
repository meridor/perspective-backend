package org.meridor.perspective.aws;

import com.amazonaws.services.ec2.model.NetworkInterface;
import org.meridor.perspective.beans.*;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.worker.operation.AbstractListProjectsOperation;
import org.meridor.perspective.worker.operation.OperationUtils;
import org.meridor.perspective.worker.operation.RegionsAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ListProjectsOperation extends AbstractListProjectsOperation<String, Api> {

    private static final Logger LOG = LoggerFactory.getLogger(ListProjectsOperation.class);

    private final ApiProvider apiProvider;

    private final OperationUtils operationUtils;

    @Autowired
    public ListProjectsOperation(ApiProvider apiProvider, OperationUtils operationUtils) {
        this.apiProvider = apiProvider;
        this.operationUtils = operationUtils;
    }

    @Override
    protected Project processProject(Cloud cloud, String region, Api api) {
        Project project = operationUtils.createProject(cloud, region);
        addFlavors(project, api);
        addNetworks(project, cloud, region, api);
        addKeyPairs(project, cloud, region, api);
        addAvailabilityZones(project, cloud, region, api);
        addQuota(project);
        return project;
    }

    @Override
    protected RegionsAware<String, Api> getRegionsAware() {
        return apiProvider;
    }

    private void addFlavors(Project project, Api api) {
        project.getFlavors().addAll(api.listFlavors());
    }

    private void addNetworks(Project project, Cloud cloud, String region, Api api) {
        try {
            Map<String, Network> networks = api.listNetworks().stream()
                    .collect(Collectors.toMap(
                            NetworkInterface::getSubnetId,
                            n -> new Network() {
                                {
                                    setId(n.getNetworkInterfaceId());
                                    setName(n.getDescription());
                                    setState(n.getStatus());
                                    setIsShared(true);
                                }
                            }
                    ));
            api.listSubnets().forEach(s -> {
                String subnetId = s.getSubnetId();
                if (networks.containsKey(subnetId)) {
                    Network network = networks.get(subnetId);
                    Subnet subnet = processSubnet(s);
                    network.setSubnets(Collections.singletonList(subnet));
                }
            });
            project.getNetworks().addAll(networks.values());
        } catch (Exception e) {
            LOG.debug("Failed to fetch networks information for cloud = {}, region = {}", cloud.getName(), region);
        }
    }

    private Subnet processSubnet(com.amazonaws.services.ec2.model.Subnet subnet) {
        Subnet ret = new Subnet();
        ret.setId(subnet.getSubnetId());
        ret.setCidr(subnet.getCidrBlock());
        ret.setGateway("unknown"); //TODO: what about this one and the next one?
        ret.setIsDHCPEnabled(true);
        ret.setName(subnet.getCidrBlock());
        int ipVersion = subnet.getAssignIpv6AddressOnCreation() ? 6 : 4;
        ret.setProtocolVersion(ipVersion);
        return ret;
    }

    private void addAvailabilityZones(Project project, Cloud cloud, String region, Api api) {
        try {
            List<AvailabilityZone> availabilityZones = api.listAvailabilityZones().stream()
                    .map(az -> new AvailabilityZone() {
                        {
                            setName(az.getZoneName());
                        }
                    })
                    .collect(Collectors.toList());
            project.getAvailabilityZones().addAll(availabilityZones);
        } catch (Exception e) {
            LOG.debug("Failed to fetch availability zone information for cloud = {}, region = {}", cloud.getName(), region);
        }
    }

    private void addKeyPairs(Project project, Cloud cloud, String region, Api api) {

        try {
            List<Keypair> keypairs = api.listKeypairs().stream()
                    .map(kp -> new Keypair() {
                        {
                            setName(kp.getKeyName());
                            setFingerprint(kp.getKeyFingerprint());
                        }
                    })
                    .collect(Collectors.toList());
            project.getKeypairs().addAll(keypairs);
        } catch (Exception e) {
            LOG.debug("Failed to fetch keypairs information for cloud = {}, region = {}", cloud.getName(), region);
        }
    }

    private void addQuota(Project project) {
        Quota quota = new Quota();
        project.setQuota(quota);
    }

}
