package org.meridor.perspective.rest.resources;

import io.undertow.websockets.core.WebSocketCallback;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import org.meridor.perspective.beans.DestinationName;
import org.meridor.perspective.beans.Letter;
import org.meridor.perspective.framework.messaging.Dispatcher;
import org.meridor.perspective.framework.messaging.Message;
import org.meridor.perspective.framework.messaging.impl.BaseConsumer;
import org.meridor.perspective.rest.handler.WebsocketResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.meridor.perspective.api.SerializationUtils.serialize;

@SuppressWarnings("RestResourceMethodInspection")
@Component
@Path("/mail")
public class MailResource extends BaseConsumer implements Dispatcher, WebsocketResource {

    private static final Logger LOG = LoggerFactory.getLogger(MailResource.class);

    @Value("${perspective.messaging.mail.consumers:1}")
    private int parallelConsumers;

    @Value("${perspective.messaging.mail.cache.size:1000}")
    private int maxCacheSize;

    private final List<Letter> cache = new ArrayList<>();

    private WebSocketChannel channel;

    @Override
    public Optional<Message> dispatch(Message message) {
        Object payload = message.getPayload();
        if (payload instanceof Letter) {
            Letter letter = (Letter) payload;
            if (channel != null) {
                while (cache.size() > 0) {
                    Letter letterFromCache = cache.remove(0);
                    sendLetter(letterFromCache, channel);
                }
                sendLetter(letter, channel);
            } else {
                if (maxCacheSize > 0 && cache.size() >= maxCacheSize) {
                    cache.remove(0);
                }
                cache.add(letter);
            }
        } else {
            LOG.warn("Skipping message {} as it does not contain letter", message);
        }
        return Optional.empty();
    }

    private static void sendLetter(Letter letter, WebSocketChannel channel) {
        try {
            WebSockets.sendText(serialize(letter), channel, new WebSocketCallback<Void>() {
                @Override
                public void complete(WebSocketChannel channel, Void context) {
                    LOG.debug("Successfully sent {}", letter);
                }

                @Override
                public void onError(WebSocketChannel channel, Void context, Throwable t) {
                    LOG.error(String.format("Failed to send %s", letter), t);
                }
            });
        } catch (IOException e) {
            LOG.error(String.format("Failed to serialize %s", letter), e);
        }
    }

    @Override
    protected String getStorageKey() {
        return DestinationName.MAIL.value();
    }

    @Override
    protected int getParallelConsumers() {
        return parallelConsumers;
    }

    @Override
    protected Dispatcher getDispatcher() {
        return this;
    }

    @Override
    public void onOpen(WebSocketChannel channel) {
        this.channel = channel;
    }
}
