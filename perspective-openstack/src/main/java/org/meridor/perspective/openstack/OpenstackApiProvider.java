package org.meridor.perspective.openstack;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.jclouds.ContextBuilder;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.neutron.v2.NeutronApi;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.meridor.perspective.config.Cloud;
import org.springframework.stereotype.Component;

import java.util.Properties;

import static org.jclouds.Constants.*;

@Component
public class OpenstackApiProvider {

    //TODO: probably implement API pooling

    private static final Iterable<Module> LOGGING_MODULES = ImmutableSet.of(new SLF4JLoggingModule());

    public NovaApi getNovaApi(Cloud cloud) {
        
        return ContextBuilder.newBuilder("openstack-nova")
                .endpoint(cloud.getEndpoint())
                .credentials(cloud.getIdentity(), cloud.getCredential())
                .modules(LOGGING_MODULES)
                .overrides(getConnectionSettings())
                .buildApi(NovaApi.class);
    }

    public NeutronApi getNeutronApi(Cloud cloud) {
        return ContextBuilder.newBuilder("openstack-neutron")
                .endpoint(cloud.getEndpoint())
                .credentials(cloud.getIdentity(), cloud.getCredential())
                .modules(LOGGING_MODULES)
                .overrides(getConnectionSettings())
                .buildApi(NeutronApi.class);
    }
    
    private static Properties getConnectionSettings() {
        return new Properties(){
            {
                setProperty(PROPERTY_CONNECTION_TIMEOUT, "10000");
                setProperty(PROPERTY_SO_TIMEOUT, "10000");
                setProperty(PROPERTY_REQUEST_TIMEOUT, "30000");
                setProperty(PROPERTY_MAX_RETRIES, "2");
            }
        };

    }

}
