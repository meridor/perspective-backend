package org.meridor.perspective.openstack;

import org.jclouds.ContextBuilder;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.meridor.perspective.config.Cloud;
import org.springframework.stereotype.Component;

@Component
public class OpenstackApiProvider {
    
    public NovaApi getApi(Cloud cloud) {
        return ContextBuilder.newBuilder("openstack-nova")
                .endpoint(cloud.getEndpoint())
                .credentials(cloud.getIdentity(), cloud.getCredential())
                .buildApi(NovaApi.class);
    }
    
}
