package org.meridor.perspective.docker;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import org.jclouds.ContextBuilder;
import org.jclouds.docker.DockerApi;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.meridor.perspective.config.Cloud;
import org.springframework.stereotype.Component;

@Component
public class DockerApiProvider {

    private static final Iterable<Module> LOGGING_MODULES = ImmutableSet.<Module> of(new SLF4JLoggingModule());
    
    public DockerApi getApi(Cloud cloud) {
        return ContextBuilder.newBuilder("docker")
                .endpoint(cloud.getEndpoint())
                .credentials(cloud.getIdentity(), cloud.getCredential())
                .modules(LOGGING_MODULES)
                .buildApi(DockerApi.class);
    }
}
