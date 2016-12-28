package org.meridor.perspective.events;

import org.meridor.perspective.beans.*;
import org.meridor.perspective.beans.InstanceState;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.meridor.perspective.beans.InstanceState.*;

public final class EventFactory {

    public static <T extends InstanceEvent> T instanceEvent(Class<T> eventClass, Instance instance) {
        try {
            T event = eventClass.newInstance();
            ZonedDateTime now = now();
            event.setId(uuid());
            event.setTimestamp(now);
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
            throw new IllegalArgumentException("Instance state can't be null");
        }
        switch (instanceState) {
            case DELETING:
                return new InstanceDeletingEvent();
            case ERROR:
                return new InstanceErrorEvent();
            case HARD_REBOOTING:
                return new InstanceHardRebootingEvent();
            default:
            case LAUNCHED:
                return new InstanceLaunchedEvent();
            case LAUNCHING:
                return new InstanceLaunchingEvent();
            case MIGRATING:
                return new InstanceMigratingEvent();
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
            case RESUMING:
                return new InstanceResumingEvent();
            case SHUTOFF:
                return new InstanceShutOffEvent();
            case SHUTTING_DOWN:
                return new InstanceShuttingDownEvent();
            case STARTING:
                return new InstanceStartingEvent();
            case SUSPENDING:
                return new InstanceSuspendingEvent();
            case SUSPENDED:
                return new InstanceSuspendedEvent();
        }
    }

    public static InstanceState instanceEventToState(InstanceEvent event) {
        if (event instanceof InstanceDeletingEvent) {
            return DELETING;
        } else if (event instanceof InstanceErrorEvent) {
            return ERROR;
        } else if (event instanceof InstanceHardRebootingEvent) {
            return HARD_REBOOTING;
        } else if (event instanceof InstanceLaunchedEvent) {
            return LAUNCHED;
        } else if (event instanceof InstanceLaunchingEvent) {
            return LAUNCHING;
        } else if (event instanceof InstanceMigratingEvent) {
            return MIGRATING;
        } else if (event instanceof InstancePausedEvent) {
            return PAUSED;
        } else if (event instanceof InstancePausingEvent) {
            return PAUSING;
        } else if (event instanceof InstanceQueuedEvent) {
            return QUEUED;
        } else if (event instanceof InstanceRebootingEvent) {
            return REBOOTING;
        } else if (event instanceof InstanceRebuildingEvent) {
            return REBUILDING;
        } else if (event instanceof InstanceResizingEvent) {
            return RESIZING;
        } else if (event instanceof InstanceResumingEvent) {
            return RESUMING;
        } else if (event instanceof InstanceShutOffEvent) {
            return SHUTOFF;
        } else if (event instanceof InstanceShuttingDownEvent) {
            return SHUTTING_DOWN;
        } else if (event instanceof InstanceStartingEvent) {
            return STARTING;
        } else if (event instanceof InstanceSuspendingEvent) {
            return SUSPENDING;
        } else if (event instanceof InstanceSuspendedEvent) {
            return SUSPENDED;
        }
        throw new IllegalArgumentException("Unsupported event: " + event.getClass().getSimpleName());
    }
    
    public static <T extends ImageEvent> T imageEvent(Class<T> eventClass, Image image) {
        try {
            T event = eventClass.newInstance();
            ZonedDateTime now = now();
            event.setId(uuid());
            event.setTimestamp(now);
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
            throw new IllegalArgumentException("Image state can't be null");
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

    public static ImageState imageEventToState(ImageEvent event) {
        if (event instanceof ImageDeletingEvent) {
            return ImageState.DELETING;
        } else if (event instanceof ImageErrorEvent) {
            return ImageState.ERROR;
        } else if (event instanceof ImageQueuedEvent) {
            return ImageState.QUEUED;
        } else if (event instanceof ImageSavingEvent) {
            return ImageState.SAVING;
        } else if (event instanceof ImageSavedEvent) {
            return ImageState.SAVED;
        }
        throw new IllegalArgumentException("Unsupported event: " + event.getClass().getSimpleName());
    }
    
    public static <T extends ProjectEvent> T projectEvent(Class<T> eventClass, Project project) {
        try {
            T event = eventClass.newInstance();
            ZonedDateTime now = now();
            event.setId(uuid());
            event.setTimestamp(now);
            event.setProject(project);
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

}
