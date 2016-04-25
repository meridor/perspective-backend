package org.meridor.perspective.worker.processor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.events.EventFactory;
import org.meridor.perspective.events.InstanceLaunchingEvent;
import org.meridor.perspective.framework.EntityGenerator;
import org.meridor.perspective.framework.storage.InstancesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.yandex.qatools.fsm.Yatomata;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/fsm-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class InstanceFSMTest {

    @Autowired
    private FSMBuilderAware fsmBuilderAware;

    @Autowired
    private InstancesAware storage;
    
    @Test
    public void testLongLaunch() throws Exception {
        Instance instanceOne = EntityGenerator.getInstance();
        assertThat(storage.instanceExists(instanceOne.getId()), is(false));
        ZonedDateTime now = ZonedDateTime.now();
        instanceOne.setTimestamp(now.minusSeconds(10));
        InstanceLaunchingEvent eventOne = EventFactory.instanceEvent(InstanceLaunchingEvent.class, instanceOne);
        eventOne.setSync(true);
        Yatomata<InstanceFSM> fsm = fsmBuilderAware.get(InstanceFSM.class).build();
        fsm.fire(eventOne);
        assertThat(storage.instanceExists(instanceOne.getId()), is(true));
        
        //Emulating eviction
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.schedule(() -> {
            storage.getInstances().removeIf(
                    i -> Duration.between(i.getTimestamp(), now).getSeconds() > 5
            );
        }, 100, TimeUnit.MILLISECONDS);

        Thread.sleep(50);
        Instance instanceTwo = EntityGenerator.getInstance(); //This is the same instance but with more recent timestamp
        instanceTwo.setTimestamp(now);
        InstanceLaunchingEvent eventTwo = EventFactory.instanceEvent(InstanceLaunchingEvent.class, instanceTwo);
        eventTwo.setSync(true);
        fsm.fire(eventTwo);
        
        Thread.sleep(100);
        assertThat(storage.instanceExists(instanceOne.getId()), is(true));
    }

}