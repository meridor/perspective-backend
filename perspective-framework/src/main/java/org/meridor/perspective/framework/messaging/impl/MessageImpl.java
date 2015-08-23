package org.meridor.perspective.framework.messaging.impl;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.framework.messaging.Message;

import java.util.Optional;
import java.util.UUID;

public class MessageImpl implements Message {

    private final String id;

    private final CloudType cloudType;

    private final Object payload;

    public MessageImpl(CloudType cloudType, Object payload) {
        this.id = UUID.randomUUID().toString();
        this.cloudType = cloudType;
        this.payload = payload;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public CloudType getCloudType() {
        return cloudType;
    }

    @Override
    public Object getPayload() {
        return payload;
    }

    @Override
    public <T> Optional<T> getPayload(Class<T> cls) {
        if (payload == null) {
            return Optional.empty();
        }
        if (!cls.isAssignableFrom(payload.getClass())) {
            throw new IllegalArgumentException(String.format(
                    "Incompatible types %s and %s",
                    cls.getCanonicalName(),
                    payload.getClass().getCanonicalName()
            ));
        }
        return Optional.of(cls.cast(payload));
    }
}
