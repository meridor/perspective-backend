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
    
    public static <T extends InstancesEvent> T instancesEvent(Class<T> eventClass, CloudType cloudType, List<Instance> instances) throws Exception {
        T event = eventClass.newInstance();
        event.setTimestamp(now());
        event.setCloudType(cloudType);
        event.getInstances().addAll(instances);
        return event;
    }

    public static <T extends ProjectsEvent> T projectsEvent(Class<T> eventClass, CloudType cloudType, List<Project> projects) throws Exception {
        T event = eventClass.newInstance();
        event.setTimestamp(now());
        event.setCloudType(cloudType);
        event.getProjects().addAll(projects);
        return event;
    }
    
    private static XMLGregorianCalendar now() throws Exception {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    }

}
