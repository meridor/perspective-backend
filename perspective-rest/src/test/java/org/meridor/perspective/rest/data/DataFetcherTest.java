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
    public void testFetchProjects() {
        checkAssertions(TableName.PROJECTS, new LinkedHashMap<String, Object>(){
            {
                put("id", "test-project");
                put("name", "test-project - test-region");
            }
        });
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
    public void testFetchAvailabilityZones() {
        checkAssertions(TableName.AVAILABILITY_ZONES, new LinkedHashMap<String, Object>(){
            {
                put("name", "test-zone");
                put("project_id", "test-project");
            }
        });
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
    public void testFetchNetworks() {
        checkAssertions(TableName.NETWORKS, new LinkedHashMap<String, Object>(){
            {
                put("project_id", "test-project");
                put("name", "test-network");
            }
        });
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
    public void testFetchImages() {
        checkAssertions(TableName.IMAGES, new LinkedHashMap<String, Object>(){
            {
                put("name", "test-image");
                put("state", "saved");
            }
        });
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
    public void testFetchInstances() {
        checkAssertions(TableName.INSTANCES, new LinkedHashMap<String, Object>(){
            {
                put("id", "test-instance");
                put("state", "launched");
            }
        });
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
    public void testFetchProjectMetadata() {
        checkAssertions(TableName.PROJECT_METADATA, new LinkedHashMap<String, Object>(){
            {
                put("project_id", "test-project");
                put("key", "region");
                put("value", "test-region");
            }
        });
    }
    
    private void checkAssertions(TableName tableName, LinkedHashMap<String, Object> expectedValues) {
        Set<String> columnNames = expectedValues.keySet();
        String tableNameString = tableName.getTableName();
        Set<Column> columns = new LinkedHashSet<>();
        tablesAware.getColumns(tableNameString).stream()
                .filter(c -> columnNames.contains(c.getName()))
                .forEach(columns::add);
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
    
}