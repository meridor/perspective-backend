package org.meridor.perspective.rest.data;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.sql.*;
import org.meridor.perspective.sql.impl.table.Column;
import org.meridor.perspective.sql.impl.table.TableName;
import org.meridor.perspective.sql.impl.table.TablesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Contains end-to-end SQL engine tests. Real tables from rest module are needed
 * for this test to work properly.
 */
@ContextConfiguration(locations = "/META-INF/spring/query-processor-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class QueryProcessorTest {

    @Autowired
    private QueryProcessor queryProcessor;

    @Autowired
    private TablesAware tablesAware;

    @Autowired
    private InstancesAware instancesAware;
    
    @Autowired
    private ProjectsAware projectsAware;

    @Before
    public void before() {
        instancesAware.saveInstance(EntityGenerator.getInstance());
        projectsAware.saveProject(EntityGenerator.getProject());
    }

    @Test
    public void testShowTables() {
        Query query = new Query() {
            {
                setSql("show tables");
            }
        };
        List<QueryResult> queryResults = queryProcessor.process(query);
        assertThat(queryResults, hasSize(1));
        QueryResult queryResult = queryResults.get(0);
        assertThat(queryResult.getStatus(), equalTo(QueryStatus.SUCCESS));
        assertThat(queryResult.getData().getColumnNames(), contains("table_name"));
        assertThat(queryResult.getCount(), is(greaterThan(0)));
        assertThat(queryResult.getData().getRows().size(), is(greaterThan(0)));
    }

    @Test
    public void testSelectAsterisk() {
        Query query = new Query() {
            {
                setSql("select * from instances");
            }
        };
        List<QueryResult> queryResults = queryProcessor.process(query);
        assertThat(queryResults, hasSize(1));
        QueryResult queryResult = queryResults.get(0);
        assertThat(queryResult.getStatus(), equalTo(QueryStatus.SUCCESS));
        List<String> columnNamesList = tablesAware.getColumns(TableName.INSTANCES).stream()
                .map(Column::getName).collect(Collectors.toList());
        String[] columnNames = columnNamesList.toArray(new String[columnNamesList.size()]);
        assertThat(queryResult.getData().getColumnNames(), contains(columnNames));
        List<DataRow> rows = queryResult.getData().getRows();
        assertThat(rows, hasSize(1));
        assertThat(rows.get(0).get("name"), equalTo("test-instance"));
    }

    @Test
    public void testSelectQueryWithPlaceholders() {
        Query query = new Query() {
            {
                setSql("select state from instances where id = ?");
                setParameters(new ArrayList<Parameter>() {
                    {
                        add(new Parameter() {
                            {
                                setIndex(1);
                                setValue("test-instance");
                            }
                        });
                    }
                });
            }
        };
        List<QueryResult> queryResults = queryProcessor.process(query);
        assertThat(queryResults, hasSize(1));
        QueryResult queryResult = queryResults.get(0);
        assertThat(queryResult.getStatus(), equalTo(QueryStatus.SUCCESS));
        assertThat(queryResult.getData().getColumnNames(), containsInAnyOrder("state"));
        List<DataRow> rows = queryResult.getData().getRows();
        assertThat(rows, hasSize(1));
        assertThat(rows.get(0).get("state"), equalTo("launched"));
    }
    
    @Test
    public void testInnerJoinWithCondition() {
        Query query = new Query(){
            {
                setSql("select p.name as project_name, i.name as instance_name " +
                        "from instances as i inner join projects as p " +
                        "on i.project_id = p.id");
            }
        };
        List<QueryResult> queryResults = queryProcessor.process(query);
        assertThat(queryResults, hasSize(1));
        QueryResult queryResult = queryResults.get(0);
        assertThat(queryResult.getStatus(), equalTo(QueryStatus.SUCCESS));
        assertThat(queryResult.getData().getColumnNames(), contains("project_name", "instance_name"));
        List<DataRow> rows = queryResult.getData().getRows();
        assertThat(rows, hasSize(1));
        assertThat(rows.get(0).get("project_name"), equalTo("test-project - test-region"));
        assertThat(rows.get(0).get("instance_name"), equalTo("test-instance"));
    }
    
    @Test
    public void testInnerJoinWithUsingClause() {
        testInnerJoinWithUsingClause("select n.name, i.instance_id as id " +
                "from instance_networks as i inner join network_subnets as n " +
                "using (network_id)");
    }
    
    @Test
    public void testNaturalInnerJoin() {
        testInnerJoinWithUsingClause("select n.name, i.instance_id as id " +
                "from instance_networks as i natural join network_subnets as n");
    }
    
    private void testInnerJoinWithUsingClause(String sql) {
        Query query = new Query();
        query.setSql(sql);
        List<QueryResult> queryResults = queryProcessor.process(query);
        assertThat(queryResults, hasSize(1));
        QueryResult queryResult = queryResults.get(0);
        assertThat(queryResult.getStatus(), equalTo(QueryStatus.SUCCESS));
        assertThat(queryResult.getData().getColumnNames(), contains("n.name", "id"));
        List<DataRow> rows = queryResult.getData().getRows();
        assertThat(rows, hasSize(1));
        assertThat(rows.get(0).get("n.name"), equalTo("test-subnet"));
        assertThat(rows.get(0).get("id"), equalTo("test-instance"));
    }
}