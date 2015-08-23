package org.meridor.perspective.worker.misc;

import org.meridor.perspective.config.Cloud;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public interface CloudConfigurationProvider {

    Cloud getCloud(String cloudId);

    Collection<Cloud> getClouds();

}
