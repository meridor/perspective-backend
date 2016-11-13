package org.meridor.perspective.worker.processor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.backend.messaging.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.backend.messaging.MessageUtils.message;

@ContextConfiguration(locations = "/META-INF/spring/dispatcher-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class WorkerDispatcherTest {

    @Autowired
    private WorkerDispatcher dispatcher;
    
    @Autowired
    private AlwaysSucceedingProcessor alwaysSucceedingProcessor;
    
    @Test
    public void testEmptyMessage() {
        assertThat(
                dispatcher
                        .dispatch(message(MOCK, null))
                        .isPresent(),
                is(false)
        );
    }
    
    @Test
    public void testUnknownProcessor() {
        assertThat(
                dispatcher
                        .dispatch(message(MOCK, "unknown_payload"))
                        .isPresent(),
                is(false)
        );
    }

    @Test
    public void testNoProcessor() {
        assertThat(
                //No processor for doubles in context
                dispatcher.dispatch(message(MOCK, 123d)).isPresent(),
                is(false)
        );
    }

    @Test
    public void testProcessingException() {
        Message message = message(MOCK, 42); //Mock processor for Integers is always failing
        assertThat(
                dispatcher.dispatch(message),
                equalTo(Optional.of(message))
        );
    }
    
    @Test
    public void testProcessingSuccess() {
        assertThat(
                //Mock processor for String is always succeeding
                dispatcher.dispatch(message(MOCK, "payload")).isPresent(),
                is(false)
        );
        assertThat(alwaysSucceedingProcessor.wasProcessed(), is(true));
    }
    
}