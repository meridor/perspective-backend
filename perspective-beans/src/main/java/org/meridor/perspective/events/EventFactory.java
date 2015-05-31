package org.meridor.perspective.events;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.InstanceStatus;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.CloudType;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public final class EventFactory {
    
    public static <T extends InstanceEvent> T instanceEvent(Class<T> eventClass, Instance instance) throws Exception {
        T event = eventClass.newInstance();
        event.setTimestamp(now());
        event.setInstance(instance);
        return event;
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

    public static <T extends ProjectEvent> T projectEvent(Class<T> eventClass, Project project) throws Exception {
        T event = eventClass.newInstance();
        event.setTimestamp(now());
        event.setProject(project);
        return event;
    }
    
    public static XMLGregorianCalendar now() throws Exception {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    }

}
