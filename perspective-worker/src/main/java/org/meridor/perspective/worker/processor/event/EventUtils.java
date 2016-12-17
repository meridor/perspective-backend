package org.meridor.perspective.worker.processor.event;

import org.meridor.perspective.common.events.EventBus;
import org.meridor.perspective.config.Cloud;
import org.meridor.perspective.events.NeedProjectSyncEvent;

public final class EventUtils {

    public static void requestProjectSync(EventBus eventBus, Cloud cloud, String projectId) {
        NeedProjectSyncEvent event = new NeedProjectSyncEvent();
        event.setCloud(cloud);
        event.setProjectId(projectId);
        eventBus.fireAsync(event);
    }
}
