package org.meridor.perspective.worker.processor.event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.meridor.perspective.backend.messaging.Message;
import org.meridor.perspective.events.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Arrays;
import java.util.Collection;

import static org.meridor.perspective.backend.EntityGenerator.getInstance;
import static org.meridor.perspective.backend.messaging.MessageUtils.message;
import static org.meridor.perspective.config.CloudType.MOCK;
import static org.meridor.perspective.events.EventFactory.instanceEvent;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@RunWith(Parameterized.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class InstanceOperationFailureListenerTest extends BaseOperationFailureTest {

    @Parameterized.Parameters(name = "Failing \"{0}\" should be processed")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {InstanceLaunchingEvent.class},
                {InstanceRebootingEvent.class},
                {InstanceHardRebootingEvent.class},
                {InstanceShuttingDownEvent.class},
                {InstancePausingEvent.class},
                {InstanceResumingEvent.class},
                {InstanceRebuildingEvent.class},
                {InstanceResizingEvent.class},
                {InstanceStartingEvent.class},
                {InstanceSuspendingEvent.class},
                {InstanceMigratingEvent.class},
                {InstanceDeletingEvent.class},
                {InstanceErrorEvent.class},
        });
    }

    private final Class<? extends InstanceEvent> cls;

    public InstanceOperationFailureListenerTest(Class<? extends InstanceEvent> cls) {
        this.cls = cls;
    }
    
    @Test
    public void testListenerWorks() throws Exception {
        InstanceEvent instanceEvent = instanceEvent(cls, getInstance());
        Message message = message(MOCK, instanceEvent);
        assertListenerWorks(message);
    }
    

}