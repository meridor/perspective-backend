package org.meridor.perspective.worker.processor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.InstanceState;
import org.meridor.perspective.events.*;
import org.meridor.perspective.backend.EntityGenerator;
import org.meridor.perspective.backend.storage.InstancesAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.yandex.qatools.fsm.Yatomata;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.beans.InstanceState.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/fsm-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class InstanceFSMTest {

    @Autowired
    private FSMBuilderAware fsmBuilderAware;

    @Autowired
    private InstancesAware instancesAware;
    
    @Test
    public void testLongLaunch() throws Exception {
        Instance instanceOne = EntityGenerator.getInstance();
        assertThat(instancesAware.instanceExists(instanceOne.getId()), is(false));
        ZonedDateTime now = ZonedDateTime.now();
        instanceOne.setTimestamp(now.minusSeconds(10));
        InstanceLaunchingEvent eventOne = EventFactory.instanceEvent(InstanceLaunchingEvent.class, instanceOne);
        eventOne.setSync(true);
        Yatomata<InstanceFSM> fsm = fsmBuilderAware.get(InstanceFSM.class).build();
        fsm.fire(eventOne);
        assertThat(instancesAware.instanceExists(instanceOne.getId()), is(true));
        
        //Emulating eviction
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.schedule(() -> instancesAware.getInstances().removeIf(
                i -> Duration.between(i.getTimestamp(), now).getSeconds() > 5
        ), 100, TimeUnit.MILLISECONDS);

        Thread.sleep(50);
        Instance instanceTwo = EntityGenerator.getInstance(); //This is the same instance but with more recent timestamp
        instanceTwo.setTimestamp(now);
        InstanceLaunchingEvent eventTwo = EventFactory.instanceEvent(InstanceLaunchingEvent.class, instanceTwo);
        eventTwo.setSync(true);
        fsm.fire(eventTwo);
        
        Thread.sleep(100);
        assertThat(instancesAware.instanceExists(instanceOne.getId()), is(true));
    }

    @Test
    public void testOnInstanceQueued() {
        testOnInstanceSyncEvent(InstanceQueuedEvent.class, QUEUED);
    }
    
    @Test
    public void testOnInstanceLaunching() {
        testOnInstanceSyncEvent(InstanceLaunchingEvent.class, LAUNCHING);
    }
    @Test
    public void onInstanceAdding() {
        testOnInstanceEvent(InstanceLaunchingEvent.class, LAUNCHING, false);
    }

    @Test
    public void testOnInstanceLaunched() {
        testOnInstanceSyncEvent(InstanceLaunchedEvent.class, LAUNCHED);
    }
    
    @Test
    public void testOnInstanceRebooting() {
        testOnInstanceSyncEvent(InstanceRebootingEvent.class, REBOOTING);
    }

    @Test
    public void testOnInstanceHardRebooting() {
        testOnInstanceSyncEvent(InstanceHardRebootingEvent.class, HARD_REBOOTING);
    }

    @Test
    public void testOnInstanceShuttingDown() {
        testOnInstanceSyncEvent(InstanceShuttingDownEvent.class, SHUTTING_DOWN);
    }

    @Test
    public void testOnInstanceShutoff() {
        testOnInstanceSyncEvent(InstanceShutOffEvent.class, SHUTOFF);
    }

    @Test
    public void testOnInstancePausing() {
        testOnInstanceSyncEvent(InstancePausingEvent.class, PAUSING);
    }

    @Test
    public void testOnInstancePaused() {
        testOnInstanceSyncEvent(InstancePausedEvent.class, PAUSED);
    }

    @Test
    public void testOnInstanceResuming() {
        testOnInstanceSyncEvent(InstanceResumingEvent.class, RESUMING);
    }

    @Test
    public void testOnInstanceRebuilding() {
        testOnInstanceSyncEvent(InstanceRebuildingEvent.class, REBUILDING);
    }

    @Test
    public void testOnInstanceResizing() {
        testOnInstanceSyncEvent(InstanceResizingEvent.class, RESIZING);
    }

    @Test
    public void testOnInstanceSuspending() {
        testOnInstanceSyncEvent(InstanceSuspendingEvent.class, SUSPENDING);
    }

    @Test
    public void testOnInstanceSuspended() {
        testOnInstanceSyncEvent(InstanceSuspendedEvent.class, SUSPENDED);
    }

    @Test
    public void testOnInstanceMigrating() {
        testOnInstanceSyncEvent(InstanceMigratingEvent.class, MIGRATING);
    }

    @Test
    public void testOnInstanceDeletingSync() {
        testOnInstanceSyncEvent(InstanceDeletingEvent.class, DELETING);
    }

    @Test
    public void testOnInstanceStartingSync() {
        testOnInstanceSyncEvent(InstanceStartingEvent.class, STARTING);
    }

    @Test
    public void testOnInstanceDeleting() {
        Instance instance = EntityGenerator.getInstance();
        instancesAware.saveInstance(instance);
        String instanceId = instance.getId();
        assertThat(instancesAware.instanceExists(instanceId), is(true));
        fireEvent(InstanceDeletingEvent.class, instance, false);
        assertThat(instancesAware.instanceExists(instanceId), is(false));
    }

    @Test
    public void testOnInstanceSnapshotting() {
        testOnInstanceSyncEvent(InstanceSnapshottingEvent.class, SNAPSHOTTING);
    }

    @Test
    public void testOnInstanceError() {
        testOnInstanceSyncEvent(InstanceErrorEvent.class, ERROR);
    }

    private void testOnInstanceSyncEvent(Class<? extends InstanceEvent> cls, InstanceState correctState) {
        testOnInstanceEvent(cls, correctState, true);
    }

    private void testOnInstanceEvent(Class<? extends InstanceEvent> cls, InstanceState correctState, boolean isSyncEvent) {
        Instance instance = EntityGenerator.getInstance();
        String instanceId = instance.getId();
        assertThat(instancesAware.instanceExists(instanceId), is(false));
        fireEvent(cls, instance, isSyncEvent);
        assertThat(instancesAware.instanceExists(instanceId), is(true));
        assertThat(instancesAware.getInstance(instanceId).get().getState(), equalTo(correctState));
    }

    private void fireEvent(Class<? extends InstanceEvent> cls, Instance instance, boolean isSyncEvent) {
        Yatomata<InstanceFSM> fsm = getFSMBuilder().build();
        InstanceEvent instanceEvent = EventFactory.instanceEvent(cls, instance);
        instanceEvent.setSync(isSyncEvent);
        fsm.fire(instanceEvent);
    }
    
    private Yatomata.Builder<InstanceFSM> getFSMBuilder() {
        return fsmBuilderAware.get(InstanceFSM.class);
    }


}