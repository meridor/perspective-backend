package org.meridor.perspective.storage;

import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;
import java.util.stream.Collectors;

@Component
public class StorageConfigurationLogger {
    
    private static final Logger LOG = LoggerFactory.getLogger(StorageConfigurationLogger.class);
    
    @Autowired
    private HazelcastInstance hazelcastInstance;
    
    @PostConstruct
    public void init() {
        listProperties();
    }
    
    private void listProperties() {
        Properties properties = hazelcastInstance.getConfig().getProperties();
        String joinedProperties = properties.stringPropertyNames().stream()
                .map(pn -> String.format("%s=%s", pn, properties.get(pn)))
                .collect(Collectors.joining("\n"));
        LOG.info("Started storage with properties: \n\n{}\n", joinedProperties);
    }
    
}
