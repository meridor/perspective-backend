package org.meridor.perspective.openstack;

import org.meridor.perspective.beans.Instance;
import org.meridor.perspective.config.OperationType;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Action;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

import static org.meridor.perspective.config.OperationType.SUSPEND_INSTANCE;

@Component
public class SuspendInstanceOperation extends BaseInstanceOperation {

    @Override
    protected BiConsumer<OSClient, Instance> getAction() {
        return (api, instance) -> api.compute().servers().action(instance.getRealId(), Action.SUSPEND);
    }

    @Override
    protected String getSuccessMessage(Instance instance) {
        return String.format("Suspended instance %s (%s)", instance.getName(), instance.getId());
    }

    @Override
    protected String getErrorMessage() {
        return "Failed to suspend instance";
    }

    @Override
    public OperationType[] getTypes() {
        return new OperationType[]{SUSPEND_INSTANCE};
    }
    
}
