package org.meridor.perspective.rest.handler;

import io.undertow.server.HttpHandler;

import java.util.Collection;

public interface HandlerProvider {

    HttpHandler provide(Collection<Object> beans);

}
