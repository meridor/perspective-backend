package org.meridor.perspective.events;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.beans.Project;
import org.meridor.perspective.config.CloudType;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public final class EventFactory {
    
    public static <T extends InstanceEvent> T instancesEvent(Class<T> eventClass, CloudType cloudType, Instance instance) throws Exception {
        T event = eventClass.newInstance();
        event.setTimestamp(now());
        event.setCloudType(cloudType);
        event.setInstance(instance);
        return event;
    }

    public static <T extends ProjectEvent> T projectsEvent(Class<T> eventClass, CloudType cloudType, Project project) throws Exception {
        T event = eventClass.newInstance();
        event.setTimestamp(now());
        event.setCloudType(cloudType);
        event.setProject(project);
        return event;
    }
    
    private static XMLGregorianCalendar now() throws Exception {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    }

}
