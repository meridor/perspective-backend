package org.meridor.perspective.rest.handler;

import java.util.Optional;

import static io.undertow.util.StatusCodes.*;

public class Response {

    private final int statusCode;

    private final Object entity;

    private final String message;

    private Response(int statusCode, Object entity, String message) {
        this.statusCode = statusCode;
        this.entity = entity;
        this.message = message;
    }

    public static Response ok() {
        return new Response(OK, null, null);
    }

    public static Response ok(Object entity) {
        return new Response(OK, entity, null);
    }

    public static Response notFound(String message) {
        return new Response(NOT_FOUND, null, message);
    }

    public static Response serviceUnavailable() {
        return new Response(SERVICE_UNAVAILABLE, null, null);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Optional<Object> getEntity() {
        return Optional.ofNullable(entity);
    }

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }
}
