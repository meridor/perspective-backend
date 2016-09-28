package org.meridor.perspective.docker;

import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ImageInfo;
import org.meridor.perspective.config.Cloud;

import java.util.function.BiConsumer;

public interface Api {

    String addContainer(String imageId, String containerName, String command) throws Exception;

    void deleteContainer(String containerId) throws Exception;

    void listContainers(BiConsumer<Cloud, ContainerInfo> action) throws Exception;

    String addImage(String containerId, String imageName) throws Exception;

    void deleteImage(String imageId) throws Exception;

    void listImages(BiConsumer<com.spotify.docker.client.messages.Image, ImageInfo> action) throws Exception;

    void close();

}
