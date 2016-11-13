package org.meridor.perspective.rest.resources;

import org.glassfish.tyrus.client.ClientManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.DestinationName;
import org.meridor.perspective.beans.Letter;
import org.meridor.perspective.client.ApiAware;
import org.meridor.perspective.framework.messaging.Destination;
import org.meridor.perspective.framework.messaging.Producer;
import org.meridor.perspective.rest.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.api.SerializationUtils.unserialize;
import static org.meridor.perspective.framework.messaging.MessageUtils.message;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/integration-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@ClientEndpoint
public class MailResourceTest {

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    private static final Letter FIRST_LETTER = createLetter("first", "first-text");
    private static final Letter SECOND_LETTER = createLetter("second", "second-text");

    private final List<Exception> exceptions = new ArrayList<>();
    private final List<Letter> letters = new ArrayList<>();

    private final CountDownLatch connectionLatch = new CountDownLatch(1);
    private final CountDownLatch receiveLatch = new CountDownLatch(2);

    @Destination(DestinationName.MAIL)
    private Producer producer;

    @Autowired
    private Server server;

    @Test
    public void testSendLetter() throws Exception {
        putLetterToQueue(FIRST_LETTER); //This one goes to cache as no client connection exists
        ClientManager client = ClientManager.createClient();
        URI endpoint = new URI(ApiAware.withUrl(server.getBaseUrl()).getWebSocketUrl("mail"));
        client.connectToServer(this, endpoint);
        connectionLatch.await();
        putLetterToQueue(SECOND_LETTER); //This one should be sent immediately
        receiveLatch.await();
        assertThat(exceptions, is(empty()));
        assertThat(letters, containsInAnyOrder(FIRST_LETTER, SECOND_LETTER));
    }

    private static Letter createLetter(String id, String text) {
        Letter letter = new Letter();
        letter.setId(id);
        letter.setText(text);
        letter.setTimestamp(ZonedDateTime.now());
        return letter;
    }

    private void putLetterToQueue(Letter letter) {
        producer.produce(message(letter));
    }

    @OnOpen
    public void onOpen(Session session) {
        connectionLatch.countDown();
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Letter letter = unserialize(message, Letter.class);
            letters.add(letter);
            receiveLatch.countDown();
        } catch (Exception e) {
            exceptions.add(e);
        }
    }

}