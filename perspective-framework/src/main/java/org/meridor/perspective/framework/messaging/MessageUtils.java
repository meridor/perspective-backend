package org.meridor.perspective.framework.messaging;

import org.meridor.perspective.config.CloudType;
import org.meridor.perspective.framework.messaging.impl.MessageImpl;

import java.util.Optional;

public final class MessageUtils {

    public static Optional<Message> retry(Message message) {
        int ttl = message.getTtl();
        if (ttl > 1) {
            return Optional.of(message(message.getCloudType(), message.getPayload(), message.getTtl() - 1));
        }
        return Optional.empty();
    }
    
    public static Message message(CloudType cloudType, Object payload, int ttl) {
        if (cloudType == null) {
            throw new IllegalArgumentException("Cloud type can't be null");
        }
        return new MessageImpl(cloudType, payload, ttl);
    }
    
    public static Message message(CloudType cloudType, Object payload) {
        return message(cloudType, payload, 1);
    }

    public static String getRealQueueName(String queueName, CloudType cloudType) {
        return String.format("%s_%s", cloudType.value(), queueName);
    }
    
    private MessageUtils() {
        
    }

}
