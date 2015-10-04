package org.meridor.perspective.docker;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.AuthConfig;
import org.meridor.perspective.config.Cloud;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.URI;

@Component
public class DockerApiProvider {

    private DockerClient dockerClient;
    
    public DockerClient getApi(Cloud cloud) {
        //TODO: support SSL certificates
        if (dockerClient == null) {
            AuthConfig authConfig = AuthConfig.builder()
                    .username(cloud.getIdentity())
                    .password(cloud.getCredential())
                    .build();
            this.dockerClient = DefaultDockerClient.builder()
                    .uri(URI.create(cloud.getEndpoint()))
                    .authConfig(authConfig)
                    .build();
        }
        return dockerClient;
    }
    
    @PreDestroy
    public void preDestroy() {
        if (dockerClient != null) {
            dockerClient.close();
        }
    }
    
}
