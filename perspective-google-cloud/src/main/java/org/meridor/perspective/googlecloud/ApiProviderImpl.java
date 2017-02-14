package org.meridor.perspective.googlecloud;

import com.google.api.services.compute.model.Region;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.compute.Compute;
import com.google.cloud.compute.ComputeOptions;
import org.meridor.perspective.config.Cloud;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiConsumer;

import static java.nio.file.StandardOpenOption.READ;

public class ApiProviderImpl implements ApiProvider {

    @Override
    public Api getApi(Cloud cloud) {
        return new ApiImpl(cloud);
    }

    @Override
    public void forEachRegion(Cloud cloud, BiConsumer<Region, Api> action) throws Exception {

    }

    private class ApiImpl implements Api {

        private final Compute computeApi;

        ApiImpl(Cloud cloud) {
            this.computeApi = createComputeApi(cloud);
        }

        private Compute createComputeApi(Cloud cloud) {

            Path jsonPath = Paths.get(cloud.getCredential());
            try (InputStream inputStream = Files.newInputStream(jsonPath, READ)) {
                Credentials credentials = GoogleCredentials.fromStream(inputStream);
                return ComputeOptions.newBuilder()
                        .setCredentials(credentials)
                        .build().getService();
            } catch (IOException e) {
                throw new RuntimeException(String.format(
                        "Failed to read JSON credentials file [%s]",
                        jsonPath.toAbsolutePath().toString()
                ), e);
            }
        }
    }
}
