package org.meridor.perspective.rest.handler.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.undertow.Handlers;
import io.undertow.io.Receiver;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import org.meridor.perspective.api.ObjectMapperFactory;
import org.meridor.perspective.rest.handler.HandlerProvider;
import org.meridor.perspective.rest.handler.Response;

import javax.ws.rs.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.undertow.util.Headers.ACCEPT;
import static io.undertow.util.Headers.CONTENT_TYPE;
import static io.undertow.util.StatusCodes.UNSUPPORTED_MEDIA_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class HandlerProviderImpl implements HandlerProvider {

    private static final String SLASH = "/";
    private static final String DEFAULT_CONTENT_TYPE = APPLICATION_JSON;

    @Override
    public HttpHandler provide(Collection<Object> beans) {
        return provideImpl(beans);
    }

    private HttpHandler provideImpl(Collection<Object> beans) {
        RoutingHandler root = Handlers.routing();
        beans.forEach(bean -> {
            Class<?> cls = bean.getClass();
            String rootPath = cls.isAnnotationPresent(Path.class) ?
                    cls.getAnnotation(Path.class).value()
                    : SLASH;
            Arrays.stream(cls.getMethods()).forEach(m -> {
                HttpString method = getHttpMethod(m);
                HttpHandler handler = getHandler(bean, m);
                String template = getTemplate(m);
                String fullTemplate = getFullTemplate(rootPath, template);
                root.add(method, fullTemplate, handler);
            });
        });
        return root;
    }

    private String getFullTemplate(String rootPath, String template) {
        if (SLASH.equals(template)) {
            return rootPath;
        }
        if (SLASH.equals(rootPath) && template.startsWith(SLASH)) {
            return template;
        }
        return rootPath + template;
    }

    private HttpString getHttpMethod(Method method) {
        if (method.isAnnotationPresent(POST.class)) {
            return Methods.POST;
        } else if (method.isAnnotationPresent(PUT.class)) {
            return Methods.PUT;
        } else if (method.isAnnotationPresent(DELETE.class)) {
            return Methods.DELETE;
        } else if (method.isAnnotationPresent(OPTIONS.class)) {
            return Methods.OPTIONS;
        } else if (method.isAnnotationPresent(HEAD.class)) {
            return Methods.HEAD;
        } else {
            return Methods.GET;
        }
    }

    private String getTemplate(Method method) {
        return method.isAnnotationPresent(Path.class) ?
                method.getAnnotation(Path.class).value()
                : SLASH;
    }

    private HttpHandler getHandler(Object bean, Method method) {
        List<String> inputMediaTypes = getMediaTypes(method, Consumes.class, Consumes::value);
        List<String> outputMediaTypes = getMediaTypes(method, Produces.class, Produces::value);
        return exchange -> {
            Optional<String> supportedRequestContentType = getSupportedContentType(exchange, CONTENT_TYPE, inputMediaTypes);
            if (supportedRequestContentType.isPresent()) {
                getBody(exchange, method, body -> {
                    try {
                        List<Object> parameterValues = Arrays.stream(method.getParameters())
                                .map(p -> getParameterValue(p, body, exchange))
                                .collect(Collectors.toList());
                        Object result = method.invoke(bean, parameterValues.toArray());
                        Optional<String> supportedResponseContentType = getSupportedContentType(exchange, ACCEPT, outputMediaTypes);
                        if (supportedResponseContentType.isPresent()) {
                            exchange.getResponseHeaders().put(CONTENT_TYPE, supportedResponseContentType.get());
                            writeResultIfNeeded(exchange, method.getReturnType(), result);
                        } else {
                            exchange.setStatusCode(UNSUPPORTED_MEDIA_TYPE);
                            exchange.endExchange();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } else {
                exchange.setStatusCode(UNSUPPORTED_MEDIA_TYPE);
                exchange.endExchange();
            }
        };
    }

    private JavaType getBodyType(ObjectMapper objectMapper, Method method) {
        Optional<Parameter> bodyParameterCandidate = Arrays.stream(method.getParameters())
                .filter(p -> p.getAnnotations().length == 0)
                .findFirst();
        if (!bodyParameterCandidate.isPresent()) {
            return objectMapper.getTypeFactory().constructType(Object.class);
        }
        Parameter bodyParameter = bodyParameterCandidate.get();
        Type type = bodyParameter.getParameterizedType();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            List<? extends Class<?>> parameterClasses = Arrays.stream(parameterizedType.getActualTypeArguments())
                    .map(pt -> (Class<?>) pt)
                    .collect(Collectors.toList());
            return objectMapper.getTypeFactory().constructParametricType(
                    (Class<?>) parameterizedType.getRawType(),
                    parameterClasses.toArray(new Class[parameterClasses.size()])
            );
        }
        return objectMapper.getTypeFactory().constructType(type);
    }

    private Object getParameterValue(Parameter parameter, Object body, HttpServerExchange exchange) {
        if (parameter.isAnnotationPresent(PathParam.class)) {
            return getPathParameterValue(parameter, exchange);
        } else if (parameter.isAnnotationPresent(QueryParam.class)) {
            return getQueryParameterValue(parameter, exchange);
        } else if (parameter.isAnnotationPresent(HeaderParam.class)) {
            return getHeaderParameterValue(parameter, exchange);
        } else if (parameter.isAnnotationPresent(CookieParam.class)) {
            return getCookieParameterValue(parameter, exchange);
        } else if (parameter.isAnnotationPresent(FormParam.class)) {
            throw new UnsupportedOperationException("Form parameters are currently not supported");
        } else {
            return body;
        }
    }

    private Object getPathParameterValue(Parameter parameter, HttpServerExchange exchange) {
        String parameterName = parameter.getAnnotation(PathParam.class).value();
        //This is strange but path parameters in RoutingHandler in fact go to exchange query parameters  
        Map<String, Deque<String>> pathParameters = exchange.getQueryParameters();
        return (pathParameters.containsKey(parameterName)) ?
                pathParameters.get(parameterName).getFirst() : null;
    }

    private Object getQueryParameterValue(Parameter parameter, HttpServerExchange exchange) {
        String parameterName = parameter.getAnnotation(QueryParam.class).value();
        Map<String, Deque<String>> queryParameters = exchange.getQueryParameters();
        return (queryParameters.containsKey(parameterName)) ?
                queryParameters.get(parameterName).getFirst() : null;
    }

    private Object getHeaderParameterValue(Parameter parameter, HttpServerExchange exchange) {
        String parameterName = parameter.getAnnotation(HeaderParam.class).value();
        Map<String, Deque<String>> headerParameters = exchange.getQueryParameters();
        return (headerParameters.containsKey(parameterName)) ?
                headerParameters.get(parameterName).getFirst() : null;
    }

    private Object getCookieParameterValue(Parameter parameter, HttpServerExchange exchange) {
        String parameterName = parameter.getAnnotation(CookieParam.class).value();
        Map<String, Cookie> cookiesMap = exchange.getRequestCookies();
        return (cookiesMap.containsKey(parameterName)) ?
                cookiesMap.get(parameterName).getValue() : null;
    }

    private <T extends Annotation> List<String> getMediaTypes(Method method, Class<T> cls, Function<T, String[]> mediaTypeProvider) {
        return method.isAnnotationPresent(cls) ?
                Arrays.asList(mediaTypeProvider.apply(method.getAnnotation(cls))) :
                Collections.singletonList(DEFAULT_CONTENT_TYPE);
    }

    private Optional<String> getSupportedContentType(HttpServerExchange exchange, HttpString header, List<String> supportedContentTypes) {
        Optional<String> contentTypeCandidate = getHeader(exchange, header);
        return contentTypeCandidate.isPresent() ?
                getSupportedContentType(contentTypeCandidate.get(), supportedContentTypes) :
                Optional.ofNullable(supportedContentTypes.get(0));
    }

    private Optional<String> getSupportedContentType(String contentTypeCandidate, List<String> supportedContentTypes) {
        return supportedContentTypes.stream()
                .filter(contentTypeCandidate::contains)
                .findFirst();
    }

    private Optional<String> getHeader(HttpServerExchange exchange, HttpString headerName) {
        HeaderMap requestHeaders = exchange.getRequestHeaders();
        return requestHeaders.contains(headerName) ?
                Optional.ofNullable(requestHeaders.get(headerName).getFirst()) :
                Optional.empty();
    }

    private void getBody(HttpServerExchange exchange, Method method, Consumer<Object> consumer) {
        Receiver requestReceiver = exchange.getRequestReceiver();
        requestReceiver.receiveFullBytes(
                (exc, bytes) -> {
                    try {
                        ObjectMapper objectMapper = ObjectMapperFactory.createDefaultMapper();
                        JavaType bodyType = getBodyType(objectMapper, method);
                        Object body = (bytes.length > 0) ?
                                objectMapper.readValue(bytes, bodyType) : null;
                        consumer.accept(body);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private void writeResultIfNeeded(
            HttpServerExchange exchange, Class<?> returnType, Object result) {
        try {
            if (Response.class.isAssignableFrom(returnType)) {
                Response response = Response.class.cast(result);
                exchange.setStatusCode(response.getStatusCode());
                Optional<String> messageCandidate = response.getMessage();
                if (messageCandidate.isPresent()) {
                    exchange.setReasonPhrase(messageCandidate.get());
                }
                Optional<Object> entityCandidate = response.getEntity();
                if (entityCandidate.isPresent()) {
                    Object entity = entityCandidate.get();
                    write(entity, exchange);
                }
            } else if (result != null) {
                write(result, exchange);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void write(Object body, HttpServerExchange exchange) throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.createDefaultMapper();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(outputStream, body);
        exchange.getResponseSender().send(new String(outputStream.toByteArray()));
    }
}
