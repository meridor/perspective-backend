package org.meridor.perspective.framework.messaging;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.DestinationName;
import org.meridor.perspective.framework.messaging.impl.ProducerImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/mocked-storage-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class ProduceBeanPostProcessorTest {
    
    private Producer simpleProducer;
    
    @Destination(DestinationName.READ_TASKS)
    private Producer producerWithDestination;
    
    @Test
    public void testSimpleProducer() {
        doTest(simpleProducer, getClass().getCanonicalName());
    }
    
    @Test
    public void testProducerWithDestination() {
        doTest(producerWithDestination, DestinationName.READ_TASKS.value());
    }
    
    private void doTest(Producer producerField, String correctQueueName) {
        assertThat(producerField, is(notNullValue()));
        assertThat(producerField, is(instanceOf(ProducerImpl.class)));
        ProducerImpl producerImpl = (ProducerImpl) producerField;
        assertThat(producerImpl.getQueueName(), equalTo(correctQueueName));
    }
    
}