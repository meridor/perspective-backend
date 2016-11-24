package org.meridor.perspective.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import org.meridor.perspective.config.Cloud;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.function.BiConsumer;

@Component
public class ApiProviderImpl implements ApiProvider {

    @Override
    public Api getApi(Cloud cloud, Regions region) {
        return new ApiImpl(cloud, region);
    }

    @Override
    public void forEachRegion(Cloud cloud, BiConsumer<String, Api> action) throws Exception {
        Arrays.stream(Regions.values()).forEach(r -> action.accept(r.getName(), new ApiImpl(cloud, r)));
    }

    private class ApiImpl implements Api {

        private final AmazonEC2 client;

        public ApiImpl(Cloud cloud, Regions region) {
            this.client = createClient(cloud, region);
        }

        private AmazonEC2 createClient(Cloud cloud, Regions region) {
            return AmazonEC2ClientBuilder.standard()
                    .withRegion(region)
                    .withCredentials(new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(
                                    cloud.getIdentity(),
                                    cloud.getCredential()
                            )
                    ))
                    .build();
        }
    }
}
