package org.meridor.perspective.events;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.InstanceStatus;
import org.meridor.perspective.beans.Project;

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
    
    public static InstanceEvent statusToEvent(InstanceStatus instanceStatus) {
        if (instanceStatus == null) {
            throw new IllegalArgumentException("Instance status can't be null");
        }
        switch (instanceStatus) {
            case DELETING: return new InstanceDeletingEvent();
            case ERROR: return new InstanceErrorEvent();
            case HARD_REBOOTING: return new InstanceHardRebootingEvent();
            case LAUNCHED: return new InstanceLaunchedEvent();
            case LAUNCHING: return new InstanceLaunchingEvent();
            case MIGRATING: return new InstanceMigratingEvent();
            default:
            case PAUSED: return new InstancePausedEvent();
            case PAUSING: return new InstancePausingEvent();
            case QUEUED: return new InstanceQueuedEvent();
            case REBOOTING: return new InstanceRebootingEvent();
            case REBUILDING: return new InstanceRebuildingEvent();
            case RESIZING: return new InstanceResizingEvent();
            case SHUTOFF: return new InstanceShutOffEvent();
            case SHUTTING_DOWN: return new InstanceShuttingDownEvent();
            case SNAPSHOTTING: return new InstanceSnapshottingEvent();
            case SUSPENDING: return new InstanceSuspendingEvent();
            case SUSPENDED: return new InstanceSuspendedEvent();
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

    private EventFactory(){}
}
