package org.meridor.perspective.rest;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import org.meridor.perspective.rest.handler.HandlerProvider;
import org.meridor.perspective.rest.handler.impl.HandlerProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.Path;

@Component
public class Server {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    private final ApplicationContext applicationContext;

    private Undertow server;

    @Value("${perspective.rest.listen.host:localhost}")
    private String host;

    @Value("${perspective.rest.listen.port:0}")
    private int port;

    @Autowired
    public Server(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        server = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(createHandler())
                .build();
        server.start();
        LOG.info("Listening on {}:{}", host, port);
    }

    public String getBaseUrl() {
        Undertow.ListenerInfo listenerInfo = server.getListenerInfo().get(0);
        return String.format("%s://%s/", listenerInfo.getProtcol(), String.valueOf(listenerInfo.getAddress()).replace("/", ""));
    }

    private HttpHandler createHandler() {
        HandlerProvider handlerProvider = new HandlerProviderImpl();
        return handlerProvider.provide(
                applicationContext.getBeansWithAnnotation(Path.class).values()
        );
    }

    @PreDestroy
    public void destroy() {
        if (server != null) {
            LOG.info("Stopping REST process");
            server.stop();
        }
    }

}
