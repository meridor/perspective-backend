package org.meridor.perspective.openstack;

import org.meridor.perspective.config.Cloud;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.types.Facing;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.core.transport.Config;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.identity.v3.Endpoint;
import org.openstack4j.model.identity.v3.Service;
import org.openstack4j.openstack.OSFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Component
public class OpenstackApiProvider {

    //TODO: probably implement API pooling

    private static final String DELIMITER = ":";
    
    public OSClient.OSClientV3 getApi(Cloud cloud, String region) {
        String[] identity = cloud.getIdentity().split(DELIMITER);
        Assert.isTrue(identity.length == 2, "Identity should be in format project:username");
        String projectName = identity[0];
        String userName = identity[1];
        OSClient.OSClientV3 api = OSFactory.builderV3()
                .withConfig(getConnectionSettings())
                .endpoint(cloud.getEndpoint())
                .credentials(userName, cloud.getCredential(), Identifier.byId("default"))
                .scopeToProject(Identifier.byName(projectName), Identifier.byName("default"))
                .authenticate();
        return region != null ? api.useRegion(region) : api;
    }
    
    private OSClient.OSClientV3 getApi(Cloud cloud) {
        return getApi(cloud, null);
    }

    public void forEachComputeRegion(Cloud cloud, BiConsumer<String, OSClient.OSClientV3> action) {
        OSClient.OSClientV3 api = getApi(cloud);
        Set<String> computeRegions = getRegions(api, ServiceType.COMPUTE);
        computeRegions.forEach(cr -> action.accept(cr, api.useRegion(cr)));
    }
    
    private static Set<String> getRegions(OSClient.OSClientV3 api, ServiceType serviceType) {

        Optional<? extends Service> serviceCandidate = api.getToken().getCatalog().stream()
                .filter(s -> s.getType().equals(serviceType.getType()))
                .findFirst();

        return serviceCandidate.isPresent() ?
                    serviceCandidate.get().getEndpoints().stream()
                        .filter(e -> e.getIface() == Facing.PUBLIC)
                        .map(Endpoint::getRegion)
                        .collect(Collectors.toSet()) :
                    Collections.emptySet();
    }
    
    private static Config getConnectionSettings() {
        return Config.newConfig()
                .withConnectionTimeout(10000)
                .withReadTimeout(30000);
    }

}
