package org.meridor.perspective.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {

    @Value("${perspective.rest.listen.host:localhost}")
    private String host;

    @Value("${perspective.rest.listen.port:0}")
    private int port;

    @Value("${perspective.messaging.mail.consumers:1}")
    private int mailParallelConsumers;

    @Value("${perspective.messaging.mail.cache.size:1000}")
    private int mailMaxCacheSize;

    @Value("${perspective.messaging.max.retries:5}")
    private int messagingMaxRetries;

    String getHost() {
        return host;
    }

    int getPort() {
        return port;
    }

    public int getMailParallelConsumers() {
        return mailParallelConsumers;
    }

    public int getMailMaxCacheSize() {
        return mailMaxCacheSize;
    }

    public int getMessagingMaxRetries() {
        return messagingMaxRetries;
    }
}
