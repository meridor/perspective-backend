package org.meridor.perspective.shell.common.repository.impl;

import org.glassfish.tyrus.client.ClientManager;
import org.meridor.perspective.beans.Letter;
import org.meridor.perspective.client.ApiAware;
import org.meridor.perspective.common.events.EventBus;
import org.meridor.perspective.common.events.EventListener;
import org.meridor.perspective.shell.common.events.PromptChangedEvent;
import org.meridor.perspective.shell.common.events.ShellStartedEvent;
import org.meridor.perspective.shell.common.misc.Logger;
import org.meridor.perspective.shell.common.repository.ApiProvider;
import org.meridor.perspective.shell.common.repository.MailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.meridor.perspective.api.SerializationUtils.unserialize;

@Component
@ClientEndpoint
public class MailRepositoryImpl implements MailRepository, EventListener<ShellStartedEvent> {

    private final Map<String, Letter> letters = new LinkedHashMap<>();

    private CountDownLatch countDownLatch;
    
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final ApiProvider apiProvider;

    private final Logger logger;

    private final EventBus eventBus;
    
    @Autowired
    public MailRepositoryImpl(ApiProvider apiProvider, Logger logger, EventBus eventBus) {
        this.apiProvider = apiProvider;
        this.logger = logger;
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void init() {
        eventBus.addListener(ShellStartedEvent.class, this);
    }
    
    @Override
    public void onEvent(ShellStartedEvent event) {
        connect();
    }

    private void connect() {
        countDownLatch = new CountDownLatch(1);
        executorService.submit(new MailRunnable());
    }
    
    @PreDestroy
    public void destroy() {
        countDownLatch.countDown();
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    @Override
    public List<Letter> getLetters() {
        return new ArrayList<>(letters.values());
    }

    @Override
    public void deleteLetter(String id) {
        if (letters.containsKey(id)) {
            letters.remove(id);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Letter letter = unserialize(message, Letter.class);
            letters.put(letter.getId(), letter);
            eventBus.fire(new PromptChangedEvent());
        } catch (IOException e) {
            logger.error(String.format("Failed to receive mail from the API: %s", e.getMessage()));
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        countDownLatch.countDown();
        if (!executorService.isShutdown()) {
            connect(); //Trying to reconnect
        }
    }

    private class MailRunnable implements Runnable {
        @Override
        public void run() {
            ClientManager client = ClientManager.createClient();
            try {
                URI endpoint = new URI(ApiAware.withUrl(apiProvider.getBaseUri()).getWebSocketUrl("mail"));
                client.connectToServer(MailRepositoryImpl.class, endpoint);
                countDownLatch.await();
                client.shutdown();
            } catch (Exception e) {
                logger.warn(String.format(
                        "Failed to connect to mail endpoint. Mail functionality is disabled. Error is: %s",
                        e.getMessage()
                ));
            }
        }
    }
}
