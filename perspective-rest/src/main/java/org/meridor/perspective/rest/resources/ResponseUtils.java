package org.meridor.perspective.rest.resources;

import javax.ws.rs.core.Response;

public final class ResponseUtils {
    
    public static Response clientError(final String message) {
        return Response.status(new Response.StatusType() {
            @Override
            public int getStatusCode() {
                return Response.Status.BAD_REQUEST.getStatusCode();
            }

            @Override
            public Response.Status.Family getFamily() {
                return Response.Status.Family.CLIENT_ERROR;
            }

            @Override
            public String getReasonPhrase() {
                return message;
            }
        }).build();
    }
    
    public static Response notFound(final String message) {
        return Response.status(new Response.StatusType() {
            @Override
            public int getStatusCode() {
                return Response.Status.NOT_FOUND.getStatusCode();
            }

            @Override
            public Response.Status.Family getFamily() {
                return Response.Status.Family.CLIENT_ERROR;
            }

            @Override
            public String getReasonPhrase() {
                return message;
            }
        }).build();
    }
    
}
