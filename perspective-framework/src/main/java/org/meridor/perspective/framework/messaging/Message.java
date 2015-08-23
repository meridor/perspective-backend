package org.meridor.perspective.framework.messaging;

import org.meridor.perspective.config.CloudType;

import java.io.Serializable;
import java.util.Optional;

public interface Message extends Serializable {

    String getId();

    CloudType getCloudType();

    Object getPayload();

    <T> Optional<T> getPayload(Class<T> cls);

}
