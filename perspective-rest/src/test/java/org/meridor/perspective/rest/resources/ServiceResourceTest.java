package org.meridor.perspective.rest.resources;

import okhttp3.ResponseBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.client.ServiceApi;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import retrofit2.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@ContextConfiguration(locations = "/META-INF/spring/integration-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ServiceResourceTest extends BaseResourceTest<ServiceApi> {

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

    @Override
    protected Class<ServiceApi> getApiClass() {
        return ServiceApi.class;
    }
}
