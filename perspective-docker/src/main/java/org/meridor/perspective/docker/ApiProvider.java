package org.meridor.perspective.docker;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.*;
import org.meridor.perspective.config.Cloud;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
public class ApiProvider {

    private final Map<String, Api> apiMap = new HashMap<>();

    public Api getApi(Cloud cloud) {
        String cloudId = cloud.getId();
        return apiMap.computeIfAbsent(cloudId, any -> new ApiImpl(cloud));
    }

    @PreDestroy
    public void preDestroy() {
        apiMap.values().forEach(Api::close);
    }

    private static class ApiImpl implements Api {

        private final Cloud cloud;
        private final DockerClient dockerClient;

        private ApiImpl(Cloud cloud) {
            this.cloud = cloud;
            //TODO: support SSL certificates
            AuthConfig authConfig = AuthConfig.builder()
                    .username(cloud.getIdentity())
                    .password(cloud.getCredential())
                    .build();
            this.dockerClient = DefaultDockerClient.builder()
                    .uri(URI.create(cloud.getEndpoint()))
                    .authConfig(authConfig)
                    .build();
        }

        @Override
        public String addContainer(String imageId, String containerName, String command) throws Exception {
            ContainerConfig containerConfig = ContainerConfig.builder()
                    .cmd(command)
                    .image(imageId)
                    .build();
            ContainerCreation createdContainer = dockerClient.createContainer(containerConfig, containerName);
            return createdContainer.id();
        }

        @Override
        public void deleteContainer(String containerId) throws Exception {
            dockerClient.removeContainer(containerId);
        }

        @Override
        public void listContainers(BiConsumer<Cloud, ContainerInfo> action) throws Exception {
            List<Container> containers = dockerClient.listContainers(DockerClient.ListContainersParam.allContainers());
            for (Container container : containers) {
                ContainerInfo containerInfo = dockerClient.inspectContainer(container.id());
                action.accept(cloud, containerInfo);
            }
        }

        @Override
        public String addImage(String containerId, String imageName) throws Exception {
            ContainerConfig containerConfig = ContainerConfig.builder().build();
            ContainerCreation createdImage = dockerClient.commitContainer(
                    containerId,
                    imageName,
                    null,
                    containerConfig,
                    null,
                    null
            );
            return createdImage.id();
        }

        @Override
        public void deleteImage(String imageId) throws Exception {
            dockerClient.removeImage(imageId);
        }

        @Override
        public void listImages(BiConsumer<com.spotify.docker.client.messages.Image, ImageInfo> action) throws Exception {
            List<com.spotify.docker.client.messages.Image> dockerImages = dockerClient.listImages(DockerClient.ListImagesParam.allImages(false));
            for (com.spotify.docker.client.messages.Image image : dockerImages) {
                ImageInfo imageInfo = dockerClient.inspectImage(image.id());
                action.accept(image, imageInfo);
            }
        }

        @Override
        public void close() {
            if (dockerClient != null) {
                dockerClient.close();
            }
        }
    }
}
