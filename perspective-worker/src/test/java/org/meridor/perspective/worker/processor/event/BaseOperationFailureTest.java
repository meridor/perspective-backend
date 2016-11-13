package org.meridor.perspective.worker.processor.event;

import org.junit.ClassRule;
import org.junit.Rule;
import org.meridor.perspective.backend.messaging.Message;
import org.meridor.perspective.backend.messaging.TestStorage;
import org.meridor.perspective.beans.DestinationName;
import org.meridor.perspective.beans.Letter;
import org.meridor.perspective.common.events.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/failure-listener-context.xml")
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public abstract class BaseOperationFailureTest {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private EventBus eventBus;

    @Autowired
    private TestStorage storage;

    void assertListenerWorks(Message notProcessedMessage) throws Exception {
        eventBus.fire(new MessageNotProcessedEvent(notProcessedMessage));
        BlockingQueue<Object> queue = storage.getQueue(DestinationName.MAIL.value());
        assertThat(queue, hasSize(1));
        Object messageWithLetterObject = queue.poll(100, TimeUnit.MILLISECONDS);
        assertThat(messageWithLetterObject, is(instanceOf(Message.class)));
        Message messageWithLetter = (Message) messageWithLetterObject;
        assertThat(messageWithLetter.getCloudType(), is(nullValue()));
        Serializable letterCandidate = messageWithLetter.getPayload();
        assertThat(letterCandidate, is(instanceOf(Letter.class)));
        Letter letter = (Letter) letterCandidate;
        assertThat(letter.getId(), is(notNullValue()));
        assertThat(letter.getText(), is(notNullValue()));
        assertThat(letter.getType(), is(notNullValue()));
        assertThat(letter.getTimestamp(), is(notNullValue()));
    }

}
