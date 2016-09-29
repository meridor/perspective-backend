package org.meridor.perspective.openstack;

import org.meridor.perspective.config.Cloud;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.types.Facing;
import org.openstack4j.api.types.ServiceType;
import org.openstack4j.core.transport.Config;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.*;
import org.openstack4j.model.compute.actions.RebuildOptions;
import org.openstack4j.model.compute.ext.AvailabilityZone;
import org.openstack4j.model.identity.v3.Endpoint;
import org.openstack4j.model.identity.v3.Service;
import org.openstack4j.model.image.Image;
import org.openstack4j.model.network.Network;
import org.openstack4j.openstack.OSFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Component
public class OpenstackApiProvider {


    public Api getApi(Cloud cloud, String region) {
        return region != null ? new ApiImpl(cloud, region) : new ApiImpl(cloud);
    }

    private Api getApi(Cloud cloud) {
        return getApi(cloud, null);
    }

    public void forEachComputeRegion(Cloud cloud, BiConsumer<String, Api> action) {
        Api api = getApi(cloud);
        Set<String> computeRegions = api.getComputeRegions();
        computeRegions.forEach(cr -> action.accept(cr, new ApiImpl(cloud, cr)));
    }

    private static class ApiImpl implements Api {

        //TODO: probably implement API pooling

        private static final String DELIMITER = ":";

        private final OSClient.OSClientV3 api;

        private ApiImpl(Cloud cloud) {
            this(cloud, null);
        }

        private ApiImpl(Cloud cloud, String region) {
            this.api = createApi(cloud, region);
        }

        private static OSClient.OSClientV3 createApi(Cloud cloud, String region) {
            String[] identity = cloud.getIdentity().split(DELIMITER);
            Assert.isTrue(identity.length == 2, "Identity should be in format project:username");
            String projectName = identity[0];
            String userName = identity[1];
            OSClient.OSClientV3 api = OSFactory.builderV3()
                    .withConfig(getConnectionSettings())
                    .endpoint(cloud.getEndpoint())
                    .credentials(userName, cloud.getCredential(), Identifier.byId("default"))
                    .scopeToProject(Identifier.byName(projectName), Identifier.byName("default"))
                    .authenticate();
            return region != null ? api.useRegion(region) : api;
        }

        private static Config getConnectionSettings() {
            return Config.newConfig()
                    .withConnectionTimeout(10000)
                    .withReadTimeout(30000);
        }

        @Override
        public Set<String> getComputeRegions() {
            return getRegions(ServiceType.COMPUTE);
        }

        private Set<String> getRegions(ServiceType serviceType) {

            Optional<? extends Service> serviceCandidate = api.getToken().getCatalog().stream()
                    .filter(s -> s.getType().equals(serviceType.getType()))
                    .findFirst();

            return serviceCandidate.isPresent() ?
                    serviceCandidate.get().getEndpoints().stream()
                            .filter(e -> e.getIface() == Facing.PUBLIC)
                            .map(Endpoint::getRegion)
                            .collect(Collectors.toSet()) :
                    Collections.emptySet();
        }

        @Override
        public List<? extends Flavor> listFlavors() {
            return api.compute().flavors().list();
        }

        @Override
        public List<? extends Network> listNetworks() {
            return api.networking().network().list();
        }

        @Override
        public List<? extends AvailabilityZone> listAvailabilityZones() {
            return api.compute().zones().list();
        }

        @Override
        public List<? extends Keypair> listKeypairs() {
            return api.compute().keypairs().list();
        }

        @Override
        public AbsoluteLimit getQuota() {
            return api.compute().quotaSets().limits().getAbsolute();
        }

        @Override
        public String addInstance(ServerCreate serverConfig) {
            return api.compute().servers().boot(serverConfig).getId();
        }

        @Override
        public void deleteInstance(String instanceId) {
            api.compute().servers().delete(instanceId);
        }

        @Override
        public void startInstance(String instanceId) {
            api.compute().servers().action(instanceId, Action.START);
        }

        @Override
        public void shutdownInstance(String instanceId) {
            api.compute().servers().action(instanceId, Action.STOP);
        }

        @Override
        public void rebootInstance(String instanceId) {
            api.compute().servers().reboot(instanceId, RebootType.SOFT);
        }

        @Override
        public void hardRebootInstance(String instanceId) {
            api.compute().servers().reboot(instanceId, RebootType.HARD);
        }

        @Override
        public void resizeInstance(String instanceId, String flavorId) {
            api.compute().servers().resize(instanceId, flavorId);
        }

        @Override
        public void rebuildInstance(String instanceId, String imageId) {
            api.compute().servers().rebuild(
                    instanceId,
                    RebuildOptions.create().image(imageId)
            );
        }

        @Override
        public void pauseInstance(String instanceId) {
            api.compute().servers().action(instanceId, Action.PAUSE);
        }

        @Override
        public void suspendInstance(String instanceId) {
            api.compute().servers().action(instanceId, Action.SUSPEND);
        }

        @Override
        public void resumeInstance(String instanceId) {
            api.compute().servers().action(instanceId, Action.RESUME);
        }

        @Override
        public List<? extends Server> listInstances() {
            return api.compute().servers().list(true);
        }

        @Override
        public Optional<Server> getInstanceById(String instanceId) {
            return Optional.ofNullable(api.compute().servers().get(instanceId));
        }

        @Override
        public String getInstanceConsoleUrl(String instanceId, String consoleType) {
            return api.compute().servers().getVNCConsole(instanceId, VNCConsole.Type.value(consoleType)).getURL();
        }

        @Override
        public String addImage(String instanceId, String imageName) {
            return api.compute().servers().createSnapshot(instanceId, imageName);
        }

        @Override
        public void deleteImage(String imageId) {
            api.compute().images().delete(imageId);
        }

        @Override
        public List<? extends org.openstack4j.model.image.Image> listImages() {
            return api.images().listAll();
        }

        @Override
        public Optional<Image> getImageById(String imageId) {
            return Optional.ofNullable(api.images().get(imageId));
        }

    }

}
