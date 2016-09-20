package org.meridor.perspective.rest.resources;

import org.junit.runner.RunWith;
import org.meridor.perspective.client.ApiAware;
import org.meridor.perspective.rest.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = "/META-INF/spring/integration-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class BaseResourceTest<T> {

    @Autowired
    private Server server;

    T getApi() {
        return ApiAware.withUrl(server.getBaseUrl()).get(getApiClass());
    }

    protected abstract Class<T> getApiClass();
}
