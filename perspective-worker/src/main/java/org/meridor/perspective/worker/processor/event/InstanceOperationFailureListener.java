package org.meridor.perspective.worker.processor.event;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.events.*;
import org.springframework.stereotype.Component;

@Component
public class InstanceOperationFailureListener extends OperationFailureListener<InstanceEvent> {
    
    @Override
    protected void processEvent(InstanceEvent instanceEvent) {
        Instance instance = instanceEvent.getInstance();
        if (instanceEvent instanceof InstanceLaunchingEvent) {
            sendInstanceLetter("Failed to launch instance %s (%s)", instance);
        } else if (instanceEvent instanceof InstanceRebootingEvent) {
            sendInstanceLetter("Failed to reboot instance %s (%s)", instance);
        } else if (instanceEvent instanceof InstanceHardRebootingEvent) {
            sendInstanceLetter("Failed to hard reboot instance %s (%s)", instance);
        } else if (instanceEvent instanceof InstanceShuttingDownEvent) {
            sendInstanceLetter("Failed to shut down instance %s (%s)", instance);
        } else if (instanceEvent instanceof InstancePausingEvent) {
            sendInstanceLetter("Failed to pause instance %s (%s)", instance);
        } else if (instanceEvent instanceof InstanceResumingEvent) {
            sendInstanceLetter("Failed to resume instance %s (%s)", instance);
        } else if (instanceEvent instanceof InstanceRebuildingEvent) {
            sendLetter(String.format(
                    "Failed to rebuild instance %s (%s) to image %s (%s)",
                    instance.getName(),
                    instance.getId(),
                    instance.getImage().getName(),
                    instance.getImage().getId()
            ));
        } else if (instanceEvent instanceof InstanceResizingEvent) {
            sendLetter(String.format(
                    "Failed to resize instance %s (%s) to flavor %s (%s)",
                    instance.getName(),
                    instance.getId(),
                    instance.getFlavor().getName(),
                    instance.getFlavor().getId()
            ));
        } else if (instanceEvent instanceof InstanceStartingEvent) {
            sendInstanceLetter("Failed to start instance %s (%s)", instance);
        } else if (instanceEvent instanceof InstanceSuspendingEvent) {
            sendInstanceLetter("Failed to suspend instance %s (%s)", instance);
        } else if (instanceEvent instanceof InstanceMigratingEvent) {
            sendInstanceLetter("Failed to migrate instance %s (%s)", instance);
        } else if (instanceEvent instanceof InstanceDeletingEvent) {
            sendInstanceLetter("Failed to delete instance %s (%s)", instance);
        } else {
            sendLetter(String.format(
                    "Failed to process %s event for instance %s (%s)",
                    instanceEvent.getClass().getSimpleName(),
                    instance.getName(),
                    instance.getId()
            ));
        }
    }

    @Override
    protected Class<InstanceEvent> getEventClass() {
        return InstanceEvent.class;
    }

    private void sendInstanceLetter(String text, Instance instance) {
        sendLetter(String.format(text, instance.getName(), instance.getId()));
    }

}
