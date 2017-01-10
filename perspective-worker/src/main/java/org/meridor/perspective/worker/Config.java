package org.meridor.perspective.worker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class Config {

    @Value("${perspective.fetch.delay.images:3600000}")
    private int imagesFetchDelay;

    @Value("${perspective.fetch.delay.instances:1800000}")
    private int instancesFetchDelay;

    @Value("${perspective.fetch.delay.projects:3600000}")
    private int projectsFetchDelay;

    @Value("${perspective.messaging.read.consumers:8}")
    private int readConsumers;

    @Value("${perspective.messaging.write.consumers:2}")
    private int writeConsumers;

    @Value("${perspective.worker.event.consumers:5}")
    private int eventConsumers;

    @Value("${perspective.storage.id.cache.size:1000}")
    private int idCacheSize;

    @Value("${perspective.worker.id:worker}")
    private String workerId;

    @Value("${perspective.configuration.file:classpath:clouds.xml}")
    private Resource configurationFileResource;

    public int getImagesFetchDelay() {
        return imagesFetchDelay;
    }

    public int getInstancesFetchDelay() {
        return instancesFetchDelay;
    }

    public int getProjectsFetchDelay() {
        return projectsFetchDelay;
    }

    public int getReadConsumers() {
        return readConsumers;
    }

    public int getWriteConsumers() {
        return writeConsumers;
    }

    public int getEventConsumers() {
        return eventConsumers;
    }

    public int getIdCacheSize() {
        return idCacheSize;
    }

    public String getWorkerId() {
        return workerId;
    }

    public Resource getConfigurationFileResource() {
        return configurationFileResource;
    }
}
