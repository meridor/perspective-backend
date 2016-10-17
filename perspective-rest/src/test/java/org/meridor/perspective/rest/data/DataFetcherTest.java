package org.meridor.perspective.rest.data;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.sql.DataContainer;
import org.meridor.perspective.sql.DataRow;
import org.meridor.perspective.sql.impl.storage.DataFetcher;
import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/data-fetcher-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class DataFetcherTest {

    private static final String TEST_ALIAS = "alias";
    
    @Autowired
    private ProjectsAware projectsAware;
    
    @Autowired
    private ImagesAware imagesAware;
    
    @Autowired
    private InstancesAware instancesAware;
    
    @Autowired
    private DataFetcher dataFetcher;
    
    @Autowired
    private TablesAware tablesAware;
    
    @Before
    public void before() {
        projectsAware.saveProject(EntityGenerator.getProject());
        imagesAware.saveImage(EntityGenerator.getImage());
        instancesAware.saveInstance(EntityGenerator.getInstance());
    }
    
    @Test
    public void testFetchClouds() {
        checkAssertions(TableName.CLOUDS, new LinkedHashMap<String, Object>(){
            {
                put("id", CloudType.MOCK.value());
                put("type", CloudType.MOCK.value());
            }
        });
    }

    @Test
    public void testFetchCloudsByIds() {
        checkByIdsAssertions(
                TableName.CLOUDS,
                new HashSet<>(Arrays.asList("id", "type")),
                Collections.singleton(CloudType.MOCK.value()),
                Collections.singletonMap(CloudType.MOCK.value(), Arrays.asList(CloudType.MOCK.value(), CloudType.MOCK.value()))
        );
    }
    
    @Test
    public void testFetchProjects() {
        checkAssertions(TableName.PROJECTS, new LinkedHashMap<String, Object>(){
            {
                put("id", "test-project");
                put("name", "test-project - test-region");
            }
        });
    }
    
    @Test
    public void testFetchProjectsByIds() {
        checkByIdsAssertions(
                TableName.PROJECTS,
                new HashSet<>(Arrays.asList("id", "name")),
                Collections.singleton("test-project"),
                Collections.singletonMap("test-project", Arrays.asList("test-project", "test-project - test-region"))
        );
    }

    @Test
    public void testFetchKeypairs() {
        checkAssertions(TableName.KEYPAIRS, new LinkedHashMap<String, Object>(){
            {
                put("name", "test-keypair");
                put("project_id", "test-project");
                put("public_key", "test-public-key");
            }
        });
    }
    
    @Test
    public void testFetchKeypairsByIds() {
        checkByIdsAssertions(
                TableName.KEYPAIRS,
                new HashSet<>(Arrays.asList("name", "project_id", "public_key")),
                Collections.singleton("test-project:test-keypair"),
                Collections.singletonMap("test-project:test-keypair", Arrays.asList("test-keypair", "test-project", "test-public-key"))
        );
    }
    
    @Test
    public void testFetchAvailabilityZones() {
        checkAssertions(TableName.AVAILABILITY_ZONES, new LinkedHashMap<String, Object>(){
            {
                put("name", "test-zone");
                put("project_id", "test-project");
            }
        });
    }
    
    @Test
    public void testFetchAvailabilityZonesByIds() {
        checkByIdsAssertions(
                TableName.AVAILABILITY_ZONES,
                new HashSet<>(Arrays.asList("name", "project_id")),
                Collections.singleton("test-project:test-zone"),
                Collections.singletonMap("test-project:test-zone", Arrays.asList("test-zone", "test-project"))
        );
    }
    
    @Test
    public void testFetchFlavors() {
        checkAssertions(TableName.FLAVORS, new LinkedHashMap<String, Object>(){
            {
                put("project_id", "test-project");
                put("name", "test-flavor");
            }
        });
    }
    
    @Test
    public void testFetchFlavorsByIds() {
        checkByIdsAssertions(
                TableName.FLAVORS,
                new HashSet<>(Arrays.asList("project_id", "name")),
                Collections.singleton("test-project:test-flavor"),
                Collections.singletonMap("test-project:test-flavor", Arrays.asList("test-project", "test-flavor"))
        );
    }

    @Test
    public void testFetchNetworks() {
        checkAssertions(TableName.NETWORKS, new LinkedHashMap<String, Object>(){
            {
                put("project_id", "test-project");
                put("name", "test-network");
            }
        });
    }
    
    @Test
    public void testFetchNetworksByIds() {
        checkByIdsAssertions(
                TableName.NETWORKS,
                new HashSet<>(Arrays.asList("project_id", "name")),
                Collections.singleton("test-project:test-network"),
                Collections.singletonMap("test-project:test-network", Arrays.asList("test-project", "test-network"))
        );
    }

    @Test
    public void testFetchNetworkSubnets() {
        checkAssertions(TableName.NETWORK_SUBNETS, new LinkedHashMap<String, Object>(){
            {
                put("id", "test-subnet");
                put("cidr", "5.255.210.0/24");
            }
        });
    }
    
    @Test
    public void testFetchNetworkSubnetsByIds() {
        checkByIdsAssertions(
                TableName.NETWORK_SUBNETS,
                new HashSet<>(Arrays.asList("id", "cidr")),
                Collections.singleton("test-project:test-network:test-subnet"),
                Collections.singletonMap("test-project:test-network:test-subnet", Arrays.asList("test-subnet", "5.255.210.0/24"))
        );
    }

    @Test
    public void testFetchImages() {
        checkAssertions(TableName.IMAGES, new LinkedHashMap<String, Object>(){
            {
                put("name", "test-image");
                put("state", "saved");
            }
        });
    }
    
    @Test
    public void testFetchImagesByIds() {
        checkByIdsAssertions(
                TableName.IMAGES,
                new HashSet<>(Arrays.asList("name", "state")),
                Collections.singleton("test-image"),
                Collections.singletonMap("test-image", Arrays.asList("test-image", "saved"))
        );
    }

    @Test
    public void testFetchProjectImages() {
        checkAssertions(TableName.PROJECT_IMAGES, new LinkedHashMap<String, Object>(){
            {
                put("project_id", "test-project");
                put("image_id", "test-image");
            }
        });
    }

    @Test
    public void testFetchProjectImagesByIds() {
        checkByIdsAssertions(
                TableName.PROJECT_IMAGES,
                new HashSet<>(Arrays.asList("project_id", "image_id")),
                Collections.singleton("test-project:test-image"),
                Collections.singletonMap("test-project:test-image", Arrays.asList("test-project", "test-image"))
        );
    }
    
    @Test
    public void testFetchProjectQuota() {
        checkAssertions(TableName.PROJECT_QUOTA, new LinkedHashMap<String, Object>(){
            {
                put("project_id", "test-project");
                put("instances", "instances-quota");
                put("vcpus", "vcpus-quota");
            }
        });
    }

    @Test
    public void testFetchProjectQuotaByIds() {
        checkByIdsAssertions(
                TableName.PROJECT_QUOTA,
                new HashSet<>(Arrays.asList("project_id", "instances", "vcpus")),
                Collections.singleton("test-project"),
                Collections.singletonMap("test-project", Arrays.asList("test-project", "instances-quota", "vcpus-quota"))
        );
    }
    
    @Test
    public void testFetchInstances() {
        checkAssertions(TableName.INSTANCES, new LinkedHashMap<String, Object>(){
            {
                put("id", "test-instance");
                put("state", "launched");
                put("fqdn", "test-instance.example.com");
            }
        });
    }
    
    @Test
    public void testFetchInstancesByIds() {
        checkByIdsAssertions(
                TableName.INSTANCES,
                new HashSet<>(Arrays.asList("id", "state")),
                Collections.singleton("test-instance"),
                Collections.singletonMap("test-instance", Arrays.asList("test-instance", "launched"))
        );
    }

    @Test
    public void testFetchInstanceNetworks() {
        checkAssertions(TableName.INSTANCE_NETWORKS, new LinkedHashMap<String, Object>(){
            {
                put("instance_id", "test-instance");
                put("network_id", "test-network");
            }
        });
    }
    
    @Test
    public void testFetchInstanceNetworksByIds() {
        checkByIdsAssertions(
                TableName.INSTANCE_NETWORKS,
                new HashSet<>(Arrays.asList("instance_id", "network_id")),
                Collections.singleton("test-instance:test-network"),
                Collections.singletonMap("test-instance:test-network", Arrays.asList("test-instance", "test-network"))
        );
    }

    @Test
    public void testFetchInstanceMetadata() {
        checkAssertions(TableName.INSTANCE_METADATA, new LinkedHashMap<String, Object>(){
            {
                put("instance_id", "test-instance");
                put("key", "region");
                put("value", "test-region");
            }
        });
    }
    
    @Test
    public void testFetchInstanceMetadataByIds() {
        checkByIdsAssertions(
                TableName.INSTANCE_METADATA,
                new HashSet<>(Arrays.asList("instance_id", "key", "value")),
                Collections.singleton("test-instance:region"),
                Collections.singletonMap("test-instance:region", Arrays.asList("test-instance", "region", "test-region"))
        );
    }

    @Test
    public void testFetchImageMetadata() {
        checkAssertions(TableName.IMAGE_METADATA, new LinkedHashMap<String, Object>(){
            {
                put("image_id", "test-image");
                put("key", "architecture");
                put("value", "x86");
            }
        });
    }
    
    @Test
    public void testFetchImageMetadataByIds() {
        checkByIdsAssertions(
                TableName.IMAGE_METADATA,
                new HashSet<>(Arrays.asList("image_id", "key", "value")),
                Collections.singleton("test-image:architecture"),
                Collections.singletonMap("test-image:architecture", Arrays.asList("test-image", "architecture", "x86"))
        );
    }

    @Test
    public void testFetchProjectMetadata() {
        checkAssertions(TableName.PROJECT_METADATA, new LinkedHashMap<String, Object>(){
            {
                put("project_id", "test-project");
                put("key", "region");
                put("value", "test-region");
            }
        });
    }
    
    @Test
    public void testFetchProjectMetadataByIds() {
        checkByIdsAssertions(
                TableName.PROJECT_METADATA,
                new HashSet<>(Arrays.asList("project_id", "key", "value")),
                Collections.singleton("test-project:region"),
                Collections.singletonMap("test-project:region", Arrays.asList("test-project", "region", "test-region"))
        );
    }

    private void checkAssertions(TableName tableName, LinkedHashMap<String, Object> expectedValues) {
        Set<String> columnNames = expectedValues.keySet();
        String tableNameString = tableName.getTableName();
        List<Column> columns = getTableColumnNames(tableNameString, columnNames);
        DataContainer data = dataFetcher.fetch(tableNameString, TEST_ALIAS, columns);
        Map<String, List<String>> columnsMap = data.getColumnsMap();
        assertThat(columnsMap.keySet(), hasSize(1));
        assertThat(columnsMap.keySet(), contains(TEST_ALIAS));
        assertThat(columnsMap.get(TEST_ALIAS), contains(columnNames.toArray()));
        List<DataRow> rows = data.getRows();
        assertThat(rows, hasSize(1));
        DataRow row = rows.get(0);
        columnNames.forEach(cn -> assertThat(row.get(cn), equalTo(expectedValues.get(cn))));
    }
    
    private List<Column> getTableColumnNames(String tableName, Set<String> columnNames) {
        List<Column> columns = new ArrayList<>();
        tablesAware.getColumns(tableName).stream()
                .filter(c -> columnNames.contains(c.getName()))
                .forEach(columns::add);
        return columns;
    }
    
    private void checkByIdsAssertions(TableName tableName, Set<String> columnNames, Set<String> ids, Map<String, List<Object>> expectedValues) {
        String tableNameString = tableName.getTableName();
        List<Column> columns = getTableColumnNames(tableNameString, columnNames);
        Map<String, List<Object>> data = dataFetcher.fetch(tableNameString, columns, ids);
        assertThat(data, equalTo(expectedValues));
    }
    
}