package org.meridor.perspective.backend.messaging;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.backend.messaging.impl.MessageImpl;

import java.io.Serializable;
import java.util.Optional;

public final class MessageUtils {

    private static final int DEFAULT_TTL = 1;
    
    public static Optional<Message> retry(Message message) {
        int ttl = message.getTtl();
        if (ttl > 1) {
            return Optional.of(message(message.getCloudType(), message.getPayload(), ttl - 1));
        }
        return Optional.empty();
    }
    
    public static Message message(CloudType cloudType, Serializable payload, int ttl) {
        return new MessageImpl(cloudType, payload, ttl);
    }
    
    public static Message message(CloudType cloudType, Serializable payload) {
        return message(cloudType, payload, DEFAULT_TTL);
    }

    public static Message message(Serializable payload) {
        return message(null, payload);
    }

    public static String getRealQueueName(String queueName, CloudType cloudType) {
        return cloudType != null ? 
                String.format("%s_%s", cloudType.value(), queueName) : queueName;
    }

}
