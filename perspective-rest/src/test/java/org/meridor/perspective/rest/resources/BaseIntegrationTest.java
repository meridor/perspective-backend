package org.meridor.perspective.rest.resources;

import org.glassfish.jersey.server.spring.SpringLifecycleListener;
import org.glassfish.jersey.test.JerseyTest;

import javax.ws.rs.core.Application;

public abstract class BaseIntegrationTest extends JerseyTest {

    @Override
    protected Application configure() {
        org.meridor.perspective.rest.Application app = new org.meridor.perspective.rest.Application();
        app.register(SpringLifecycleListener.class);
        app.property("contextConfigLocation", "classpath:META-INF/spring/context.xml");
        return app;
    }

}
