package org.meridor.perspective.rest.filters;

import org.apache.camel.Body;
import org.meridor.perspective.events.InstanceDeletingEvent;
import org.meridor.perspective.events.InstanceLaunchingEvent;
import org.springframework.stereotype.Component;

@Component
public class InstancesFilter {
    
    public boolean isInstanceLaunchingEvent(@Body Object body) {
        return body instanceof InstanceLaunchingEvent;
    }
    
    public boolean isInstanceDeletingEvent(@Body Object body) {
        return body instanceof InstanceDeletingEvent;
    }
    
}
