package org.meridor.perspective.rest.resources;

import okhttp3.ResponseBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.client.ServiceApi;
import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.config.OperationType;
import org.meridor.perspective.framework.storage.OperationsRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import retrofit2.Response;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.config.OperationType.ADD_IMAGE;

@ContextConfiguration(locations = "/META-INF/spring/integration-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ServiceResourceTest extends BaseResourceTest<ServiceApi> {

    @Autowired
    private OperationsRegistry operationsRegistry;

    @Test
    public void testPing() throws Exception {
        Response<ResponseBody> response = getApi().ping().execute();
        assertThat(response.isSuccessful(), is(true));
    }
    
    @Test
    public void testVersion() throws Exception {
        Response<String> response = getApi().version().execute();
        assertThat(response.isSuccessful(), is(true));
        assertThat(response.body(), equalTo("unknown"));
    }

    @Test
    public void testGetSupportedOperations() throws Exception {
        operationsRegistry.addOperation(MOCK, ADD_IMAGE);
        Response<Map<CloudType, Set<OperationType>>> response = getApi().getSupportedOperations().execute();
        assertThat(response.isSuccessful(), is(true));
        assertThat(response.body(), equalTo(Collections.singletonMap(MOCK, Collections.singleton(ADD_IMAGE))));
    }

    @Override
    protected Class<ServiceApi> getApiClass() {
        return ServiceApi.class;
    }
}
