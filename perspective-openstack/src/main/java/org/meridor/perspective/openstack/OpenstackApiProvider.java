package org.meridor.perspective.openstack;

import org.meridor.perspective.config.Cloud;
import org.openstack4j.api.OSClient;
import org.openstack4j.core.transport.Config;
import org.openstack4j.model.identity.v2.Endpoint;
import org.openstack4j.openstack.OSFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OpenstackApiProvider {

    //TODO: probably implement API pooling

    private static final String DELIMITER = ":";
    
    public OSClient.OSClientV2 getApi(Cloud cloud) {
        String[] identity = cloud.getIdentity().split(DELIMITER);
        Assert.isTrue(identity.length == 2, "Identity should be in format project:username");
        String projectName = identity[0];
        String userName = identity[1];
        return OSFactory.builderV2()
                .withConfig(getConnectionSettings())
                .endpoint(cloud.getEndpoint())
                .credentials(userName, cloud.getCredential())
                .tenantName(projectName)
                .authenticate();
    }

    public static Set<String> getRegions(OSClient.OSClientV2 api) {
        return api.identity().listTokenEndpoints().stream()
                .map(Endpoint::getRegion)
                .collect(Collectors.toSet());
    }
    
    private static Config getConnectionSettings() {
        return Config.newConfig()
                .withConnectionTimeout(10000)
                .withReadTimeout(30000);
    }

}
