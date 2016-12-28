package org.meridor.perspective.worker.processor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.backend.EntityGenerator;
import org.meridor.perspective.backend.storage.InstancesAware;
import org.meridor.perspective.backend.storage.Storage;
import org.meridor.perspective.beans.DestinationName;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.InstanceState;
import org.meridor.perspective.events.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.yandex.qatools.fsm.Yatomata;
import ru.yandex.qatools.fsm.impl.FSMBuilder;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.beans.InstanceState.*;
import static org.meridor.perspective.events.EventFactory.instanceToEvent;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@ContextConfiguration(locations = "/META-INF/spring/fsm-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class InstanceFSMTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private InstancesAware instancesAware;

    @Autowired
    private Storage storage;

    @Test
    public void testLongLaunch() throws Exception {
        Instance instanceOne = EntityGenerator.getInstance();
        assertThat(instancesAware.instanceExists(instanceOne.getId()), is(false));
        ZonedDateTime now = ZonedDateTime.now();
        instanceOne.setTimestamp(now.minusSeconds(10));
        InstanceLaunchingEvent eventOne = EventFactory.instanceEvent(InstanceLaunchingEvent.class, instanceOne);
        eventOne.setSync(true);
        Yatomata<InstanceFSM> fsm = getFSMBuilder().build();
        fsm.fire(eventOne);
        assertThat(instancesAware.instanceExists(instanceOne.getId()), is(true));
        
        //Emulating eviction
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.schedule(() -> instancesAware.getInstances().removeIf(
                i -> Duration.between(i.getTimestamp(), now).getSeconds() > 5
        ), 100, TimeUnit.MILLISECONDS);

        Thread.sleep(100);
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
    public void testOnInstanceRenaming() {
        Instance instance = EntityGenerator.getInstance();
        String instanceId = instance.getId();
        instancesAware.saveInstance(instance);

        Optional<Instance> instanceBeforeRenaming = instancesAware.getInstance(instanceId);
        assertThat(instanceBeforeRenaming.isPresent(), is(true));
        assertThat(instanceBeforeRenaming.get().getName(), equalTo(EntityGenerator.getInstance().getName()));

        Instance newInstance = EntityGenerator.getInstance();
        newInstance.setName("new-name");
        fireEvent(InstanceRenamingEvent.class, newInstance, false);

        Optional<Instance> instanceAfterRenaming = instancesAware.getInstance(instanceId);
        assertThat(instanceAfterRenaming.isPresent(), is(true));
        assertThat(instanceAfterRenaming.get().getName(), equalTo("new-name"));
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
        fireEvent(null, cls, instance, isSyncEvent);
    }

    private void fireEvent(InstanceEvent initialState, Class<? extends InstanceEvent> cls, Instance instance, boolean isSyncEvent) {
        Yatomata<InstanceFSM> fsm = initialState != null ?
                getFSMBuilder().build(initialState) :
                getFSMBuilder().build();
        InstanceEvent instanceEvent = EventFactory.instanceEvent(cls, instance);
        instanceEvent.setSync(isSyncEvent);
        fsm.fire(instanceEvent);
    }
    
    private Yatomata.Builder<InstanceFSM> getFSMBuilder() {
        return new FSMBuilder<>(applicationContext.getBean(InstanceFSM.class));
    }

    @Test
    public void testSendMailOnInstanceError() {
        BlockingQueue<Object> queue = storage.getQueue(DestinationName.MAIL.value());
        assertThat(queue, is(empty()));
        Instance instance = EntityGenerator.getInstance();
        instance.setState(InstanceState.REBOOTING);
        InstanceEvent initialState = instanceToEvent(instance);
        fireEvent(initialState, InstanceErrorEvent.class, instance, true);
        fireEvent(InstanceErrorEvent.class, instance, true);
        assertThat(queue, hasSize(1));
        fireEvent(InstanceErrorEvent.class, instance, true);
        assertThat(queue, hasSize(1)); //Does not change
    }

}