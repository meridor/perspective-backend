package org.meridor.perspective.rest.data;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.impl.storage.DataFetcher;
import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TableName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class DataFetcherImpl implements DataFetcher {
    
    private static final Logger LOG = LoggerFactory.getLogger(DataFetcher.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    @Autowired
    private ProjectsAware projectsAware;
    
    @Autowired
    private ImagesAware imagesAware;
    
    @Autowired
    private InstancesAware instancesAware;

    @Override
    public DataContainer fetch(TableName tableName, String tableAlias, List<Column> columns) {
        List<String> columnNames = columnsToNames(columns);
        Map<String, List<String>> columnsMap = new HashMap<String, List<String>>(){
            {
                put(tableAlias, columnNames);
            }
        }; 
        DataContainer dataContainer = new DataContainer(columnsMap);
        List<List<Object>> rows = fetchData(tableName, columns);
        rows.forEach(dataContainer::addRow);
        return dataContainer;
    }
    
    private List<List<Object>> fetchData(TableName tableName, List<Column> columns) {
        switch (tableName) {
            case CLOUDS: return fetchClouds(columns);
            case PROJECTS: return fetchProjects(columns);
            case FLAVORS: return fetchFlavors(columns);
            case NETWORKS: return fetchNetworks(columns);
            case NETWORK_SUBNETS: return fetchNetworkSubnets(columns);
            case KEYPAIRS: return fetchKeypairs(columns);
            case AVAILABILITY_ZONES: return fetchAvailabilityZones(columns);
            case IMAGES: return fetchImages(columns);
            case PROJECT_IMAGES: return fetchProjectImages(columns);
            case INSTANCES: return fetchInstances(columns);
            case INSTANCE_NETWORKS: return fetchInstanceNetworks(columns);
            case INSTANCE_METADATA: return fetchInstanceMetadata(columns);
            case PROJECT_METADATA: return fetchProjectMetadata(columns);
            case IMAGE_METADATA: return fetchImageMetadata(columns);
            default:
                throw new IllegalArgumentException(String.format("Fetching from table \"%s\" is not supported", tableName.name().toLowerCase()));
        }
    }
    
    private static <T> List<List<Object>> prepareData(
            Callable<Collection<T>> rawDataSupplier,
            Map<String, Function<T, Object>> columnMapping,
            List<Column> columns,
            Supplier<String> errorMessageSupplier
    ) {
        try {
            Collection<T> rawEntities = rawDataSupplier.call();
            BiFunction<T, Column, Object> columnProcessor = prepareColumnProcessor(columnMapping);
            return rawEntities.stream()
                    .map(re -> columns.stream()
                            .map(c -> {
                                Object value = columnProcessor.apply(re, c);
                                return (value == null && c.getDefaultValue() != null) ? 
                                        c.getDefaultValue() : value;
                            })
                            .collect(Collectors.toList())
                    )
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error(errorMessageSupplier.get(), e);
            return Collections.emptyList();
        }
    }
    
    private static <T> BiFunction<T, Column, Object> prepareColumnProcessor(Map<String, Function<T, Object>> columnMapping) {
        return (e, c) -> {
            String columnName = c.getName();
            if (!columnMapping.containsKey(columnName)) {
                throw new IllegalArgumentException(String.format("Fetching column \"%s\" is not supported", columnName));
            }
            return columnMapping.get(columnName).apply(e);
        };
    }
    
    private List<List<Object>> fetchClouds(List<Column> columns) {
        Map<String, Function<Project, Object>> columnMapping = new HashMap<String, Function<Project, Object>>(){
            {
                put("id", Project::getCloudId);
                put("type", p -> p.getCloudType().value());
            }
        };
        return prepareData(
                () -> projectsAware.getProjects(Optional.empty()),
                columnMapping,
                columns,
                () -> "Failed to fetch \"clouds\" table contents"
        );
    }
    
    private List<List<Object>> fetchProjects(List<Column> columns) {
        Map<String, Function<Project, Object>> columnMapping = new HashMap<String, Function<Project, Object>>(){
            {
                put("id", Project::getId);
                put("name", Project::getName);
                put("cloud_id", Project::getCloudId);
                put("cloud_type", p -> p.getCloudType().value());
                put("last_updated", p -> p.getTimestamp().format(DATE_FORMATTER));
            }
        };
        return prepareData(
                () -> projectsAware.getProjects(Optional.empty()),
                columnMapping,
                columns,
                () -> "Failed to fetch \"projects\" table contents"
        );
    }
    
    private List<List<Object>> fetchKeypairs(List<Column> columns) {
        Map<String, Function<ExtendedKeypair, Object>> columnMapping = new HashMap<String, Function<ExtendedKeypair, Object>>(){
            {
                put("project_id", ExtendedKeypair::getProjectId);
                put("name", ExtendedKeypair::getName);
                put("fingerprint", ExtendedKeypair::getFingerprint);
                put("public_key", ExtendedKeypair::getPublicKey);
            }
        };
        return prepareData(
                () -> projectsAware.getProjects(Optional.empty()).stream()
                        .flatMap(p -> p.getKeypairs().stream().map(k -> new ExtendedKeypair(p.getId(), k)))
                        .collect(Collectors.toList()),
                columnMapping,
                columns,
                () -> "Failed to fetch \"keypairs\" table contents"
        );
    }
    
    private List<List<Object>> fetchAvailabilityZones(List<Column> columns) {
        Map<String, Function<ExtendedAvailabilityZone, Object>> columnMapping = new HashMap<String, Function<ExtendedAvailabilityZone, Object>>(){
            {
                put("project_id", ExtendedAvailabilityZone::getProjectId);
                put("name", ExtendedAvailabilityZone::getName);
            }
        };
        return prepareData(
                () -> projectsAware.getProjects(Optional.empty()).stream()
                        .flatMap(p -> p.getAvailabilityZones().stream().map(k -> new ExtendedAvailabilityZone(p.getId(), k)))
                        .collect(Collectors.toList()),
                columnMapping,
                columns,
                () -> "Failed to fetch \"availability_zones\" table contents"
        );
    }
    
    private List<List<Object>> fetchFlavors(List<Column> columns) {
        Map<String, Function<ExtendedFlavor, Object>> columnMapping = new HashMap<String, Function<ExtendedFlavor, Object>>(){
            {
                put("project_id", ExtendedFlavor::getProjectId);
                put("id", ExtendedFlavor::getId);
                put("name", ExtendedFlavor::getName);
                put("ram", ExtendedFlavor::getRam);
                put("vcpus", ExtendedFlavor::getVcpus);
                put("root_disk", ExtendedFlavor::getRootDisk);
                put("ephemeral_disk", ExtendedFlavor::getEphemeralDisk);
                put("has_swap", ExtendedFlavor::hasSwap);
                put("is_public", ExtendedFlavor::isPublic);
            }
        };
        return prepareData(
                () -> projectsAware.getProjects(Optional.empty()).stream()
                        .flatMap(p -> p.getFlavors().stream().map(f -> new ExtendedFlavor(p.getId(), f)))
                        .collect(Collectors.toList()),
                columnMapping,
                columns,
                () -> "Failed to fetch \"flavors\" table contents"
        );
    }
    
    private List<List<Object>> fetchNetworks(List<Column> columns) {
        Map<String, Function<ExtendedNetwork, Object>> columnMapping = new HashMap<String, Function<ExtendedNetwork, Object>>(){
            {
                put("id", ExtendedNetwork::getId);
                put("project_id", ExtendedNetwork::getProjectId);
                put("name", ExtendedNetwork::getName);
                put("state", ExtendedNetwork::getState);
                put("is_shared", ExtendedNetwork::isShared);
            }
        };
        return prepareData(
                () -> projectsAware.getProjects(Optional.empty()).stream()
                        .flatMap(p -> p.getNetworks().stream().map(n -> new ExtendedNetwork(p.getId(), n)))
                        .collect(Collectors.toList()),
                columnMapping,
                columns,
                () -> "Failed to fetch \"networks\" table contents"
        );
    }
    
    private List<List<Object>> fetchNetworkSubnets(List<Column> columns) {
        Map<String, Function<ExtendedNetworkSubnet, Object>> columnMapping = new HashMap<String, Function<ExtendedNetworkSubnet, Object>>(){
            {
                put("id", ExtendedNetworkSubnet::getId);
                put("project_id", ExtendedNetworkSubnet::getProjectId);
                put("network_id", ExtendedNetworkSubnet::getNetworkId);
                put("name", ExtendedNetworkSubnet::getName);
                put("cidr", s -> String.format("%s/%d", s.getCidr().getAddress(), s.getCidr().getPrefixSize()));
                put("protocol_version", ExtendedNetworkSubnet::getProtocolVersion);
                put("gateway", ExtendedNetworkSubnet::getGateway);
                put("is_dhcp_enabled", ExtendedNetworkSubnet::isDHCPEnabled);
            }
        };
        return prepareData(
                () -> projectsAware.getProjects(Optional.empty()).stream()
                        .flatMap(p -> 
                                p.getNetworks().stream().flatMap(n -> 
                                        n.getSubnets().stream()
                                        .map(s -> new ExtendedNetworkSubnet(p.getId(), n.getId(), s))
                                )
                        )
                        .collect(Collectors.toList()),
                columnMapping,
                columns,
                () -> "Failed to fetch \"network_subnets\" table contents"
        );
    }
    
    private List<List<Object>> fetchImages(List<Column> columns) {
        Map<String, Function<Image, Object>> columnMapping = new HashMap<String, Function<Image, Object>>(){
            {
                put("id", Image::getId);
                put("real_id", Image::getRealId);
                put("name", Image::getName);
                put("cloud_id", Image::getCloudId);
                put("cloud_type", i -> i.getCloudType().value());
                put("last_updated", i -> i.getTimestamp().format(DATE_FORMATTER));
                put("created", i -> i.getCreated().format(DATE_FORMATTER));
                put("state", i -> i.getState().value());
                put("checksum", Image::getChecksum);
            }
        };
        return prepareData(
                () -> imagesAware.getImages(),
                columnMapping,
                columns,
                () -> "Failed to fetch \"images\" table contents"
        );
    }
    
    private List<List<Object>> fetchProjectImages(List<Column> columns) {
        Map<String, Function<ProjectImage, Object>> columnMapping = new HashMap<String, Function<ProjectImage, Object>>(){
            {
                put("project_id", ProjectImage::getProjectId);
                put("image_id", ProjectImage::getImageId);
            }
        };
        return prepareData(
                () -> imagesAware.getImages().stream()
                .flatMap(i -> 
                        i.getProjectIds().stream()
                        .map(p -> new ProjectImage(p, i.getId()))
                )
                .collect(Collectors.toList()),
                columnMapping,
                columns,
                () -> "Failed to fetch \"project_images\" table contents"
        );
    }
    
    private List<List<Object>> fetchInstances(List<Column> columns) {
        Map<String, Function<Instance, Object>> columnMapping = new HashMap<String, Function<Instance, Object>>(){
            {
                put("id", Instance::getId);
                put("real_id", Instance::getRealId);
                put("name", Instance::getName);
                put("cloud_id", Instance::getCloudId);
                put("cloud_type", i -> i.getCloudType().value());
                put("project_id", Instance::getProjectId);
                put("flavor_id", i -> (i.getFlavor() != null) ? i.getFlavor().getId() : null);
                put("image_id", i -> (i.getImage() != null) ? i.getImage().getId() : null);
                put("state", i -> i.getState().value());
                put("last_updated", i -> i.getTimestamp().format(DATE_FORMATTER));
                put("created", i -> i.getCreated().format(DATE_FORMATTER));
                put("availability_zone", i -> (i.getAvailabilityZone() != null) ? i.getAvailabilityZone().getName() : null);
                put("addresses", i -> i.getAddresses().stream().collect(Collectors.joining("\n")));
            }
        };
        return prepareData(
                () -> instancesAware.getInstances(),
                columnMapping,
                columns,
                () -> "Failed to fetch \"instances\" table contents"
        );
    }

    private List<List<Object>> fetchInstanceNetworks(List<Column> columns) {
        Map<String, Function<InstanceNetwork, Object>> columnMapping = new HashMap<String, Function<InstanceNetwork, Object>>(){
            {
                put("instance_id", InstanceNetwork::getInstanceId);
                put("network_id", InstanceNetwork::getNetworkId);
            }
        };
        return prepareData(
                () -> instancesAware.getInstances().stream()
                        .flatMap(i ->
                                i.getNetworks().stream()
                                        .map(n -> new InstanceNetwork(i.getId(), n.getId()))
                        )
                        .collect(Collectors.toList()),
                columnMapping,
                columns,
                () -> "Failed to fetch \"instance_networks\" table contents"
        );
    }
    
    private List<List<Object>> fetchInstanceMetadata(List<Column> columns) {
        Map<String, Function<EntityMetadata, Object>> columnMapping = new HashMap<String, Function<EntityMetadata, Object>>(){
            {
                put("instance_id", EntityMetadata::getEntityId);
                put("key", EntityMetadata::getKey);
                put("value", EntityMetadata::getValue);
            }
        };
        return prepareData(
                () -> instancesAware.getInstances().stream()
                        .flatMap(i ->
                                i.getMetadata().keySet().stream()
                                        .map(k -> new EntityMetadata(i.getId(), k.toString().toLowerCase(), i.getMetadata().get(k)))
                        )
                        .collect(Collectors.toList()),
                columnMapping,
                columns,
                () -> "Failed to fetch \"instance_metadata\" table contents"
        );
    }
    
    private List<List<Object>> fetchImageMetadata(List<Column> columns) {
        Map<String, Function<EntityMetadata, Object>> columnMapping = new HashMap<String, Function<EntityMetadata, Object>>(){
            {
                put("image_id", EntityMetadata::getEntityId);
                put("key", EntityMetadata::getKey);
                put("value", EntityMetadata::getValue);
            }
        };
        return prepareData(
                () -> imagesAware.getImages().stream()
                        .flatMap(i ->
                                i.getMetadata().keySet().stream()
                                        .map(k -> new EntityMetadata(i.getId(), k.toString().toLowerCase(), i.getMetadata().get(k)))
                        )
                        .collect(Collectors.toList()),
                columnMapping,
                columns,
                () -> "Failed to fetch \"image_metadata\" table contents"
        );
    }
    
    private List<List<Object>> fetchProjectMetadata(List<Column> columns) {
        Map<String, Function<EntityMetadata, Object>> columnMapping = new HashMap<String, Function<EntityMetadata, Object>>(){
            {
                put("project_id", EntityMetadata::getEntityId);
                put("key", EntityMetadata::getKey);
                put("value", EntityMetadata::getValue);
            }
        };
        return prepareData(
                () -> projectsAware.getProjects(Optional.empty()).stream()
                        .flatMap(p ->
                                p.getMetadata().keySet().stream()
                                        .map(k -> new EntityMetadata(p.getId(), k.toString().toLowerCase(), p.getMetadata().get(k)))
                        )
                        .collect(Collectors.toList()),
                columnMapping,
                columns,
                () -> "Failed to fetch \"projects_metadata\" table contents"
        );
    }

    // Private classes below are needed because we are using BiFunction taking only one
    // argument type but need to show such columns as projectId for project-specific 
    // stuff like flavors, keypairs and so on. They can be safely removed if a better
    // approach of mapping objects to table columns is found.
    
    private static class ExtendedKeypair {
        
        private final String projectId;
        private final Keypair keypair;

        public ExtendedKeypair(String projectId, Keypair keypair) {
            this.projectId = projectId;
            this.keypair = keypair;
        }

        public String getProjectId() {
            return projectId;
        }

        public String getName() {
            return keypair.getName();
        }

        public String getFingerprint() {
            return keypair.getFingerprint();
        }

        public String getPublicKey() {
            return keypair.getPublicKey();
        }
    }
    
    private static class ExtendedAvailabilityZone {
        private final String projectId;
        private final AvailabilityZone availabilityZone;

        public ExtendedAvailabilityZone(String projectId, AvailabilityZone availabilityZone) {
            this.projectId = projectId;
            this.availabilityZone = availabilityZone;
        }

        public String getProjectId() {
            return projectId;
        }

        public String getName() {
            return availabilityZone.getName();
        }
    }
    
    private static class ExtendedFlavor {
        private final String projectId;
        private final Flavor flavor;

        public ExtendedFlavor(String projectId, Flavor flavor) {
            this.projectId = projectId;
            this.flavor = flavor;
        }

        public String getProjectId() {
            return projectId;
        }

        public String getId() {
            return flavor.getId();
        }

        public String getName() {
            return flavor.getName();
        }

        public int getRam() {
            return flavor.getRam();
        }

        public int getVcpus() {
            return flavor.getVcpus();
        }

        public int getRootDisk() {
            return flavor.getRootDisk();
        }

        public int getEphemeralDisk() {
            return flavor.getEphemeralDisk();
        }

        public boolean hasSwap() {
            return flavor.isHasSwap();
        }

        public boolean isPublic() {
            return flavor.isIsPublic();
        }
    }
    
    private static class ExtendedNetwork {
        private final String projectId;
        private final Network network;

        public ExtendedNetwork(String projectId, Network network) {
            this.projectId = projectId;
            this.network = network;
        }

        public String getProjectId() {
            return projectId;
        }

        public String getId() {
            return network.getId();
        }

        public String getName() {
            return network.getName();
        }

        public String getState() {
            return network.getState();
        }

        public boolean isShared() {
            return network.isIsShared();
        }
    }
    
    private static class ExtendedNetworkSubnet {
        private final String projectId;
        private final String networkId;
        private final Subnet subnet;

        public ExtendedNetworkSubnet(String projectId, String networkId, Subnet subnet) {
            this.projectId = projectId;
            this.networkId = networkId;
            this.subnet = subnet;
        }

        public String getProjectId() {
            return projectId;
        }

        public String getNetworkId() {
            return networkId;
        }

        public String getId() {
            return subnet.getId();
        }

        public String getName() {
            return subnet.getName();
        }

        public Cidr getCidr() {
            return subnet.getCidr();
        }

        public int getProtocolVersion() {
            return subnet.getProtocolVersion();
        }

        public String getGateway() {
            return subnet.getGateway();
        }

        public boolean isDHCPEnabled() {
            return subnet.isIsDHCPEnabled();
        }
    }
    
    private static class ProjectImage {
        private final String projectId;
        private final String imageId;

        public ProjectImage(String projectId, String imageId) {
            this.projectId = projectId;
            this.imageId = imageId;
        }

        public String getProjectId() {
            return projectId;
        }

        public String getImageId() {
            return imageId;
        }
    }
    
    private static class InstanceNetwork {
        private final String instanceId;
        private final String networkId;

        public InstanceNetwork(String instanceId, String networkId) {
            this.instanceId = instanceId;
            this.networkId = networkId;
        }

        public String getInstanceId() {
            return instanceId;
        }

        public String getNetworkId() {
            return networkId;
        }
    }
    
    private static class EntityMetadata {
        private final String entityId;
        private final String key;
        private final String value;

        public EntityMetadata(String entityId, String key, String value) {
            this.entityId = entityId;
            this.key = key;
            this.value = value;
        }

        public String getEntityId() {
            return entityId;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
    
}
