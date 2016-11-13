package org.meridor.perspective.shell.common.repository.impl;

import org.glassfish.tyrus.client.ClientManager;
import org.meridor.perspective.beans.Letter;
import org.meridor.perspective.client.ApiAware;
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

import static org.meridor.perspective.api.SerializationUtils.unserialize;

@Component
@ClientEndpoint
public class MailRepositoryImpl implements MailRepository {

    private final Map<String, Letter> letters = new LinkedHashMap<>();

    private CountDownLatch countDownLatch;

    private final ApiProvider apiProvider;

    private final Logger logger;

    @Autowired
    public MailRepositoryImpl(ApiProvider apiProvider, Logger logger) {
        this.apiProvider = apiProvider;
        this.logger = logger;
    }

    @PostConstruct
    public void init() {
        countDownLatch = new CountDownLatch(1);
        new MailThread().run();
    }

    @PreDestroy
    public void destroy() {
        countDownLatch.countDown();
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
        } catch (IOException e) {
            logger.error(String.format("Failed to receive mail from the API: %s", e.getMessage()));
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        countDownLatch.countDown();
        init(); //Trying to reconnect
    }

    private class MailThread extends Thread {
        @Override
        public void run() {
            try {
                ClientManager client = ClientManager.createClient();
                URI endpoint = new URI(ApiAware.withUrl(apiProvider.getBaseUri()).getWebSocketUrl("mail"));
                client.connectToServer(MailRepositoryImpl.class, endpoint);
                countDownLatch.await();
            } catch (Exception e) {
                logger.warn(String.format(
                        "Failed to connect to mail endpoint. Mail functionality is disabled. Error is: %s",
                        e.getMessage()
                ));
            }
        }
    }
}
