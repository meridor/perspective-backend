package org.meridor.perspective.framework.messaging;

import org.meridor.perspective.config.CloudType;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public interface Message extends Serializable {

    String getId();
    
    int getTtl();
    
    CloudType getCloudType();

    Object getPayload();

    <T> Optional<T> getPayload(Class<T> cls);

}
