package org.meridor.perspective.worker.misc.impl;

import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.config.Clouds;
import org.meridor.perspective.config.ObjectFactory;
import org.meridor.perspective.worker.misc.CloudConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CloudConfigurationProviderImpl implements CloudConfigurationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(CloudConfigurationProviderImpl.class);

    @Value("${perspective.configuration.file}")
    private Resource configurationFileResource;

    private final Map<String, Cloud> cloudsMap = new HashMap<>();

    @PostConstruct
    public void init() {
        if (configurationFileResource.exists()) {
            LOG.info("Loading cloud configuration from [{}]", configurationFileResource.toString());
            try (InputStream configFileInputStream = configurationFileResource.getInputStream()) {
                load(configFileInputStream);
            } catch (Exception e) {
                LOG.error("Failed to load clouds configuration", e);
            }
        } else {
            LOG.error("Configuration resource [{}] does not exist", configurationFileResource.toString());
        }
    }

    private void load(InputStream configFileInputStream) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Clouds clouds = (Clouds) unmarshaller.unmarshal(configFileInputStream);
        List<Cloud> enabledClouds = clouds.getClouds().stream()
                .filter(Cloud::isEnabled)
                .collect(Collectors.toList());
        if (enabledClouds.isEmpty()) {
            LOG.warn(
                    clouds.getClouds().isEmpty() ?
                            "Cloud configuration file is empty - will not work." :
                            "All clouds defined in configuration file are disabled - will not work."
                    
            );
        }
        enabledClouds.forEach(c -> cloudsMap.put(c.getId(), c));
    }

    public Cloud getCloud(String cloudId) {
        if (!cloudsMap.containsKey(cloudId)) {
            throw new IllegalArgumentException(String.format("Cloud with id = %s does not exist", cloudId));
        }
        return cloudsMap.get(cloudId);
    }

    public Collection<Cloud> getClouds() {
        return cloudsMap.values();
    }

}
