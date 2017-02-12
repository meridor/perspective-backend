package org.meridor.perspective.aws;

import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.apache.http.HttpStatus;
import org.meridor.perspective.beans.Flavor;
import org.meridor.perspective.config.Cloud;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        public List<Flavor> listFlavors() {
            return Stream.of(AvailableFlavor.values())
                    .map(AvailableFlavor::toFlavor)
                    .collect(Collectors.toList());
        }

        @Override
        public List<NetworkInterface> listNetworks() {
            return client.describeNetworkInterfaces().getNetworkInterfaces();
        }

        @Override
        public List<Subnet> listSubnets() {
            return client.describeSubnets().getSubnets();
        }

        @Override
        public List<AvailabilityZone> listAvailabilityZones() {
            return client.describeAvailabilityZones().getAvailabilityZones();
        }

        @Override
        public List<KeyPairInfo> listKeypairs() {
            return client.describeKeyPairs().getKeyPairs();
        }

        @Override
        public List<Instance> listInstances(Set<String> instanceIds) {
            DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest()
                    .withInstanceIds(instanceIds);
            DescribeInstancesResult describeInstancesResult = client.describeInstances(describeInstancesRequest);
            return resultToInstances(describeInstancesResult);
        }

        @Override
        public List<Instance> listInstances() {
            return resultToInstances(client.describeInstances());
        }

        private List<Instance> resultToInstances(DescribeInstancesResult describeInstancesResult) {
            return isResponseSuccessful(describeInstancesResult) ?
                    describeInstancesResult.getReservations()
                            .stream()
                            .flatMap(r -> r.getInstances().stream())
                            .collect(Collectors.toList()) : Collections.emptyList();
        }

        @Override
        public boolean rebootInstance(String instanceId) {
            RebootInstancesRequest request = new RebootInstancesRequest(Collections.singletonList(instanceId));
            return isResponseSuccessful(client.rebootInstances(request));
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

        @Override
        public String addImage(String instanceId, String imageName) {
            CreateImageRequest createImageRequest = new CreateImageRequest(instanceId, imageName);
            CreateImageResult createImageResult = client.createImage(createImageRequest);
            return createImageResult.getImageId();
        }

        @Override
        public List<Image> listImages(Set<String> imageIds) {
            DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest()
                    .withImageIds(imageIds);
            return client.describeImages(describeImagesRequest).getImages();
        }

        @Override
        public List<Image> listImages() {
            return client.describeImages().getImages();
        }

        @Override
        public boolean deleteImage(String imageId) {
            List<Image> images = listImages(Collections.singleton(imageId));
            if (images.size() >= 1) {
                Image image = images.get(0);
                DeregisterImageRequest deregisterImageRequest = new DeregisterImageRequest(imageId);
                DeregisterImageResult deregisterImageResult = client.deregisterImage(deregisterImageRequest);
                for (BlockDeviceMapping blockDeviceMapping : image.getBlockDeviceMappings()) {
                    String snapshotId = blockDeviceMapping.getEbs().getSnapshotId();
                    if (snapshotId != null) {
                        DeleteSnapshotRequest deleteSnapshotRequest = new DeleteSnapshotRequest(snapshotId);
                        DeleteSnapshotResult deleteSnapshotResult = client.deleteSnapshot(deleteSnapshotRequest);
                        return isResponseSuccessful(deregisterImageResult) && isResponseSuccessful(deleteSnapshotResult);
                    }
                }
                return isResponseSuccessful(deregisterImageResult);
            }
            return false;
        }

        private boolean isResponseSuccessful(AmazonWebServiceResult<?> response) {
            return response.getSdkHttpMetadata().getHttpStatusCode() == HttpStatus.SC_OK;
        }

        @Override
        public void close() {
            client.shutdown();
        }


    }
}
