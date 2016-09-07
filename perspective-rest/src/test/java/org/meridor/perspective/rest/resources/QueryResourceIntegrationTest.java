package org.meridor.perspective.rest.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;
import org.meridor.perspective.sql.QueryStatus;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(Parameterized.class)
public class QueryResourceIntegrationTest extends BaseIntegrationTest {

    @Parameterized.Parameters(name = "Query \"{0}\" should work")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "select * from projects" },
                { "select * from instances" },
                { "select * from images" }
        });
    }

    private final String sql;

    @Autowired
    private ProjectsAware projectsAware;

    @Autowired
    private InstancesAware instancesAware;
    
    @Autowired
    private ImagesAware imagesAware;

    public QueryResourceIntegrationTest(String sql) {
        this.sql = sql;
    }
    
    @Before
    public void before() {
        projectsAware.saveProject(EntityGenerator.getProject());
        instancesAware.saveInstance(EntityGenerator.getInstance());
        imagesAware.saveImage(EntityGenerator.getImage());
    }
    
    @Test
    public void testExecuteQuery() throws Exception {
        Thread.sleep(500);
        Query query = new Query();
        query.setSql(sql);
        List<Query> queries = new ArrayList<>();
        queries.add(query);
        Entity<List<Query>> entity = Entity.entity(queries, MediaType.APPLICATION_JSON);
        GenericType<List<QueryResult>> resultType = new GenericType<List<QueryResult>>(){};
        List<QueryResult> queryResults = target("/query")
                .request()
                .post(entity, resultType);
        assertThat(queryResults, hasSize(1));
        QueryResult queryResult = queryResults.get(0);
        assertThat(queryResult.getStatus(), equalTo(QueryStatus.SUCCESS));
        assertThat(queryResult.getCount(), equalTo(1));
        assertThat(queryResult.getData().getRows(), hasSize(1));
    }
    
}
