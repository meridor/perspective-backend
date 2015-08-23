package org.meridor.perspective.openstack;

import org.jclouds.ContextBuilder;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.meridor.perspective.config.Cloud;
import org.springframework.stereotype.Component;

@Component
public class OpenstackApiProvider {

    //TODO: probably implement API pooling

    public NovaApi getNovaApi(Cloud cloud) {
        return ContextBuilder.newBuilder("openstack-nova")
                .endpoint(cloud.getEndpoint())
                .credentials(cloud.getIdentity(), cloud.getCredential())
                .buildApi(NovaApi.class);
    }

    public NeutronApi getNeutronApi(Cloud cloud) {
        return ContextBuilder.newBuilder("openstack-neutron")
                .endpoint(cloud.getEndpoint())
                .credentials(cloud.getIdentity(), cloud.getCredential())
                .buildApi(NeutronApi.class);
    }

}
