package org.meridor.perspective.rest.resources;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.meridor.perspective.client.QueryApi;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.framework.storage.ImagesAware;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.meridor.perspective.framework.storage.ProjectsAware;
import org.meridor.perspective.sql.Query;
import org.meridor.perspective.sql.QueryResult;
import org.meridor.perspective.sql.QueryStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ContextConfiguration(locations = "/META-INF/spring/integration-test-context.xml")
@RunWith(Parameterized.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class QueryResourceTest extends BaseResourceTest<QueryApi> {

    @Parameterized.Parameters(name = "Query \"{0}\" should work")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "select * from projects" },
                { "select * from instances" },
                { "select * from images" }
        });
    }

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private ProjectsAware projectsAware;

    @Autowired
    private InstancesAware instancesAware;
    
    @Autowired
    private ImagesAware imagesAware;

    private final String sql;

    public QueryResourceTest(String sql) {
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
        Query query = new Query();
        query.setSql(sql);
        List<Query> queries = new ArrayList<>();
        queries.add(query);
        Response<Collection<QueryResult>> response = getApi().query(queries).execute();
        assertThat(response.isSuccessful(), is(true));
        List<QueryResult> queryResults = new ArrayList<>(response.body());
        assertThat(queryResults, hasSize(1));
        QueryResult queryResult = queryResults.get(0);
        assertThat(queryResult.getStatus(), equalTo(QueryStatus.SUCCESS));
        assertThat(queryResult.getCount(), equalTo(1));
        assertThat(queryResult.getData().getRows(), hasSize(1));
    }

    @Override
    protected Class<QueryApi> getApiClass() {
        return QueryApi.class;
    }
}
