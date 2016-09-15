package org.meridor.perspective.events;

import org.junit.Test;
import org.meridor.perspective.beans.Image;
import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;

import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
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
    
}