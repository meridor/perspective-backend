package org.meridor.perspective.events;

import org.junit.Test;
import org.meridor.perspective.beans.*;
import org.meridor.perspective.beans.InstanceState;

import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.beans.InstanceState.*;
import static org.meridor.perspective.events.EventFactory.*;

public class EventFactoryTest {
    
    @Test
    public void testProjectEvent() throws Exception {
        Project project = new Project();
        ZonedDateTime timestamp = now();
        project.setTimestamp(timestamp);
        ProjectSyncEvent event1 = projectEvent(ProjectSyncEvent.class, project);
        Thread.sleep(50);
        ProjectSyncEvent event2 = projectEvent(ProjectSyncEvent.class, project);
        assertThat(event1.getId(), not(equalTo(event2.getId())));
        assertThat(event1.getTimestamp(), not(equalTo(event2.getTimestamp())));
        assertThat(event1.getProject(), equalTo(event2.getProject()));
        assertThat(event1.getProject().getTimestamp(), equalTo(timestamp));
    }
    
    @Test
    public void testInstanceEvent() throws Exception {
        Instance instance = new Instance();
        ZonedDateTime timestamp = now();
        instance.setTimestamp(timestamp);
        InstanceEvent event1 = instanceEvent(InstanceLaunchingEvent.class, instance);
        Thread.sleep(50);
        InstanceEvent event2 = instanceEvent(InstanceLaunchingEvent.class, instance);
        assertThat(event1.getId(), not(equalTo(event2.getId())));
        assertThat(event1.getTimestamp(), not(equalTo(event2.getTimestamp())));
        assertThat(event1.getInstance(), equalTo(event2.getInstance()));
        assertThat(event1.getInstance().getTimestamp(), equalTo(timestamp));
    }
    
    @Test
    public void testImageEvent() throws Exception {
        Image image = new Image();
        ZonedDateTime timestamp = now();
        image.setTimestamp(timestamp);
        ImageEvent event1 = imageEvent(ImageSavedEvent.class, image);
        Thread.sleep(50);
        ImageEvent event2 = imageEvent(ImageSavedEvent.class, image);
        assertThat(event1.getId(), not(equalTo(event2.getId())));
        assertThat(event1.getTimestamp(), not(equalTo(event2.getTimestamp())));
        assertThat(event1.getImage(), equalTo(event2.getImage()));
        assertThat(event1.getImage().getTimestamp(), equalTo(timestamp));
    }
    
