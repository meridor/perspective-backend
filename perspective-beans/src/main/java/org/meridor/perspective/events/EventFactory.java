package org.meridor.perspective.events;

import org.meridor.perspective.beans.*;

import java.time.ZonedDateTime;
import java.util.UUID;

public final class EventFactory {

    public static <T extends InstanceEvent> T instanceEvent(Class<T> eventClass, Instance instance) {
        try {
            T event = eventClass.newInstance();
            ZonedDateTime now = now();
            event.setId(uuid());
            event.setTimestamp(now);
            instance.setTimestamp(now);
            event.setInstance(instance);
            return event;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static InstanceEvent instanceToEvent(Instance instance) {
        InstanceState instanceState = instance.getState();
        InstanceEvent event = instanceStateToEvent(instanceState);
        event.setInstance(instance);
        event.setId(uuid());
        event.setTimestamp(now());
        return event;
    }

    private static InstanceEvent instanceStateToEvent(InstanceState instanceState) {
        if (instanceState == null) {
            throw new IllegalArgumentException("Instance status can't be null");
        }
        switch (instanceState) {
            case DELETING:
                return new InstanceDeletingEvent();
            case ERROR:
                return new InstanceErrorEvent();
            case HARD_REBOOTING:
                return new InstanceHardRebootingEvent();
            case LAUNCHED:
                return new InstanceLaunchedEvent();
            case LAUNCHING:
                return new InstanceLaunchingEvent();
            case MIGRATING:
                return new InstanceMigratingEvent();
            default:
            case PAUSED:
                return new InstancePausedEvent();
            case PAUSING:
                return new InstancePausingEvent();
            case QUEUED:
                return new InstanceQueuedEvent();
            case REBOOTING:
                return new InstanceRebootingEvent();
            case REBUILDING:
                return new InstanceRebuildingEvent();
            case RESIZING:
                return new InstanceResizingEvent();
            case SHUTOFF:
                return new InstanceShutOffEvent();
            case SHUTTING_DOWN:
                return new InstanceShuttingDownEvent();
            case SNAPSHOTTING:
                return new InstanceSnapshottingEvent();
            case SUSPENDING:
                return new InstanceSuspendingEvent();
            case SUSPENDED:
                return new InstanceSuspendedEvent();
        }
    }

    public static <T extends ImageEvent> T imageEvent(Class<T> eventClass, Image image) {
        try {
            T event = eventClass.newInstance();
            ZonedDateTime now = now();
            event.setId(uuid());
            event.setTimestamp(now);
            image.setTimestamp(now);
            event.setImage(image);
            return event;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ImageEvent imageToEvent(Image image) {
        ImageState imageState = image.getState();
        ImageEvent event = imageStateToEvent(imageState);
        event.setImage(image);
        event.setId(uuid());
        event.setTimestamp(now());
        return event;
    }
    
    private static ImageEvent imageStateToEvent(ImageState imageState) {
        if (imageState == null) {
            throw new IllegalArgumentException("Instance status can't be null");
        }
        switch (imageState) {
            case DELETING:
                return new ImageDeletingEvent();
            case ERROR:
                return new ImageErrorEvent();
            case QUEUED:
                return new ImageQueuedEvent();
            case SAVING:
                return new ImageSavingEvent();
            default:
            case SAVED:
                return new ImageSavedEvent();
        }
    }

    public static <T extends ProjectEvent> T projectEvent(Class<T> eventClass, Project project) {
        try {
            T event = eventClass.newInstance();
            ZonedDateTime now = now();
            event.setId(uuid());
            event.setTimestamp(now);
            event.setProject(project);
            project.setTimestamp(now);
            return event;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static ZonedDateTime now() {
        return ZonedDateTime.now();
    }

    private EventFactory() {
    }
}
