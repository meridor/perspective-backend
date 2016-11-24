package org.meridor.perspective.aws;

import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import org.meridor.perspective.config.Cloud;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
public class ApiProviderImpl implements ApiProvider {

    private final Map<Cloud, Map<Regions, Api>> apiCache = new HashMap<>();
    
    @Override
    public Api getApi(Cloud cloud, Regions region) {
        apiCache.putIfAbsent(cloud, new HashMap<>());
        apiCache.get(cloud).putIfAbsent(region, new ApiImpl(cloud, region));
        return apiCache.get(cloud).get(region);
    }

    @Override
    public void forEachRegion(Cloud cloud, BiConsumer<String, Api> action) throws Exception {
        Arrays.stream(Regions.values()).forEach(r -> action.accept(r.getName(), getApi(cloud, r)));
    }

    @PreDestroy
    public void onDestroy() {
        apiCache.keySet().forEach(c -> {
            Map<Regions, Api> regionsMap = apiCache.get(c);
            regionsMap.keySet().forEach(r -> regionsMap.get(r).close());
        });
    }

    private class ApiImpl implements Api {

        private final AmazonEC2 client;

        ApiImpl(Cloud cloud, Regions region) {
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

        @Override
        public boolean rebootInstance(String instanceId) {
            RebootInstancesRequest request = new RebootInstancesRequest(Collections.singletonList(instanceId));
            return isResponseSuccessful(client.rebootInstances(request));
        }

        @Override
        public boolean hardRebootInstance(String instanceId) {
            //TODO: to be implemented!!!
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean startInstance(String instanceId) {
            StartInstancesRequest request = new StartInstancesRequest(Collections.singletonList(instanceId));
            return isResponseSuccessful(client.startInstances(request));
        }

        @Override
        public boolean shutdownInstance(String instanceId) {
            StopInstancesRequest request = new StopInstancesRequest(Collections.singletonList(instanceId));
            return isResponseSuccessful(client.stopInstances(request));
        }

        @Override
        public boolean deleteInstance(String instanceId) {
            TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest(Collections.singletonList(instanceId));
            return isResponseSuccessful(client.terminateInstances(terminateInstancesRequest));
        }

        private boolean isResponseSuccessful(AmazonWebServiceResult<?> response) {
            //TODO: to be implemented!
            return true;
        }

        @Override
        public void close() {
            client.shutdown();
        }


    }
}