    @Test
    public void testInstanceToEvent() {
        testInstanceToEvent(DELETING, InstanceDeletingEvent.class);
        testInstanceToEvent(ERROR, InstanceErrorEvent.class);
        testInstanceToEvent(HARD_REBOOTING, InstanceHardRebootingEvent.class);
        testInstanceToEvent(LAUNCHED, InstanceLaunchedEvent.class);
        testInstanceToEvent(LAUNCHING, InstanceLaunchingEvent.class);
        testInstanceToEvent(MIGRATING, InstanceMigratingEvent.class);
        testInstanceToEvent(PAUSED, InstancePausedEvent.class);
        testInstanceToEvent(PAUSING, InstancePausingEvent.class);
        testInstanceToEvent(QUEUED, InstanceQueuedEvent.class);
        testInstanceToEvent(REBOOTING, InstanceRebootingEvent.class);
        testInstanceToEvent(REBUILDING, InstanceRebuildingEvent.class);
        testInstanceToEvent(RESIZING, InstanceResizingEvent.class);
        testInstanceToEvent(RESUMING, InstanceResumingEvent.class);
        testInstanceToEvent(SHUTOFF, InstanceShutOffEvent.class);
        testInstanceToEvent(SHUTTING_DOWN, InstanceShuttingDownEvent.class);
        testInstanceToEvent(STARTING, InstanceStartingEvent.class);
        testInstanceToEvent(SUSPENDING, InstanceSuspendingEvent.class);
        testInstanceToEvent(SUSPENDED, InstanceSuspendedEvent.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstanceToEventWrongState() {
        Instance instance = new Instance();
        instance.setState(null);
        instanceToEvent(instance);
    }
    
    private void testInstanceToEvent(InstanceState instanceState, Class<? extends InstanceEvent> cls) {
        Instance instance = new Instance();
        instance.setId("test-id");
        instance.setState(instanceState);
        InstanceEvent instanceEvent = instanceToEvent(instance);
        assertThat(instanceEvent, is(instanceOf(cls)));
        assertThat(instanceEvent.getId(), is(notNullValue()));
        assertThat(instanceEvent.getTimestamp(), is(notNullValue()));
        assertThat(instanceEvent.getInstance(), equalTo(instance));
    }

    @Test
    public void testEventToState() {
        assertThat(instanceEventToState(new InstanceDeletingEvent()), equalTo(DELETING));
        assertThat(instanceEventToState(new InstanceErrorEvent()), equalTo(ERROR));
        assertThat(instanceEventToState(new InstanceHardRebootingEvent()), equalTo(HARD_REBOOTING));
        assertThat(instanceEventToState(new InstanceLaunchedEvent()), equalTo(LAUNCHED));
        assertThat(instanceEventToState(new InstanceLaunchingEvent()), equalTo(LAUNCHING));
        assertThat(instanceEventToState(new InstanceMigratingEvent()), equalTo(MIGRATING));
        assertThat(instanceEventToState(new InstancePausedEvent()), equalTo(PAUSED));
        assertThat(instanceEventToState(new InstancePausingEvent()), equalTo(PAUSING));
        assertThat(instanceEventToState(new InstanceQueuedEvent()), equalTo(QUEUED));
        assertThat(instanceEventToState(new InstanceRebootingEvent()), equalTo(REBOOTING));
        assertThat(instanceEventToState(new InstanceRebuildingEvent()), equalTo(REBUILDING));
        assertThat(instanceEventToState(new InstanceResizingEvent()), equalTo(RESIZING));
        assertThat(instanceEventToState(new InstanceResumingEvent()), equalTo(RESUMING));
        assertThat(instanceEventToState(new InstanceShutOffEvent()), equalTo(SHUTOFF));
        assertThat(instanceEventToState(new InstanceShuttingDownEvent()), equalTo(SHUTTING_DOWN));
        assertThat(instanceEventToState(new InstanceStartingEvent()), equalTo(STARTING));
        assertThat(instanceEventToState(new InstanceSuspendingEvent()), equalTo(SUSPENDING));
        assertThat(instanceEventToState(new InstanceSuspendedEvent()), equalTo(SUSPENDED));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongEventToState() {
        instanceEventToState(new InstanceSnapshottingEvent());
    }
    @Test
    public void testImageToEvent() {
        testImageToEvent(ImageState.DELETING, ImageDeletingEvent.class);
        testImageToEvent(ImageState.ERROR, ImageErrorEvent.class);
        testImageToEvent(ImageState.QUEUED, ImageQueuedEvent.class);
        testImageToEvent(ImageState.SAVING, ImageSavingEvent.class);
        testImageToEvent(ImageState.SAVED, ImageSavedEvent.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testImageToEventWrongState() {
        Image image = new Image();
        image.setState(null);
        imageToEvent(image);
    }
    
    private void testImageToEvent(ImageState imageState, Class<? extends ImageEvent> cls) {
        Image image = new Image();
        image.setId("test-id");
        image.setState(imageState);
        ImageEvent imageEvent = imageToEvent(image);
        assertThat(imageEvent, is(instanceOf(cls)));
        assertThat(imageEvent.getId(), is(notNullValue()));
        assertThat(imageEvent.getTimestamp(), is(notNullValue()));
        assertThat(imageEvent.getImage(), equalTo(image));
    }

    @Test
    public void testImageEventToState() {
        assertThat(imageEventToState(new ImageDeletingEvent()), equalTo(ImageState.DELETING));
        assertThat(imageEventToState(new ImageErrorEvent()), equalTo(ImageState.ERROR));
        assertThat(imageEventToState(new ImageQueuedEvent()), equalTo(ImageState.QUEUED));
        assertThat(imageEventToState(new ImageSavingEvent()), equalTo(ImageState.SAVING));
        assertThat(imageEventToState(new ImageSavedEvent()), equalTo(ImageState.SAVED));
    }
    
}