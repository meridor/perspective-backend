package org.meridor.perspective.docker;

import org.jclouds.ContextBuilder;
import org.jclouds.docker.DockerApi;
import org.meridor.perspective.config.Cloud;
import org.springframework.stereotype.Component;

@Component
public class DockerApiProvider {

    public DockerApi getApi(Cloud cloud) {
        return ContextBuilder.newBuilder("docker")
                .endpoint(cloud.getEndpoint())
                .credentials(cloud.getIdentity(), cloud.getCredential())
                .buildApi(DockerApi.class);
    }
}
